/*
 * This file is generated by jOOQ.
 */
package edu.harvard.dbmi.avillach.dataupload.codegen.generated.tables;


import edu.harvard.dbmi.avillach.dataupload.codegen.generated.tables.records.QueryStatusRecord;
import edu.harvard.dbmi.avillach.dataupload.codegen.generated.Keys;
import edu.harvard.dbmi.avillach.dataupload.codegen.generated.Uploader;

import java.time.LocalDateTime;
import java.util.Collection;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class QueryStatus extends TableImpl<QueryStatusRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>uploader.query_status</code>
     */
    public static final QueryStatus QUERY_STATUS = new QueryStatus();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<QueryStatusRecord> getRecordType() {
        return QueryStatusRecord.class;
    }

    /**
     * The column <code>uploader.query_status.QUERY</code>.
     */
    public final TableField<QueryStatusRecord, byte[]> QUERY = createField(DSL.name("QUERY"), SQLDataType.BINARY(16).nullable(false), this, "");

    /**
     * The column <code>uploader.query_status.GENOMIC_STATUS</code>.
     */
    public final TableField<QueryStatusRecord, String> GENOMIC_STATUS = createField(DSL.name("GENOMIC_STATUS"), SQLDataType.VARCHAR(64).nullable(false).defaultValue(DSL.inline("Unsent", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>uploader.query_status.PHENOTYPIC_STATUS</code>.
     */
    public final TableField<QueryStatusRecord, String> PHENOTYPIC_STATUS = createField(DSL.name("PHENOTYPIC_STATUS"), SQLDataType.VARCHAR(64).nullable(false).defaultValue(DSL.inline("Unsent", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>uploader.query_status.APPROVED</code>.
     */
    public final TableField<QueryStatusRecord, LocalDateTime> APPROVED = createField(DSL.name("APPROVED"), SQLDataType.LOCALDATETIME(0), this, "");

    /**
     * The column <code>uploader.query_status.SITE</code>.
     */
    public final TableField<QueryStatusRecord, String> SITE = createField(DSL.name("SITE"), SQLDataType.VARCHAR(64).nullable(false).defaultValue(DSL.inline("", SQLDataType.VARCHAR)), this, "");

    private QueryStatus(Name alias, Table<QueryStatusRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private QueryStatus(Name alias, Table<QueryStatusRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>uploader.query_status</code> table reference
     */
    public QueryStatus(String alias) {
        this(DSL.name(alias), QUERY_STATUS);
    }

    /**
     * Create an aliased <code>uploader.query_status</code> table reference
     */
    public QueryStatus(Name alias) {
        this(alias, QUERY_STATUS);
    }

    /**
     * Create a <code>uploader.query_status</code> table reference
     */
    public QueryStatus() {
        this(DSL.name("query_status"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Uploader.UPLOADER;
    }

    @Override
    public UniqueKey<QueryStatusRecord> getPrimaryKey() {
        return Keys.KEY_QUERY_STATUS_PRIMARY;
    }

    @Override
    public QueryStatus as(String alias) {
        return new QueryStatus(DSL.name(alias), this);
    }

    @Override
    public QueryStatus as(Name alias) {
        return new QueryStatus(alias, this);
    }

    @Override
    public QueryStatus as(Table<?> alias) {
        return new QueryStatus(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public QueryStatus rename(String name) {
        return new QueryStatus(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public QueryStatus rename(Name name) {
        return new QueryStatus(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public QueryStatus rename(Table<?> name) {
        return new QueryStatus(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public QueryStatus where(Condition condition) {
        return new QueryStatus(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public QueryStatus where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public QueryStatus where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public QueryStatus where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public QueryStatus where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public QueryStatus where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public QueryStatus where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public QueryStatus where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public QueryStatus whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public QueryStatus whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
