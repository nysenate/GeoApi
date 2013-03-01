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

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class YahooBoss implements GeocodeInterface, Observer
{
  private static final String DEFAULT_BASE_URL = "http://yboss.yahooapis.com/geo/placefinder";
  private final Logger m_logger;
  private String m_baseUrl;
  private String m_consumerKey;
  private String m_consumerSecret;


  public class ParallelRequest implements Callable<Result>
  {
    private final YahooBoss m_yahooBoss;
    private final Address m_address;

    ParallelRequest(YahooBoss yahooBoss, Address address)
    {
      m_yahooBoss = yahooBoss;
      m_address = address;
    }

    @Override
    public Result call() throws GeoException
    {
      return m_yahooBoss.geocode(m_address);
    }
  } // ParallelRequest


  public YahooBoss() throws Exception
  {
    Config.notify(this);
    configure();
    m_logger = Logger.getLogger(this.getClass());
    m_logger.info("Initialized Yahoo Adapter");
  }

  public void update(Observable o, Object arg)
  {
    configure();
  } // update()


  // Yahoo doesn't implement batch geocoding so we use the single address geocoding
  // method in parallel for performance improvements on our end.
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
      String urlText = m_baseUrl+"?flags=J&location="+URLEncoder.encode(address.as_raw(), "UTF-8").replace("+", "%20");
      result = new Result(urlText);
      m_logger.info(urlText);

      URL u = new URL(urlText);
      HttpURLConnection uc = (HttpURLConnection)u.openConnection();
      OAuthConsumer consumer = new DefaultOAuthConsumer(m_consumerKey, m_consumerSecret);
      consumer.sign(uc);
      int httpRespCode = uc.getResponseCode();

      if (httpRespCode != HttpURLConnection.HTTP_OK) {
        result.setStatus(String.valueOf(httpRespCode));
        result.addMessage(IOUtils.toString(uc.getErrorStream()));
        return result;
      }

      String body = IOUtils.toString(uc.getInputStream());
      JSONObject jsonObj = new JSONObject(body).getJSONObject("bossresponse");
      String status = jsonObj.getString("responsecode");
      if (!status.equals("200")) {
        result.setStatus(status);
        result.addMessage(jsonObj.getString("reason"));
        return result;
      }

      JSONArray resultSet = jsonObj.getJSONObject("placefinder").getJSONArray("results");
      for (int i = 0; i < resultSet.length(); i++) {
        Address resultAddr = buildAddress(resultSet.getJSONObject(i));
        result.addAddress(resultAddr);
      }

      return result;
    }
    catch (UnsupportedEncodingException e) {
      String msg = "UTF-8 encoding not supported!?";
      m_logger.error(msg);
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
    catch (OAuthMessageSignerException e) {
      String msg = "OAuthMessageSignerException";
      m_logger.error(msg, e);
      throw new GeoException(msg ,e);
    }
    catch (OAuthExpectationFailedException e) {
      String msg = "OAuthExpectationFailedException";
      m_logger.error(msg, e);
      throw new GeoException(msg ,e);
    }
    catch (OAuthCommunicationException e) {
      String msg = "OAuthCommunicationException";
      m_logger.error(msg, e);
      throw new GeoException(msg ,e);
    }
    catch (JSONException jsonEx) {
      String msg = "Error while processing JSON result";
      m_logger.error(msg, jsonEx);
      throw new GeoException(msg, jsonEx);
    }
  } // geocode()


  private void configure() {
    m_baseUrl = Config.read("yahoo.boss.url");
    m_consumerKey = Config.read("yahoo.boss.consumer_key");
    m_consumerSecret = Config.read("yahoo.boss.consumer_secret");

    if (m_baseUrl.isEmpty()) {
      m_baseUrl = DEFAULT_BASE_URL;
    }
  } //configure()


  private Address buildAddress(JSONObject jsonRes) throws JSONException
  {
    String street = jsonRes.optString("line1", null);
    String city = jsonRes.optString("city", null);
    String state = jsonRes.optString("statecode", null);
    String zip_code = jsonRes.optString("postal", null);
    int quality = jsonRes.optInt("quality", 0);
    double lat = jsonRes.optDouble("offsetlat", 0.0);
    double lng = jsonRes.optDouble("offsetlon", 0.0);

    Address resultAddress = new Address(street, city, state, zip_code);
    resultAddress.setGeocode(lat, lng, quality);
    return resultAddress;
  } // buildAddress()
}
