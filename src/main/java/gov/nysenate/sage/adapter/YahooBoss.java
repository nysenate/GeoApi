package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeService;
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

/**
 * Yahoo Boss - free geocoding service
 */
public class YahooBoss implements GeocodeService, Observer
{
    private static final String DEFAULT_BASE_URL = "http://yboss.yahooapis.com/geo/placefinder";
    private final Logger logger = Logger.getLogger(YahooBoss.class);
    private Config config;
    private String baseUrl;
    private String consumerKey;
    private String consumerSecret;

    @Override
    public GeocodeResult geocode(gov.nysenate.sage.model.address.Address address)
    {
        return null;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<gov.nysenate.sage.model.address.Address> addresses)
    {
        return null;
    }

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        return null;
    }

    public class ParallelRequest implements Callable<Result>
    {
        private final YahooBoss yahooBoss;
        private final Address address;

        ParallelRequest(YahooBoss yahooBoss, Address address)
        {
            this.yahooBoss = yahooBoss;
            this.address = address;
        }

        @Override
        public Result call()
        {
            return yahooBoss.geocode(address);
        }
    }

    public YahooBoss() throws Exception
    {
        config = ApplicationFactory.getConfig();
        configure();
        logger.info("Initialized Yahoo Adapter");
    }

    public void update(Observable o, Object arg)
    {
        configure();
    }

    /**
     *  Yahoo doesn't implement batch geocoding so we use the single address geocoding
     *  method in parallel for performance improvements on our end.
     */
    public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint)
    {
        ArrayList<Result> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        ArrayList<Future<Result>> futureResults = new ArrayList<>();

        for (Address address : addresses) {
            futureResults.add(executor.submit(new ParallelRequest(this, address)));
        }

        for (Future<Result> result : futureResults) {
            try {
                results.add(result.get());
            }
            catch (InterruptedException e) {
                //throw new GeoException(e);
            }
            catch (ExecutionException e) {
                //throw new GeoException(e.getCause());
            }
        }
        executor.shutdown();
        return results;
    }


    public Result geocode(Address address)
    {
        Result result = null;

        if (address == null) {
            return result;
        }

        try {
            /** Parse the API response */
            String urlText = baseUrl +"?flags=J&location="+URLEncoder.encode(address.as_raw(), "UTF-8").replace("+", "%20");
            result = new Result(urlText);
            logger.info(urlText);

            URL u = new URL(urlText);
            HttpURLConnection uc = (HttpURLConnection)u.openConnection();
            OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
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
            logger.error(msg);
        }
        catch (MalformedURLException e) {
            String msg = "Malformed URL '"+result.getSource()+"', check API key and address values.";
            logger.error(msg, e);
        }
        catch (IOException e) {
            String msg = "Error opening API resource '"+result.getSource()+"'";
            logger.error(msg, e);
            result.setStatus("500");
            result.addMessage(e.getMessage());
            return result;
        }
        catch (OAuthMessageSignerException e) {
            String msg = "OAuthMessageSignerException";
            logger.error(msg, e);
        }
        catch (OAuthExpectationFailedException e) {
            String msg = "OAuthExpectationFailedException";
            logger.error(msg, e);
        }
        catch (OAuthCommunicationException e) {
            String msg = "OAuthCommunicationException";
            logger.error(msg, e);
        }
        catch (JSONException jsonEx) {
            String msg = "Error while processing JSON result";
            logger.error(msg, jsonEx);
        }
        return null;
    } // geocode()


    private void configure()
    {
        config.notifyOnChange(this);
        baseUrl = config.getValue("yahoo.boss.url");
        consumerKey = config.getValue("yahoo.boss.consumer_key");
        consumerSecret = config.getValue("yahoo.boss.consumer_secret");

        if (baseUrl.isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
    }


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
    }
}
