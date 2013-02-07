package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.api.exceptions.ApiException;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.ValidateResponse;
import gov.nysenate.sage.service.AddressService;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CityStateLookupMethod extends ApiExecution
{
    AddressService addressService;

    public CityStateLookupMethod() throws Exception
    {
        addressService = new AddressService();
    }


    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiException
    {
        String type = more.get(RequestCodes.TYPE.code());
        Address address;

        System.out.println(type);
        if (type.equals("extended")) {
            address = new Address("","","","",request.getParameter("zip5"),"");
        }
        else if (type.equals("zip")) {
            address = new Address("","","","",more.get(RequestCodes.ZIP.code()),"");
        }
        else {
            throw new ApiTypeException(type);
        }

        Result result = addressService.lookupCityState(address, "usps");

        if (result == null) {
            throw new ApiInternalException();
        }
        else if (result.getStatus().equals("0")) {
            return new ValidateResponse(result.getAddress());
        }
        else {
            String msg = "";
            for (String m : result.getMessages()) {
                msg += "\n"+m;
            }
            return new ErrorResponse(msg.toString());
        }
    }
}
