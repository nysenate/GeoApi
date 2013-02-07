package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.DistAssignInterface;
import gov.nysenate.sage.service.DistrictService.DistException;
import gov.nysenate.sage.service.DistrictService.TYPE;
import gov.nysenate.sage.util.Config;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GeoServer implements DistAssignInterface, Observer
{
  public class ParallelRequest implements Callable<Result>
  {
    public final DistAssignInterface adapter;
    public final Address address;
    public final List<DistrictService.TYPE> types;

    ParallelRequest(DistAssignInterface adapter, Address address, List<DistrictService.TYPE> types)
    {
      this.address = address;
      this.adapter = adapter;
      this.types = types;
    }

    @Override
    public Result call() throws DistException
    {
      return adapter.assignDistricts(address, types);
    }
  }

  private static final String DEFAULT_BASE_URL = "http://geoserver.nysenate.gov:8080/wfs";
  private final Logger logger;
  private String m_queryUrl;

  HashMap<Integer, Integer> COUNTY_CODES;


  public GeoServer() throws IOException
  {
    Config.notify(this);
    configure();
    logger = Logger.getLogger(this.getClass());

    COUNTY_CODES = new HashMap<Integer, Integer>();
    File county_code_file = FileUtils.toFile(this.getClass().getClassLoader().getResource("county_codes.tsv"));
    List<String> lines = FileUtils.readLines(county_code_file, "UTF-8");
    for (String line : lines) {
      String[] parts = line.split("\t");
      COUNTY_CODES.put(Integer.parseInt(parts[2]), Integer.parseInt(parts[0]));
    }
  } // GeoServer()


  public void update(Observable o, Object arg)
  {
    configure();
  } // update()


  public ArrayList<Result> getNearbyDistricts(Address address, DistrictService.TYPE distType, double distanceFeet)
  {
    double distance;
    String url;
    String filter;
    String filter_format = "CROSS(the_geom, LINESTRING(%f %f, %f %f)) OR CROSS(the_geom, LINESTRING(%f %f, %f %f))";

    // Approximate feet per degree at our altitude
    distance = distanceFeet / 364400;
    double x = address.latitude;
    double y = address.longitude;
    filter = String.format(filter_format, x, y-distance, x, y+distance, x-distance, y, x+distance, y);
    getFeatures(filter, distType);
    /* this method is not yet complete */
    return null;
  } // getNearbyDistricts()


  @Override
  public Result assignDistrict(Address address, DistrictService.TYPE type) throws DistException
  {
    return assignDistrict(new ArrayList<Address>(Arrays.asList(new Address[]{address})), type).get(0);
  } // assignDistrict()


  public ArrayList<Result> assignDistrict(ArrayList<Address> addresses, DistrictService.TYPE type) throws DistException
  {
    return assignDistricts(addresses, new ArrayList<TYPE>(Arrays.asList(type)));
  } // assignDistrict()


  public Result assignDistricts(Address address, List<TYPE> distTypes)
                throws DistException
  {
    if (address == null) {
      return null;
    }

    Result result = new Result();
    result.setAddress(address);
    Address addrCopy = address.clone();

    Content page = null;
    try {
      String filter = String.format("INTERSECTS(the_geom, POINT ( %f %f ))", address.latitude, address.longitude);
      JSONArray features = getFeatures(filter, distTypes);

      // Should only match one feature per layer as a point intersection
      if (features.length() == 0) {
        result.setStatus("1");
        result.addMessage("No matching features found for ["+address.toString()+"]");
        return result;
      }
      else if (features.length() > distTypes.size()) {
        result.setStatus("2");
        result.addMessage("Multiple matching features found for some layers. aborting.");
        return result;
      }

      for (int i = 0; i < features.length(); i++) {
        JSONObject feature = features.getJSONObject(i);
        JSONObject properties = feature.getJSONObject("properties");
        String layer = feature.getString("id").split("\\.")[0];
        if (layer.equals("school")) {
          addrCopy.school_name = properties.getString("NAME");
          addrCopy.school_code = properties.getString("TFCODE");
        }
        else if (layer.equals("town")) {
          addrCopy.town_name = properties.getString("NAME");
          addrCopy.town_code = properties.getString("ABBREV");
        }
        else if (layer.equals("election")) {
          addrCopy.election_code = properties.getInt("ED");
          addrCopy.election_name = "ED "+address.school_code;
        }
        else if (layer.equals("congressional")) {
          // Accommodate both old shape files and new 2012 shape files
          addrCopy.congressional_name = properties.has("NAMELSAD") ? properties.getString("NAMELSAD") : properties.getString("NAME");
          addrCopy.congressional_code = properties.has("CD111FP") ? properties.getInt("CD111FP") : properties.getInt("DISTRICT");
        }
        else if (layer.equals("county")) {
          addrCopy.county_name = properties.getString("NAMELSAD"); // or NAME
          addrCopy.county_code = COUNTY_CODES.get(properties.getInt("COUNTYFP"));
        }
        else if (layer.equals("assembly")) {
          // Accommodate both old shape files and new 2012 shape files
          addrCopy.assembly_name = properties.has("NAMELSAD") ? properties.getString("NAMELSAD") : properties.getString("NAME");
          addrCopy.assembly_code = properties.has("SLDLST") ? properties.getInt("SLDLST") : properties.getInt("DISTRICT");
        }
        else if (layer.equals("senate")) {
          // Accommodate both old shape files and new 2012 shape files
          addrCopy.senate_name = properties.has("NAMELSAD") ? properties.getString("NAMELSAD") : properties.getString("NAME");
          addrCopy.senate_code = properties.has("SLDUST") ? properties.getInt("SLDUST") : properties.getInt("DISTRICT");
        }
        else {
          result.setStatus("3");
          result.addMessage("Unidentified feature id "+feature.getString("id")+" found, aborting");
          return result;
        }
      }

      result.setAddress(addrCopy);
      return result;
    }
    catch (JSONException e) {
      String msg = "Malformed JSON response from GeoServer\n"+page.asString();
      logger.error(msg, e);
      throw new DistException(msg, e);
    }
  } // assignDistricts()


  @Override
  public ArrayList<Result> assignDistricts(ArrayList<Address> addresses, List<TYPE> types) throws DistException
  {
    ArrayList<Result> results = new ArrayList<Result>();
    ExecutorService executor = Executors.newFixedThreadPool(5);
    ArrayList<Future<Result>> futureResults = new ArrayList<Future<Result>>();

    for (Address address : addresses) {
      futureResults.add(executor.submit(new ParallelRequest(this, address, types)));
    }

    for (Future<Result> result : futureResults) {
      try {
        results.add(result.get());
      }
      catch (InterruptedException e) {
        throw new DistException(e);
      }
      catch (ExecutionException e) {
        throw new DistException(e.getCause());
      }
    }
    executor.shutdown();
    return results;
  } // assignDistricts()


  public ArrayList<Point> getDistrictBoundary(int distNum, TYPE distType)
  {
    ArrayList<Point> points = new ArrayList<Point>();
    String filter = "DISTRICT="+distNum;

    JSONArray features = getFeatures(filter, distType);
    if (features == null || features.length() != 1) {
      logger.error("Unable to get mapping data for district "+distNum);
      return null;
    }

    try {
      // The array of coordinates is 3 levels deep within the "coordinates"
      // attribute.
      JSONArray coordinates = features.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0).getJSONArray(0);
      for (int i = 0; i < coordinates.length(); i++) {
        double lat = coordinates.getJSONArray(i).getDouble(0);
        double lon = coordinates.getJSONArray(i).getDouble(1);
        Point p = new Point(lat, lon);
        points.add(p);
      }
      return points;
    }
    catch (JSONException jsonEx) {
      return null;
    }
  } // getDistrictBoundary()


  private void configure()
  {
    String baseUrl = Config.read("geoserver.url");

    if (baseUrl.isEmpty()) {
      baseUrl = DEFAULT_BASE_URL;
    }

    m_queryUrl = baseUrl+"?service=WFS&version=1.1.0&request=GetFeature";
  } // configure()


  private JSONArray getFeatures(String filter, List<TYPE> distTypes)
  {
    ArrayList<String> geotypes = new ArrayList<String>();
    for (TYPE distType : distTypes) {
      geotypes.add("nysenate:"+distType.toString().toLowerCase());
    }
    String geotypeAttr = "typename="+StringUtils.join(geotypes, ",");

    try {
      String sourceUrl = String.format(m_queryUrl+"&%s&CQL_FILTER=%s&outputformat=JSON", geotypeAttr, URLEncoder.encode(filter, "UTF-8"));
      logger.info(sourceUrl);

      Content page = Request.Get(sourceUrl).execute().returnContent();
      JSONObject response = new JSONObject(page.asString());
      JSONArray features = response.getJSONArray("features");
      return features;
    }
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
  } // getFeatures()


  private JSONArray getFeatures(String filter, DistrictService.TYPE distType)
  {
    return getFeatures(filter, new ArrayList<TYPE>(Arrays.asList(distType)));
  } // getFeatures()
}
