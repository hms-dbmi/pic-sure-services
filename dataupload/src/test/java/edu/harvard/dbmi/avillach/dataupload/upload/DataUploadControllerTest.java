package edu.harvard.dbmi.avillach.dataupload.upload;

import edu.harvard.dbmi.avillach.dataupload.hpds.Query;
import edu.harvard.dbmi.avillach.dataupload.status.QueryStatus;
import edu.harvard.dbmi.avillach.dataupload.status.UploadStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class DataUploadControllerTest {

    @Mock
    DataUploadService uploadService;

    @InjectMocks
    DataUploadController subject;

    @Test
    void shouldUpload() {
        Query query = new Query();
        query.setId("my id");

        ResponseEntity<QueryStatus> actual = subject.startUpload(query, "BCH");
        ResponseEntity<QueryStatus> expected = ResponseEntity.ok(
            new QueryStatus(
                UploadStatus.InProgress, UploadStatus.InProgress, query.getId(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE), "BCH"
            )
        );

        Mockito.verify(uploadService, Mockito.times(1))
            .upload(query, "BCH");
        Assertions.assertEquals(expected, actual);
    }
}