package gov.nysenate.sage.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import gov.nysenate.sage.client.response.DistrictResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.util.auth.JobUserAuth;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

public class Test
{
    public enum TYPE {ASSEMBLY,CONGRESSIONAL,COUNTY,ELECTION,SENATE,SCHOOL,TOWN;};

    public static void main(String args[]) throws Exception
    {
        FormatUtil.printObject((808+94-1)/95);
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


}
