package edu.harvard.dbmi.avillach.dataupload.status;

public record QueryStatus(
    UploadStatus genomic, UploadStatus phenotypic,
    String queryId, String uploadDate, String site
) {
}
