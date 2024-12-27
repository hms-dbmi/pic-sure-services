package edu.harvard.dbmi.avillach.dataupload.aws;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@ActiveProfiles("aws_mock")
@SpringBootTest
class AWSClientBuilderTest {

    @MockBean
    Map<String, SiteAWSInfo> sites;

    @MockBean
    StsClient stsClient;

    @MockBean
    StsClientProvider stsClientProvider;

    @MockBean
    S3AsyncClientBuilder S3AsyncClientBuilder;

    @Autowired
    AWSClientBuilder subject;

    @Test
    void shouldNotBuildClientIfSiteDNE() {
        Mockito.when(sites.get("Narnia"))
            .thenReturn(null);

        Optional<S3AsyncClient> actual = subject.buildClientForSite("Narnia");
        Optional<S3AsyncClient> expected = Optional.empty();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldNotBuildClientIfRoleRequestFails() {
        SiteAWSInfo siteAWSInfo = new SiteAWSInfo("bch", "aws:arn:420", "external", "bucket", "aws:kms:420");
        Mockito.when(sites.get("bch"))
            .thenReturn(siteAWSInfo);
        Mockito.when(sites.containsKey("bch")).thenReturn(true);

        ArgumentMatcher<AssumeRoleRequest> requestMatcher =
            (r) -> r.roleArn().equals("aws:arn:420")
                && r.roleSessionName().startsWith("test_session")
                && r.externalId().equals("external")
                && r.durationSeconds().equals(3600);
        AssumeRoleResponse response = Mockito.mock(AssumeRoleResponse.class);
        Mockito.when(stsClient.assumeRole(Mockito.argThat(requestMatcher)))
            .thenReturn(response);
        Mockito.when(stsClientProvider.createClient())
            .thenReturn(Optional.of(stsClient));

        Optional<S3AsyncClient> actual = subject.buildClientForSite("bch");
        Optional<S3AsyncClient> expected = Optional.empty();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldBuildClient() {
        SiteAWSInfo siteAWSInfo = new SiteAWSInfo("bch", "aws:arn:420", "external", "bucket", "aws:kms:420");
        Mockito.when(sites.get("bch"))
            .thenReturn(siteAWSInfo);
        Mockito.when(sites.containsKey("bch")).thenReturn(true);

        Credentials credentials = Mockito.mock(Credentials.class);
        Mockito.when(credentials.accessKeyId()).thenReturn("access_key_id");
        Mockito.when(credentials.secretAccessKey()).thenReturn("secret");
        Mockito.when(credentials.sessionToken()).thenReturn("session");
        Mockito.when(credentials.expiration()).thenReturn(Instant.MAX);
        AssumeRoleResponse assumeRoleResponse = Mockito.mock(AssumeRoleResponse.class);
        Mockito.when(assumeRoleResponse.credentials())
            .thenReturn(credentials);
        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.builder()
            .accessKeyId(credentials.accessKeyId())
            .secretAccessKey(credentials.secretAccessKey())
            .sessionToken(credentials.sessionToken())
            .expirationTime(credentials.expiration())
            .build();
        ArgumentMatcher<AssumeRoleRequest> requestMatcher =
            (r) -> r.roleArn().equals("aws:arn:420")
                && r.roleSessionName().startsWith("test_session")
                && r.externalId().equals("external")
                && r.durationSeconds().equals(3600);
        Mockito.when(stsClient.assumeRole(Mockito.argThat(requestMatcher)))
            .thenReturn(assumeRoleResponse);
        Mockito.when(stsClientProvider.createClient())
            .thenReturn(Optional.of(stsClient));

        StaticCredentialsProvider provider = StaticCredentialsProvider.create(sessionCredentials);
        ArgumentMatcher<AwsCredentialsProvider> credsMatcher = (AwsCredentialsProvider p) -> p.toString().equals(provider.toString());
        S3AsyncClient S3AsyncClient = Mockito.mock(S3AsyncClient.class);
        Mockito.when(S3AsyncClientBuilder.credentialsProvider(Mockito.argThat(credsMatcher)))
            .thenReturn(S3AsyncClientBuilder);
        Mockito.when(S3AsyncClientBuilder.build())
            .thenReturn(S3AsyncClient);

        Optional<S3AsyncClient> actual = subject.buildClientForSite("bch");
        Optional<S3AsyncClient> expected = Optional.of(S3AsyncClient);

        Assertions.assertEquals(expected, actual);
    }
}