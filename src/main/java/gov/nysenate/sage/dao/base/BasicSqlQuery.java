package gov.nysenate.sage.dao.base;

public interface BasicSqlQuery {
    /**
     * Return the sql query as is.
     */
    String getSql();

    /**
     * Retrieve a formatted sql String with the envSchema value replaced where
     * applicable. This is needed for allowing configurable schema names.
     */
    default String getSql(String envSchema) {
        return SqlQueryUtils.getSqlWithSchema(getSql(), envSchema);
    }
}
