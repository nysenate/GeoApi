package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.StringBuilder;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RubyGeocoder implements GeocodeInterface, Observer
{
    private static final String DEFAULT_BASE_URL = "http://geocoder.nysenate.gov/GeoRubyAdapter/api";
    private final Logger logger;
    private final int BATCH_SIZE = 24;
    private String m_baseUrl;
    private String m_baseBulkUrl;


    public RubyGeocoder() throws Exception
    {
        Config.notify(this);
        configure();
        logger = Logger.getLogger(this.getClass());
        logger.info("Initialized RubyGeocoder Adapter");
    }


    public void update(Observable o, Object arg)
    {
        configure();
    } // update()


    @Override
    public Result geocode(Address address)
    {
        if (address == null) {
            return null;
        }

        Content page = null;
        String urlText;

        if (address.is_parsed()) {
            urlText = m_baseUrl+"?street="+address.addr2+"&city="+address.city+"&state="+address.state+"&zip="+address.zip5;
        }
        else {
            urlText = m_baseUrl+"address="+address.raw;
        }
        urlText = urlText.replaceAll(" ", "%20");

        try {
            Result result = new Result(urlText);
            logger.info(urlText);
            page = Request.Get(urlText).execute().returnContent();

            JSONArray array = new JSONArray(page.asString());
            if (array.length() == 0) {
                result.setStatus("404");
                result.addMessage("Lookup failure for "+address.toString());
                return result;
            }

            JSONObject jsonResult = array.getJSONObject(0);
            if (jsonResult.has("lat")) {
                // For lower granularity lookups these fields might not be available.
                String street = "";
                if (jsonResult.has("prenum"))
                    street += jsonResult.getString("prenum")+" ";
                if (jsonResult.has("number"))
                    street += jsonResult.getString("number")+" ";
                if (jsonResult.has("street"))
                    street += jsonResult.getString("street");
                street = street.trim();
                String city = jsonResult.has("city") ? jsonResult.getString("city") : "";
                String state = jsonResult.has("state") ? jsonResult.getString("state") : "";
                String zip = jsonResult.has("zip") ? jsonResult.getString("zip") : "";
                double lat = jsonResult.has("lat") ? jsonResult.getDouble("lat") : 0;
                double lon = jsonResult.has("lon") ? jsonResult.getDouble("lon") : 0;
                int quality = jsonResult.has("score") ? (int)(jsonResult.getDouble("score")*100) : 0;
                if (quality == 100) {
                    quality = 99; // No geocode is perfect
                }
                Address resultAddress = new Address(street, city, state, zip);
                resultAddress.setGeocode(lat, lon, quality);
                result.addAddress(resultAddress);
                result.setStatus("0");
            }
            else {
                result.setStatus("1");
                result.addMessage("Empty object returned to indicate geocode failure.");
            }
            return result;
        }
        catch (MalformedURLException e) {
            logger.error("Malformed URL '"+urlText+"', check API key and address values.", e);
            return null;
        }
        catch (IOException e) {
            logger.error("Error opening API resource '"+urlText+"'", e);
            return null;
        }
        catch (JSONException e) {
            logger.error("Malformed JSON Response received:\n"+page.asString(), e);
            return null;
        }
    }


    @Override
    public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException
    {

        // RubyGeocoder has a special bulk method if all addresses are parsed
        if (hint == Address.TYPE.PARSED) {
            return geocodeParsedBulk(addresses);
        }

        // Otherwise we need to do it one until the GeoRubyAdapter is made better
        ArrayList<Result> results = new ArrayList<Result>();
        for (Address address : addresses) {
            results.add(geocode(address));
        }

        return results;
    }


    private ArrayList<Result> geocodeParsedBulk(ArrayList<Address> addresses) throws GeoException
    {
        Content page = null;
        String urlText = "";
        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<Result> batchResults = new ArrayList<Result>();

        if (addresses.size() == 0) {
            return results;
        }

        try {
            StringBuilder json = new StringBuilder();
            // Start with a=1 to make the batch boundary condition work nicely
            for (int a = 1; a <= addresses.size(); a++) {
                Address address = addresses.get(a-1);
                if (address == null) {
                    batchResults.add(null);
                }
                else {
                    batchResults.add(new Result());
                    json.append(",");
                    json.append(addressToJson(address)); 
                }

                // Stop here unless we've filled this batch request
                if (a%BATCH_SIZE != 0 && a != addresses.size()) {
                    continue;
                }

                urlText = m_baseBulkUrl+"?json=["+URLEncoder.encode(json.substring(1), "utf-8")+"]";
                logger.info(urlText);
                page = Request.Get(urlText).execute().returnContent();

                // Log the source URL for all results
                for (Result result : batchResults) {
                    //result.setStatus("0"); // RubyGeo doesn't report errors?
                    result.setSource(urlText);
                }

                // Each address specified produces its own result node.
                // Because null addresses aren't sent to MapQuest we need
                // to track an offset to the corresponding result.
                int resultOffset = 0;
                JSONArray jsonResults = new JSONArray("["+page.asString()+"]");
                for (int i = 0; i < jsonResults.length(); i++) {
                    JSONObject jsonResult = jsonResults.getJSONObject(i);
                    Result result;
                    while ((result = batchResults.get(i+resultOffset)) == null) {
                        resultOffset++;
                    }

                    // If we don't have a lat then it was a geocode failure
                    if (jsonResult.has("lat")) {
                        String street = (jsonResult.has("prenum") ? jsonResult.getString("prenum")+" " : "")
                                       +(jsonResult.has("number") ? jsonResult.getString("number")+" " : "")
                                       +(jsonResult.has("street") ? jsonResult.getString("street")     :  "");
                        String city = jsonResult.has("city") ? jsonResult.getString("city") : "";
                        String state = jsonResult.has("state") ? jsonResult.getString("state") : "";
                        String zip = jsonResult.has("zip") ? jsonResult.getString("zip") : "";
                        double lat = jsonResult.getDouble("lat");
                        double lon = jsonResult.getDouble("lon");
                        int quality = (int)(jsonResult.getDouble("score")*100);

                        Address resultAddress = new Address(street, city, state, zip);
                        resultAddress.setGeocode(lat, lon, quality);
                        result.addAddress(resultAddress);

                    }
                    else {
                        result.setStatus("1");
                        result.addMessage("Empty object returned to indicate geocode failure.");
                    }
                }

                json = new StringBuilder();
                results.addAll(batchResults);
                batchResults.clear();
            }

            return results;
        }
        catch (UnsupportedEncodingException e) {
            String msg = "UTF-8 encoding not supported!?";
            logger.error(msg);
            throw new GeoException(msg);
        }
        catch (MalformedURLException e) {
            String msg = "Malformed URL '"+urlText+"', check API key and address values.";
            logger.error(msg, e);
            throw new GeoException(msg);

        }
        catch (IOException e) {
            String msg = "Error opening API resource '"+urlText+"'";
            logger.error(msg, e);
            throw new GeoException(msg);

        }
        catch (JSONException e) {
            String msg = "Malformed JSON Response received:\n"+page.asString();
            logger.error(msg, e);
            throw new GeoException(msg);
        }
    }


    private void configure()
    {
        m_baseUrl = Config.read("geocoder.url");
        m_baseBulkUrl = Config.read("geocoder.bulk.url");

        if (m_baseUrl.isEmpty()) {
            m_baseUrl = DEFAULT_BASE_URL+"/geocode";
        }
        if (m_baseBulkUrl.isEmpty()) {
            m_baseBulkUrl = DEFAULT_BASE_URL+"/bulk";
        }
    } // configure()


    private String addressToJson(Address address)
    {
        return String.format("{\"street\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"zip5\":\"%s\"}", address.addr2, address.city, address.state, address.zip5);
    } // addressToJson()
}
