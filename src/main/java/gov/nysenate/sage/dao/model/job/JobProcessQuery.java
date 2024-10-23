package gov.nysenate.sage.dao.model.job;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum JobProcessQuery implements BasicSqlQuery {
    INSERT_JOB_PROCESS("INSERT INTO ${schema}." + SqlTable.PROCESS + " (userId, fileName, fileType, sourceFileName, requestTime, recordCount, validationReq, geocodeReq, districtReq) " +
            "VALUES (:userId,:fileName,:fileType,:sourceFileName,:requestTime,:recordCount,:validationReq,:geocodeReq,:districtReq) RETURNING id"),

    UPDATE_JOB_PROCESS_STATUS("UPDATE ${schema}." + SqlTable.STATUS + " SET processId = :processId, condition = :condition, completedRecords = :completedRecords, startTime = :startTime, completeTime = :completeTime, completed = :completed, messages = :messages WHERE processId = :processId"),

    INSERT_JOB_PROCESS_STATUS("INSERT INTO ${schema}." + SqlTable.STATUS + " (processId, condition, completedRecords, startTime, completeTime, completed, messages) " +
            "VALUES (:processId,:condition,:completedRecords,:startTime,:completeTime,:completed,:messages)"),

    GET_JOB_PROCESS_STATUS("SELECT * FROM ${schema}." + SqlTable.PROCESS + " \n" +
            "LEFT JOIN ${schema}." + SqlTable.STATUS + " status ON id = processId " +
            "WHERE processId = :processId"),

    GET_JOB_PROCESS_STATUS_BY_CONDITIONS("SELECT * FROM ${schema}." + SqlTable.PROCESS + "\n" +
            "LEFT JOIN ${schema}." + SqlTable.STATUS + " ON id = processId " +
            "WHERE "
    ),

    GET_RECENTLY_COMPLETED_JOB_PROCESSES("SELECT * FROM ${schema}." + SqlTable.PROCESS + " " +
            "LEFT JOIN ${schema}." + SqlTable.STATUS + " ON id = processId " +
            "WHERE status.completeTime >= :afterThis "  // completeTime filter
    );

    private final String sql;

    JobProcessQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}
