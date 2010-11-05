package control;

import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import connectors.FCCConnect;

import model.*;
import model.districts.Assembly;
import model.districts.Congressional;
import model.districts.County;
import model.districts.DistrictResponse;
import model.districts.Election;
import model.districts.Senate;


import generated.geoserver.json.*;

/**
 * @author Jared Williams
 * 
 * used for district related queries to the ApiServlet
 *
 */
public class DistrictServices {
	
	static String GEO_CQL_START = "&CQL_FILTER=";	
	static String GEO_API = "geoserver.url";
	static String GEO_CQL_LOC = GEO_CQL_START + "INTERSECT(the_geom,%20POINT%20(";
	static String GEO_CQL_END = "))";
	static String GEO_OUTPUT = "&outputformat=JSON";
	static String GEO_CQL_LIKE = "%20LIKE%20";
	
	public static void main(String[] args) throws IOException {
		
		for(int i = 1; i <= 62; i++) {
			System.out.println("    <url>\n"
		      +"      <loc>http://geo.nysenate.gov/examples/kml/sd" + i + ".kml</loc>\n"
		      +"      <geo:geo>\n"
		      +"        <geo:format>kml</geo:format>\n"
		      +"      </geo:geo>\n"
		      +"    </url>");
		}
		
		
		
//		DistrictServices ds = new DistrictServices();
//		
//		for(int i = 1; i <= 62; i++) {
//			FileWriter fw = new FileWriter("WebContent/kml/sd" + i + ".kml");
//			new File("WebContent/kml/sd" + i + ".kml").createNewFile();
//			ds.getPolyFromDistrict("senate",i+"", "kml", new PrintWriter(fw));
//			fw.close();
//		}
		
		
	}
	
	public void getPolyFromDistrict(String type, String district, String format, PrintWriter out) throws IOException {
		if(type.equals("senate")) {
			district = "State%20Senate%20District%20" + district;;
		}
		else if(type.equals("assembly")) {
			district = "Assembly%20District%20" + district;;
		}
		else if(type.equals("congressional")) {
			district = "Congressional%20District%20" + district;
		}
		else if(type.equals("county")) {
			//will have to be different
		}
		else if(type.equals("election")) {
			//stay the same
		}
		else {
			//throw error
		}
				
		WFS_POLY_NAME wfs = new WFS_POLY_NAME(type);
				
		String in = flatten(wfs.construct(district));
		
		polyPrep(in, format, type, out);
	}
	
	
	/**
	 * sends request to getPolygon for polygon retrieval
	 */
	public void getPolyFromAddress(String address, String format, String service, String type, PrintWriter out) throws IOException {
		Point p = GeoCode.getGeoCodedResponse(address, service);
		
		WFS_POLY_COORD wfs = new WFS_POLY_COORD(type);
		String in = flatten(wfs.construct(p.lat, p.lon));
		
		polyPrep(in, format, type, out);
		
	}
	
	/**
	 * if service is "none" point is broken down, if service is active sends request to be
	 * geocoded and then in either scenario request is sent to getPolygon for retrieval
	 */
	public void getPolyFromPoint(String latlng, String format, String service, String type, PrintWriter out) throws IOException {
		Point p = null;
		
		if(service != null && service.equals("none")) {
			p = new Point(new Double(latlng.split(",")[0]),new Double(latlng.split(",")[1]),"");
		
		}
		else {
			p = GeoCode.getReverseGeoCodedResponse(latlng, service).iterator().next();
			
		}
		
		if(p == null) {
			return;
			
		}
		
		WFS_POLY_COORD wfs = new WFS_POLY_COORD(type);
		String in = flatten(wfs.construct(p.lat, p.lon));
		
		polyPrep(in, format, type, out);
	}
	
	private void polyPrep(String in, String format, String type, PrintWriter out) throws IOException {
		
		
		String start = "\"geometry\":";
		String end = "\\]\\]\\]},";
		
		Pattern jsonP = Pattern.compile(start + ".*?" + end);
		Matcher jsonM = jsonP.matcher(in);
		
		Collection<Collection<Double>> points = new ArrayList<Collection<Double>>();
		GeoResult gr = null;
		
		/*
		 * This WFS call returns a polygon that has enough points to lock up gson,
		 * so I cut out the polygon from the json result via regex and construct that information
		 * myself, the rest of the object is deserialized with gson
		 */
		
		if(jsonM.find()) {
			Gson gson = new Gson();
			String json = in.substring(0, jsonM.start()) + in.substring(jsonM.end());
			String coords = in.substring(jsonM.start(), jsonM.end());
			gr = gson.fromJson(json, GeoResult.class);
			
			
			Pattern coordP = Pattern.compile("-?\\d*\\.\\d*,-?\\d*\\.\\d*");
			Matcher coordM = coordP.matcher(coords);
			
			
			while(coordM.find()) {
				String cur = coords.substring(coordM.start(), coordM.end());
				String[] parts = cur.split(",");
				
				Collection<Double> point = new ArrayList<Double>();
				for(String s:parts) {
					point.add(new Double(s));
				}
				
				points.add(point);
				
			}
		}

		String data = null;
		
		if(type.matches("county|assembly|congressional|senate")) {
			data = gr.getFeatures().iterator().next().getProperties().getNAMELSAD();
		}
		else if(type.equals("election")) {
			data = "Election District " + gr.getFeatures().iterator().next().getProperties().getED();
		}	
		
		getPolygon(points, data, format, out);
	}
		
	public void getPolygon(Collection<Collection<Double>> coordinates, String data, String format, PrintWriter out) {
				
		int count = 0;
				
		if(format.equals("kml")) {
			String xml_version = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			
			String kml_s = "<kml xmlns=\"http://www.opengis.net/kml/2.2\">";
			String kml_e = "</kml>";
			
			String pmark_s = "<Placemark>";
			String pmark_e = "</Placemark>";
			
			String name_s = "<name>";
			String name_e = "</name>";
			
			String poly_s = "<Polygon>";
			String poly_e = "</Polygon>";
			
			String extrude_s = "<extrude>";
			String extrude_e = "</extrude>";
			
			String altMode_s = "<altitudeMode>";
			String altMode_e = "</altitudeMode>";
			
			String outBoundIs_s = "<outerBoundaryIs>";
			String outBoundIs_e = "</outerBoundaryIs>";
			
			String linRing_s = "<LinearRing>";
			String linRing_e = "</LinearRing>";
			
			String coords_s = "<coordinates>";
			String coords_e = "</coordinates>";
			
			out.print(xml_version + "\n");
			out.print(kml_s + "\n");
			out.print("  " + pmark_s + "\n");
			out.print("    " + name_s + data + name_e + "\n");
			out.print("    " + poly_s + "\n");
			out.print("      " + extrude_s + "1" + extrude_e + "\n");
			out.print("      " + altMode_s + "relativeToGround" + altMode_e + "\n");
			out.print("      " + outBoundIs_s + "\n");
			out.print("        " + linRing_s + "\n");
			out.print("          " + coords_s + "\n");
			
			for(Collection<Double> three:coordinates) {
				Point2D point = new Point2D.Double();
				for(Double d:three) {
					if(count % 2 == 0) {
						point.setLocation(0.0, d);
						
					}
					else {
						point.setLocation(d, point.getY());
						
					}
					count++;
				}
				out.print("            " + point.getX() + "," + point.getY() + "\n");
				
			}

			out.print("          " + coords_e + "\n");
			out.print("        " + linRing_e + "\n");
			out.print("      " + outBoundIs_e + "\n");
			out.print("    " + poly_e + "\n");
			out.print("  " + pmark_e + "\n");
			out.print(kml_e + "\n");
		}
		else {
			String geom_start = "\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[";
			String geom_end = "]]]}}";
			
			String distr_start = "{\"placemark\":\"";
			String distr_end = "\",";
			
			out.print(distr_start + data + distr_end);
			
			out.print(geom_start);
			
			for(Collection<Double> three:coordinates) {
				Point2D point = new Point2D.Double();
				for(Double d:three) {
					if(count % 2 == 0) {
						point.setLocation(0.0, d);
						
					}
					else {
						point.setLocation(d, point.getY());
						
					}
					count++;
				}

				out.print((count > 2 ? "," : "") + "[" + point.getX() + "," + point.getY() + "]");
			}
			
			out.print(geom_end);
			
		}
	}
	
	/**
	 * sends request to districts for district information retrieval
	 */
	public String getDistrictsFromAddress(String address, String format, String service) throws Exception {
		return districts(GeoCode.getGeoCodedResponse(address, service), format);
		
	}
	
	/**
	 * if service is "none" point is broken down, if service is active sends request to be
	 * geocoded and then in either scenario request is sent to districts for retrieval
	 */
	public String getDistrictsFromPoint(String latlng, String format, String service) throws Exception {
		Point p = null;
		if(service != null && service.equals("none")) {
			p = new Point(new Double(latlng.split(",")[0]),new Double(latlng.split(",")[1]),"");
		
		}
		else {
			p = GeoCode.getReverseGeoCodedResponse(latlng, service).iterator().next();
			
		}
		
		if(p == null) {
			return null;
			
		}
		
		return districts(p, format);
	}
	
	/**
	 * based on point connects to GeoServer layers and retrieves district information
	 * 
	 * @returns xml or json string representation of data
	 */
	public String districts(Point p, String format) throws Exception {
		Connect c = new Connect();
		DistrictResponse dr = new DistrictResponse();
		GeoResult gr = null;
		
		gr = fromGeoserver(new WFS_County(), p);
		dr.setCounty(new County(gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		
		gr = fromGeoserver(new WFS_Election(), p);
		dr.setElection(new Election("Election District " + gr.getFeatures().iterator().next().getProperties().getED()));
		
		gr = fromGeoserver(new WFS_Assembly(), p);
		dr.setAssembly((Assembly)c.getObject(Assembly.class,
				"district",
				gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		
		gr = fromGeoserver(new WFS_Congressional(), p);
		dr.setCongressional((Congressional)c.getObject(Congressional.class,
				"district",
				gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));		
		
		gr = fromGeoserver(new WFS_Senate(), p);
		dr.setSenate((Senate)c.getObject(Senate.class,
				"district",
				gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		c.close();
		
		dr.setLat(p.lat);
		dr.setLon(p.lon);
		dr.setAddress(p.address);
		
		dr.setCensus(FCCConnect.doParsing(p.lat+"", p.lon+""));
				
		Gson gson = new Gson();
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{Point.class,DistrictResponse.class});
			return xstream.toXML(dr);
		}
		return gson.toJson(dr);
	}
	
	/**
	 * this function allows any type of WFS_ connectors to connect to GeoServer and retrieve
	 * applicable information, returns result from GeoServer
	 */
	public GeoResult fromGeoserver(WFS_ wfs, Point p) throws IOException {
		String json = flatten(wfs.construct(p.lat,p.lon));
		
		Gson gson = new Gson();
		
		GeoResult gr = null;
		
		try {
			gr = (GeoResult)gson.fromJson(json, GeoResult.class);
		}
		catch (Exception e) {
			
		}
		
		return gr;
	}
	
	/**
	 * flattens the data from a url in to one string, used in conjuction with google-json
	 * to decipher WFS responses
	 */
	public static String flatten(String url) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		
		StringBuilder sb = new StringBuilder("");
		
		String in = null;
		
		while((in = br.readLine()) != null) {
			sb.append(in);
		}
		
		br.close();
		
		return sb.toString();
	}
	
	/*
	 * The following are connectors types for the the GeoServer, they all have their own
	 * defined url structure and contents to return
	 */
	
	public class WFS_Election extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:election";
		static final String GEO_PROPERTY = "&propertyname=ED";
		/*static final String GEO_PROPERTY = "&propertyname=ED,EDS_COPY_,EDS_COPY_I,COUNTY,MCD2,WARD,EDP";*/
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_LOC + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_County extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:county";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		/*static final String GEO_PROPERTY = "&propertyname=COUNTYFP,NAME,NAMELSAD";*/
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_LOC + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_Assembly extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:assembly";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_LOC + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_Congressional extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:congressional";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_LOC + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	//opt/apache-tomcat-6.0.26/webapps/geoserver/data/data/shapes/
	//EPSG:4326
	public class WFS_Senate extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:senate";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_LOC + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_POLY_COORD extends WFS_ {
		String GEO_TYPE = "&typename=";
		
		public WFS_POLY_COORD(String type) {
			setGeoType(type);
		}
		
		private void setGeoType(String type) {
			if(type.equals("assembly")) {
				GEO_TYPE += "nysenate:assembly";
			}
			else if(type.equals("congressional")){
				GEO_TYPE += "nysenate:congressional";
			}
			else if(type.equals("county")){
				GEO_TYPE += "nysenate:county";
			}
			else if(type.equals("election")){
				GEO_TYPE += "nysenate:election";
			}
			else if(type.equals("senate")){
				GEO_TYPE += "nysenate:senate";
			}
		}
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_CQL_LOC + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	
	public class WFS_POLY_NAME extends WFS_ {
		String GEO_TYPE = "&typename=";
		String GEO_FILTER_TYPE;

		public WFS_POLY_NAME(String type) {
			setGeoType(type);
		}
		
		private void setGeoType(String type) {
			if(type.equals("assembly")) {
				GEO_TYPE += "nysenate:assembly";
				GEO_FILTER_TYPE="NAMELSAD";
			}
			else if(type.equals("congressional")){
				GEO_TYPE += "nysenate:congressional";
				GEO_FILTER_TYPE="NAMELSAD";
			}
			else if(type.equals("county")){
				GEO_TYPE += "nysenate:county";
				GEO_FILTER_TYPE="NAMELSAD";
			}
			else if(type.equals("election")){
				GEO_TYPE += "nysenate:election";
				GEO_FILTER_TYPE="ED";
			}
			else if(type.equals("senate")){
				GEO_TYPE += "nysenate:senate";
				GEO_FILTER_TYPE="NAMELSAD";
			}
		}
		
		public String construct(double x, double y) {
			return null;
		}
		
		public String construct(String value) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_CQL_START + GEO_FILTER_TYPE + GEO_CQL_LIKE + "'" + value + "'" + GEO_OUTPUT;
		}
		
	}
	public abstract class WFS_ {
		public abstract String construct(double x, double y);
	}
}
