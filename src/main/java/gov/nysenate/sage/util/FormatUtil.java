package gov.nysenate.sage.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintStream;
import java.util.HashMap;

/**
 * Utility class for performing JSON object serialization/de-serialization and mapping.
*/

public class FormatUtil {

    protected static Logger logger = Logger.getLogger(FormatUtil.class);
    protected static ObjectMapper mapper = new ObjectMapper();

    public static String getString(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) ? json.getString(key) : "";
        } catch (JSONException e) {
            return null;
        }
    }

    public static Double getDouble(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) && !json.getString(key).equals("") ? json.getDouble(key) : 0;
        } catch (JSONException e) {
            return null;
        }
    }

    public static Integer getInteger(JSONObject json, String key) {

        try {
            return json.has(key) && !json.isNull(key) && !json.getString(key).equals("") ? json.getInt(key) : 0;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Returns JSON representation of object.
     * Failure to map object results in empty string.
     *
     * @return String   JSON string
     * */
    public static String toJsonString(Object o){
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(o);
        }
        catch(JsonGenerationException g){
            logger.error("Object to JSON Error: ".concat(g.getMessage()));
            return "";
        }
        catch(JsonMappingException m){
            logger.error("Object to JSON Error: ".concat(m.getMessage()));
            return "";
        }
        catch(Exception ex){
            logger.error("Object to JSON Error: ".concat(ex.getMessage()));
            return "";
        }
    }

    /** Prints out JSON representation of object to standard out */
    public static String printObject(Object o){
        return printObject(o, System.out);
    }

    /** Prints out JSON representation of object to the given print stream */
    public static String printObject(Object o, PrintStream ps){
        String s = toJsonString(o);
        if (ps != null){
            ps.println(s);
        }
        return s;
    }

    /**
     * Given a JSON string return a hash map of the key value pairs.
     * */
    public static HashMap<String,Object> jsonToHashMap(String json){
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
        try {
            return mapper.readValue(json, typeRef);
        }
        catch(JsonGenerationException g){
            logger.error("Object to JSON Error: ".concat(g.getMessage()));
            return null;
        }
        catch(JsonMappingException m){
            logger.error("Object to JSON Error: ".concat(m.getMessage()));
            return null;
        }
        catch(Exception ex){
            logger.error("Object to JSON Error: ".concat(ex.getMessage()));
            return null;
        }
    }
}