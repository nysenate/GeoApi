package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.controller.api.filter.ApiFilter;
import gov.nysenate.sage.dao.provider.district.SqlDistrictShapefileDao;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.API_REQUEST_INVALID;
import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "data", produces = APPLICATION_JSON_VALUE)
public class DataController {

    private Logger logger = LoggerFactory.getLogger(DataController.class);

    private SqlDistrictShapefileDao sqlDistrictShapefileDao;

    private ApiFilter apiFilter;

    @Autowired
    public DataController(SqlDistrictShapefileDao sqlDistrictShapefileDao, ApiFilter apiFilter) {
        this.sqlDistrictShapefileDao = sqlDistrictShapefileDao;
        this.apiFilter = apiFilter;
    }

    /**
     * Cache Shape Files Api
     * -------------------------------
     *
     * Batch city state validation with USPS
     *
     * Usage:
     * (GET)    /api/v2/data/sencache
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/sencache", method = RequestMethod.GET)
    public Object updateSencache(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object responseCode = new ApiError(this.getClass(), API_REQUEST_INVALID);

        ApiRequest apiRequest = apiFilter.getOrCreateApiRequest(request);

        logger.info("=======================================================");
        logger.info("Data Request");
        logger.info(String.format("| Mode: %s | IP: %s", apiRequest.getRequest(), apiRequest.getIpAddress()));
        logger.info("=======================================================");

        if (apiRequest.getRequest().equalsIgnoreCase("sencache")) {
            try {
                responseCode = new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
                sqlDistrictShapefileDao.cacheDistrictMaps();
            } catch (Exception e) {
                responseCode = new ApiError(this.getClass(), INTERNAL_ERROR);
            }

        }
//        setApiResponse(responseCode, request);
        return responseCode;
    }
}
