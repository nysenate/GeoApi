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
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class BulkDistrictMethod extends ApiExecution {

    public static final ArrayList<DistrictService.TYPE> districtTypes = new ArrayList<DistrictService.TYPE>(Arrays.asList(DistrictService.TYPE.ASSEMBLY,DistrictService.TYPE.CONGRESSIONAL,DistrictService.TYPE.SENATE,DistrictService.TYPE.COUNTY,DistrictService.TYPE.ELECTION));

    private class BluebirdAddress extends BOEStreetAddress {
        public String id;
        public double latitude;
        public double longitude;
        public int geo_accuracy;

        public BluebirdAddress(String id) { this.id = id; }
    }

    private final GeoService geoService;
    private final DistrictLookup streetData;
    private final DistrictService districtService;

    public BulkDistrictMethod() throws Exception {
        Resource config = new Resource();

        MysqlDataSource db = new MysqlDataSource();
        db.setServerName(config.fetch("db.host"));
        db.setUser(config.fetch("db.user"));
        db.setPassword(config.fetch("db.pass"));
        db.setDatabaseName(config.fetch("db.name"));

        streetData = new DistrictLookup(db);
        geoService = new GeoService();
        districtService = new DistrictService();
    }

    private BluebirdAddress requestToAddress(HttpServletRequest request) {
        BluebirdAddress address = new BluebirdAddress(request.getParameter("id"));

        String street = request.getParameter("street");
        String town = request.getParameter("town");
        String state = request.getParameter("state");
        String zip5 = request.getParameter("zip5");
        String apt_num = request.getParameter("apt_num");
        String bldg_num = request.getParameter("bldg_num");
        String latitude = request.getParameter("latitude");
        String longitude = request.getParameter("longitude");

        address.street = (street==null ? "" : street.toUpperCase().trim());
        address.town = (town==null ? "" : town.toUpperCase().trim());
        address.state = (state==null ? "" : state.toUpperCase().trim());
        address.zip5 = (zip5==null ? 0 : Integer.parseInt(zip5.trim()));
        address.apt_num = (apt_num==null ? 0 : Integer.parseInt(apt_num.trim()));
        address.bldg_num = (bldg_num==null ? 0 : Integer.parseInt(bldg_num.trim()));
        address.latitude = (latitude.equals("null") ? 0 : Double.parseDouble(latitude));
        address.longitude = (longitude.equals("null") ? 0 : Double.parseDouble(longitude));
        address.geo_accuracy = (address.latitude==0 || address.longitude==0)? 0 : 100;
        address.bldg_chr = "";
        address.apt_chr = "";
        AddressUtils.normalizeAddress(address);
        return address;
    }

    private BluebirdAddress jsonToAddress(String id, JSONObject json) throws JSONException {
        // {"street":"West 187th Street ","town":"New York","state":"NY","zip5":"10033","apt":null,"building":"650"}
        if (json == null) return null;

        String street = json.getString("street");
        String town = json.getString("town");
        String state = json.getString("state");
        String zip5 = json.getString("zip5");
        String apt_num = json.getString("apt");
        String bldg_num = json.getString("building");
        String latitude = json.has("latitidue") ? json.getString("latitude") : "null";
        String longitude = json.has("longitude") ? json.getString("longitude") : "null";

        BluebirdAddress address = new BluebirdAddress(id);
        address.street = (street.equals("null") ? "" : street.toUpperCase());
        address.town = (town.equals("null") ? "" : town.toUpperCase());
        address.state = (state.equals("null") ? "" : state.toUpperCase());
        address.zip5 = (zip5.equals("null") || zip5.equals("") ? 0 : Integer.parseInt(zip5));
        address.apt_num = (apt_num.equals("null") || apt_num.equals("") ? 0 : Integer.parseInt(apt_num));
        address.bldg_num = (bldg_num.equals("null") || bldg_num.equals("") ? 0 : Integer.parseInt(bldg_num));
        address.latitude = (latitude.equals("null") ? 0 : Double.parseDouble(latitude));
        address.longitude = (longitude.equals("null") ? 0 : Double.parseDouble(longitude));
        address.geo_accuracy = (address.latitude==0 || address.longitude==0)? 0 : 100;
        address.bldg_chr = "";
        address.apt_chr = "";
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

        public BulkResult(STATUS status, String message, BluebirdAddress address, BOEAddressRange match) {
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

        Address sageAddress = new Address(
            address.street,
            address.town,
            address.state,
            (address.zip5 != 0) ? String.valueOf(address.zip5) : ""
        );
        sageAddress.setGeocode(address.latitude, address.longitude, address.geo_accuracy);
        return sageAddress;
    }

    public class ParallelRequest implements Callable<BulkResult> {

        private final BluebirdAddress address;
        private final boolean useShapefile;
        private final boolean useGeocoder;
        private final String geocoder;

        public ParallelRequest(BluebirdAddress address, boolean useShapefile, boolean useGeocoder, String geocoder) {
            this.address = address;
            this.useShapefile = useShapefile;
            this.useGeocoder = useGeocoder;
            this.geocoder = geocoder;
        }

        @Override
        public BulkResult call() throws SQLException {
        	// Don't bother with NULL addresses
            if (address==null) {
            	return new BulkResult(BulkResult.STATUS.INVALID,"Invalid JSON Entry",new BluebirdAddress("-1"),new BOEAddressRange());
            }

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

            // Unless explicitly disabled by a user option
            if (!useShapefile) {
            	return new BulkResult(BulkResult.STATUS.NOMATCH, "Shapefiles disabled.", address, new BOEAddressRange());
            }

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
                 return new BulkResult(BulkResult.STATUS.NOMATCH, "DistAssign Exception for: "+address.toString(), address, new BOEAddressRange() );
            }

            BOEAddressRange addressRange = SAGE2Range(sageAddress);
            return new BulkResult(BulkResult.STATUS.SHAPEFILE, "SHAPEFILE MATCH for "+sageAddress, address, addressRange );
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
                e.printStackTrace();
                throw new ApiTypeException("No request body found.", e);
            } catch (JSONException e) {
                e.printStackTrace();
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
        System.out.println("Running with "+threadCount+" threads.");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        ArrayList<Future<BulkResult>> futureResults = new ArrayList<Future<BulkResult>>();
        for(BluebirdAddress address : bluebirdAddresses) {
            futureResults.add(executor.submit(new ParallelRequest(address, useShapefile, useGeocoder, geocoder)));
        }

        // Wait for the results to come back
        ArrayList<BulkResult> results = new ArrayList<BulkResult>();
        for (Future<BulkResult> result : futureResults) {
            try {
                results.add(result.get());
            } catch (InterruptedException e) {
                throw new ApiInternalException(e);
            } catch (ExecutionException e) {
                throw new ApiInternalException(e.getCause());
            }
        }

        // then shutdown and return
        executor.shutdown();
        return results;
    }
}
