package edu.harvard.dbmi.avillach.dataupload.aws;

import edu.harvard.dbmi.avillach.dataupload.status.ClientStatusService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.encryption.s3.S3EncryptionClient;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * In order to make s3 requests across accounts, we have to assume a role in AWS
 * These operations last 1 hour, after which point requests will 401/3
 * This class wraps the S3 client in a getter, and runs an automated task to refresh
 * the client when the token expires. Requests to the getter will block while this
 * refresh task is running
 */
@ConditionalOnProperty(name = "production", havingValue = "true")
@Service
public class SelfRefreshingS3Client {
    private static final Logger LOG = LoggerFactory.getLogger(SelfRefreshingS3Client.class);
    private Map<String, ReadWriteLock> locks;
    private Map<String, S3Client> clients = new HashMap<>();

    @Value("${aws.s3.role_arn}")
    private String roleArn;

    @Value("${aws.s3.shared_secret}")
    private String sharedSecret;

    @Value("${aws.kms.key_id}")
    private String kmsKeyId;

    @Autowired
    S3EncryptionClient.Builder encryptionClientBuilder;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private Map<String, StsClient> stsClients;

    @Autowired
    ClientStatusService statusService;

    @PostConstruct
    private void refreshClient() {
        locks = stsClients.keySet().stream()
            .collect(Collectors.toMap(Function.identity(), (s) -> new ReentrantReadWriteLock()));
        stsClients.keySet().stream().parallel().forEach(this::refreshClient);
    }

    // exposed for testing
    void refreshClient(String siteName) {
        StsClient stsClient = stsClients.get(siteName);
        LOG.info("Starting client refresh for {}", siteName);

        // block further s3 calls while we refresh
        LOG.info("Locking s3 client while refreshing session");
        locks.get(siteName).writeLock().lock();
        statusService.setClientStatus("initializing");

        // assume the role
        LOG.info("Attempting to assume data uploader role");
        AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
            .roleArn(roleArn)
            .roleSessionName("test_session" + System.nanoTime())
            .externalId(sharedSecret)
            .durationSeconds(60*60) // 1 hour
            .build();
        AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(roleRequest);
        if (assumeRoleResponse.credentials() == null ) {
            LOG.error("Error assuming role, no credentials returned! Exiting!");
            statusService.setClientStatus("error");
            context.close();
        }
        LOG.info("Successfully assumed role, using credentials to create new S3 client");

        // Use the credentials from the role to create the S3 client
        Credentials credentials = assumeRoleResponse.credentials();
        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.builder()
            .accessKeyId(credentials.accessKeyId())
            .secretAccessKey(credentials.secretAccessKey())
            .sessionToken(credentials.sessionToken())
            .expirationTime(credentials.expiration())
            .build();
        StaticCredentialsProvider provider = StaticCredentialsProvider.create(sessionCredentials);
        S3Client client = S3Client.builder()
            .credentialsProvider(provider)
            .region(Region.US_EAST_1)
            .build();
        LOG.info("Created S3 client");

        LOG.info("Verifying KMS encryption");
        S3EncryptionClient clientWithEncryption = encryptionClientBuilder
            .wrappedClient(client)
            .kmsKeyId(kmsKeyId)
            .build();
        clients.put(siteName, clientWithEncryption);
        LOG.info("Encryption key exists and we can build an encryption client");

        // now that client is refreshed, unlock for reading
        LOG.info("Unlocking s3 client. Session refreshed");
        locks.get(siteName).writeLock().unlock();
        statusService.setClientStatus("ready");

        // create virtual thread to handle next refresh, to occur 5 mins before session expires.
        Thread.ofVirtual().start(() -> delayedRefresh(credentials.expiration().minus(5, ChronoUnit.MINUTES)));
    }

    private void delayedRefresh(Instant refresh) {
        LOG.info("Next refresh will be at {}", refresh);
        try {
            Thread.sleep(Duration.between(Instant.now(), refresh));
        } catch (InterruptedException e) {
            LOG.warn("Couldn't wait. Refreshing early", e);
        }
        LOG.info("Refreshing s3 client");

    }

    public S3Client getS3Client(String siteName) {
        S3Client client;
        locks.get(siteName).readLock().lock();
        client = clients.get(siteName);
        locks.get(siteName).readLock().unlock();
        return client;
    }
}
