package gov.nysenate.sage.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import gov.nysenate.sage.client.response.DistrictResponse;
import gov.nysenate.sage.model.result.DistrictResult;

import java.io.IOException;
import java.util.Collection;

public class Test
{
    public enum TYPE {ASSEMBLY,CONGRESSIONAL,COUNTY,ELECTION,SENATE,SCHOOL,TOWN;};

    public static void main(String args[]) throws Exception
    {
        /*ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        ApiFormatError error = new ApiFormatError();

        System.out.println(om.writeValueAsString(error));
        XmlMapper xmlMapper = new XmlMapper();
        SimpleModule simpleModule = new SimpleModule("Collection", new Version(1,1,0,"","",""));
        simpleModule.addSerializer(new CollectionSerializer());
        xmlMapper.registerModule(simpleModule);

        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        System.out.println(xmlMapper.writeValueAsString(error));
        */
        DistrictResponse districtResponse = new DistrictResponse(new DistrictResult());
        FormatUtil.printObject(districtResponse);

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
