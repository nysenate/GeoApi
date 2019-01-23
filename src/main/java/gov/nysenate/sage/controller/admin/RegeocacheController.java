package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.admin.SqlAdminUserDao;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "regeocache")
public class RegeocacheController {

    private Logger logger = LoggerFactory.getLogger(RegeocacheController.class);

    private BaseDao baseDao;
    private Environment env;
    private SqlAdminUserDao sqlAdminUserDao;
    private AdminUserAuth adminUserAuth;

    @Autowired
    public RegeocacheController(BaseDao baseDao, Environment env, SqlAdminUserDao sqlAdminUserDao,
                                AdminUserAuth adminUserAuth) {
        this.baseDao = baseDao;
        this.env = env;
        this.sqlAdminUserDao = sqlAdminUserDao;
        this.adminUserAuth = adminUserAuth;
    }

    /**
     * REQUIRES ADMIN PERMISSIONS
     * @param request
     * @param response
     * @param username
     * @param password
     */
    @RequestMapping(value = "/zip", method = RequestMethod.GET)
    public void geocacheZips(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam String username, @RequestParam(required = false) String password) {
        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);

        String forwardedForIp = request.getHeader("x-forwarded-for");
        String ipAddr= forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") || sqlAdminUserDao.checkAdminUser(username, password)) {
            adminUserAuth.setUpPermissions(request, username, ipAddr);

            String GET_ZIP_SQL = "select zcta5ce10 from districts.zip;";

            String BASE_URL = env.getBaseUrl() + "/api/v2/geo/geocode?addr=";
            String GEO_PROVIDER_URL = "&provider=google";
            List<String> zipCodes = null;

        /*
        Execute SQL and get zip codes
         */
            try {
                zipCodes = baseDao.geoApiJbdcTemplate.query(GET_ZIP_SQL, (rs, rowNum) -> rs.getString("zcta5ce10"));
            }
            catch (Exception ex) {
                logger.error("Error retrieving zip codes from geoapi db", ex);
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
                setApiResponse(apiResponse, request);
                return;
            }

            //Fail if we couldnt get zip codes properly
            if (zipCodes == null) {
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
                setApiResponse(apiResponse, request);
                return;
            }


            //Cycle through zip codes
            for (String zip: zipCodes) {
                logger.info("Geocoding zip: " +zip);
            /*
        Execute http request
         */
                HttpClient httpClient = HttpClientBuilder.create().build();
                try {
                    HttpPost httpRequest = new HttpPost(BASE_URL + zip + GEO_PROVIDER_URL);
                    httpClient.execute(httpRequest);
                    ((CloseableHttpClient) httpClient).close();
                }
                catch (Exception e) {
                    logger.error("Failed to make Http request because of exception" + e.getMessage());
                    apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
                    setApiResponse(apiResponse, request);
                    return;
                }
            }
            apiResponse = new ApiError(this.getClass(), SUCCESS);
            setApiResponse(apiResponse, request);
        }
        else {
            setApiResponse(apiResponse, request);
            return;
        }
    }
}
