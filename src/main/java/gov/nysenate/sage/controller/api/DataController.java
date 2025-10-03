package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileDao;
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
    private final ShapefileDao shapefileDao;

    @Autowired
    public DataController(ShapefileDao shapefileDao) {
        this.shapefileDao = shapefileDao;
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
            shapefileDao.cacheDistrictMaps();
            return new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        } catch (Exception e) {
            return new ApiError(this.getClass(), INTERNAL_ERROR);
        }
    }
}
