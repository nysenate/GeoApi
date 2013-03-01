package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;


public class OSM implements GeocodeInterface, Observer
{
  private static final String DEFAULT_BASE_URL = "http://open.mapquestapi.com/nominatim/v1/search";
  private final Logger logger;
  private String m_baseUrl;


  public class ParallelRequest implements Callable<Result>
  {
    public final OSM osm;
    public final Address address;

    ParallelRequest(OSM osm, Address address) {
      this.osm = osm;
      this.address = address;
    }

    @Override
    public Result call() throws GeoException {
      return osm.geocode(address);
    }
  }


  public OSM() throws Exception
  {
    Config.notify(this);
    configure();
    logger = Logger.getLogger(this.getClass());
    logger.info("Initialized OSM Adapter");
  } // OSM()


  public void update(Observable o, Object arg)
  {
    configure();
  } // update()


  @Override
  public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException
  {
    ArrayList<Result> results = new ArrayList<Result>();
    ExecutorService executor = Executors.newFixedThreadPool(5);
    ArrayList<Future<Result>> futureResults = new ArrayList<Future<Result>>();

    for (Address address : addresses) {
      futureResults.add(executor.submit(new ParallelRequest(this, address)));
    }

    for (Future<Result> result : futureResults) {
      try {
        results.add(result.get());
      }
      catch (InterruptedException e) {
        throw new GeoException(e);
      }
      catch (ExecutionException e) {
        throw new GeoException(e.getCause());
      }
    }
    executor.shutdown();
    return results;
  }


  @Override
  public Result geocode(Address address) throws GeoException
  {
    if (address == null) {
      return null;
    }

    Content page = null;
    Document response = null;
    Result result = new Result();

    try {
      // Parse the API response
      String urlText = m_baseUrl+"?format=json&q="
          + URLEncoder.encode(address.as_raw(), "UTF-8")
          + "&addressdetails=1&limit=3&viewbox=-1.99%2C52.02%2C0.78%2C50.94";
      result.setSource(urlText);
      logger.info(urlText);
      page = Request.Get(urlText).execute().returnContent();

      if (page.asString().equals("[]")) {
        result.addMessage("ResultSet/ErrorMessage");
        return result;
      }

      // Creates a Json array from the page returns from the API request
      logger.info(page.asString());
      JSONArray json = new JSONArray(page.asString());
      // Creates a JSON Object from the first object in the Json array.
      JSONObject jsonResult = json.getJSONObject(0);
      JSONObject jsonAddress = jsonResult.getJSONObject("address");

      // if an object has a latitude it will have a longitude
      // the opt statements take care of unique cases where some of the following information is missing
      if (jsonResult.has("lat")) {
        String street1 = null;
        String OSMclass = jsonResult.optString("class");
        String type = jsonResult.optString("type");
        double lat = jsonResult.optDouble("lat");
        double lon = jsonResult.optDouble("lon");

        /**
        * Other types are possible here and can be implemented.
        * SEE http://wiki.openstreetmap.org/wiki/Map_Features for complete list.
        */
        if (jsonResult.getString("type").equals("house")) {
          street1 = jsonAddress.optString("house_number");
        }
        else {
          street1 = jsonAddress.optString("building");
        }
        String street = street1 + " " + jsonAddress.optString("road");
        String city = jsonAddress.optString("city");
        String state = jsonAddress.optString("state");
        String zip_code = jsonAddress.optString("postcode");
        Address resultAddress = new Address(street, city, state, zip_code);
        resultAddress.setGeocode(lat, lon, 80);
        result.addAddress(resultAddress);
      }
      return result;
    }
    catch (UnsupportedEncodingException e) {
      String msg = "UTF-8 encoding not supported!?";
      logger.error(msg);
      throw new GeoException(msg);
    }
    catch (MalformedURLException e) {
      String msg = "Malformed URL '"+result.getSource()+"', check api key and address values.";
      logger.error(msg, e);
      throw new GeoException(msg, e);
    }
    catch (IOException e) {
      String msg = "Error opening API resource '"+result.getSource()+"'";
      logger.error(msg, e);
      result.setStatus("500");
      result.addMessage(e.getMessage());
      return result;
    }
    catch (JSONException e) {
      String msg = "Malformed JSON Response received:\n";
      logger.error(msg, e);
      throw new GeoException(msg);
    }
  }


  private void configure()
  {
    m_baseUrl = Config.read("osm.url");

    if (m_baseUrl.isEmpty()) {
      m_baseUrl = DEFAULT_BASE_URL;
    }
  } // configure()
}
