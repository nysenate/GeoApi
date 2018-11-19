package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.API_REQUEST_INVALID;
import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "data")
public class DataController {

    private Logger logger = LoggerFactory.getLogger(DataController.class);

    @RequestMapping(value = "/sencache", method = RequestMethod.GET)
    public void updateSencache(HttpServletRequest request) {
        Object responseCode = new ApiError(this.getClass(), API_REQUEST_INVALID);

        ApiRequest apiRequest = getApiRequest(request);

        logger.info("=======================================================");
        logger.info("Data Request");
        logger.info(String.format("| Mode: %s | IP: %s", apiRequest.getRequest(), apiRequest.getIpAddress()));
        logger.info("=======================================================");

        if (apiRequest.getRequest().equalsIgnoreCase("sencache")) {
            try {
                responseCode = new ApiError(this.getClass(), SUCCESS);
            } catch (Exception e) {
                responseCode = new ApiError(this.getClass(), INTERNAL_ERROR);
            }

        }
        setApiResponse(responseCode, request);
    }
}
