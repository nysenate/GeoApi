package gov.nysenate.sage.service.data;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.data.SqlRegeocacheDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

@Service
public class RegeocacheService implements SageRegeocacheService{

    private Logger logger = LoggerFactory.getLogger(RegeocacheService.class);
    private SqlRegeocacheDao sqlRegeocacheDao;
    private Environment env;
    final int nys_limit = 2000;

    @Autowired
    public RegeocacheService(SqlRegeocacheDao sqlRegeocacheDao, Environment env) {
        this.sqlRegeocacheDao = sqlRegeocacheDao;
        this.env = env;
    }

    public Object updateZipsInGeocache() {
        Object apiResponse;
        String BASE_URL = env.getBaseUrl() + "/api/v2/geo/geocode?addr=";
        String GEO_PROVIDER_URL = "&provider=google";
        List<String> zipCodes = null;

        /*
        Execute SQL and get zip codes
         */
        try {
            zipCodes = sqlRegeocacheDao.getAllZips();
        } catch (Exception ex) {
            logger.error("Error retrieving zip codes from geoapi db", ex);
            apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            return apiResponse;
        }

        //Fail if we couldnt get zip codes properly
        if (zipCodes == null) {
            apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            return apiResponse;
        }


        //Cycle through zip codes
        for (String zip : zipCodes) {
            logger.info("Geocoding zip: " + zip);
            /*
        Execute http request
         */
            HttpClient httpClient = HttpClientBuilder.create().build();
            try {
                HttpPost httpRequest = new HttpPost(BASE_URL + zip + GEO_PROVIDER_URL);
                httpClient.execute(httpRequest);
                ((CloseableHttpClient) httpClient).close();
            } catch (Exception e) {
                logger.error("Failed to make Http request because of exception" + e.getMessage());
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
                return apiResponse;
            }
        }
        apiResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        return apiResponse;
    }

    public Object updatesDupsInGeocacheWithNysGeo(int nys_offset) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        HttpClient httpClient = HttpClientBuilder.create().build();
        final int limit = 2000;
        int offset = 0;
        //data loggers
        int updatedRecords = 0;
        int badRecords = 0;
        int nysGeoDups = 0;
        int queriesToNYSGEO = 0;

        try {
            //Get total number of addresses that will be used to update our geocache
            int total = sqlRegeocacheDao.getNYSTotalDupAddressesCount();
            System.out.println("NYS Geo Dup total iteration count: " + total);

            StreetAddress lastAddress = null;


            while (total > offset) {
                //Get batch of 2000
                List<NYSGeoAddress> nysGeoAddresses = sqlRegeocacheDao.getBatchOfNysGeoDups(limit, offset);
                System.out.println("At offset: " + offset);
                offset = limit + offset;

                //For some reason NYS GEO has the same address multiple times
                //which is different from the issue we are trying to address with this script
                //if the same address comes up again sequentially we need to skip it

                for (NYSGeoAddress nysGeoAddress : nysGeoAddresses) {

                    StreetAddress nysStreetAddress = nysGeoAddress.toStreetAddress();
                    Address nysAddress = nysStreetAddress.toAddress();
                    StreetAddress uspsStreetAddress = null;


                    if (nysGeoAddress.getPointtype() != 1) {
                        //if its not a rooftop point, we need to send it to google to get the rooftop
                        String nysGeoUrl = env.getUspsAmsApiUrl() +
                                "/api/v2/geo/geocode?addr1=%s&addr2=%s&city=%s&state=%s&zip5=%s&provider=nysgeo";
                        nysGeoUrl = formatUrlString(nysGeoUrl,nysAddress);

                        try {
                            HttpGet request = new HttpGet(nysGeoUrl);
                            HttpResponse response = httpClient.execute(request);
                            UrlRequest.convertStreamToString(response.getEntity().getContent());
                            queriesToNYSGEO++;
                        } catch (IOException e) {
                            continue;
                        }

                        continue; //Only Send it to
                    }


                    if (lastAddress != null && nysStreetAddress.equals(lastAddress)) {
                        nysGeoDups++;
                        continue;
                    } else {
                        lastAddress = nysStreetAddress;
                    }

                    String url = env.getUspsAmsApiUrl() + "validate?detail=true&addr1=%s&addr2=%s&city=%s&state=%s&zip5=%s";
                    url = formatUrlString(url, nysAddress);

                    try {
                        HttpGet request = new HttpGet(url);
                        HttpResponse response = httpClient.execute(request);

                        JSONObject json = new JSONObject(UrlRequest.convertStreamToString(response.getEntity().getContent()));
                        if (json.getBoolean("validated")) {
                            JSONObject addressJson = json.getJSONObject("address");
                            uspsStreetAddress = constructStreetAddressFromUSPSJson(addressJson);
                        }
                    } catch (IOException e) {
                        badRecords++;
                        continue;
                    }

                    if (uspsStreetAddress == null) {
                        badRecords++;
                        continue;
                    }

                    if (nysGeoAddress.getPointtype() == 1) {
                        sqlRegeocacheDao.updateGeocache(uspsStreetAddress, nysGeoAddress.toGeocode());
                        updatedRecords++;
                    } else {
                        badRecords++;
                    }

                }
            }

            String repsonseString = "Duplicate addresses Geocded \n" +
                    "Queries to NYS GEO: " + queriesToNYSGEO + "\n" +
                    "Updated Records: " + updatedRecords + "\n" +
                    "Bad Records (Not found by USPS but rooftop level): " + badRecords + "\n" +
                    "NYS Geo duplicate address records: " + nysGeoDups;

            logger.info(repsonseString);

            apiResponse = new GenericResponse(true,repsonseString);
        } catch (SQLException e) {
            System.err.println("Error reading from the geocoder database");
            return apiResponse;
        }
        return apiResponse;
    }

    public Object updateGeocacheWithNYSGeoData(int nys_offset) {
        Object apiResponse;
        HttpClient httpClient = HttpClientBuilder.create().build();
        int updatedRecordsCount = 0;
        try {
            //Get total number of addresses that will be used to update our geocache
            Integer total = sqlRegeocacheDao.getNYSTotalAddresses();

            //start from offset and loop until the total number in batches of 2000
            //It is best to start from 0
            while (total > nys_offset) {
                //Get batch of 2000
                List<NYSGeoAddress> nysGeoAddresses = sqlRegeocacheDao.getBatchOfNYSGeoAddresses(nys_limit, nys_offset);
                logger.info("At nys_offset: " + nys_offset);
                nys_offset = nys_limit + nys_offset;

                //Handle batch
                for (NYSGeoAddress nysGeoAddress : nysGeoAddresses) {
                    //Convert NYSGeoAddressto an Adress Object
                    Address nysAddress = nysGeoAddress.toAddress();

                    //Extract NYSGeoAddress coordinates to form a geocode
                    Geocode nysGeocode = nysGeoAddress.toGeocode();

                    //Convert NYSGeoAddress to a Street address like our cache
                    StreetAddress nysStreetAddress = nysGeoAddress.toStreetAddress();

                    //Run new Address through USPS
                    String httpRequestString = env.getUspsAmsApiUrl()
                            + "validate?detail=true&addr1=%s&addr2=&city=%s&state=%s&zip5=%s&zip4=";
                    httpRequestString = formatUrlString(httpRequestString, nysAddress);

                    try {
                        HttpGet httpRequest = new HttpGet(httpRequestString);
                        HttpResponse httpResponse = httpClient.execute(httpRequest);

                        JSONObject uspsJson =
                                new JSONObject(UrlRequest.convertStreamToString(httpResponse.getEntity().getContent()));
                        if (uspsJson.getBoolean("validated")) {
                            JSONObject uspsAddressJson = uspsJson.getJSONObject("address");
                            nysStreetAddress = constructStreetAddressFromUSPSJson(uspsAddressJson);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to make Http request because of exception" + e.getMessage());
                        logger.error("Failed address was: " + nysAddress.toString());
                        continue; //only put it in the database if USPS has data on it / corrected the address
                    }

                    //Determine if address exits in our Geocache by getting
                    // the method of its results (GoogleDao, YahooDao, etc)
                    GeocodedStreetAddress geocachedStreetAddress =
                            sqlRegeocacheDao.getProviderOfAddressInCacheIfExists(nysStreetAddress);

                    String geocachedStreetAddressProvider = "";
                    if (geocachedStreetAddress != null && geocachedStreetAddress.getStreetAddress().equals(nysStreetAddress)) {
                        geocachedStreetAddressProvider = geocachedStreetAddress.getGeocode().getMethod();
                        //update only if its not google. Address was matched so it should insert fine
                        if (!geocachedStreetAddressProvider.equals("GoogleDao") && nysGeoAddress.getPointtype() == 1) {
                            try {
                                sqlRegeocacheDao.updateGeocache(nysStreetAddress, nysGeocode);
                                updatedRecordsCount++;
                            } catch (SQLException e) {
                                //should not happen beacause it was verified
                                logger.warn("Update Failed for " + nysStreetAddress.toString());
                            }
                        }
                    }
                    //If the geocacheStreetAddressProvider is empty, we don't have the address cached, so insert the address
                    else if (StringUtils.isEmpty(geocachedStreetAddressProvider)) {
                        //insert
                        try {
                            sqlRegeocacheDao.insetIntoGeocache(nysStreetAddress, nysGeocode);
                            updatedRecordsCount++;
                        } catch (SQLException e) {
                            logger.warn("Insert Failed for " + nysStreetAddress.toString());
                        }
                    }
                }
            }

            logger.info("Updated " + updatedRecordsCount + " records");

            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException e) {
                logger.error("Failed to close http connection \n" + e);
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
                return apiResponse;
            }

        } catch (Exception ex) {
            logger.error("Error retrieving addresses from geocache \n" + ex);
            apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            return apiResponse;
        }
        apiResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        return apiResponse;
    }

    private String formatUrlString(String urlToFormat, Address addressToFormat) {
        urlToFormat = String.format(urlToFormat, addressToFormat.getAddr1(), addressToFormat.getAddr2(),
                addressToFormat.getCity(), addressToFormat.getState(), addressToFormat.getZip5());
        urlToFormat = urlToFormat.replaceAll(" ", "%20");
        urlToFormat = StringUtils.deleteWhitespace(urlToFormat);
        urlToFormat = urlToFormat.replaceAll("`", "");
        urlToFormat = urlToFormat.replaceAll("#", "");
        return urlToFormat;
    }

    private StreetAddress constructStreetAddressFromUSPSJson(JSONObject uspsJson) {
        return StreetAddressParser.parseAddress(new Address(
                uspsJson.getString("addr1"), uspsJson.getString("addr2"),
                uspsJson.getString("city"), uspsJson.getString("state"),
                uspsJson.getString("zip5"), uspsJson.getString("zip4")));
    }
}
