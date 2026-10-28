package gov.nysenate.sage.dao.base;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

public interface BasicSqlQuery {
    /**
     * Return the SQL query as is.
     */
    String getSql();

    /**
     * Retrieve a formatted SQL String with the envSchema value replaced where
     * applicable. This is needed for allowing configurable schema names.
     */
    default String getSql(String envSchema) {
        return getSql(envSchema, Map.of());
    }

    /**
     * Replaces the ${schema} placeholder in the given SQL String with the given schema name, among other replacements.
     * This is mainly used for queries where the schema name can be user defined, e.g. the environment schema.
     */
    default String getSql(String envSchema, Map<String, String> otherReplacements) {
        Map<String, String> replacements = new HashMap<>(otherReplacements);
        replacements.put("schema", envSchema);
        return new StringSubstitutor(replacements).replace(getSql());
    }
}
