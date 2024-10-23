package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.provider.district.SqlDistrictShapefileDao;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "data")
public class DataController {
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);
    private final SqlDistrictShapefileDao sqlDistrictShapefileDao;

    @Autowired
    public DataController(SqlDistrictShapefileDao sqlDistrictShapefileDao) {
        this.sqlDistrictShapefileDao = sqlDistrictShapefileDao;
    }

    /**
     * Cache Shape Files Api
     * -------------------------------
     * Batch city state validation with USPS
     * Usage:
     * (GET)    /api/v2/data/sencache
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/sencache", method = RequestMethod.GET)
    public void updateSencache(HttpServletRequest request, HttpServletResponse response) {
        Object responseCode = new ApiError(this.getClass(), API_REQUEST_INVALID);

        ApiRequest apiRequest = getApiRequest(request);

        logger.info("=======================================================");
        logger.info("Data Request");
        logger.info("| Mode: {} | IP: {}", apiRequest.getRequest(), apiRequest.getIpAddress());
        logger.info("=======================================================");

        if (apiRequest.getRequest().equalsIgnoreCase("sencache")) {
            try {
                responseCode = new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
                sqlDistrictShapefileDao.cacheDistrictMaps();
            } catch (Exception e) {
                responseCode = new ApiError(this.getClass(), INTERNAL_ERROR);
            }

        }
        setApiResponse(responseCode, request);
    }
}
