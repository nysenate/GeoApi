package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.client.view.job.JobProcessStatusView;
import gov.nysenate.sage.dao.model.api.ApiUserDao;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileDao;
import gov.nysenate.sage.dao.stats.api.SqlApiUsageStatsDao;
import gov.nysenate.sage.dao.stats.deployment.SqlDeploymentStatsDao;
import gov.nysenate.sage.dao.stats.geocode.SqlGeocodeStatsDao;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.stats.DeploymentStats;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

@RestController
// TODO: change to use common method in DataGenController
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/api")
public class AdminApiController {
    private final SqlApiUsageStatsDao sqlApiUsageStatsDao;
    private final SqlDeploymentStatsDao sqlDeploymentStatsDao;
    private final ApiUserDao apiUserDao;
    private final SqlGeocodeStatsDao sqlGeocodeStatsDao;
    private final SqlJobProcessDao sqlJobProcessDao;
    private final ShapefileDao shapefileDao;
    private final ApiUserAuth apiUserAuth;
    private final AdminUserAuth adminUserAuth;

    @Autowired
    public AdminApiController(SqlApiUsageStatsDao sqlApiUsageStatsDao, SqlDeploymentStatsDao sqlDeploymentStatsDao,
                              ApiUserDao apiUserDao, SqlGeocodeStatsDao sqlGeocodeStatsDao,
                              SqlJobProcessDao sqlJobProcessDao, ShapefileDao shapefileDao,
                              ApiUserAuth apiUserAuth, AdminUserAuth adminUserAuth) {
        this.sqlApiUsageStatsDao = sqlApiUsageStatsDao;
        this.sqlDeploymentStatsDao = sqlDeploymentStatsDao;
        this.apiUserDao = apiUserDao;
        this.sqlGeocodeStatsDao = sqlGeocodeStatsDao;
        this.sqlJobProcessDao = sqlJobProcessDao;
        this.shapefileDao = shapefileDao;
        this.apiUserAuth = apiUserAuth;
        this.adminUserAuth = adminUserAuth;
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
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
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
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            return sqlApiUsageStatsDao.getApiUsageStats(getBeginTimestamp(request), getEndTimestamp(request),
                    request.getParameter("interval"));
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
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            return sqlGeocodeStatsDao.getGeocodeStats(getBeginTimestamp(request), getEndTimestamp(request));
        }
        return invalidAuthResponse;

    }

    /**
     * Job Statuses Api
     * ---------------------
     * Returns current job statuses
     * Usage:
     * (GET)    /admin/api/jobStatuses
     *
     */
    @GetMapping(value = "/jobStatuses")
    public Object jobStatuses(HttpServletRequest request,
                            @RequestParam(required = false, defaultValue = "defaultUser") String username,
                            @RequestParam(required = false, defaultValue = "defaultPass") String password,
                            @RequestParam(required = false, defaultValue = "") String key) {
        String ipAddr= ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key) ) {
            return getJobProcessStatusList(request);
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
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
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
        DistrictType districtType = DistrictType.valueOf(type.toUpperCase());
        if (!districtType.hasShapefile()) {
            return new GenericResponse(false, "District type {} does not have shapefiles.");
        }
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request,username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            shapefileDao.cleanMaps(districtType);
        }
        return invalidAuthResponse;
    }

    /**
     * Returns a List of JobProcessStatus objects within the given 'from' and 'to' request time range.
     * @param request HttpServletRequest, Optional Params: 'from' (Start timestamp value for job requestTime)
     *                                                     'to' (End timestamp value for job requestTime)
     * @return List<JobProcessStatus>
     */
    private List<JobProcessStatusView> getJobProcessStatusList(HttpServletRequest request) {
        List<JobProcessStatusView> statusViews = new ArrayList<>();
        Timestamp from = getBeginTimestamp(request);
        Timestamp to = getEndTimestamp(request);
        List<JobProcessStatus> statuses = sqlJobProcessDao.getJobStatusesByConditions(
                List.of(JobProcessStatus.Condition.values()), null, from, to
        );
        for (JobProcessStatus jobProcessStatus : statuses) {
            statusViews.add(new JobProcessStatusView(jobProcessStatus));
        }
        return statusViews;
    }

}
