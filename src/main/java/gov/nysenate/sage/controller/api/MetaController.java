package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.meta.MetaProviderResponse;
import gov.nysenate.sage.model.api.ApiRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "meta")
public class MetaController
{
    private static Logger logger = LoggerFactory.getLogger(MetaController.class);
    private GeocodeServiceProvider geocodeServiceProvider;

    @Autowired
    public MetaController(GeocodeServiceProvider geocodeServiceProvider) {
        this.geocodeServiceProvider = geocodeServiceProvider;
    }

    /**
     * Meta Data Api
     * ---------------------------
     *
     * returns a map containing the current active geocoders
     *
     * Usage:
     * (GET)    /api/v2/meta/provider
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/provider", method = RequestMethod.GET)
    public void metaProvider(HttpServletRequest request, HttpServletResponse response) {
        Object metaResponse;
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        logMetaRequest(apiRequest);
        metaResponse = new MetaProviderResponse(geocodeServiceProvider.getActiveGeocoderClassMap());
        setApiResponse(metaResponse,request);
    }

    private void logMetaRequest(ApiRequest apiRequest) {
        logger.info("Making a meta request with request: " + apiRequest.getRequest());
    }
}
