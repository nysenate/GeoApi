package gov.nysenate.sage.service.data;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.data.SqlRegeocacheDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.util.StreetAddressParser;
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
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

@Service
public class RegeocacheService {

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
        apiResponse = new ApiError(this.getClass(), SUCCESS);
        return apiResponse;
    }

    public Object updateGeocacheWithNYSGeoData(int nys_offset) {
        Object apiResponse;
        HttpClient httpClient = HttpClientBuilder.create().build();
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
                    httpRequestString = String.format(httpRequestString, nysAddress.getAddr1(),
                            nysAddress.getCity(), nysAddress.getState(), nysAddress.getZip5());
                    httpRequestString = httpRequestString.replaceAll(" ", "%20");
                    httpRequestString = StringUtils.deleteWhitespace(httpRequestString);
                    httpRequestString = httpRequestString.replaceAll("`", "");

                    try {
                        HttpGet httpRequest = new HttpGet(httpRequestString);
                        HttpResponse httpResponse = httpClient.execute(httpRequest);

                        JSONObject uspsJson =
                                new JSONObject(convertStreamToString(httpResponse.getEntity().getContent()));
                        if (uspsJson.getBoolean("validated")) {
                            JSONObject uspsAddressJson = uspsJson.getJSONObject("address");
                            nysStreetAddress = StreetAddressParser.parseAddress(new Address(
                                    uspsAddressJson.getString("addr1"), uspsAddressJson.getString("addr2"),
                                    uspsAddressJson.getString("city"), uspsAddressJson.getString("state"),
                                    uspsAddressJson.getString("zip5"), uspsAddressJson.getString("zip4")));
                        }
                    } catch (Exception e) {
                        logger.error("Failed to make Http request because of exception" + e.getMessage());
                        logger.error("Failed address was: " + nysAddress.toString());
                    }

                    //Determine if address exits in our Geocache by getting
                    // the method of its results (GoogleDao, YahooDao, etc)
                    String geocachedStreetAddressProvider =
                            sqlRegeocacheDao.getProviderOfAddressInCacheIfExists(nysStreetAddress);

                    //If the geocacheStreetAddressProvider is empty,
                    // we don't have the address cached, so insert the address
                    if (StringUtils.isEmpty(geocachedStreetAddressProvider)) {
                        //insert
                        sqlRegeocacheDao.insetIntoGeocache(nysStreetAddress, nysGeocode);
                    }
                    //If the provider is not Google and NYS Geo has a rooftop coordinate, update the cache
                    else if (!geocachedStreetAddressProvider.equals("GoogleDao") && nysGeoAddress.getPointtype() == 1) {
                        //update
                        sqlRegeocacheDao.updateGeocache(nysStreetAddress, nysGeocode);
                    }
                }
            }

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
        apiResponse = new ApiError(this.getClass(), SUCCESS);
        return apiResponse;
    }

    private String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return sb.toString();
    }
}
