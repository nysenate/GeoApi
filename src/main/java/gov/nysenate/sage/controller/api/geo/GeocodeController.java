package gov.nysenate.sage.controller.api.geo;

import gov.nysenate.sage.controller.api.BaseApiController;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.ServiceProviders;
import gov.nysenate.sage.service.geo.GeocodeService;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.controller.api.RequestAttribute.PARAM_SOURCE;
import static gov.nysenate.sage.controller.api.RequestAttribute.REQUEST_TYPE;

/**
 *
 */
public class GeocodeController extends BaseApiController
{
    private Logger logger = Logger.getLogger(GeocodeController.class);
    private ServiceProviders<GeocodeService> geocodeServiceProviders;

    public static String geoCodeRequest = "geocode";
    public static String revGeoCodeRequest = "revgeo";

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        geocodeServiceProviders = ApplicationFactory.getGeoCodeServiceProviders();
        logger.debug("Initialized " + this.getClass().getSimpleName());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        GeocodeResult geocodeResult = new GeocodeResult();

        /** Get the URI attributes */
        String source = (String) request.getAttribute(PARAM_SOURCE.toString());
        String requestType = (String) request.getAttribute(REQUEST_TYPE.toString());

        /** Get the URI parameters */
        String service = request.getParameter("service");
        String fallBack = request.getParameter("fallback");
        boolean useFallback = (fallBack != null && fallBack.equals("true")) ? true : false;

        /** Obtain an GeoCodeService */
        GeocodeService geocodeService = geocodeServiceProviders.newServiceInstance(service, useFallback);

        if (geocodeService != null){

            if (requestType.equalsIgnoreCase(geoCodeRequest)){
                Address address = getAddressFromParams(request);
                if (address != null){
                    geocodeResult = geocodeService.geocode(address);
                }
            }
            else if (requestType.equalsIgnoreCase(revGeoCodeRequest)){
                Point point = getPointFromParams(request);
                logger.debug(point);
                if (point != null){
                    geocodeResult = geocodeService.reverseGeocode(point);
                }
            }
        }

        setResponse(geocodeResult.toMap(), request);
    }


}
