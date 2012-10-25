package gov.nysenate.sage.connectors;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	static double CROSS_DISTANCE = 0.005;
	
	/*
	 * interface used for various requests to map response appropriate object
	 */
	private static interface GeoResultAdapterInterface<T> {
		public String getStringValue(String string);
		public String getStringValuePadded(String string);
		public T getObjectValue(GeoResult geoResult);
		public T getObjectValuePadded(GeoResult geoResult, Connect connect);
	}
	
	static abstract class GeoResultAdapter<T> implements GeoResultAdapterInterface<T> {
		private DistrictDataAssignment<T> districtDataAssignment;
		
		public GeoResultAdapter(DistrictDataAssignment<T> districtDataAssignment) {
			this.districtDataAssignment = districtDataAssignment;
		}
		
		public T getValue(GeoResult geoResult, Connect connect, boolean padded) {
			if(geoResult == null || geoResult.getFeatures().isEmpty()) return null;
			
			if(padded) {
				return (T) getObjectValuePadded(geoResult, connect);
			}
			return (T) getObjectValue(geoResult);
		}
		
		public DistrictResponse assign(DistrictResponse dr, GeoResult geoResult, Connect connect, boolean padded) {
			return districtDataAssignment.assign(getValue(geoResult, connect, padded), dr);
		}
	}
	
	/*
	 * current Senate, Congressional and Assembly requests all follow
	 * the same workflow to make a query to geoserver then pull
	 * an object from the database
	 */
 	static class NAMELSADFeatureValue<T> extends GeoResultAdapter<T> {
		private final Class<T> clazz;
		private final String columnName;
		private final String replaceValue;
		
		public NAMELSADFeatureValue(DistrictDataAssignment<T> districtDataAssignment, Class<T> clazz, String columnName, String replaceValue) {
			super(districtDataAssignment);
			this.clazz = clazz;
			this.columnName = columnName;
			this.replaceValue = replaceValue;
		}
		
		public String getStringValue(String string) {
			return string.replaceAll(replaceValue, "");
		}
		
		public String getStringValuePadded(String string) {
			return string;
		}
		
		public T getObjectValue(GeoResult geoResult) {
			T t = null;
			
			Constructor<T> constructor;
			try {
				constructor = clazz.getConstructor(String.class);
				
				t = constructor.newInstance(getStringValue( 
						geoResult.getFeatures().iterator().next().getProperties().getNAMELSAD()
				));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			
			return t;
		}
		
		@SuppressWarnings("unchecked")
		public T getObjectValuePadded(GeoResult geoResult, Connect connect) {
			try {
				return (T) connect.getObject(clazz, columnName, getStringValuePadded(
						geoResult.getFeatures().iterator().next().getProperties().getNAMELSAD()
					));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	static class CountyFeatureValue extends GeoResultAdapter<County> {
		public CountyFeatureValue() {
			super(new CountyDistrictData());
		}
		
		public String getStringValue(String string) {
			return string;
		}
		
		public String getStringValuePadded(String string) {
			return string;
		}
		
		public County getObjectValue(GeoResult geoResult) {
			return new County(getStringValue(
					geoResult.getFeatures().iterator().next().getProperties().getNAMELSAD()
				));
		}
		
		public County getObjectValuePadded(GeoResult geoResult, Connect connect) {
			return new County(getStringValuePadded(
					geoResult.getFeatures().iterator().next().getProperties().getNAMELSAD()
				));
		}
	}
	
	static class ElectionDistrictFeatureValue extends GeoResultAdapter<Election> {
		public ElectionDistrictFeatureValue() {
			super(new ElectionDistrictData());
		}
		
		public String getStringValue(String string) {
			return string;
		}
		
		public String getStringValuePadded(String string) {
			return "Election District " + string;
		}
		
		public Election getObjectValue(GeoResult geoResult) {
			return new Election(getStringValue(
					geoResult.getFeatures().iterator().next().getProperties().getED()
				));
		}
		
		public Election getObjectValuePadded(GeoResult geoResult, Connect connect) {
			return new Election(getStringValuePadded(
					geoResult.getFeatures().iterator().next().getProperties().getED()
				));
		}
	}
	
	/*
	 * interface used to actually assign the georesult mapped object to a districtresponse
	 */
	static interface DistrictDataAssignment<T> {
		public DistrictResponse assign(T value, DistrictResponse districtResponse);
	}
	
	static class SenateDistrictData implements DistrictDataAssignment<Senate> {
		public DistrictResponse assign(Senate senate, DistrictResponse districtResponse) {
			districtResponse.setSenate(senate);
			return districtResponse;
		}
	}
	
	static class AssemblyDistrictData  implements DistrictDataAssignment<Assembly> {
		public DistrictResponse assign(Assembly assembly, DistrictResponse districtResponse) {
			districtResponse.setAssembly(assembly);
			return districtResponse;
		}
	}
	
	static class CongressionalDistrictData  implements DistrictDataAssignment<Congressional> {
		public DistrictResponse assign(Congressional congressional, DistrictResponse districtResponse) {
			districtResponse.setCongressional(congressional);
			return districtResponse;
		}
	}
	
	static class ElectionDistrictData  implements DistrictDataAssignment<Election> {
		public DistrictResponse assign(Election election, DistrictResponse districtResponse) {
			districtResponse.setElection(election);
			return districtResponse;
		}
	}
	
	static class CountyDistrictData  implements DistrictDataAssignment<County> {
		public DistrictResponse assign(County county, DistrictResponse districtResponse) {
			districtResponse.setCounty(county);
			return districtResponse;
		}
	}
	
	/*
	 * DistrictType enum used to store layer specific data and processing
	 * in a single data structure..
	 * 
	 * ex.
	 * 
	 * //open database
	 * Connect connect = new Connect();
	 * // get or create point
	 * Point point = ...;
	 * //query geoserver with point
	 * GeoResult gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(DistrictType.SENATE), point);
	 * //get relevant senate data
	 * Senate senate = DistrictType.SENATE.geoResultAdapter.getObjectValuePadded(gr, connect);
	 * GeoResult gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(DistrictType.ASSEMBLY), point);
	 * Assembly assembly = DistrictType.ASSEMBLY.geoResultAdapter.getObjectValuePadded(gr, connect);
	 * 
	 */
	public static enum DistrictType {
		ASSEMBLY		("assembly", "Assembly%20District%20",
							new NAMELSADFeatureValue<Assembly>(
								new AssemblyDistrictData(), 
								Assembly.class, 
								"district", 
								"Assembly District "
							)
						),
							
		CONGRESSIONAL	("congressional", "Congressional%20District%20",
							new NAMELSADFeatureValue<Congressional>(
								new CongressionalDistrictData(), 
								Congressional.class, 
								"district", 
								"Congressional District "
							)
						),
							
		COUNTY			("county", "",
							new CountyFeatureValue()
						),
							
		ELECTION		("election", "",
							new ElectionDistrictFeatureValue()
						),
							
		SENATE			("senate", "State%20Senate%20District%20",
							new NAMELSADFeatureValue<Senate>(
								new SenateDistrictData(), 
								Senate.class, 
								"district", 
								"State Senate District "
							)
						);
		
		public final String type;
		public final String idQualifier;
		public final GeoResultAdapter<?> geoResultAdapter;
		
		private DistrictType(String type, String idQualifier, GeoResultAdapter<?> geoResultAdapter) {
			this.type = type;
			this.idQualifier = idQualifier;
			this.geoResultAdapter = geoResultAdapter;
		}
		
		public static DistrictType getDistrictType(String type) {
			if(type == null) return null;
			
			for(DistrictType districtType:DistrictType.values()) {
				if(districtType.type.equals(type)) {
					return districtType;
				}
			}
			return null;
		}
	}
	
	static GeoServerConnect gsCon = new GeoServerConnect();
	
	/**
	 * This is used to get a KML or JSON polygon from GeoServer if the district name is known
	 * 
	 * @param type match type from POLY_NAMES
	 * @param district associated with NAMSLAD or ED field on GeoServer
	 * @param format kml or json
	 * @throws IOException
	 */
	public static StringBuffer getPolyFromDistrict(DistrictType districtType, String district, String format) throws IOException {
		district = districtType.idQualifier + district;
		
		/* get flattened request from geoserver */
		WFS_POLY wfs = gsCon.new WFS_POLY(districtType);
		String json = gsCon.flatten(wfs.construct(district));
		
		return polyPrep(json, format, districtType);
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
	public static StringBuffer getPolyFromAddress(String address, String format, String service, DistrictType districtType) throws IOException {
		Point p = GeoCode.getGeoCodedResponse(address, service);
		
		/* get flattened request from geoserver */
		WFS_POLY wfs = gsCon.new WFS_POLY(districtType);
		String json = gsCon.flatten(wfs.construct(p.lat, p.lon));
		
		return polyPrep(json, format, districtType);
		
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
	public static StringBuffer getPolyFromPoint(String latlng, String format, String service, DistrictType districtType) throws IOException {
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
		WFS_POLY wfs = gsCon.new WFS_POLY(districtType);
		String json = gsCon.flatten(wfs.construct(p.lat, p.lon));
		
		return polyPrep(json, format, districtType);
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
	private static StringBuffer polyPrep(String in, String format, DistrictType districtType) throws IOException {
		
		
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

		Senate senate = (Senate) districtType.geoResultAdapter.getObjectValue(gr);
		
		return getPolygon(points, senate.getDistrict(), format);
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
	
	public static DistrictResponse getDistrictsForBlueBird(String latlng) throws Exception {
		String tuple[] = latlng.split(",");
		
		Point p = new Point(new Double(tuple[0]), new Double(tuple[1]));
		
		DistrictResponse dr = districts(p, false);
		
		return dr;
	}
	/*
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
	*/

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
	public static DistrictResponse districts(Point point, boolean padded) throws Exception {
		Connect connect = new Connect();
		DistrictResponse dr = new DistrictResponse();
		GeoResult gr = null;
		
		for(DistrictType districtType: DistrictType.values()) {
			gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(districtType), point);
			dr = districtType.geoResultAdapter.assign(dr, gr, connect, padded);
		}
		
		List<Senate> nearbySenateDistricts = gsCon.getNearbySenateDistricts(dr.getSenate().getDistrict(),
				point, DistrictType.SENATE, CROSS_DISTANCE);
		
		if(nearbySenateDistricts != null && !nearbySenateDistricts.isEmpty()) {
			if(!padded) {
				for(int i = 0; i < nearbySenateDistricts.size(); i++) {
					Senate nearby = nearbySenateDistricts.get(i);
					nearby.setDistrict(DistrictType.SENATE.geoResultAdapter.getStringValue(nearby.getDistrict()));
					nearby.setDistrictUrl(null);
				}
			}
			
			dr.getSenate().setNearbyDistricts(nearbySenateDistricts);
		}
		
		connect.close();		
		
		dr.setLat(point.lat);
		dr.setLon(point.lon);

		dr.setAddress((String)point.address);

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
	
	
	
	
	/*
	 * currently districts are assigned serially by sending one api request to geoserver
	 * after another. below is a prototype interface with a serial implementaion
	 * as well as a parallel implementation which reduces query time by roughly 33%
	 */
	
	interface DistrictAssign {
		public DistrictResponse assignDistrictsToPoint(Point point, boolean padded) throws Exception;
	}
	
	static class SerialDistrictAssign implements DistrictAssign {
		public DistrictResponse assignDistrictsToPoint(Point point, boolean padded) throws Exception {
			Connect connect = new Connect();
			DistrictResponse dr = new DistrictResponse();
			GeoResult gr = null;
			
			for(DistrictType districtType: DistrictType.values()) {
				gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(districtType), point);
				dr = districtType.geoResultAdapter.assign(dr, gr, connect, padded);
			}
			
			List<Senate> nearbySenateDistricts = gsCon.getNearbySenateDistricts(dr.getSenate().getDistrict(),
					point, DistrictType.SENATE, CROSS_DISTANCE);
			
			if(nearbySenateDistricts != null && !nearbySenateDistricts.isEmpty()) {
				if(!padded) {
					for(int i = 0; i < nearbySenateDistricts.size(); i++) {
						Senate nearby = nearbySenateDistricts.get(i);
						nearby.setDistrict(DistrictType.SENATE.geoResultAdapter.getStringValue(nearby.getDistrict()));
						nearby.setDistrictUrl(null);
					}
				}
				
				dr.getSenate().setNearbyDistricts(nearbySenateDistricts);
			}
			
			connect.close();
			
			return dr;
		}
	}
	
	static class ParallelDistrictAssign implements DistrictAssign {
		
		private class ParallelAssign implements Runnable {
			DistrictType type;
			Point point;
			DistrictResponse dr;
			Connect connect;
			boolean padded;
			
			public ParallelAssign(DistrictType type, Point point, DistrictResponse dr, Connect connect, boolean padded) {
				this.type = type;
				this.point = point;
				this.dr = dr;
				this.connect = connect;
				this.padded = padded;
			}
			
			public void run() {
				GeoResult gr = null;
				try {
					gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(type), point);
				} catch (IOException e) {
					e.printStackTrace();
				}
				dr = type.geoResultAdapter.assign(dr, gr, connect, padded);
			}
		}
		
		public DistrictResponse assignDistrictsToPoint(Point point, boolean padded) throws Exception {
			Connect connect = new Connect();
			
			ExecutorService executor = Executors.newFixedThreadPool(5);
			
			DistrictResponse dr = new DistrictResponse();
			
			for(DistrictType districtType: DistrictType.values()) {
				executor.submit(new ParallelAssign(districtType, point, dr, connect, padded));
			}
			
			executor.shutdown();
			while(!executor.isTerminated());
			
			List<Senate> nearbySenateDistricts = gsCon.getNearbySenateDistricts(dr.getSenate().getDistrict(),
					point, DistrictType.SENATE, CROSS_DISTANCE);
			
			if(nearbySenateDistricts != null && !nearbySenateDistricts.isEmpty()) {
				if(!padded) {
					for(int i = 0; i < nearbySenateDistricts.size(); i++) {
						Senate nearby = nearbySenateDistricts.get(i);
						nearby.setDistrict(DistrictType.SENATE.geoResultAdapter.getStringValue(nearby.getDistrict()));
						nearby.setDistrictUrl(null);
					}
				}
				
				dr.getSenate().setNearbyDistricts(nearbySenateDistricts);
			}
			
			connect.close();
			
			return dr;
		}
		
	}
}
