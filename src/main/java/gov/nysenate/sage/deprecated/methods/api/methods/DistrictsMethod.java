package gov.nysenate.sage.deprecated.methods.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiFormatException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiInternalException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.deprecated.districts.Assembly;
import gov.nysenate.sage.deprecated.districts.Congressional;
import gov.nysenate.sage.deprecated.districts.County;
import gov.nysenate.sage.deprecated.districts.DistrictResponse;
import gov.nysenate.sage.deprecated.districts.Election;
import gov.nysenate.sage.deprecated.districts.School;
import gov.nysenate.sage.deprecated.districts.Senate;
import gov.nysenate.sage.deprecated.districts.Town;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.GeoService;
import gov.nysenate.sage.util.Connect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DistrictsMethod extends ApiExecution {
    private final GeoService geoService;
    private final DistrictService districtService;

    public DistrictsMethod() throws Exception {
        geoService = new GeoService();
        districtService = new DistrictService();
    }

    public Object getDistricts(Address address, String service) throws ApiInternalException {
        Connect db = new Connect();
        try {
            DistrictResponse dr = new DistrictResponse();
            dr.validated = false;
            dr.setAddress(address.as_raw());

            if (address.is_geocoded()) {
                dr.geocoded = true;
                dr.setLat(address.latitude);
                dr.setLon(address.longitude);

            } else {
                Result result = geoService.geocode(address, service);
                if (result == null || !result.status_code.equals("0")) {
                    dr.geocoded = false;
                    dr.errors.add(result.messages.get(0));
                } else {
                    dr.geocoded = true;
                    address = result.addresses.get(0);
                    dr.setLat(address.latitude);
                    dr.setLon(address.longitude);
                }
            }

            List<DistrictService.TYPE> types = Arrays.asList(DistrictService.TYPE.ASSEMBLY,DistrictService.TYPE.CONGRESSIONAL,DistrictService.TYPE.SENATE,DistrictService.TYPE.SCHOOL,DistrictService.TYPE.COUNTY,DistrictService.TYPE.TOWN);
            Result result = districtService.assignDistricts(address, types, "geoserver");
            if (result == null || !result.status_code.equals("0")) {
                dr.distassigned = false;
                dr.errors.addAll(result.messages);

            } else {
                dr.setAssembly((Assembly)db.getObject(Assembly.class, "district", "Assembly District "+result.address.assembly_code));
                dr.setCongressional((Congressional)db.getObject(Congressional.class, "district", "Congressional District "+result.address.congressional_code));
                dr.setCounty(new County(result.address.county_name));
                // dr.setElection(new Election("Election District "+result.address.election_code));
                dr.setElection(new Election(""));
                dr.setSenate((Senate)db.getObject(Senate.class, "district", "State Senate District "+result.address.senate_code));
                dr.setSchool(new School("School District "+result.address.school_code));
                dr.setTown(new Town("Town " + result.address.town_code));
            }

            return dr;
        } catch (UnsupportedEncodingException e) {
            throw new ApiInternalException("UTF-8 unsupported uncoding.", e);
        } catch (IOException e) {
            throw new ApiInternalException("Unable to connect to database.", e);
        } catch (Exception e) {
            throw new ApiInternalException("Error retrieving object.", e);
        } finally {
            db.close();
        }
    }

    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiFormatException, ApiTypeException, ApiInternalException {
        String type = more.get(RequestCodes.TYPE.code());
        String service = request.getParameter("service");
        if (service == null) service = "geocoder";

        Address address = null;
        if (type.equals("addr")) {
            if (service.equals("geocoder")) {
                throw new ApiFormatException("Ruby Geocoder cannot accept an unparsed address");
            }
            address = new Address(more.get(RequestCodes.ADDRESS.code()));
        } else if (type.equals("extended")) {
            address = new Address(
                request.getParameter("addr1"),
                request.getParameter("addr2"),
                request.getParameter("city"),
                request.getParameter("state"),
                request.getParameter("zip5"),
                request.getParameter("zip4")
            );
        } else if (type.equals("latlon")) {
            address = new Address();
            String[] coord = more.get(RequestCodes.LATLON.code()).split(",");
            address.setGeocode(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]), 100);
        } else {
            throw new ApiTypeException(type);
        }

        return getDistricts(address, service);
    }
}
