package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.model.api.ApiUserDao;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileDao;
import gov.nysenate.sage.dao.stats.api.SqlApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.deployment.SqlDeploymentStatsDao;
import gov.nysenate.sage.dao.stats.geocode.SqlGeocodeStatsDao;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.stats.DeploymentStats;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

@RestController
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/api")
public class AdminApiController extends BaseAdminApiController {
    private final SqlApiUsageStatsDao sqlApiUsageStatsDao;
    private final SqlDeploymentStatsDao sqlDeploymentStatsDao;
    private final ApiUserDao apiUserDao;
    private final SqlGeocodeStatsDao sqlGeocodeStatsDao;
    private final ShapefileDao shapefileDao;

    @Autowired
    public AdminApiController(SqlApiUsageStatsDao sqlApiUsageStatsDao, SqlDeploymentStatsDao sqlDeploymentStatsDao,
                              ApiUserDao apiUserDao, SqlGeocodeStatsDao sqlGeocodeStatsDao,
                              ShapefileDao shapefileDao,
                              AdminUserAuth adminUserAuth, ApiUserAuth apiUserAuth) {
        super(adminUserAuth,  apiUserAuth);
        this.sqlApiUsageStatsDao = sqlApiUsageStatsDao;
        this.sqlDeploymentStatsDao = sqlDeploymentStatsDao;
        this.apiUserDao = apiUserDao;
        this.sqlGeocodeStatsDao = sqlGeocodeStatsDao;
        this.shapefileDao = shapefileDao;
    }

    /**
     * Current Api Users Api
     * ---------------------
     * Returns the current api users
     * Usage:
     * (GET)    /admin/api/currentApiUsers
     *
     */
    @GetMapping(value = "/currentApiUsers")
    public Object currentApiUsers(HttpServletRequest request,
                                @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                @RequestParam(required = false, defaultValue = "") String key) {
        if (authenticate(request, username, password, key)) {
            return apiUserDao.getApiUsers();
        }
        return invalidAuthResponse;
    }

    /**
     * Api Usage Api
     * ---------------------
     * Returns api user usage request stats
     * Usage:
     * (GET)    /admin/api/usage
     *
     */
    @GetMapping(value = "/usage")
    public Object usage(HttpServletRequest request,
                      @RequestParam(required = false, defaultValue = "defaultUser") String username,
                      @RequestParam(required = false, defaultValue = "defaultPass") String password,
                      @RequestParam(required = false, defaultValue = "") String key) {
        if (authenticate(request, username, password, key)) {
            return sqlApiUsageStatsDao.getApiUsageStats(getBeginTimestamp(request), getEndTimestamp(request));
        }
        return invalidAuthResponse;

    }

    /**
     * Geocode Usage Api
     * ---------------------
     * Returns geocode usage stats
     * Usage:
     * (GET)    /admin/api/geocodeUsage
     *
     */
    @GetMapping(value = "/geocodeUsage")
    public Object geocodeUsage(HttpServletRequest request,
                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                             @RequestParam(required = false, defaultValue = "") String key) {
        if (authenticate(request, username, password, key)) {
            return sqlGeocodeStatsDao.getGeocodeStats(getBeginTimestamp(request), getEndTimestamp(request));
        }
        return invalidAuthResponse;
    }

    /**
     * Deployment Stats Api
     * ---------------------
     * Returns deployment stats
     * Usage:
     * (GET)    /admin/api/deployment
     *
     */
    @GetMapping(value = "/deployment")
    public Object deployment(HttpServletRequest request,
                           @RequestParam(required = false, defaultValue = "defaultUser") String username,
                           @RequestParam(required = false, defaultValue = "defaultPass") String password,
                           @RequestParam(required = false, defaultValue = "") String key) {
        if (authenticate(request, username, password, key)) {
            return new DeploymentStats(sqlDeploymentStatsDao.getDeploymentStats());
        }
        return invalidAuthResponse;
    }

    @GetMapping(value = "/cleanMaps")
    public Object cleanMaps(HttpServletRequest request,
                            @RequestParam(required = false, defaultValue = "defaultUser") String username,
                            @RequestParam(required = false, defaultValue = "defaultPass") String password,
                            @RequestParam(required = false, defaultValue = "") String key,
                            @RequestParam String type) {
        if (authenticate(request, username, password, key)) {
            DistrictType districtType = DistrictType.valueOf(type.toUpperCase());
            Boolean validGeometry = shapefileDao.cleanMaps(districtType);
            if (validGeometry == null) {
                return new ApiError(EMPTY_GEOMETRY_TABLE);
            }
            return new GenericResponse(validGeometry, validGeometry ? "Cleaned maps" :
                    "Cleaned maps, but some geometries are invalid. Manual fixes are required.");
        }
        return invalidAuthResponse;
    }

    /**
     * Cache Shape Files Api
     * -------------------------------
     * Re-cache district maps.
     * Usage:
     * (GET)    /api/v2/data/recache
     */
    @GetMapping(value = "/recache")
    public BaseResponse updateCaches(HttpServletRequest request,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                     @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                     @RequestParam(required = false, defaultValue = "") String key) {
        if (!authenticate(request, username, password, key)) {
            return invalidAuthResponse;
        }
        try {
            shapefileDao.cacheDistrictGeometryData();
            return new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        } catch (Exception e) {
            return new ApiError(this.getClass(), INTERNAL_ERROR);
        }
    }
}
