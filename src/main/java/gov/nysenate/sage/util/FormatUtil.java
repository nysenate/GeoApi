package gov.nysenate.sage.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
     * Returns an Xml string representation of the supplied map.
     * @param map               Map to format into Xml
     * @param rootElementName   Top most element name
     * @return
     * @throws JsonProcessingException
     */
    public static String mapToXml(Map<String,Object> map, String rootElementName, boolean indent) throws JsonProcessingException
    {
        JacksonXmlModule module = new JacksonXmlModule();
        module.addSerializer(Collection.class, new CollectionSerializer());
        XmlMapper xmlMapper = new XmlMapper(module);
        xmlMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

        if (indent){
            xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }

        /** The top most element is always a Map so we remove it */
        String xml = xmlMapper.writer().withRootName(rootElementName).writeValueAsString(map);
        xml = xml.replaceAll("^(<[^>]*?>\n*)", "").replaceAll("(</[^>]*?>\n*)$", "");
        return xml;
    }

    /**
     * Returns a JSON string representation of the supplied map.
     * @param map
     * @return
     * @throws JsonProcessingException
     */
    public static String mapToJson(Map<String, Object> map) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }

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