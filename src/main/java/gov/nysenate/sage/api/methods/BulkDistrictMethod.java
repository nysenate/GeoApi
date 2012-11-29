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

        address.street = (street==null ? "" : street.toUpperCase().trim());
        address.town = (town==null ? "" : town.toUpperCase().trim());
        address.state = (state==null ? "" : state.toUpperCase().trim());
        address.zip5 = (zip5==null ? 0 : Integer.parseInt(zip5.trim()));
        address.apt_num = (apt_num==null ? 0 : Integer.parseInt(apt_num.trim()));
        address.bldg_num = (bldg_num==null ? 0 : Integer.parseInt(bldg_num.trim()));
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

        BluebirdAddress address = new BluebirdAddress(id);
        address.street = (street.equals("null") ? "" : street.toUpperCase());
        address.town = (town.equals("null") ? "" : town.toUpperCase());
        address.state = (state.equals("null") ? "" : state.toUpperCase());
        address.zip5 = (zip5.equals("null") || zip5.equals("") ? 0 : Integer.parseInt(zip5));
        address.apt_num = (apt_num.equals("null") || apt_num.equals("") ? 0 : Integer.parseInt(apt_num));
        address.bldg_num = (bldg_num.equals("null") || bldg_num.equals("") ? 0 : Integer.parseInt(bldg_num));
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
                addresses.add(null);
            }
        }
        return addresses;
    }

    @SuppressWarnings("unused")
    private static class BulkResult {
        public static enum STATUS { STREETNUM, STREETNAME, ZIPCODE, SHAPEFILE, INVALID, NOMATCH };

        public STATUS status_code;
        public String message;
        public String address_id;
        public int county_code;
        public int assembly_code;
        public int congressional_code;
        public int senate_code;
        public int election_code;
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
            this.latitude = address.latitude;
            this.longitude = address.longitude;
            this.geo_accuracy = address.geo_accuracy;
        }
    }

    private BOEAddressRange SAGE2Bluebird(Address address) {
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
        return range;
    }
    private Address Bluebird2SAGE(BluebirdAddress address) {
        if (address == null) {
            return null;
        }

        return new Address(
            address.street,
            address.town,
            address.state,
            (address.zip5 != 0) ? String.valueOf(address.zip5) : ""
        );
    }

    private final boolean SHAPEFILE = false;
    private BulkResult shapefileLookup(BluebirdAddress bluebirdAddress) throws GeoException, DistException{
        if (!SHAPEFILE)
            return new BulkResult(BulkResult.STATUS.NOMATCH, "Shapefiles are turned off", bluebirdAddress, new BOEAddressRange() );

        if (bluebirdAddress == null) {
            return null;
        }

        Address address = Bluebird2SAGE(bluebirdAddress);
        Result geoResult = geoService.geocode(address, "rubygeocoder");
        if (!geoResult.status_code.equals("0")) {
            throw new GeoException("Bad status code "+geoResult.status_code);
        }
        if (geoResult.addresses.size()==0) {
            System.out.println(address);
            System.out.println(geoResult.status_code+" - "+geoResult.messages);
        }
        Result distResult = districtService.assignDistricts(geoResult.addresses.get(0), Arrays.asList(DistrictService.TYPE.ASSEMBLY,DistrictService.TYPE.CONGRESSIONAL,DistrictService.TYPE.SENATE,DistrictService.TYPE.COUNTY), "geoserver");
        // Result distResult = districtService.assignDistricts(geoResult.addresses.get(0), Arrays.asList(DistrictService.TYPE.SENATE), "geoserver");
        if (!distResult.status_code.equals("0")) {
            throw new DistException("Bad status code "+distResult.status_code);
        }

        Address resultAddress = distResult.address;
        bluebirdAddress.latitude = resultAddress.latitude;
        bluebirdAddress.longitude = resultAddress.longitude;
        bluebirdAddress.geo_accuracy = resultAddress.geocode_quality;
        BOEAddressRange addressRange = SAGE2Bluebird(distResult.address);
        return new BulkResult(BulkResult.STATUS.SHAPEFILE, "SHAPEFILE MATCH for"+resultAddress, bluebirdAddress, addressRange );
    }

    public class ParallelRequest implements Callable<BulkResult> {

        private final BluebirdAddress address;
        public ParallelRequest(BluebirdAddress address) {
            this.address = address;
        }

        @Override
        public BulkResult call() throws SQLException {
            if (address!=null) {
                List<BOEAddressRange> matches = streetData.getRanges(address,true);
                try {
                    if (matches.size()==1) {
                        return new BulkResult(BulkResult.STATUS.STREETNUM, "EXACT MATCH", address, matches.get(0));

                    } else if (matches.size()==0) {
                        // If at first you don't succeed, try again without the building number
                        matches = streetData.getRanges(address,false);

                        if (matches.size()==0) {
                            return shapefileLookup(address);

                        } else {
                            // Consolidate these results to "range fill"
                            BOEAddressRange consolidated = AddressUtils.consolidateRanges(matches);
                            if (consolidated != null) {
                                return new BulkResult(BulkResult.STATUS.STREETNAME, "CONSOLIDATED RANGEFILL", address, consolidated);
                            } else {
                                return shapefileLookup(address);
                            }
                        }

                    } else {
                        BOEAddressRange consolidated = AddressUtils.consolidateRanges(matches);
                        if (consolidated != null) {
                            return new BulkResult(BulkResult.STATUS.STREETNAME, "CONSOLIDATED MULTIMATCH", address, consolidated);
                        } else {
                            return shapefileLookup(address);
                        }
                    }
                } catch (GeoException e) {
                    e.printStackTrace();
                    return new BulkResult(BulkResult.STATUS.NOMATCH, "GeoException for: "+address.toString(), address, new BOEAddressRange() );
                } catch (DistException e) {
                    e.printStackTrace();
                    return new BulkResult(BulkResult.STATUS.NOMATCH, "DistException for: "+address.toString(), address, new BOEAddressRange() );
                }

            } else {
                return new BulkResult(BulkResult.STATUS.INVALID,"Invalid JSON Entry",new BluebirdAddress("-1"),new BOEAddressRange());
            }
        }
    }
    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
        String type = more.get(RequestCodes.TYPE.code());
        ArrayList<BulkResult> results = new ArrayList<BulkResult>();
        ArrayList<BluebirdAddress> bluebirdAddresses;
        try {
            if(type.equals("url")) {
                bluebirdAddresses = new ArrayList<BluebirdAddress>(Arrays.asList(requestToAddress(request)));
            } else if (type.equals("body")) {
                String json = IOUtils.toString(request.getInputStream(),"UTF-8");
                bluebirdAddresses = readAddresses(json);
            } else {
                throw new ApiTypeException(type);
            }

            ExecutorService executor = Executors.newFixedThreadPool(3);
            ArrayList<Future<BulkResult>> futureResults = new ArrayList<Future<BulkResult>>();

            for(BluebirdAddress address : bluebirdAddresses) {
                futureResults.add(executor.submit(new ParallelRequest(address)));
            }

            for (Future<BulkResult> result : futureResults) {
                try {
                    results.add(result.get());
                } catch (InterruptedException e) {
                    throw new ApiInternalException(e);
                } catch (ExecutionException e) {
                    throw new ApiInternalException(e.getCause());
                }
            }

            executor.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiTypeException("No request body found.", e);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ApiTypeException("Invalid JSON recieved.",e);
        }

        return results;
    }

}
