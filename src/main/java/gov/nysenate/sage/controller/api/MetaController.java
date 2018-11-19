package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.meta.MetaInfoResponse;
import gov.nysenate.sage.client.response.meta.MetaProviderResponse;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.ResultStatus;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "meta")
public class MetaController
{
    private static Logger logger = LoggerFactory.getLogger(MetaController.class);
    private static MavenXpp3Reader pomReader = new MavenXpp3Reader();
    private static Model pomModel = null;
    private GeocodeServiceProvider geocodeServiceProvider;

    @Autowired
    public MetaController(GeocodeServiceProvider geocodeServiceProvider) {
        this.geocodeServiceProvider = geocodeServiceProvider;
        try {
            pomModel = pomReader.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("pom.xml"));
        }
        catch (IOException ex) {
            logger.error("Failed to read pom.xml.", ex);
        }
        catch (XmlPullParserException ex) {
            logger.error("Failed to parse pom.xml.", ex);
        }
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public void metaInfo(HttpServletRequest request, HttpServletResponse response) {
        Object metaResponse;
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        logMetaRequest(apiRequest);

        if (pomModel != null) {
            metaResponse = new MetaInfoResponse(pomModel);
        }
        else {
            logger.error("POM file is missing from the WEB-INF/classes folder!");
            metaResponse = new ApiError(MetaController.class, ResultStatus.CONFIG_FILE_MISSING);
        }

        setApiResponse(metaResponse,request);
    }

    @RequestMapping(value = "/provider", method = RequestMethod.GET)
    public void metaProvider(HttpServletRequest request, HttpServletResponse response) {
        Object metaResponse;
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        logMetaRequest(apiRequest);

        metaResponse = new MetaProviderResponse(geocodeServiceProvider.getActiveGeoProviders());

        setApiResponse(metaResponse,request);
    }

    private void logMetaRequest(ApiRequest apiRequest) {
        logger.info("Making a meta request with request: " + apiRequest.getRequest());
    }
}
