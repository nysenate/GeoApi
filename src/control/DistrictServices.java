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
	
	static String GEO_API = "geoserver.url";
	static String GEO_CQL_START = "&CQL_FILTER=INTERSECT(the_geom,%20POINT%20(";
	static String GEO_CQL_END = "))";
	static String GEO_OUTPUT = "&outputformat=JSON";
	
	/**
	 * sends request to getPolygon for polygon retrieval
	 */
	public void getPolyFromAddress(String address, String format, String service, String type, PrintWriter out) throws IOException {
		polyPrep(GeoCode.getGeoCodedResponse(address, service), format, type, out);
		
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
		
		polyPrep(p, format, type, out);
	}
	
	private void polyPrep(Point p, String format, String type, PrintWriter out) throws IOException {
		
		
		String start = "\"geometry\":";
		String end = "\\]\\]\\]},";
		
		
		WFS_POLY wfs = new WFS_POLY(type);
		String in = flatten(wfs.construct(p.lat, p.lon));
		
		Pattern jsonP = Pattern.compile(start + ".*?" + end);
		Matcher jsonM = jsonP.matcher(in);
		
		Collection<Collection<Double>> points = new ArrayList<Collection<Double>>();
		GeoResult gr = null;
		
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
		
		getPolygon(p, points, data, format, out);
	}
	
	public void getPolygon(Point p, Collection<Collection<Double>> coordinates, String data, String format, PrintWriter out) {
				
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
				out.print("            " + point.getY() + "," + point.getX() + "\n");
				
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
			
			String lat_start = "{\"lat\":";
			String lat_end = ",";
			
			String lon_start = "\"lon\":";
			String lon_end = ",";
			
			String addr_start = "\"address\":\"";
			String addr_end = "\",";
			
			String distr_start = "\"placemark\":\"";
			String distr_end = "\",";
			
			out.print(lat_start + p.lat + lat_end);
			out.print(lon_start + p.lon + lon_end);
			out.print((p.address != null) ? addr_start + p.address + addr_end: "");
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

				out.print((count > 2 ? "," : "") + "[" + point.getY() + "," + point.getX() + "]");
			}
			
			out.print(geom_end);
			
		}
	}
	
	/**
	 * sends request to districts for district information retrieval
	 */
	public String getDistrictsFromAddress(String address, String format, String service) throws IOException {
		return districts(GeoCode.getGeoCodedResponse(address, service), format);
		
	}
	
	/**
	 * if service is "none" point is broken down, if service is active sends request to be
	 * geocoded and then in either scenario request is sent to districts for retrieval
	 */
	public String getDistrictsFromPoint(String latlng, String format, String service) throws IOException {
		Point p = null;
		System.out.println(service);
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
	public String districts(Point p, String format) throws IOException {
		Connect c = new Connect();
		DistrictResponse dr = new DistrictResponse();
		GeoResult gr = null;
		
		gr = fromGeoserver(new WFS_County(), p);
		dr.setCounty(new County(gr.getFeatures().iterator().next().getProperties().getNAMELSAD()));
		
		gr = fromGeoserver(new WFS_Election(), p);
		dr.setElection(new Election("Election District " + gr.getFeatures().iterator().next().getProperties().getED()));
		
		gr = fromGeoserver(new WFS_Assembly(), p);
		dr.setAssembly((Assembly)c.objectFromClosedResultSet(Assembly.class, c.getObjectById(Assembly.class,
				"district", gr.getFeatures().iterator().next().getProperties().getNAMELSAD())));
		
		gr = fromGeoserver(new WFS_Congressional(), p);
		dr.setCongressional((Congressional)c.objectFromClosedResultSet(Congressional.class, c.getObjectById(
				Congressional.class, "district",gr.getFeatures().iterator().next().getProperties().getNAMELSAD())));		
		
		gr = fromGeoserver(new WFS_Senate(), p);
		dr.setSenate((Senate)c.objectFromClosedResultSet(Senate.class,c.getObjectById(
				Senate.class, "district",gr.getFeatures().iterator().next().getProperties().getNAMELSAD())));
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
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_START + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_County extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:county";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		/*static final String GEO_PROPERTY = "&propertyname=COUNTYFP,NAME,NAMELSAD";*/
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_START + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_Assembly extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:assembly";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_START + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_Congressional extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:congressional";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_START + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	//opt/apache-tomcat-6.0.26/webapps/geoserver/data/data/shapes/
	//EPSG:4326
	public class WFS_Senate extends WFS_ {
		static final String GEO_TYPE = "&typename=nysenate:senate";
		static final String GEO_PROPERTY = "&propertyname=NAMELSAD";
		
		public String construct(double x, double y) {
			return Resource.get(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_START + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public class WFS_POLY extends WFS_ {
		String GEO_TYPE = "&typename=";
		
		public WFS_POLY(String type) {
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
			return Resource.get(GEO_API) + GEO_TYPE + GEO_CQL_START + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
		}
	}
	public abstract class WFS_ {
		public abstract String construct(double x, double y);
	}
}
