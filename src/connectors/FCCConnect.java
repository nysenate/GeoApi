package connectors;

import generated.fcc.Response;

import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import model.districts.Census;

public class FCCConnect {
	
	public static final String FCC_API = "http://data.fcc.gov/api/block/find?";
	public static final String FCC_LAT = "latitude=";
	public static final String FCC_LON = "&longitude=";
	public static final String FCC_XML = "";
	
	public static Census doParsing(String lat, String lon) {
		Census c = new Census();
		
		try {
			Response r = parseStream(new URL(constructURL(lat, lon)));
			
			c.setFips(r.getBlock().getFIPS().toString());
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return c;
	}
	
	public static Census doParsing(String latlon) {
		return doParsing(latlon.split(",")[0],latlon.split(",")[1]);
	}
	
	public static String constructURL(String lat, String lon) {
		return FCC_API + FCC_LAT + lat + FCC_LON + lon + FCC_XML;
	}
	
	
	public static Response parseStream(URL url) throws Exception{
		String packageName = "generated.fcc";
		JAXBContext jc = JAXBContext.newInstance(packageName);
		Unmarshaller u = jc.createUnmarshaller();
		Response r = (Response)u.unmarshal(url);

		return r;
	}
}
