package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.model.ValidateResponse;
import gov.nysenate.sage.model.districts.Assembly;
import gov.nysenate.sage.model.districts.Congressional;
import gov.nysenate.sage.model.districts.County;
import gov.nysenate.sage.model.districts.DistrictResponse;
import gov.nysenate.sage.model.districts.Election;
import gov.nysenate.sage.model.districts.School;
import gov.nysenate.sage.model.districts.Senate;
import gov.nysenate.sage.model.districts.Town;
import gov.nysenate.sage.service.AddressService;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.DistException;
import gov.nysenate.sage.service.GeoService;
import gov.nysenate.sage.service.GeoService.GeoException;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BluebirdMethod extends ApiExecution {
    private final GeoService geoService;
    private final DistrictService districtService;
    private final AddressService addressService;

    public BluebirdMethod() throws Exception {
        geoService = new GeoService();
        districtService = new DistrictService();
        addressService = new AddressService();
    }

    // Included to match the output from before
    static class AddressType {
        Object simple;
        Object extended;
        AddressType(Object simple, Object extended) {
            this.simple = simple;
            this.extended = extended;
        }
    }

    public Object getDistricts(Address address) throws ApiInternalException {
        boolean address_validated = false;
        if (address.is_parsed()) {
            // Use USPS address if we can succesfully validate it
            Result result = addressService.validate(address, "usps");
            if (result!=null && result.status_code.equals("0")) {
                address.addr1 = result.address.addr1;
                address.addr2 = result.address.addr2;
                address.city = result.address.city;
                address.state = result.address.state;
                address.zip5 = result.address.zip5;
                address.zip4 = result.address.zip4;
                address_validated = true;
            }
        }

        if (!address.is_geocoded()) {
            try {
                Result result = geoService.geocode(address, "yahoo");
                if (!result.status_code.equals("0"))
                    throw new ApiInternalException(result.messages.get(0));

                address = result.addresses.get(0);
            } catch (GeoException e) {
                throw new ApiInternalException("Fatal geocoding Error.", e);
            }
        }

        try {
            Result result = districtService.assignAll(address, "geoserver");
            if (result == null)
                throw new ApiInternalException();
            else if (!result.status_code.equals("0"))
                throw new ApiInternalException(result.messages.get(0));

            address = result.address;
        } catch (DistException e) {
            throw new ApiInternalException("Fatal district assignment Error.", e);
        }

        DistrictResponse dr = new DistrictResponse();
        if (address_validated) {
            dr.setAddress(new AddressType(null,new ValidateResponse(address)));
        } else {
            dr.setAddress(new AddressType(address.as_raw(),null));
        }
        dr.setAssembly(new Assembly(address.assembly_code+""));
        dr.setCongressional(new Congressional(address.congressional_code+""));
        dr.setCounty(new County(address.county_code+""));
        dr.setElection(new Election(address.election_code+""));
        dr.setLat(address.latitude);
        dr.setLon(address.longitude);
        dr.setSenate(new Senate(address.senate_code+""));
        dr.setSchool(new School(address.school_code+""));
        dr.setTown(new Town(address.town_code));
        return dr;
    }

    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
        String type = more.get(RequestCodes.TYPE.code());
        //String service = request.getParameter("service");

        Address address = null;
        if (type.equals("addr")) {
            address = new Address(more.get(RequestCodes.ADDRESS.code()));
        } else if (type.equals("extended")) {
            address = new Address(
                request.getParameter("addr1"),
                request.getParameter("addr2"),
                request.getParameter("city"),
                request.getParameter("state"),
                request.getParameter("zip4"),
                request.getParameter("zip5")
            );
        } else if (type.equals("latlon")) {
            address = new Address();
            String[] coord = more.get(RequestCodes.LATLON.code()).split(",");
            address.setGeocode(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]), 100);
        } else {
            throw new ApiTypeException(type);
        }

        return getDistricts(address);
    }
}
