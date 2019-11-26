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
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public Object regeocacheSpecificMethodWithNysGeoWebService(int user_offset, String method) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        HttpClient httpClient = HttpClientBuilder.create().build();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end;
        final int limit = 2000;
        int offset = user_offset;
        //Create count for showing the requests made to SAGE
        int regeocacheQueries = 0;

        System.out.println("Handle Method data supplied offset: " + offset);
        try {
            //Get total number of addresses that will be used to update our geocache
            List<Integer> totalList = sqlRegeocacheDao.getMethodTotalCount(method);
            if (totalList == null || totalList.isEmpty()) {
                logger.error("Failed to get a total count for the specified method " + method);
                return apiResponse;
            }
            int total = totalList.get(0);
            logger.info("Total number of records currently cached with the method " + method + " : " + total);
            //Where the magic happens
            while (total > offset) {
                //Get batch of 2000
                List<StreetAddress> nysGeoDBAddresses = sqlRegeocacheDao.getMethodBatch(offset, limit, method);

                //Let the admin know about progress made & increment offset
                logger.info("At offset: " + offset);
                offset = limit + offset;

                if (!nysGeoDBAddresses.isEmpty()) {
                    //Query SAGE with the nysgeo webservice specified
                    for (StreetAddress nysGeoStreetAddress : nysGeoDBAddresses) {

                        //Build URL
                        Address nysgeoAddress = nysGeoStreetAddress.toAddress();
                        String regeocacheUrl = env.getBaseUrl() +
                                "/api/v2/geo/geocode?addr1=%s&addr2=%s&city=%s&state=%s&zip5=%s&provider=nysgeo";
                        regeocacheUrl = String.format(regeocacheUrl, nysgeoAddress.getAddr1(), nysgeoAddress.getAddr2(),
                                nysgeoAddress.getCity(), nysgeoAddress.getState(), nysgeoAddress.getZip5());
                        regeocacheUrl = regeocacheUrl.replaceAll(" ", "%20");
                        regeocacheUrl = StringUtils.deleteWhitespace(regeocacheUrl);
                        regeocacheUrl = regeocacheUrl.replaceAll("`", "");
                        regeocacheUrl = regeocacheUrl.replaceAll("#", "");
                        regeocacheUrl = regeocacheUrl.replaceAll("\\\\", "");

                        //Execute URL
                        try {
                            HttpGet request = new HttpGet(regeocacheUrl);
                            HttpResponse response = httpClient.execute(request);
                            UrlRequest.convertStreamToString(response.getEntity().getContent());
                            regeocacheQueries++;
                        } catch (Exception e) {
                            //Alert Admin to failures
                            logger.error("Failed to contact SAGE with the url: " + regeocacheUrl);
                            continue;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Error refreshing the geocoder database:" + e);
            return apiResponse;
        }
        end = LocalDateTime.now();
        //Let the admin know about the work that has been done.
        logger.info("Queries to SAGE with the NYS GEO Webservice specified: " + regeocacheQueries);
        logger.info("The script started at " + start + " and ended at " + end);
        apiResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
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
