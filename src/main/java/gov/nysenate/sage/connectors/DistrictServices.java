package gov.nysenate.sage.connectors;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import generated.geoserver.json.*;
import gov.nysenate.sage.connectors.GeoServerConnect.WFS_POLY;
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
	
	static GeoServerConnect gsCon = new GeoServerConnect();
	
	public DistrictServices() {
		
	}
	
	/**
	 * This is used to get a KML or JSON polygon from GeoServer if the district name is known
	 * 
	 * @param type match type from POLY_NAMES
	 * @param district associated with NAMSLAD or ED field on GeoServer
	 * @param format kml or json
	 * @throws IOException
	 */
	public static StringBuffer getPolyFromDistrict(String type, String district, String format) throws IOException {
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
		
		return polyPrep(json, format, type);
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
	public static StringBuffer getPolyFromAddress(String address, String format, String service, String type) throws IOException {
		Point p = GeoCode.getGeoCodedResponse(address, service);
		
		/* get flattened request from geoserver */
		WFS_POLY wfs = gsCon.new WFS_POLY(type);
		String json = gsCon.flatten(wfs.construct(p.lat, p.lon));
		
		return polyPrep(json, format, type);
		
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
	public static StringBuffer getPolyFromPoint(String latlng, String format, String service, String type) throws IOException {
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
		
		
		/* get flattened request from geoserver */
		WFS_POLY wfs = gsCon.new WFS_POLY(type);
		String json = gsCon.flatten(wfs.construct(p.lat, p.lon));
		
		return polyPrep(json, format, type);
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
	private static StringBuffer polyPrep(String in, String format, String type) throws IOException {
		
		
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
		
		return getPolygon(points, data, format);
	}
	
	/**
	 * writes kml or geojson data with printwriter
	 * 
	 * @param coordinates compiled from polyPrep
	 * @param data district key
	 * @param format kml or json
	 * @param out for output
	 */
	public static StringBuffer getPolygon(Collection<Collection<Double>> coordinates, String data, String format) {
		
		StringBuffer out = new StringBuffer("");
		
		int count = 0;
				
		if(format.matches("(kml|xml)")) {
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
			
			out.append(xml_version + "\n");
			out.append(kml_s + "\n");
			out.append("  " + pmark_s + "\n");
			out.append("    " + name_s + data + name_e + "\n");
			out.append("    " + poly_s + "\n");
			out.append("      " + extrude_s + "1" + extrude_e + "\n");
			out.append("      " + altMode_s + "relativeToGround" + altMode_e + "\n");
			out.append("      " + outBoundIs_s + "\n");
			out.append("        " + linRing_s + "\n");
			out.append("          " + coords_s + "\n");
			
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
				out.append("            " + point.getX() + "," + point.getY() + "\n");
				
			}

			out.append("          " + coords_e + "\n");
			out.append("        " + linRing_e + "\n");
			out.append("      " + outBoundIs_e + "\n");
			out.append("    " + poly_e + "\n");
			out.append("  " + pmark_e + "\n");
			out.append(kml_e + "\n");
		}
		else {
			String geom_start = "\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[";
			String geom_end = "]]]}}";
			
			String distr_start = "{\"placemark\":\"";
			String distr_end = "\",";
			
			out.append(distr_start + data + distr_end);
			
			out.append(geom_start);
			
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

				out.append((count > 2 ? "," : "") + "[" + point.getX() + "," + point.getY() + "]");
			}
			
			out.append(geom_end);
			
		}
		return out;
	}
	
	static class Timer {
		long t;
		public void s() {
			t = System.currentTimeMillis();
		}
		public double e() {
			return (System.currentTimeMillis() - t)/1000.0;
		}
		public static Timer get() {
			Timer t = new Timer();
			t.s();
			return t;
		}
	}
	
	public static DistrictResponse getDistrictsForBlueBird(String latlng) throws Exception {
		String tuple[] = latlng.split(",");
		
		Point p = new Point(new Double(tuple[0]), new Double(tuple[1]));
		
		DistrictResponse dr = districts(p, false);
		
		return dr;
	}
	
	public static DistrictResponse getDistrictsForBluebird(String addr, String city,
			String state, String zip4, String zip5, String service) throws Exception {
		
		ValidateResponse vr = null;
		
		Point p = null;
		
		Object obj = null;
		
		obj = USPSConnect.validateAddress(null, addr, city, state, zip5, zip4, "false");
		
		if(obj instanceof ValidateResponse) {
			vr = (ValidateResponse)obj;
			p = GeoCode.getGeoCodedResponse(vr.getAddress2(), vr.getCity(), vr.getState(), vr.getZip4(), vr.getZip5(), service);
		}
		
		if(p == null) {
			p = GeoCode.getGeoCodedResponse(addr, city, state, zip4, zip5, service);
		}
		
		DistrictResponse dr = districts(p, false);
		
		if(vr != null) {
			dr.setAddress(new AddressType(null, vr));
		}
		else {
			dr.setAddress(new AddressType(p.address, null));
		}
		
		return dr;
	}
	
	public static DistrictResponse getDistrictsFromAddress(String addr, String city,
			String state, String zip4, String zip5, String service) throws Exception {

		Point p = GeoCode.getGeoCodedResponse(addr, city, state, zip4, zip5, service);

		return districts(p, true);
	}
	
	/**
	 * sends request to districts for district information retrieval
	 */
	public static DistrictResponse getDistrictsFromAddress(String address, String service)
																	throws Exception {
		return districts(GeoCode.getGeoCodedResponse(address, service), true);
		
	}
	
	/**
	 * if service is "none" point is broken down, if service is active sends request to be
	 * geocoded and then in either scenario request is sent to districts for retrieval
	 */
	public static DistrictResponse getDistrictsFromPoint(String latlng, String service)
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
		
		return districts(p, true);
	}
	
	/**
	 * based on point connects to GeoServer layers and retrieves district information
	 * 
	 * @returns xml or json string representation of data
	 */
	public static DistrictResponse districts(Point p, boolean meta) throws Exception {
		Connect c = new Connect();
		DistrictResponse dr = new DistrictResponse();
		GeoResult gr = null;
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(COUNTY), p);
		dr.setCounty(new County(gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(ELECTION), p);
		if(meta) {
			dr.setElection(new Election("Election District " +
					gr.getFeatures().iterator().next().getProperties().getED()));
		}
		else {
			dr.setElection(new Election(gr.getFeatures().iterator().next().getProperties().getED()));
		}
		
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(ASSEMBLY), p);
		if(meta) {
			dr.setAssembly((Assembly)c.getObject(Assembly.class,
					"district",
					gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		}
		else {
			dr.setAssembly(new Assembly(gr.getFeatures().iterator().next().getProperties().getNAMELSAD()
					.replaceAll("Assembly District ", "")));
		}
		

		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(CONGRESSIONAL), p);
		if(meta) {
			dr.setCongressional((Congressional)c.getObject(Congressional.class,
					"district",
					gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));	
		}
		else {
			dr.setCongressional(new Congressional(gr.getFeatures().iterator().next().getProperties().getNAMELSAD()
					.replaceAll("Congressional District ", "")));	
		}
			
		
		Senate senate = null;
		
		gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(SENATE), p);
		if(meta) {
			senate = (Senate)c.getObject(Senate.class,
					"district",
					gr.getFeatures().iterator().next().getProperties().getNAMELSAD());
		}
		else {
			senate = new Senate(gr.getFeatures().iterator().next().getProperties().getNAMELSAD()
					.replaceAll("State Senate District ",""));
		}
		
		List<Senate> nearbySenateDistricts = gsCon.getNearbySenateDistricts(senate.getDistrict(),
				p, SENATE, CROSS_DISTANCE);
		
		if(nearbySenateDistricts != null && !nearbySenateDistricts.isEmpty()) {
			senate.setNearbyDistricts(nearbySenateDistricts);
		}
		
		dr.setSenate(senate);
		
		c.close();		
		
		dr.setLat(p.lat);
		dr.setLon(p.lon);
		
		dr.setAddress(p.address);
		
		//API is dead
		//dr.setCensus(FCCConnect.doParsing(p.lat+"", p.lon+""));		
		
		return dr;
	}
	
	static class AddressType {
		Object simple;
		Object extended;
		AddressType(Object simple, Object extended) {
			this.simple = simple;
			this.extended = extended;
		}
	}
}
