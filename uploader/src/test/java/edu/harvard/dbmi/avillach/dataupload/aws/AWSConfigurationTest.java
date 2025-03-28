package edu.harvard.dbmi.avillach.dataupload.aws;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AWSConfigurationTest {
    @Mock
    ConfigurableApplicationContext context;

    @Mock
    StsClientBuilder stsClientBuilder;

    @Mock
    StsClient stsClient;

    @InjectMocks
    AWSConfiguration subject;

    @Test
    void shouldCreateRoles() {
        ReflectionTestUtils.setField(subject, "institutions", List.of("i1", "i2"));
        ReflectionTestUtils.setField(subject, "roleArns", List.of(":)", ">:|"));
        ReflectionTestUtils.setField(subject, "externalIDs", List.of("frodo", "gimli"));
        ReflectionTestUtils.setField(subject, "buckets", List.of("b1", "b2"));
        ReflectionTestUtils.setField(subject, "kmsKeyIds", List.of("k1", "k2"));

        Map<String, SiteAWSInfo> actual = subject.roleARNs();
        Map<String, SiteAWSInfo> expected = Map.of(
            "i1", new SiteAWSInfo("i1", ":)", "frodo", "b1", "k1"),
            "i2", new SiteAWSInfo("i2", ">:|", "gimli", "b2", "k2")
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldNotCreateRoles() {
        ReflectionTestUtils.setField(subject, "institutions", List.of("i1", "i2"));
        ReflectionTestUtils.setField(subject, "roleArns", List.of(":)", ">:|", ":o"));
        ReflectionTestUtils.setField(subject, "externalIDs", List.of("frodo", "gimli"));
        ReflectionTestUtils.setField(subject, "buckets", List.of("b1", "b2"));
        ReflectionTestUtils.setField(subject, "kmsKeyIds", List.of("k1", "k2"));

        subject.roleARNs();

        Mockito.verify(context, Mockito.times(1)).close();
    }
}