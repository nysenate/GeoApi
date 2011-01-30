package gov.nysenate.sage.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;




import generated.geoserver.json.*;
import gov.nysenate.sage.connectors.GeoServerConnect.WFS_POLY;
import gov.nysenate.sage.connectors.GeoServerConnect.WFS_REQUEST;
import gov.nysenate.sage.model.*;
import gov.nysenate.sage.model.districts.Assembly;
import gov.nysenate.sage.model.districts.Congressional;
import gov.nysenate.sage.model.districts.County;
import gov.nysenate.sage.model.districts.DistrictResponse;
import gov.nysenate.sage.model.districts.Election;
import gov.nysenate.sage.model.districts.Senate;
import gov.nysenate.sage.util.Connect;

/**
 * @author Jared Williams
 * 
 * used for district related queries in the ApiServlet
 *
 */
public class DistrictServices {	
	static String ASSEMBLY = "assembly";
	static String CONGRESSIONAL = "congressional";
	static String COUNTY = "county";
	static String ELECTION = "election";
	static String SENATE = "senate";
	static double CROSS_DISTANCE = 0.005;
	
	GeoServerConnect gsCon = new GeoServerConnect();
	
	public DistrictServices() {
		
	}
	
	/**
	 * This is used to get a KML or JSON polygon from GeoServer if the district name is known
	 * 
	 * @param type match type from POLY_NAMES
	 * @param district associated with NAMSLAD or ED field on GeoServer
	 * @param format kml or json
	 * @param out printwriter, passed in due to associated issues with the size of
	 *        the KML and JSON strings being generated
	 * @throws IOException
	 */
	public void getPolyFromDistrict(String type, String district, String format, PrintWriter out) throws IOException {
		if(type.equals(SENATE)) {
			district = "State%20Senate%20District%20" + district;;
		}
		else if(type.equals(ASSEMBLY)) {
			district = "Assembly%20District%20" + district;;
		}
		else if(type.equals(CONGRESSIONAL)) {
			district = "Congressional%20District%20" + district;
		}
		else if(type.equals(COUNTY)) {
			//TODO: have to take special consideration
		}
		else if(type.equals(ELECTION)) {
			//stay the same
		}
		else {
			//throw error
		}
		
		/* get flattened request from geoserver */
		WFS_POLY wfs = gsCon.new WFS_POLY(type);
		String json = gsCon.flatten(wfs.construct(district));
		
		polyPrep(json, format, type, out);
	}
	
	/**
	 * This is used to get a KML or JSON polygon from GeoServer when only an address is known
	 * 
	 * @param address address to be geocoded
	 * @param format kml or json
	 * @param service google|yahoo|bing|none
	 * @param type must match POLY_NAMES
	 * @param out for output
	 * @throws IOException
	 */
	public void getPolyFromAddress(String address, String format, String service, String type, PrintWriter out) throws IOException {
		Point p = GeoCode.getGeoCodedResponse(address, service);
		
		/* get flattened request from geoserver */
		WFS_POLY wfs = gsCon.new WFS_POLY(type);
		String json = gsCon.flatten(wfs.construct(p.lat, p.lon));
		
		polyPrep(json, format, type, out);
		
	}
	
	/**
	 * This is used to get a KML or JSON polygon from GeoServer when a point is known
	 * 
	 * if service is "none" point is broken down, if service is active sends request to be
	 * reverse geocoded and then in either scenario request is sent to getPolygon for retrieval
	 * 
	 * @param latlng coordinates to be looked up
	 * @param format kml or json
	 * @param service google|yahoo|bing|none
	 * @param type must match POLY_NAMES
	 * @param out for output
	 * @throws IOException
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
		
		/* get flattened request from geoserver */
		WFS_POLY wfs = gsCon.new WFS_POLY(type);
		String json = gsCon.flatten(wfs.construct(p.lat, p.lon));
		
		polyPrep(json, format, type, out);
	}
	
	/**
	 * The point data from GeoServer is very large and typically breaks the GSON parser, this separates the point
	 * data from any other api data coming from geoserver, creates GeoResponse from that and manually
	 * parses the points
	 * @param in json data
	 * @param format kml or json
	 * @param type must match POLY_NAMES
	 * @param out for output
	 * @throws IOException
	 */
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
	
	/**
	 * writes kml or geojson data with printwriter
	 * 
	 * @param coordinates compiled from polyPrep
	 * @param data district key
	 * @param format kml or json
	 * @param out for output
	 */
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
	public String getDistrictsFromAddress(String address, String format, String service)
																	throws Exception {
		return districts(GeoCode.getGeoCodedResponse(address, service), format);
		
	}
	
	/**
	 * if service is "none" point is broken down, if service is active sends request to be
	 * geocoded and then in either scenario request is sent to districts for retrieval
	 */
	public String getDistrictsFromPoint(String latlng, String format, String service)
																	throws Exception {
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
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(COUNTY), p);
		dr.setCounty(new County(gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(ELECTION), p);
		dr.setElection(new Election("Election District " +
				gr.getFeatures().iterator().next().getProperties().getED()));
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(ASSEMBLY), p);
		dr.setAssembly((Assembly)c.getObject(Assembly.class,
				"district",
				gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(CONGRESSIONAL), p);
		dr.setCongressional((Congressional)c.getObject(Congressional.class,
				"district",
				gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));		
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(SENATE), p);
		
		Senate senate = (Senate)c.getObject(Senate.class,
				"district",
				gr.getFeatures().iterator().next().getProperties().getNAMELSAD());
		senate.setNearbyDistricts(gsCon.getNearbySenateDistricts(senate.getDistrict(),
													p, SENATE, CROSS_DISTANCE));
		dr.setSenate(senate);
		
		c.close();		
		
		dr.setLat(p.lat);
		dr.setLon(p.lon);
		dr.setAddress(p.address);
		
		//API is dead
		//dr.setCensus(FCCConnect.doParsing(p.lat+"", p.lon+""));		
				
		Gson gson = new Gson();
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{Point.class,DistrictResponse.class});
			return xstream.toXML(dr);
		}
		return gson.toJson(dr);
	}
	
	public static void main(String[] args) throws Exception {
		new DistrictServices().writeJson();
	}
	
	public void writeJson() throws Exception {
		Connect c = new Connect();
		
		Gson gson = new Gson();//GsonBuilder().setPrettyPrinting().create();
		
		HashMap<Integer,String> map = new HashMap<Integer,String>();
		BufferedReader br = new BufferedReader(new FileReader(new File("zoom2")));
		
		String in = null;
		
		while((in = br.readLine()) != null) {
			map.put(new Integer(in.split(":")[0]), in.split(":")[1]);
		}
		br.close();
		
		for(int i = 1; i <= 62; i++) {
			FileWriter fw = new FileWriter("WebContent/maps/json/sd" + i + ".json");
			new File("WebContent/maps/json/sd" + i + ".json").createNewFile();
			
			PrintWriter pw = new PrintWriter(fw);
			
			WFS_REQUEST sen = gsCon.new WFS_REQUEST(SENATE);
			
			GeoResult gr = gsCon.fromGeoserver(sen,"State Senate District " + i);
			
			double lat = new Double(gr.getFeatures().iterator().next().getProperties().getINTPTLAT());
			double lon = new Double(gr.getFeatures().iterator().next().getProperties().getINTPTLON());
			
			Senate senate = (Senate) c.getObject(Senate.class, "district", "State Senate District " + i);
			
			Pattern p = Pattern.compile("(\\d+) \\((.*?),(.*?)\\)");
			Matcher m = p.matcher(map.get(i));
			
			if(m.find()) {
				SenateMapInfo smi = new SenateMapInfo(lat,lon,
						new Double(m.group(1)),
						new Double(m.group(2)),
						new Double(m.group(3)),senate);
				pw.write(gson.toJson(smi));
			}
			else {
				SenateMapInfo smi = new SenateMapInfo(lat,lon,
						new Double(map.get(i)),null,null,senate);
				pw.write(gson.toJson(smi));
			}
						
			
			pw.close();
		}
	}

/*	public void writeKml() {
		for(int i = 1; i <= 62; i++) {
		FileWriter fw = new FileWriter("WebContent/kml/sd" + i + ".kml");
		new File("WebContent/kml/sd" + i + ".kml").createNewFile();
		getPolyFromDistrict("senate",i+"", "kml", new PrintWriter(fw));
		fw.close();
	}*/
}
