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
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
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

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

@Service
public class RegeocacheService implements SageRegeocacheService {

    private Logger logger = LoggerFactory.getLogger(RegeocacheService.class);
    private SqlRegeocacheDao sqlRegeocacheDao;
    private Environment env;
    private GeocodeServiceProvider geocodeServiceProvider;
    final int nys_limit = 2000;

    @Autowired
    public RegeocacheService(SqlRegeocacheDao sqlRegeocacheDao, GeocodeServiceProvider geocodeServiceProvider,
                             Environment env) {
        this.geocodeServiceProvider = geocodeServiceProvider;
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

    /**
     * Process for handling all sorts of mass regeocaching. It can handle methods, locations, zipcodes, qualities
     * and any combination of those. Primarily accessed through the CLI
     * @param offset
     * @param user_limit
     * @param useFallback
     * @param typeList
     * @return
     */
    public Object massRegeoache(int offset, int user_limit, boolean useFallback ,ArrayList<String> typeList) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        logger.info("Starting mass geocache at " + LocalDateTime.now() + " at offset " + offset);
        int batch_limit = 2000; // this limit is for the batches of records pulled at a time
        String provider = determineIfProviderSpecified(typeList);
        //Create log file, if its empty at the end, then no errors
        String errorLogFileName = "massRegeocache" + LocalDateTime.now() + ".txt";
        createNewFile(env.getDataDir(), errorLogFileName);

        try {
            //create query for total in the specified type(s)
            //Get total number of addresses that will be used to update our geocache
            List<Integer> totalList = sqlRegeocacheDao.determineMassGeocodeRecordCount(typeList);
            if (totalList == null || totalList.isEmpty()) {
                logger.error("Failed to get a total count for the mass regeocache");
                return apiResponse;
            }
            int total = totalList.get(0);
            logger.info("Found this number of records for the mass regeocache: " + total);

            //Set various batch limits if specified
            if (total > user_limit && user_limit != 0) {
                total = user_limit;
                logger.info("A limit was specified by the user. The total (used for looping thru records)" +
                        " will be changed to the limit provided");
            }

            if (batch_limit > user_limit && user_limit != 0) {
                batch_limit = user_limit;
                logger.info("A limit was specified by the user. The batch limit (used for getting a batch of records)" +
                        " will be changed to the limit provided");
            }

            while (total > offset) {
                //Get batch of 2000 or less if specified
                List<StreetAddress> massGeocacheAddresses = sqlRegeocacheDao.getMassGeocodeBatch(offset, batch_limit, typeList);

                //Let the admin know about progress made & increment offset
                logger.info("At offset: " + offset);
                offset = batch_limit + offset;

                if (!massGeocacheAddresses.isEmpty()) {

                    for (StreetAddress geocacheStreetAddress : massGeocacheAddresses) {
                        Address geocacheAddress = geocacheStreetAddress.toAddress();

                        if (provider.isEmpty()) {
                            provider = geocodeServiceProvider.getDefaultProvider();
                        }

                        try {
                            //Geocode the address
                            GeocodeResult geocodeResult = geocodeServiceProvider.geocode(geocacheAddress,
                                    provider, geocodeServiceProvider.getDefaultFallback(),
                                    useFallback, false, false);

                            //This means geocoding failed, write to file
                            if (geocodeResult.getStatusCode() != SUCCESS) {
                                //write to file method
                                writeDataToFile(env.getDataDir() + errorLogFileName, geocacheAddress);
                            }
                        }
                        catch (Exception e) {
                            logger.error("Geocoding failed for an address. The faulty address is in the file", e);
                            //Write to file method
                            writeDataToFile(env.getDataDir() + errorLogFileName, geocacheAddress);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Error refreshing the geocoder database:" + e);
            return apiResponse;
        }

        //Let the admin know about the work that has been done.
        logger.info("The mass regeocache was completed at " + LocalDateTime.now() );
        apiResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        return apiResponse;
    }

    /**
     * This creates a error log file on each run of the Mass geocache process
     * @param directory
     * @param filename
     */
    private void createNewFile(String directory, String filename) {
        try {
            File errorFile = new File(directory + filename);
            boolean fileCreated = errorFile.createNewFile();
            if (fileCreated) {
                logger.info("File created: " + errorFile.getName());
            } else {
                logger.info("File already exists.");
            }
        } catch (IOException e) {
            logger.info("An error occurred creating the log file." , e);
        }
    }

    /**
     * Writes data to the error log file created by the mass geocache process
     * @param filename
     * @param failedAddress
     */
    private void writeDataToFile(String filename, Address failedAddress ) {
        try {
            String textToAppend = failedAddress.toNormalizedString();

            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(filename, true)  //Set true for append mode
            );
            writer.newLine();   //Add new line
            writer.write(textToAppend);
            writer.close();
        }
        catch (IOException e) {
            logger.info("Failed to log address to file. " + failedAddress.toNormalizedString(), e);
        }
    }

    //Formats a url for use with some form of geocaching
    private String formatMassGeocacheUrl(String url, Address geocacheAddress) {
        url = String.format(url, geocacheAddress.getAddr1(), geocacheAddress.getAddr2(),
                geocacheAddress.getCity(), geocacheAddress.getState(), geocacheAddress.getZip5());
        url = url.replaceAll(" ", "%20");
        url = StringUtils.deleteWhitespace(url);
        url = url.replaceAll("`", "");
        url = url.replaceAll("#", "");
        url = url.replaceAll("\\\\", "");
        return url;
    }

    /*
    If user specified a provider for the mass geocache, this method will return it or an empty string
     */
    private String determineIfProviderSpecified(ArrayList<String> typeList) {
        int position = 0;
        for (String data: typeList) {
            if (data.equals("provider")) {
                return typeList.get(position+1);
            }
            position++;

        }
        return "";
    }

    /**
     * Cycles thru the NYS GEO database and updates our own geocache with that information
     * @param nys_offset
     * @return
     */
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
                    httpRequestString = formatMassGeocacheUrl(httpRequestString, nysAddress);

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

    private StreetAddress constructStreetAddressFromUSPSJson(JSONObject uspsJson) {
        return StreetAddressParser.parseAddress(new Address(
                uspsJson.getString("addr1"), uspsJson.getString("addr2"),
                uspsJson.getString("city"), uspsJson.getString("state"),
                uspsJson.getString("zip5"), uspsJson.getString("zip4")));
    }
}
