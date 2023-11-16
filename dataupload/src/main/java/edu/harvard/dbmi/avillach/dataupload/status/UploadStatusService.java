package edu.harvard.dbmi.avillach.dataupload.status;

import edu.harvard.dbmi.avillach.dataupload.hpds.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UploadStatusService {
    @Autowired
    private UploadStatusRepository repository;

    public void setGenomicStatus(Query query, UploadStatus status, String site) {
        repository.setGenomicStatus(query.getId(), status, site);
    }

    public void setPhenotypicStatus(Query query, UploadStatus status, String site) {
        repository.setPhenotypicStatus(query.getId(), status, site);
    }

    public Optional<QueryStatus> getStatus(String queryId) {
        return repository.getStatuses(queryId);
    }

}
