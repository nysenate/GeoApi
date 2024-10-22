package gov.nysenate.sage.dao.base;

import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

/**
 * Common utility methods to be used by enums/classes that store sql queries.
 */
public final class SqlQueryUtils {
    private SqlQueryUtils() {}

    /**
     * Replaces the ${schema} placeholder in the given sql String with the given schema name.
     * This is mainly used for queries where the schema name can be user defined, e.g. the environment schema.
     *
     * @param sql String - A string that contains the ${schema} placeholders.
     * @param schema String - The name of the database schema
     * @return String
     */
    public static String getSqlWithSchema(String sql, String schema) {
        return new StringSubstitutor(Map.of("schema", schema)).replace(sql);
    }
}
