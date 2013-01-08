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

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RubyGeocoder implements GeocodeInterface {
    private final Logger logger;
    private final int BATCH_SIZE = 24;
    private final String GEO_BASE;
    private final String GEO_BASE_BULK;

    public RubyGeocoder() throws Exception {
        logger = Logger.getLogger(this.getClass());
        logger.info("Initialized RubyGeocoder Adapter");
        GEO_BASE = Config.read("geocoder.url");
        GEO_BASE_BULK = Config.read("geocoder_bulk.url");
    }

    @Override
    public Result geocode(Address address) {
        if (address == null)
            return null;

        Content page = null;
        Result result = new Result();

        if (address.is_parsed()) {
            result.source = GEO_BASE+"street="+address.addr2+"&city="+address.city+"&state="+address.state+"&zip="+address.zip5;
        } else {
            result.source = GEO_BASE+"address="+address.raw;
        }
        result.source = result.source.replaceAll(" ", "%20");

        try {
            logger.info(result.source);
            page = Request.Get(result.source).execute().returnContent();

            JSONArray array = new JSONArray(page.asString());
            if (array.length()==0) {
                result.status_code = "404";
                result.messages.add("Lookup failure for "+address.toString());
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
                result.addresses.add(resultAddress);
                result.status_code = "0";
            } else {
                result.status_code = "1";
                result.messages.add("Empty object returned to indicate geocode failure.");
            }
            return result;

        } catch (MalformedURLException e) {
            logger.error("Malformed URL '"+result.source+"', check api key and address values.", e);
            return null;

        } catch (IOException e) {
            logger.error("Error opening API resource '"+result.source+"'", e);
            return null;

        } catch (JSONException e) {
            logger.error("Malformed JSON Response received:\n"+page.asString(), e);
            return null;
        }
    }

    @Override
    public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException {

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

    private ArrayList<Result> geocodeParsedBulk(ArrayList<Address> addresses) throws GeoException {
        Content page = null;
        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<Result> batchResults = new ArrayList<Result>();

        if (addresses.size()==0)
            return results;

        String url = "";
        String json = "";

        try {
            json = "";
            // Start with a=1 to make the batch boundary condition work nicely
            for (int a=1; a <= addresses.size(); a++) {
                Address address = addresses.get(a-1);
                if (address == null) {
                    batchResults.add(null);
                } else {
                    batchResults.add(new Result());
                    json += "{\"street\":\""+address.addr2+"\", \"city\": \""+address.city+"\", \"state\": \""+address.state+"\", \"zip5\": \""+address.zip5+"\"},";
                }

                // Stop here unless we've filled this batch request
                if (a%BATCH_SIZE != 0 && a != addresses.size()) continue;

                url = GEO_BASE_BULK + "json=[" + URLEncoder.encode(json.substring(0, json.length()-1), "utf-8")+"]";
                logger.info(url);
                page = Request.Get(url).execute().returnContent();

                // Log the source URL for all results
                for (Result result : batchResults) {
                    //result.status_code = "0"; // Rubygeocoder doesn't report errors?
                    result.source = url;
                }

                // Each address specified produces its own result node
                // Because null addresses aren't sent to mapquest we need
                // to track an offset to the corresponding result.
                int resultOffset = 0;
                JSONArray jsonResults = new JSONArray("["+page.asString()+"]");
                for (int i=0; i<jsonResults.length(); i++) {
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
                        result.addresses.add(resultAddress);

                    } else {
                        result.status_code = "1";
                        result.messages.add("Empty object returned to indicate geocode failure.");
                    }
                }

                json = "";
                results.addAll(batchResults);
                batchResults.clear();
            }

            return results;

        }  catch (UnsupportedEncodingException e) {
            String msg = "UTF-8 encoding not supported!?";
            logger.error(msg);
            throw new GeoException(msg);

        } catch (MalformedURLException e) {
            String msg = "Malformed URL '"+url+"', check api key and address values.";
            logger.error(msg, e);
            throw new GeoException(msg);

        } catch (IOException e) {
            String msg = "Error opening API resource '"+url+"'";
            logger.error(msg, e);
            throw new GeoException(msg);

        } catch (JSONException e) {
            String msg = "Malformed JSON Response received:\n"+page.asString();
            logger.error(msg, e);
            throw new GeoException(msg);
        }
    }
}
