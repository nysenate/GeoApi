package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.DistrictLookup;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.DistException;
import gov.nysenate.sage.service.GeoService;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.util.DB;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class BulkDistrictMethod extends ApiExecution {
    private final Logger logger = Logger.getLogger(BulkDistrictMethod.class);

    public static final ArrayList<DistrictService.TYPE> districtTypes = new ArrayList<DistrictService.TYPE>(Arrays.asList(DistrictService.TYPE.ASSEMBLY,DistrictService.TYPE.CONGRESSIONAL,DistrictService.TYPE.SENATE,DistrictService.TYPE.COUNTY,DistrictService.TYPE.ELECTION));

    private class BluebirdAddress extends BOEStreetAddress {
        public String id;
        public double latitude;
        public double longitude;
        public int geo_accuracy;
        public boolean parse_failure;
        public String parse_message;
        public String geo_method;

        public BluebirdAddress(String id) { this.id = id; }
    }

    private final GeoService geoService;
    private final DistrictLookup streetData;
    private final DistrictService districtService;

    public BulkDistrictMethod() throws Exception {
        streetData = new DistrictLookup(DB.getDataSource());
        geoService = new GeoService();
        districtService = new DistrictService();
    }

    private BluebirdAddress requestToAddress(HttpServletRequest request) {
        BluebirdAddress address = new BluebirdAddress(request.getParameter("id"));
        address.parse_failure = false;
        address.parse_message = "";

        Map<String,String[]> parameters = request.getParameterMap();

        if (parameters.containsKey("street"))
            address.street = parameters.get("street")[0];
        else
            address.street = "";

        if (parameters.containsKey("town"))
            address.town = parameters.get("town")[0];
        else
            address.town = "";

        if (parameters.containsKey("state"))
            address.state = parameters.get("state")[0];
        else
            address.state = "";

        if (parameters.containsKey("zip5") && !parameters.get("zip5").equals("")) {
            try {
                address.zip5 = Integer.parseInt(parameters.get("zip5")[0]);
            } catch (NumberFormatException e) {
                address.parse_failure = true;
                address.parse_message += "zip5 must be an integer. ";
            }
        } else {
            address.zip5 = 0;
        }

        if (parameters.containsKey("apt") && !parameters.get("apt").equals("")) {
            try {
                address.apt_num = Integer.parseInt(parameters.get("apt")[0]);
            } catch (NumberFormatException e) {
                address.parse_failure = true;
                address.parse_message += "apt must be an integer. ";
            }
        } else {
            address.apt_num = 0;
        }
        if (parameters.containsKey("building") && !parameters.get("building").equals("")) {
            try {
                address.bldg_num = Integer.parseInt(parameters.get("building")[0]);
            } catch (NumberFormatException e) {
                address.parse_failure = true;
                address.parse_message += "building must be an integer. ";
            }
        } else {
            address.bldg_num = 0;
        }
        if (parameters.containsKey("latitude") && !parameters.get("latitude").equals("")) {
            try {
                address.latitude = Double.parseDouble(parameters.get("latitude")[0]);
            } catch (NumberFormatException e) {
                address.parse_failure = true;
                address.parse_message += "latitude must be a double. ";
            }
        } else {
            address.latitude = 0;
        }
        if (parameters.containsKey("longitude") && !parameters.get("longitude").equals("")) {
            try {
                address.longitude = Double.parseDouble(parameters.get("longitude")[0]);
            } catch (NumberFormatException e) {
                address.parse_failure = true;
                address.parse_message += "longitude must be a double. ";
            }
        } else {
            address.longitude = 0;
        }

        if (address.latitude==0 || address.longitude==0) {
            address.geo_method = "NONE";
            address.geo_accuracy = 0;
        } else {
            address.geo_method = "USER";
            address.geo_accuracy = 100;
        }
        address.bldg_chr = "";
        address.apt_chr = "";
        AddressUtils.normalizeAddress(address);
        return address;
    }

    private String jsonGetString(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) ? json.getString(key) : "";
        } catch (JSONException e) {
            return null;
        }
    }

    private Double jsonGetDouble(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) && !json.getString(key).equals("") ? json.getDouble(key) : 0;
        } catch (JSONException e) {
            return null;
        }
    }

    private Integer jsonGetInteger(JSONObject json, String key) {
        try {
            return json.has(key) && !json.isNull(key) && !json.getString(key).equals("") ? json.getInt(key) : 0;
        } catch (JSONException e) {
            return null;
        }

    }

    private BluebirdAddress jsonToAddress(String id, JSONObject json) throws JSONException {
        // {"street":"West 187th Street ","town":"New York","state":"NY","zip5":"10033","apt":null,"building":"650"}
        BluebirdAddress address = new BluebirdAddress(id);

        if (json == null) {
            address.parse_failure = true;
            address.parse_message = "Associated JSON Object was null";
            return address;
        } else {
            address.parse_failure = false;
            address.parse_message = "";
        }

        String street = jsonGetString(json, "street");
        String town = jsonGetString(json,"town");
        String state = jsonGetString(json,"state");
        Integer zip5 = jsonGetInteger(json,"zip5");
        Integer apt_num = jsonGetInteger(json,"apt");
        Integer bldg_num = jsonGetInteger(json,"building");
        Double latitude = jsonGetDouble(json, "latitude");
        Double longitude = jsonGetDouble(json, "longitude");

        if (street == null) {
            address.parse_failure = true;
            address.parse_message += "Steet address must be a string. ";
        } else {
            address.street = street.toUpperCase();
        }

        if (town == null) {
            address.parse_failure = true;
            address.parse_message += "Town must be a string. ";
        } else {
            address.town = town.toUpperCase();
        }

        if (state == null) {
            address.parse_failure = true;
            address.parse_message += "State must be a string. ";
        } else {
            address.state = state.toUpperCase();
        }

        if (zip5 == null) {
            address.parse_failure = true;
            address.parse_message += "zip5 must be an integer. ";
        } else {
            address.zip5 = zip5;
        }

        if (bldg_num == null) {
            address.parse_failure = true;
            address.parse_message += "bldg_num must be an integer. ";
        } else {
            address.bldg_num = bldg_num;
        }
        address.bldg_chr = "";

        if (apt_num == null) {
            address.parse_failure = true;
            address.parse_message += "apt_num must be an integer. ";
        } else {
            address.apt_num = apt_num;
        }
        address.apt_chr = "";

        if (latitude == null) {
            address.parse_failure = true;
            address.parse_message += "latitude must be a double. ";
        } else {
            address.latitude = latitude;
        }

        if (longitude == null) {
            address.parse_failure = true;
            address.parse_message += "longitude must be an integer. ";
        } else {
            address.longitude = longitude;
        }

        if (address.latitude==0 || address.longitude==0) {
            address.geo_method = "NONE";
            address.geo_accuracy = 0;
        } else {
            address.geo_method = "USER";
            address.geo_accuracy = 100;
        }

        AddressUtils.normalizeAddress(address);
        return address;
    }

    public ArrayList<BluebirdAddress> readAddresses(String json) throws JSONException {
        ArrayList<BluebirdAddress> addresses = new ArrayList<BluebirdAddress>();
        JSONObject bluebirdAddresses = new JSONObject(json);
        Iterator<String> keys = bluebirdAddresses.keys();
        while (keys.hasNext()) {
            String bluebirdId = keys.next();
            try {
                addresses.add(jsonToAddress(bluebirdId, bluebirdAddresses.getJSONObject(bluebirdId)));
            } catch (JSONException e) {
            	e.printStackTrace();
                addresses.add(null);
            }
        }
        return addresses;
    }

    @SuppressWarnings("unused")
    private static class BulkResult {
        public static enum STATUS { HOUSE, STREET, ZIP5, SHAPEFILE, INVALID, NOMATCH };

        public STATUS status_code;
        public String message;
        public String address_id;
        public int county_code;
        public int assembly_code;
        public int congressional_code;
        public int senate_code;
        public int election_code;
        public String ward_code;
        public String school_code;
        public String town_code;
        public String cleg_code;
        public String address;
        public double latitude;
        public double longitude;
        public int geo_accuracy;
        public String geo_method;

        public BluebirdAddress bluebird_address;

        public BulkResult(STATUS status, String message, BluebirdAddress address, BOEAddressRange match) {
            this.bluebird_address = address;
            this.address_id = address.id;
            this.status_code = status;
            this.message = message;
            this.address = address.toString();
            this.election_code = match.electionCode;
            this.senate_code = match.senateCode;
            this.congressional_code = match.congressionalCode;
            this.assembly_code = match.assemblyCode;
            this.county_code = match.countyCode;
            this.cleg_code = match.clegCode;
            this.town_code = ""; //match.townCode;
            this.school_code = ""; //match.schoolCode;
            this.ward_code = match.wardCode;
            this.latitude = address.latitude;
            this.longitude = address.longitude;
            this.geo_accuracy = address.geo_accuracy;
            this.geo_method = address.geo_method;
        }
    }

    private BOEAddressRange SAGE2Range(Address address) {
        if (address == null) {
            return null;
        }

        BOEAddressRange range = new BOEAddressRange();
        range.street = address.addr2;
        range.town = address.city;
        range.state = address.state;
        try {
            range.zip5 = Integer.parseInt(address.zip5);
        } catch (NumberFormatException e) {
            range.zip5 = 0;
        }
        range.assemblyCode = address.assembly_code;
        range.congressionalCode = address.congressional_code;
        range.senateCode = address.senate_code;
        range.countyCode = address.county_code;
        range.clegCode = address.cleg_code;
        range.schoolCode = address.school_code;
        range.wardCode = address.ward_code;
        range.townCode = address.town_code;
        return range;
    }

    private Address Bluebird2SAGE(BluebirdAddress address) {
        if (address == null) {
            return null;
        }

        String street_address = "";
        if (address.bldg_num != 0) { street_address += address.bldg_num; }
        if (address.bldg_chr != null && !address.bldg_chr.isEmpty()) { street_address += address.bldg_chr; }
        if (address.street != null && !address.street.isEmpty()) { street_address += " "+address.street; }
        Address sageAddress = new Address(
            street_address.trim(),
            address.town,
            address.state,
            (address.zip5 != 0) ? String.valueOf(address.zip5) : ""
        );
        sageAddress.setGeocode(address.latitude, address.longitude, address.geo_accuracy);
        return sageAddress;
    }

    public class DelayResult implements Callable<BulkResult> {
        public final BulkResult result;

        public DelayResult(BulkResult result) {
            this.result = result;
        }

        public BulkResult call() {
            return result;
        }
    }

    public class ParallelShapeFileRequest implements Callable<BulkResult> {
        private final BluebirdAddress address;
        private final boolean useGeocoder;
        private final String geocoder;

        public ParallelShapeFileRequest(BluebirdAddress address, boolean useGeocoder, String geocoder) {
            this.address = address;
            this.useGeocoder = useGeocoder;
            this.geocoder = geocoder;
        }

        public BulkResult call() {
            // Fall back to shape files
            Address sageAddress = Bluebird2SAGE(address);
            if (!sageAddress.is_geocoded()) {

                // Unless explicitly disabled by a user option.
                if (!useGeocoder) {
                    return new BulkResult(BulkResult.STATUS.NOMATCH, "Geocoder disabled. "+address, address, new BOEAddressRange());
                }

                // Fill in missing coordinates for addresses.
                try {
                    Result geoResult = geoService.geocode(sageAddress, geocoder);
                    if (!geoResult.status_code.equals("0")) {
                        return new BulkResult(BulkResult.STATUS.NOMATCH, "Geocode Error ["+geoResult.status_code+"] - "+geoResult.messages.get(0), address, new BOEAddressRange());
                    } else if (geoResult.addresses.size()!=1) {
                        return new BulkResult(BulkResult.STATUS.NOMATCH, "Geocode Failure - "+geoResult.addresses.size()+" results found for: "+address.toString(), address, new BOEAddressRange());
                    } else if (geoResult.addresses.get(0).geocode_quality < 40){
                        return new BulkResult(BulkResult.STATUS.NOMATCH, "Geocode Failure - "+geoResult.addresses.get(0).geocode_quality+" must be atleast 40 (state level lookup) for "+address.toString(), address, new BOEAddressRange());
                    } else {
                        sageAddress = geoResult.addresses.get(0);
                        address.latitude = sageAddress.latitude;
                        address.longitude = sageAddress.longitude;
                        address.geo_accuracy = sageAddress.geocode_quality;
                    }
                } catch (GeoException e) {
                    e.printStackTrace();
                    return new BulkResult(BulkResult.STATUS.NOMATCH, "Geocode Exception for: "+address.toString(), address, new BOEAddressRange() );
                }
            }

            try {
                Result distResult = districtService.assignDistricts(sageAddress, Arrays.asList(DistrictService.TYPE.SENATE), "geoserver");
                if (!distResult.status_code.equals("0")) {
                    return new BulkResult(BulkResult.STATUS.NOMATCH, "DistAssign Error ["+distResult.status_code+"] - "+distResult.messages.get(0), address, new BOEAddressRange());
                } else {
                    sageAddress = distResult.address;
                }
            } catch (DistException e) {
                 e.printStackTrace();
                 return new BulkResult(BulkResult.STATUS.NOMATCH, "DistAssign Exception for: "+sageAddress, address, new BOEAddressRange() );
            }

            BOEAddressRange addressRange = SAGE2Range(sageAddress);
            return new BulkResult(BulkResult.STATUS.SHAPEFILE, "SHAPEFILE MATCH for "+sageAddress, address, addressRange );
        }
    }

    public class ParallelStreetFileRequest implements Callable<BulkResult> {
        private final BluebirdAddress address;

        public ParallelStreetFileRequest(BluebirdAddress address) {
            this.address = address;
        }

        public BulkResult call() throws SQLException {
            // First attempt a street file lookup by house
            List<BOEAddressRange> matches = streetData.getRangesByHouse(address);
            if (matches.size()==1) {
                return new BulkResult(BulkResult.STATUS.HOUSE, "HOUSE MATCH for "+address, address, matches.get(0));
            }

            // Then try a street file lookup by street and consolidate
            if (address.street != null && !address.street.trim().equals("")) {
	            matches = streetData.getRangesByStreet(address);
	            BOEAddressRange consolidated = AddressUtils.consolidateRanges(matches);
	            if (consolidated != null) {
	                return new BulkResult(BulkResult.STATUS.STREET, "STREET MATCH for "+address, address, consolidated);
	            }
        	}

            // Then try a street file lookup by zip5 and consolidate
            matches = streetData.getRangesByZip(address);
            BOEAddressRange consolidated = AddressUtils.consolidateRanges(matches);
            if (consolidated != null) {
                return new BulkResult(BulkResult.STATUS.ZIP5, "ZIP5 MATCH for "+address, address, consolidated);
            }

            return new BulkResult(BulkResult.STATUS.NOMATCH, "Street file lookup failed.", address, new BOEAddressRange());
        }
    }

    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
    	// Load the request addresses
    	String type = more.get(RequestCodes.TYPE.code());
        ArrayList<BluebirdAddress> bluebirdAddresses;
        if(type.equals("url")) {
            bluebirdAddresses = new ArrayList<BluebirdAddress>(Arrays.asList(requestToAddress(request)));
        } else if (type.equals("body")) {
        	try {
                String json = IOUtils.toString(request.getInputStream(),"UTF-8");
                bluebirdAddresses = readAddresses(json);
            } catch (IOException e) {
                logger.error("No request body found.", e);
                throw new ApiTypeException("No request body found.", e);
            } catch (JSONException e) {
                logger.error("Invalid JSON recieved", e);
                throw new ApiTypeException("Invalid JSON recieved.",e);
            }
        } else {
            throw new ApiTypeException(type);
        }

        // Load the request options
        String useShapefileOption = request.getParameter("useShapefile");
        String useGeocoderOption = request.getParameter("useGeocoder");
        String geocoderOption = request.getParameter("geocoder");
        boolean useShapefile = useShapefileOption == null || useShapefileOption.equals("1");
        boolean useGeocoder = useGeocoderOption == null || useGeocoderOption.equals("1");
        String geocoder = (geocoderOption==null) ? "geocoder" : geocoderOption;

        // Make thread count an option for now, TODO: remove this for production?
        String threadCountOption = request.getParameter("threadCount");
        int threadCount = (threadCountOption == null) ? 3 : Integer.parseInt(threadCountOption);

        // Queue up all the tasks into our thread pool
        logger.info("Processing "+bluebirdAddresses.size()+" addresses with "+threadCount+" threads.");
        ExecutorService streetFileExecutor = Executors.newFixedThreadPool(threadCount);
        ArrayList<Future<BulkResult>> streetFileResults = new ArrayList<Future<BulkResult>>();
        for (BluebirdAddress address : bluebirdAddresses) {
            if (address == null) {
                BulkResult result = new BulkResult(BulkResult.STATUS.INVALID,"Invalid JSON Entry",new BluebirdAddress("-1"),new BOEAddressRange());
                streetFileResults.add(streetFileExecutor.submit(new DelayResult(result)));
            } else if (address.parse_failure) {
                BulkResult result = new BulkResult(BulkResult.STATUS.INVALID,address.parse_message,address,new BOEAddressRange());
                streetFileResults.add(streetFileExecutor.submit(new DelayResult(result)));
            } else {
                streetFileResults.add(streetFileExecutor.submit(new ParallelStreetFileRequest(address)));
            }
        }

        // Wait for the street file results to come back
        ExecutorService shapeFileExecutor = Executors.newFixedThreadPool(threadCount);
        ArrayList<Future<BulkResult>> finalResults = new ArrayList<Future<BulkResult>>();
        for (Future<BulkResult> result : streetFileResults) {
            try {
                BulkResult streetFileResult = result.get();
                Callable<BulkResult> callable;
                if (streetFileResult.status_code == BulkResult.STATUS.NOMATCH) {
                    if (useShapefile) {
                        callable = new ParallelShapeFileRequest(streetFileResult.bluebird_address, useGeocoder, geocoder);
                    } else {
                        callable = new DelayResult(new BulkResult(BulkResult.STATUS.NOMATCH, "Shapefiles disabled.", streetFileResult.bluebird_address, new BOEAddressRange()));
                    }
                } else {
                    callable = new DelayResult(streetFileResult);
                }
                finalResults.add(shapeFileExecutor.submit(callable));

            } catch (InterruptedException e) {
                throw new ApiInternalException(e);
            } catch (ExecutionException e) {
                throw new ApiInternalException(e.getCause());
            }
        }

        // Process final results
        ArrayList<BulkResult> results = new ArrayList<BulkResult>();
        for (Future<BulkResult> result : finalResults) {
            try {
                BulkResult finalResult = result.get();
                finalResult.bluebird_address = null; // Unset so it isn't echoed back. HACK!
                results.add(finalResult);
            } catch (InterruptedException e) {
                throw new ApiInternalException(e);
            } catch (ExecutionException e) {
                throw new ApiInternalException(e.getCause());
            }
        }

        // then shutdown and return
        streetFileExecutor.shutdown();
        shapeFileExecutor.shutdown();
        return results;
    }
}
