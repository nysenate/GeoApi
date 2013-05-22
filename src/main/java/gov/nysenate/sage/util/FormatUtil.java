package gov.nysenate.sage.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Utility class for performing JSON object serialization/de-serialization and mapping.
*/
public class FormatUtil {

    protected static Logger logger = Logger.getLogger(FormatUtil.class);
    protected static ObjectMapper mapper = new ObjectMapper();

    public static String toCamelCase(String s)
    {
        if (s != null && s.contains("_")) {
            return StringUtils.uncapitalize(WordUtils.capitalizeFully(s, '_').replaceAll("_", ""));
        }
        return s;
    }

    /** Removes leading zeroes in a string */
    public static String trimLeadingZeroes(String s)
    {
        if (s != null){
            return s.replaceFirst("^0+(?!$)", "");
        }
        return "";
    }

    /**
     * Custom serializer to deal with element names when outputting collections.
     * Uses the object's simple class name as the element to represent the content of the collection.
     */
    public static class CollectionSerializer extends JsonSerializer<Collection>
    {
        public void serialize(Collection value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException
        {
            generator.writeStartObject();
            for (Object o : value) {
                generator.writeFieldName(o.getClass().getSimpleName());
                JsonSerializer<Object> ser = provider.findTypedValueSerializer(o.getClass(), true, null);
                ser.serialize(o, generator, provider);
            }
            generator.writeEndObject();
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