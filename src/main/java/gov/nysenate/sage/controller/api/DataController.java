package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.api.ApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gov.nysenate.sage.model.result.ResultStatus.API_REQUEST_INVALID;
import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;


public class DataController extends BaseApiController {

    private Logger logger = LoggerFactory.getLogger(DataController.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.debug("Initialized " + this.getClass().getSimpleName());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        Object responseCode = new ApiError(this.getClass(), API_REQUEST_INVALID);

        ApiRequest apiRequest = getApiRequest(request);

        logger.info("=======================================================");
        logger.info("Data Request");
        logger.info(String.format("| Mode: %s | IP: %s", apiRequest.getRequest(), apiRequest.getIpAddress()));
        logger.info("=======================================================");

        if (apiRequest.getRequest().equalsIgnoreCase("sencache")) {
            try {
                ApplicationFactory.initializeCache();
                responseCode = new ApiError(this.getClass(), SUCCESS);
            } catch (Exception e) {
                responseCode = new ApiError(this.getClass(), INTERNAL_ERROR);
            }

        }
        setApiResponse(responseCode, request);
    }
}
