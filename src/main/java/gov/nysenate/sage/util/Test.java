package gov.nysenate.sage.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.client.methods.HttpPost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class Test
{
    public enum TYPE {ASSEMBLY,CONGRESSIONAL,COUNTY,ELECTION,SENATE,SCHOOL,TOWN;};

    public static class test {

        private String variable = "";

        public void setVariable(String var)
        {
            if (var != null){
                this.variable = var;
            }
        }
        public String getVariable()
        {
            return this.variable;
        }
    }

    public static class Testing {
        private final String a;
        private final String b;
        private final String c;

        public Testing(String a, String b, String c) {
            this.a = a; this.b=b; this.c=c;
        }

        public String getA() { return a; }
        public String getB() { return b; }
        public String getC() { return c; }
    }

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

    public static void main(String args[]) throws Exception
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("tt", Arrays.asList("x", "y", "z"));
        HashMap<String, String> b = new HashMap<String, String>();
        b.put("a", "aa");
        b.put("b", "bb");
        map.put("ss", b);


    }
}
