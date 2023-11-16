package edu.harvard.dbmi.avillach.dataupload.status;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class QueryStatusRowMapper implements RowMapper<QueryStatus> {
    @Override
    public QueryStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
        UploadStatus genomic = UploadStatus.fromString(rs.getString("GENOMIC_STATUS"));
        UploadStatus phenotypic = UploadStatus.fromString(rs.getString("PHENOTYPIC_STATUS"));
        String started = rs.getDate("DATE_STARTED").toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String id = rs.getString("QUERY");
        String site = rs.getString("SITE");
        return new QueryStatus(genomic, phenotypic, id, started, site);
    }
}
