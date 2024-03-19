package edu.harvard.dbmi.avillach.dataupload.status;

import edu.harvard.dbmi.avillach.dataupload.codegen.generated.Tables;
import edu.harvard.dbmi.avillach.dataupload.codegen.generated.tables.QueryStatus;
import edu.harvard.dbmi.avillach.dataupload.codegen.generated.tables.records.QueryStatusRecord;
import org.jooq.DSLContext;
import org.jooq.SQL;
import org.jooq.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Repository
public class StatusRepository {
    @Autowired
    private JdbcTemplate template;

    @Autowired
    private DataUploadStatusMapper mapper;

    @Autowired
    DSLContext dslContext;

    public Optional<DataUploadStatuses> getQueryStatus(String queryId) {
        UUID uuid = UUID.fromString(queryId);
        QueryStatusRecord record = dslContext
            .selectFrom(Tables.QUERY_STATUS)
            .where(QueryStatus.QUERY_STATUS.QUERY.eq(asBytes(uuid)))
            .fetchOne();
        return Optional.ofNullable(record).map(r -> mapper.map(r, queryId));
    }

    public void setGenomicStatus(String queryId, UploadStatus status) {
        UUID uuid = UUID.fromString(queryId);
        dslContext
            .insertInto(Tables.QUERY_STATUS)
            .columns(QueryStatus.QUERY_STATUS.QUERY, QueryStatus.QUERY_STATUS.GENOMIC_STATUS)
            .values(asBytes(uuid), status.name())
            .onDuplicateKeyUpdate()
            .set(QueryStatus.QUERY_STATUS.GENOMIC_STATUS, status.name())
            .execute();
    }

    public void setPhenotypicStatus(String queryId, UploadStatus status) {
        UUID uuid = UUID.fromString(queryId);
        dslContext
            .insertInto(Tables.QUERY_STATUS)
            .columns(QueryStatus.QUERY_STATUS.QUERY, QueryStatus.QUERY_STATUS.PHENOTYPIC_STATUS)
            .values(asBytes(uuid), status.name())
            .onDuplicateKeyUpdate()
            .set(QueryStatus.QUERY_STATUS.PHENOTYPIC_STATUS, status.name())
            .execute();
    }

    public void setApproved(String queryId, LocalDate approvalDate) {
        UUID uuid = UUID.fromString(queryId);
        dslContext
            .insertInto(Tables.QUERY_STATUS)
            .columns(QueryStatus.QUERY_STATUS.QUERY, QueryStatus.QUERY_STATUS.APPROVED)
            .values(asBytes(uuid), approvalDate.atStartOfDay())
            .onDuplicateKeyUpdate()
            .set(QueryStatus.QUERY_STATUS.APPROVED, approvalDate.atStartOfDay())
            .execute();
    }

    public void setSite(String picSureId, String site) {
        UUID uuid = UUID.fromString(picSureId);
        dslContext
            .insertInto(Tables.QUERY_STATUS)
            .columns(QueryStatus.QUERY_STATUS.QUERY, QueryStatus.QUERY_STATUS.SITE)
            .values(asBytes(uuid), site)
            .onDuplicateKeyUpdate()
            .set(QueryStatus.QUERY_STATUS.SITE, site)
            .execute();
    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
