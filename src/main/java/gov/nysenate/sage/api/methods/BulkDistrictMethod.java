package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.DistrictLookup;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        public BluebirdAddress(String id) { this.id = id; }
    }

    public final DistrictLookup streetData;

    public BulkDistrictMethod() throws Exception {
        Resource config = new Resource();

        MysqlDataSource db = new MysqlDataSource();
        db.setServerName(config.fetch("street_db.host"));
        db.setUser(config.fetch("street_db.user"));
        db.setPassword(config.fetch("street_db.pass"));
        db.setDatabaseName(config.fetch("street_db.name"));

        streetData =  new DistrictLookup(db);
    }


    public static void main(String[] args) throws Exception {
        BulkDistrictMethod bdMethod = new BulkDistrictMethod();

        String json = "{\"4\":{\"street\":\"West 187th Street\",\"town\":\"New York\",\"state\":\"NY\",\"zip5\":\"10033\",\"apt\":null,\"building\":\"650\"}}";
        ArrayList<BluebirdAddress> addresses = bdMethod.readAddresses(json);
        for (BluebirdAddress address : addresses) {
            for(BOEAddressRange range : bdMethod.streetData.getRanges(address)) {
                System.out.println(range.id);
            }
        }
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
    private static class Result {
        public static enum STATUS { MATCH, INVALID, NOMATCH, MULTIMATCH };

        public STATUS status_code;
        public String message;
        public String address_id;
        public int countyCode;
        public int assemblyCode;
        public int congressionalCode;
        public int senateCode;
        public int electionCode;
        public String address;

        public Result(STATUS status, String message, BluebirdAddress address, BOEAddressRange match) {
            this.address_id = address.id;
            this.status_code = status;
            this.message = message;
            this.address = address.toString();
            this.electionCode = match.electionCode;
            this.senateCode = match.senateCode;
            this.congressionalCode = match.congressionalCode;
            this.assemblyCode = match.assemblyCode;
            this.countyCode = match.countyCode;
        }
    }

    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
        String type = more.get(RequestCodes.TYPE.code());
        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<BOEAddressRange> noMatches = new ArrayList<BOEAddressRange>();
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

            for(BluebirdAddress address : bluebirdAddresses) {
                if (address!=null) {
                    List<BOEAddressRange> matches = streetData.getRanges(address,true);

                    if (matches.size()==1) {
                        results.add(new Result(Result.STATUS.MATCH,"EXACT MATCH",address,matches.get(0)));
                        continue;

                    } else if (matches.size()==0) {
                        // If at first you don't succeed, try again without the building number
                        matches = streetData.getRanges(address,false);

                        if (matches.size()==0) {
                            results.add(new Result(Result.STATUS.NOMATCH,"Street + Zip not Found for: "+address.toString(),address, new BOEAddressRange()));
                        } else {
                            // Consolidate these results to "range fill"
                            BOEAddressRange consolidated = AddressUtils.consolidateRanges(matches);
                            if (consolidated != null) {
                                results.add(new Result(Result.STATUS.MATCH,"CONSOLIDATED RANGEFILL",address, consolidated));
                            } else {
                                results.add(new Result(Result.STATUS.NOMATCH, "RANGEFILL failed for: "+address.toString(), address, new BOEAddressRange()));
                            }
                        }
                    } else {
                        BOEAddressRange consolidated = AddressUtils.consolidateRanges(matches);
                        if (consolidated != null) {
                            results.add(new Result(Result.STATUS.MATCH,"CONSOLIDATED MULTIMATCH",address, consolidated));
                        } else {
                            results.add(new Result(Result.STATUS.MULTIMATCH, matches.size()+" inconsistent matches found for: "+address.toString(), address, new BOEAddressRange()));
                        }
                    }

                } else {
                    results.add(new Result(Result.STATUS.INVALID,"Invalid JSON Entry",new BluebirdAddress("-1"),new BOEAddressRange()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiTypeException("No request body found.", e);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ApiTypeException("Invalid JSON recieved.",e);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiInternalException("Internal DB Error", e);
        }

        return results;
    }

}
