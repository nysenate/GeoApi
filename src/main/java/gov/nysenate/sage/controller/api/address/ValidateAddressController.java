package gov.nysenate.sage.controller.api.address;

import gov.nysenate.sage.controller.api.base.BaseApiController;
import gov.nysenate.sage.model.address.Address;
import static gov.nysenate.sage.controller.api.RequestAttribute.*;

import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.address.AddressServiceProviders;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public final class ValidateAddressController extends BaseApiController
{
    private Logger logger = Logger.getLogger(ValidateAddressController.class);

    @Override
    public void init(ServletConfig config) throws ServletException
    {
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
        /** Get the URI attributes */
        String source = (String) request.getAttribute(PARAM_SOURCE.toString());

        /** Get the URI parameters */
        String service = request.getParameter("service");
        String fallBack = request.getParameter("fallback");

        /** See if user does not want to use default if service lookup fails */
        boolean useFallback = true;
        if (fallBack != null && fallBack.equals("false")){
            useFallback = false;
        }

        Address address = getAddressFromParams(request);

        /** Obtain an AddressService */
        AddressService addressService = AddressServiceProviders.newServiceInstance(service, useFallback);

        AddressResult addressResult;
        if (addressService != null){
            addressResult = addressService.validate(address);
        }
        else {
            addressResult = new AddressResult(this.getClass());
            addressResult.setValidated(false);
            addressResult.addMessage("Error! There is no address validation service registered as " + service);
        }

        response.getWriter().write(FormatUtil.toJsonString(addressResult));
    }
}
