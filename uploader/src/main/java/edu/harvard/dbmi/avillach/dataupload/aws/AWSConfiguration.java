package edu.harvard.dbmi.avillach.dataupload.aws;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.context.annotation.RequestScope;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.encryption.s3.S3EncryptionClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ConditionalOnProperty(name = "production", havingValue = "true")
@Configuration
public class AWSConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(AWSConfiguration.class);

    @Value("${aws.s3.institution:}")
    private List<String> institutions;

    @Value("${aws.s3.role_arns:}")
    private List<String> roleArns;

    @Value("${aws.s3.external_ids:}")
    private List<String> externalIDs;

    @Value("${aws.s3.buckets:}")
    private List<String> buckets;

    @Value("${aws.kms.key_ids:}")
    private List<String> kmsKeyIds;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired(required = false)
    private SdkHttpClient sdkHttpClient;

    @Bean
    @ConditionalOnProperty(name = "production", havingValue = "true")
    Map<String, SiteAWSInfo> roleARNs() {
        boolean badConfig = Stream.of(roleArns, externalIDs, kmsKeyIds, buckets)
            .filter(l -> l.size() != institutions.size())
            .peek(l -> LOG.error("Mismatched aws credentials {}", l))
            .findAny()
            .isPresent();
        if (badConfig) {
            context.close();
            return Map.of();
        }
        HashMap<String, SiteAWSInfo> roles = new HashMap<>();
        for (int i = 0; i < institutions.size(); i++) {
            SiteAWSInfo info = new SiteAWSInfo(
                institutions.get(i), roleArns.get(i), externalIDs.get(i), buckets.get(i), kmsKeyIds.get(i)
            );
            roles.put(info.siteName(), info);
        }
        return roles;
    }

    @Bean
    S3EncryptionClient.Builder encryptionClientBuilder() {
        // This is a bean for mocking purposes
        return S3EncryptionClient.builder();
    }

    @Bean
    StsClientBuilder stsClientBuilder() {
        // This is a bean for mocking purposes
        return StsClient.builder();
    }

    @Bean
    S3ClientBuilder s3ClientBuilder() {
        return S3Client.builder();
    }

    @Bean
    @RequestScope
    StsClient getStsClient() {
        return StsClient.builder().region(Region.US_EAST_1).build();
    }
}
