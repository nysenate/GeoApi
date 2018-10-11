package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.meta.MetaInfoResponse;
import gov.nysenate.sage.client.response.meta.MetaProviderResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.ResultStatus;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.model.result.ResultStatus.SERVICE_NOT_SUPPORTED;

public class MetaController extends BaseApiController
{
    private static Logger logger = LoggerFactory.getLogger(MetaController.class);
    private static MavenXpp3Reader pomReader = new MavenXpp3Reader();
    private static Model pomModel = null;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
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

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object metaResponse;
        ApiRequest apiRequest = getApiRequest(request);

        logger.info("Making a meta request with request: " + apiRequest.getRequest());

        switch (apiRequest.getRequest()) {
            case "info" : {
                if (pomModel != null) {
                    metaResponse = new MetaInfoResponse(pomModel);
                }
                else {
                    logger.error("POM file is missing from the WEB-INF/classes folder!");
                    metaResponse = new ApiError(MetaController.class, ResultStatus.CONFIG_FILE_MISSING);
                }
                break;
            }
            case "provider" : {
                metaResponse = new MetaProviderResponse(ApplicationFactory.getActiveGeoProviders());
                break;
            }
            default : {
                metaResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
            }
        }

        setApiResponse(metaResponse, request);
    }
}
