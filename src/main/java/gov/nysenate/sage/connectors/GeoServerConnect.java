package gov.nysenate.sage.connectors;

import generated.geoserver.json.GeoFeatures;
import generated.geoserver.json.GeoResult;
import gov.nysenate.sage.connectors.DistrictServices.DistrictType;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.districts.Senate;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.Connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

@Deprecated
public class GeoServerConnect {
  static String GEO_CQL_START = "&CQL_FILTER=";
  static String GEO_API = "geoserver.url";
  static String GEO_CQL_LOC = GEO_CQL_START + "INTERSECT(the_geom,%20POINT%20(";
  static String GEO_CQL_END = "))";
  static String GEO_OUTPUT = "&outputformat=JSON";
  static String GEO_CQL_LIKE = "%20LIKE%20";
  static String POLY_NAMES = "(assembly|congressional|county|election|senate)";
  static String ASSEMBLY = "assembly";
  static String CONGRESSIONAL = "congressional";
  static String COUNTY = "county";
  static String ELECTION = "election";
  static String SENATE = "senate";

  /**
   * this function allows any type of WFS_ connectors to connect to GeoServer and retrieve
   * applicable information, returns result from GeoServer
   */
  public GeoResult fromGeoserver(WFS_ wfs, Point p) throws IOException {
    String json = flatten(wfs.construct(p.getLatitude(), p.getLongitude()));

    return handleGeoserverJson(json);
  }

  public GeoResult fromGeoserver(WFS_ wfs, String value) throws IOException {
    String json = flatten(wfs.construct(value).replaceAll(" ", "%20"));

    return handleGeoserverJson(json);
  }

  public GeoResult handleGeoserverJson(String json) {
    Gson gson = new Gson();

    GeoResult gr = null;

    try {
      gr = gson.fromJson(json, GeoResult.class);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return gr;
  }

  /**
   * flattens the data from a url in to one string, used in conjuction with google-json
   * to decipher WFS responses
   */
  public String flatten(String url) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));

    StringBuilder sb = new StringBuilder("");

    String in = null;

    while((in = br.readLine()) != null) {
      sb.append(in);
    }

    br.close();

    return sb.toString();
  }

  /**
   * returns list of senate districts near a given point within a certain distance, relies
   * on getNearbyDistricts
   */
  public List<Senate> getNearbySenateDistricts(String district, Point p, DistrictType districtType, double distance)
                                  throws SQLException, Exception {
    List<Senate> ret = new ArrayList<Senate>();

    Connect c = new Connect();

    for (String d : getNearbyDistricts(district, p, districtType, distance)) {
      ret.add((Senate)c.listFromClosedResultSet(
          Senate.class,c.getResultsetById(
              Senate.class, "district", d)).iterator().next());
    }

    return ret;
  }

  /**
   * returns districts near a given point within a certain distance, only looks vertically
   * and horizontally using CQL CROSS filter, can be changed to include diagonally as well
   * but this is good for now.  There is a CQL filter DWITHIN but it doesn't appear to work
   * and BBOX has too broad of a scope
   */
  public HashSet<String> getNearbyDistricts(String key, Point p, DistrictType districtType, double distance)
                                  throws SQLException, Exception {
    HashSet<String> districts = new HashSet<String>();
    WFS_REQUEST req = new WFS_REQUEST(districtType);
    double lat = p.getLatitude();
    double lon = p.getLongitude();

    GeoResult gr2 = handleGeoserverJson(flatten(req.constructCross(lat, lon, true, distance)));
    districts.addAll(getDistrictsFromGeoResult(key, districtType, gr2));

    gr2 = handleGeoserverJson(flatten(req.constructCross(lat, lon, false, distance)));
    districts.addAll(getDistrictsFromGeoResult(key, districtType, gr2));

    return districts;
  }

  /**
   * returns list of disticts from a GeoResult, differentiates between election districts and
   * all other districts because election has a unique format
   */
  private HashSet<String> getDistrictsFromGeoResult(String key, DistrictType districtType, GeoResult gr) {
    HashSet<String> districts = new HashSet<String>();
    for(GeoFeatures gf:gr.getFeatures()) {
      if(districtType == DistrictType.ELECTION) {
        if(!gf.getProperties().getED().equals(key)) {
          districts.add(gf.getProperties().getED());
        }
      }
      else {
        if(!gf.getProperties().getNAMELSAD().equals(key)) {
          districts.add(gf.getProperties().getNAMELSAD());
        }
      }
    }
    return districts;
  }

  /**
   * The following are connectors for GeoServer, they provide construct[FILTER] methods
   * that create urls for various GeoServer WFS calls
   */

  public class WFS_REQUEST extends WFS_
  {
    String GEO_TYPE = "&typename=";
    String GEO_PROPERTY = "&propertyname=NAMELSAD,INTPTLAT,INTPTLON,ALAND,AWATER";
    String GEO_FILTER_TYPE="NAMELSAD";

    public WFS_REQUEST(DistrictType districtType) {
      setGeoType(districtType);
    }

    private void setGeoType(DistrictType districtType) {
      Pattern p = Pattern.compile(POLY_NAMES);
      Matcher m = p.matcher(districtType.type);
      if(m.find()) {
        GEO_TYPE += "nysenate:" + m.group(1);
      }

      if(districtType == DistrictType.ELECTION){
        GEO_PROPERTY = "&propertyname=ED,AREA,AREA1,EDS_COPY_,EDS_COPY_I,MCD2,WARD,EDP";
        GEO_FILTER_TYPE="ED";
      }
      else if(districtType == DistrictType.COUNTY) {
        GEO_PROPERTY += ",COUNTYFP";
      }
    }

    public String construct(double x, double y) {
      return Config.read(GEO_API) + GEO_TYPE + GEO_PROPERTY
          + GEO_CQL_LOC + x + "%20" + y + GEO_CQL_END + GEO_OUTPUT;
    }

    public String construct(String value) {
      return Config.read(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_START
          + GEO_FILTER_TYPE + GEO_CQL_LIKE + "'" + value + "'" + GEO_OUTPUT;
    }

    public String constructBoundingBox(double x, double y) {
      return Config.read(GEO_API) + GEO_TYPE + GEO_PROPERTY + "&bbox=" + x
          + "," + y + "," + x + "," + y + GEO_OUTPUT;
    }

    public String constructCross(double x, double y, boolean xOrY, double amt) {
      return Config.read(GEO_API) + GEO_TYPE + GEO_PROPERTY + GEO_CQL_START +
        "CROSS(the_geom,%20LINESTRING("
          + ((xOrY) ? x + amt:x) + "%20"
          + ((xOrY) ? y:y + amt) + ","
          + ((xOrY) ? x - amt:x) + "%20"
          + ((xOrY) ? y:y - amt) + "))" + GEO_OUTPUT;
    }
  }

  public class WFS_POLY extends WFS_ {
    String GEO_TYPE = "&typename=";
    //the only time the filter is not NAMESLAD is for election layer
    String GEO_FILTER_TYPE="NAMELSAD";

    public WFS_POLY(DistrictType districtType) {
      setGeoType(districtType);
    }

    private void setGeoType(DistrictType districtType) {
      Pattern p = Pattern.compile(POLY_NAMES);
      Matcher m = p.matcher(districtType.type);
      if(m.find()) {
        GEO_TYPE += "nysenate:" + m.group(1);
      }

      if(districtType == DistrictType.ELECTION){
        GEO_FILTER_TYPE="ED";
      }
    }

    public String construct(double x, double y) {
      return Config.read(GEO_API) + GEO_TYPE + GEO_CQL_LOC + x + "%20"
          + y + GEO_CQL_END + GEO_OUTPUT;
    }

    public String construct(String value) {
      return Config.read(GEO_API) + GEO_TYPE + GEO_CQL_START
          + GEO_FILTER_TYPE + GEO_CQL_LIKE + "'" + value + "'" + GEO_OUTPUT;
    }
  }


  public abstract class WFS_
  {
    public abstract String construct(double x, double y);
    public abstract String construct(String value);
  }
}
