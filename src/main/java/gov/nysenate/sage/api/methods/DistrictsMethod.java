package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.model.districts.Assembly;
import gov.nysenate.sage.model.districts.Congressional;
import gov.nysenate.sage.model.districts.County;
import gov.nysenate.sage.model.districts.DistrictResponse;
import gov.nysenate.sage.model.districts.Election;
import gov.nysenate.sage.model.districts.School;
import gov.nysenate.sage.model.districts.Senate;
import gov.nysenate.sage.model.districts.Town;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.GeoService;
import gov.nysenate.sage.util.Connect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DistrictsMethod extends ApiExecution {
    private final GeoService geoService;
    private final DistrictService districtService;

    public DistrictsMethod() throws Exception {
        geoService = new GeoService();
        districtService = new DistrictService();
    }

    public Object getDistricts(Address address) throws ApiInternalException {
        try {
            if (!address.is_geocoded()) {
                Result result = geoService.geocode(address, "yahoo");
                if (result == null)
                    throw new ApiInternalException();

                if (!result.status_code.equals("0"))
                    throw new ApiInternalException(result.messages.get(0));

                address = result.addresses.get(0);
            }

            Result result = districtService.assignAll(address, "geoserver");
            if (result == null)
                throw new ApiInternalException();

            if (!result.status_code.equals("0"))
                throw new ApiInternalException(result.messages.get(0));

            Connect db = new Connect();
            DistrictResponse dr = new DistrictResponse();
            dr.setAddress(result.address.as_raw());
            dr.setAssembly((Assembly)db.getObject(Assembly.class, "district", "Assembly District "+result.address.assembly_code));
            dr.setCongressional((Congressional)db.getObject(Congressional.class, "district", "Congressional District "+result.address.congressional_code));
            dr.setCounty(new County(result.address.county_name));
            dr.setElection(new Election("Election District "+result.address.election_code));
            dr.setLat(result.address.latitude);
            dr.setLon(result.address.longitude);
            dr.setSenate((Senate)db.getObject(Senate.class, "district", "State Senate District "+result.address.senate_code));
            dr.setSchool(new School("School District "+result.address.school_code));
            dr.setTown(new Town(result.address.town_name));
            return dr;
        } catch (UnsupportedEncodingException e) {
            throw new ApiInternalException("UTF-8 unsupported uncoding.", e);
        } catch (IOException e) {
            throw new ApiInternalException("Unable to connect to database.", e);
        } catch (Exception e) {
            throw new ApiInternalException("Error retrieving object.", e);
        }
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
    /*
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {

		Object ret = null;
		String type = more.get(RequestCodes.TYPE.code());
		String service = request.getParameter("service");

		if(type.equals("addr")) {
			try {
				ret = DistrictServices.getDistrictsFromAddress(more.get(RequestCodes.ADDRESS.code()), service);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("extended")) {
			try {
				ret = DistrictServices.getDistrictsFromAddress(
							request.getParameter("addr2"),
							request.getParameter("city"),
							request.getParameter("state"),
							request.getParameter("zip4"),
							request.getParameter("zip5"),
						service);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("latlon")) {
			try {
				ret = DistrictServices.getDistrictsFromPoint(more.get(RequestCodes.LATLON.code()), service);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else {
			throw new ApiTypeException(type);
		}

		return ret;
	}
	*/
}
