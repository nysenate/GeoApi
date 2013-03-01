package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Yahoo implements GeocodeInterface, Observer
{
  private static final String DEFAULT_BASE_URL = "http://query.yahooapis.com/v1/public/yql";
  private final Logger m_logger;
  private String m_baseUrl;
  private String m_consumerKey;
  private String m_consumerSecret;


  public class ParallelRequest implements Callable<Result>
  {
    private final Yahoo m_yahoo;
    private final Address m_address;

    ParallelRequest(Yahoo yahoo, Address address)
    {
      m_yahoo = yahoo;
      m_address = address;
    }

    @Override
    public Result call() throws GeoException
    {
      return m_yahoo.geocode(m_address);
    }
  } // ParallelRequest


  public Yahoo() throws Exception
  {
    Config.notify(this);
    configure();
    m_logger = Logger.getLogger(this.getClass());
    m_logger.info("Initialized Yahoo Adapter");
  } // Yahoo()


  public void update(Observable o, Object arg)
  {
    configure();
  } // update()


  // Yahoo doesn't implement batch geocoding so we use the single address
  // geocoding method in parallel for performance improvements on our end.
  public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException
  {
    ArrayList<Result> results = new ArrayList<Result>();
    ExecutorService executor = Executors.newFixedThreadPool(5);
    ArrayList<Future<Result>> futureResults = new ArrayList<Future<Result>>();

    for (Address address : addresses) {
      futureResults.add(executor.submit(new ParallelRequest(this, address)));
    }

    try {
      for (Future<Result> result : futureResults) {
        results.add(result.get());
      }
    }
    catch (InterruptedException e) {
      throw new GeoException(e);
    }
    catch (ExecutionException e) {
      throw new GeoException(e.getCause());
    }
    finally {
      executor.shutdownNow();
    }
    return results;
  } // geocode()


  @Override
  public Result geocode(Address address) throws GeoException
  {
    Result result = null;

    if (address == null) {
      return result;
    }

    try {
      // Parse the API response
      String yql = "select * from geo.placefinder where text=\""+address.as_raw()+"\"";
      String encodedYql = URLEncoder.encode(yql, "UTF-8");
      String urlText = m_baseUrl+"?format=json&q="+encodedYql;
      result = new Result(urlText);
      m_logger.info(urlText);

      URL u = new URL(urlText);
      HttpURLConnection uc = (HttpURLConnection)u.openConnection();
      int httpRespCode = uc.getResponseCode();

      if (httpRespCode != HttpURLConnection.HTTP_OK) {
        result.setStatus(String.valueOf(httpRespCode));
        result.addMessage(IOUtils.toString(uc.getErrorStream()));
        m_logger.error("Yahoo geocoding service rejected this request");
        return result;
      }

      String body = IOUtils.toString(uc.getInputStream());
      JSONObject jsonObj = new JSONObject(body).getJSONObject("query");
      int resultCount = jsonObj.getInt("count");
      jsonObj = jsonObj.getJSONObject("results");

      // If count==1, then Result is a JSONObject.
      // If count>1, then Result is a JSONArray.
      if (resultCount == 1) {
        Address resultAddr = buildAddress(jsonObj.getJSONObject("Result"));
        result.addAddress(resultAddr);
      }
      else {
        JSONArray resultSet = jsonObj.getJSONArray("Result");
        for (int i = 0; i < resultSet.length(); i++) {
          Address resultAddr = buildAddress(resultSet.getJSONObject(i));
          result.addAddress(resultAddr);
        }
      }

      return result;
    }
    catch (UnsupportedEncodingException e) {
      String msg = "UTF-8 encoding not supported!?";
      m_logger.error(msg, e);
      throw new GeoException(msg);
    }
    catch (MalformedURLException e) {
      String msg = "Malformed URL '"+result.getSource()+"', check API key and address values.";
      m_logger.error(msg, e);
      throw new GeoException(msg, e);
    }
    catch (IOException e) {
      String msg = "Error opening API resource '"+result.getSource()+"'";
      m_logger.error(msg, e);
      result.setStatus("500");
      result.addMessage(e.getMessage());
      return result;
    }
    catch (JSONException jsonEx) {
      String msg = "Error while processing JSON result";
      m_logger.error(msg, jsonEx);
      throw new GeoException(msg, jsonEx);
    }
  } // geocode()


  private void configure()
  {
    m_baseUrl = Config.read("yahoo.url");
    m_consumerKey = Config.read("yahoo.consumer_key");
    m_consumerSecret = Config.read("yahoo.consumer_secret");

    if (m_baseUrl.isEmpty()) {
      m_baseUrl = DEFAULT_BASE_URL;
    }
  }


  private Address buildAddress(JSONObject jsonRes) throws JSONException
  {
    String street = jsonRes.optString("line1", null);
    String city = jsonRes.optString("city", null);
    String state = jsonRes.optString("statecode", null);
    String zip_code = jsonRes.optString("postal", null);
    int quality = jsonRes.optInt("quality", 0);
    double lat = jsonRes.optDouble("latitude", 0.0);
    double lng = jsonRes.optDouble("longitude", 0.0);

    Address resultAddress = new Address(street, city, state, zip_code);
    resultAddress.setGeocode(lat, lng, quality);
    return resultAddress;
  } // buildAddress()
}
