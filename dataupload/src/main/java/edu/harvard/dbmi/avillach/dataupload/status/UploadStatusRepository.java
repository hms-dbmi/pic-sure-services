package edu.harvard.dbmi.avillach.dataupload.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UploadStatusRepository {
    @Autowired
    private JdbcTemplate template;

    @Autowired
    private QueryStatusRowMapper mapper;

    public Optional<QueryStatus> getStatuses(String queryId) {
        try {
            return Optional.ofNullable(template.queryForObject(
                "SELECT GENOMIC_STATUS, PHENOTYPIC_STATUS, QUERY, DATE_STARTED, SITE FROM query_status WHERE query = ?",
                mapper, queryId
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void setGenomicStatus(String queryId, UploadStatus status, String site) {
        String sql = """
            INSERT INTO query_status
                (QUERY, GENOMIC_STATUS, SITE, DATE_STARTED)
                VALUES (?, ?, ?, now())
                ON DUPLICATE KEY UPDATE genomic_status=?
        """;
        template.update(sql, queryId, status.toString(), site, status.toString());
    }

    public void setPhenotypicStatus(String queryId, UploadStatus status, String site) {
        String sql = """
            INSERT INTO query_status
                (QUERY, PHENOTYPIC_STATUS, SITE, DATE_STARTED)
                VALUES (?, ?, ?, now())
                ON DUPLICATE KEY UPDATE phenotypic_status=?
        """;
        template.update(sql, queryId, status.toString(), site, status.toString());
    }
}
