package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import static gov.nysenate.sage.controller.api.RequestAttribute.*;

import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.ServiceProviders;
import gov.nysenate.sage.service.address.AddressService;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Address API controller handles the various AddressService requests including
 *  - Address Validation
 *  - City State Lookup
 *  - ZipCode Lookup
 */
public final class AddressController extends BaseApiController
{
    private Logger logger = Logger.getLogger(AddressController.class);
    private ServiceProviders<AddressService> addressServiceProviders;

    public static String validateRequest = "validate";
    public static String cityStateRequest = "citystate";
    public static String zipcodeRequest = "zipcode";

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        addressServiceProviders = ApplicationFactory.getAddressServiceProviders();
        logger.debug("Initialized " + this.getClass().getSimpleName());
    }

    /** Proxies to doGet() */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        this.doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        AddressResult addressResult;

        /** Get the URI attributes */
        String source = (String) request.getAttribute(PARAM_SOURCE.toString());
        String requestType = (String) request.getAttribute(REQUEST_TYPE.toString());

        /** Get the URI parameters */
        String service = request.getParameter("service");
        String fallBack = request.getParameter("fallback");
        boolean useFallback = (fallBack != null && fallBack.equals("true")) ? true : false;

        /** Obtain an AddressService */
        AddressService addressService = addressServiceProviders.newServiceInstance(service, useFallback);

        if (addressService != null){

            /** Retrieve address from query parameters */
            Address address = getAddressFromParams(request);

            if (requestType.equalsIgnoreCase(validateRequest)){
                addressResult = addressService.validate(address);
            }
            else if (requestType.equalsIgnoreCase(cityStateRequest)){
                addressResult = addressService.lookupCityState(address);
            }
            else if (requestType.equalsIgnoreCase(zipcodeRequest)){
                addressResult = addressService.lookupZipCode(address);
            }
            else {
                addressResult = new AddressResult(this.getClass());
                addressResult.setValidated(false);
                addressResult.addMessage("Error! " + requestType + " is not a valid request type.");
            }
        }
        else {
            addressResult = new AddressResult(this.getClass());
            addressResult.setValidated(false);
            addressResult.addMessage("Error! There is no address validation service registered as " + service);
        }

        /** Set response */

    }
}
