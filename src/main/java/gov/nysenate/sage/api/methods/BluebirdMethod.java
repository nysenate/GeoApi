package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.deprecated.districts.Assembly;
import gov.nysenate.sage.deprecated.districts.Congressional;
import gov.nysenate.sage.deprecated.districts.County;
import gov.nysenate.sage.deprecated.districts.DistrictResponse;
import gov.nysenate.sage.deprecated.districts.Election;
import gov.nysenate.sage.deprecated.districts.School;
import gov.nysenate.sage.deprecated.districts.Senate;
import gov.nysenate.sage.deprecated.districts.Town;
import gov.nysenate.sage.service.AddressService;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.DistException;
import gov.nysenate.sage.service.GeoService;
import gov.nysenate.sage.service.GeoService.GeoException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class BluebirdMethod extends ApiExecution
{
    private final GeoService geoService;
    private final DistrictService districtService;
    private final AddressService addressService;


    public BluebirdMethod() throws Exception
    {
        geoService = new GeoService();
        districtService = new DistrictService();
        addressService = new AddressService();
    }


    // Included to match the output from before
    static class AddressType
    {
        Object simple;
        Object extended;
        AddressType(Object simple, Object extended) {
            this.simple = simple;
            this.extended = extended;
        }
    }


    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException
    {
        String type = more.get(RequestCodes.TYPE.code());

        Address address = null;
        if (type.equals("addr")) {
            address = new Address(more.get(RequestCodes.ADDRESS.code()));
        }
        else if (type.equals("extended")) {
            address = new Address(
                request.getParameter("addr1"),
                request.getParameter("addr2"),
                request.getParameter("city"),
                request.getParameter("state"),
                request.getParameter("zip5"),
                request.getParameter("zip4")
            );
        }
        else if (type.equals("latlon")) {
            address = new Address();
            String[] coord = more.get(RequestCodes.LATLON.code()).split(",");
            address.setGeocode(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]), 100);
        }
        else {
            throw new ApiTypeException(type);
        }

        return getDistricts(address);
    } // execute()


    private Object getDistricts(Address address) throws ApiInternalException
    {
        DistrictResponse dr = new DistrictResponse();
        if (address==null) {
            return dr;
        }

        dr.validated = false;
        if (address.is_parsed()) {
            // Use USPS address if we can successfully validate it
            Result result = addressService.validate(address, "usps");
            if (result.getStatus().equals("0")) {
                Address resultAddress = result.getAddress();
                dr.setAddress(resultAddress.as_raw());
                dr.address1 = address.addr1 = resultAddress.addr1;
                dr.address2 = address.addr2 = resultAddress.addr2;
                dr.city = address.city = resultAddress.city;
                dr.state = address.state = resultAddress.state;
                dr.zip5 = address.zip5 = resultAddress.zip5;
                dr.zip4 = address.zip4 = resultAddress.zip4;
                dr.validated = true;
                address = resultAddress;
            } else {
                dr.errors.addAll(result.getMessages());
                dr.setAddress(address.as_raw());
            }
        } else {
            dr.setAddress(address.as_raw());
        }

        dr.geocoded = false;
        if (!address.is_geocoded()) {
            try {
                Result result = geoService.geocode(address, "yahoo");
                if (!result.getStatus().equals("0")) {
                    dr.errors.addAll(result.getMessages());
                }
                else {
                    dr.geocoded = true;
                    address = result.getFirstAddress();
                    dr.setLat(address.latitude);
                    dr.setLon(address.longitude);
                    dr.geocode_quality = address.geocode_quality;
                }

            } catch (GeoException e) {
                dr.errors.add("Fatal geocoding Error: "+e.getMessage());
            }
        }


        dr.distassigned = false;
        try {
            List<DistrictService.TYPE> types = Arrays.asList(DistrictService.TYPE.ASSEMBLY,DistrictService.TYPE.CONGRESSIONAL,DistrictService.TYPE.SENATE,DistrictService.TYPE.SCHOOL,DistrictService.TYPE.COUNTY,DistrictService.TYPE.TOWN);
            Result result = districtService.assignDistricts(address, types, "geoserver");

            if (!result.getStatus().equals("0")) {
                dr.errors.addAll(result.getMessages());
            } else {
                dr.distassigned = true;
                address = result.getAddress();

                dr.setAssembly(new Assembly(address.assembly_code+""));
                dr.setCongressional(new Congressional(address.congressional_code+""));
                dr.setCounty(new County(null,address.county_code+""));
                //dr.setElection(new Election(address.election_code+""));
                dr.setElection(new Election(""));
                dr.setSenate(new Senate(address.senate_code+""));
                dr.setSchool(new School(address.school_code+""));
                dr.setTown(new Town(address.town_code));
            }
        }
        catch (DistException e) {
            dr.errors.add("Fatal district assignment Error."+e.getMessage());
        }

        return dr;
    } // getDistricts()
}
