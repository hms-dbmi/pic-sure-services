package edu.harvard.dbmi.avillach.dataupload.status;

import edu.harvard.dbmi.avillach.dataupload.codegen.generated.tables.records.QueryStatusRecord;
import org.springframework.stereotype.Component;

@Component
public class DataUploadStatusMapper {
    public DataUploadStatuses map(QueryStatusRecord r, String queryUUID) {
        return new DataUploadStatuses(
            UploadStatus.fromString(r.getGenomicStatus()),
            UploadStatus.fromString(r.getPhenotypicStatus()),
            queryUUID,
            r.getApproved() == null ? null : r.getApproved().toLocalDate(),
            r.getSite()
        );
    }
}
