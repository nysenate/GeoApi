package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.district.SqlShapefileDao;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "data")
public class DataController {
    private final CountyDao countyDao;
    private final SqlShapefileDao sqlShapefileDao;

    @Autowired
    public DataController(CountyDao countyDao, SqlShapefileDao sqlShapefileDao) {
        this.countyDao = countyDao;
        this.sqlShapefileDao = sqlShapefileDao;
    }

    /**
     * Cache Shape Files Api
     * -------------------------------
     * Re-cache district maps.
     * Usage:
     * (GET)    /api/v2/data/sencache
     */
    @GetMapping(value = "/sencache")
    public BaseResponse updateCaches() {
        try {
            countyDao.cacheCounties();
            sqlShapefileDao.cacheDistrictMaps();
            return new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        } catch (Exception e) {
            return new ApiError(this.getClass(), INTERNAL_ERROR);
        }
    }
}
