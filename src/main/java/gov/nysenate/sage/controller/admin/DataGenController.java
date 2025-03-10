package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.scripts.streetfinder.model.ResolveConflictConfiguration;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileType;
import gov.nysenate.sage.service.PostOfficeService;
import gov.nysenate.sage.service.data.DataGenService;
import gov.nysenate.sage.service.streetfile.StreetfileProcessor;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.invalidAuthResponse;

@RestController
// TODO: label as API
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/datagen")
public class DataGenController {
    private final AdminUserAuth adminUserAuth;
    private final ApiUserAuth apiUserAuth;
    private final DataGenService dataGenService;
    private final PostOfficeService postOfficeService;
    private final StreetfileProcessor streetfileProcessor;
    private final StreetfileDao streetfileDao;

    @Autowired
    public DataGenController(AdminUserAuth adminUserAuth, ApiUserAuth apiUserAuth,
                             DataGenService dataGenService, StreetfileProcessor streetfileProcessor,
                             PostOfficeService postOfficeService, StreetfileDao streetfileDao) {
        this.adminUserAuth = adminUserAuth;
        this.apiUserAuth = apiUserAuth;
        this.dataGenService = dataGenService;
        this.postOfficeService = postOfficeService;
        this.streetfileProcessor = streetfileProcessor;
        this.streetfileDao = streetfileDao;
    }

    @GetMapping("/streetfile")
    public Object generateStreetfile(HttpServletRequest request,
                                     @RequestParam(defaultValue = "false") boolean voterFirst,
                                     @RequestParam(defaultValue = "0.8") double threshold,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                     @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                     @RequestParam(required = false, defaultValue = "") String key)
            throws SQLException, IOException {
        if (!authenticate(request, username, password, key)) {
            return invalidAuthResponse();
        }
        List<StreetfileType> priorityList = voterFirst ? List.of(StreetfileType.VOTER, StreetfileType.COUNTY) :
                List.of(StreetfileType.COUNTY, StreetfileType.VOTER);
        Path streetfilePath = streetfileProcessor.regenerateStreetfile(
                new ResolveConflictConfiguration(priorityList, threshold));
        BaseResponse apiResponse;
        if (streetfilePath == null) {
            apiResponse = new BaseResponse(ResultStatus.NO_STREETFILES_TO_PROCESS);
        }
        else {
            streetfileDao.replaceStreetfile(streetfilePath);
            apiResponse = new BaseResponse(SUCCESS);
        }
        return apiResponse;
    }

    /**
     * Generate Meta Data Api
     * -----------------------
     * <p>
     * Generate metadata related to Assembly, Senators and Congressional members
     * <p>
     * Usage:
     * (GET)    /admin/datagen/genmetadata/{option}
     * @param option   String value that can be either all, assembly, congress, senate, a, c, s
     */
    @GetMapping(value = "/genmetadata/{option}")
    public Object generateMetaData(HttpServletRequest request, @PathVariable String option,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) throws IOException {
        if (authenticate(request, username, password, key)) {
            return dataGenService.generateMetaData(option);
        }
        return invalidAuthResponse();
    }

    /**
     * Vacantize Senator Data Api
     * -----------------------
     * <p>
     * Generates and replaces the Senator table with vacant senator data
     * <p>
     * Usage:
     * (GET)    /admin/datagen/vacantize
     */
    @GetMapping(value = "/vacantize")
    public Object vacantizeSenatorData(HttpServletRequest request,
                                       @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                       @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                       @RequestParam(required = false, defaultValue = "") String key) {
        if (authenticate(request, username, password, key)) {
            return dataGenService.vacantizeSenateData();
        }
        return invalidAuthResponse();
    }

    /**
     * Senator Cache Update Api
     * ------------------------
     * Updates the Senator Cache from GenMetaData Manually
     *  /admin/datagen/rebuild/sencache
     */
    @GetMapping(value = "/rebuild/sencache")
    public Object updateSenatorCache(HttpServletRequest request,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                     @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                     @RequestParam(required = false, defaultValue = "") String key) {
        if (authenticate(request, username, password, key)) {
            dataGenService.updateSenatorCache();
            return new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        }
        return invalidAuthResponse();
    }

    @GetMapping(value = "/process/post-offices")
    public Object processPostOffices(HttpServletRequest request,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) throws IOException {
        if (authenticate(request, username, password, key)) {
            return postOfficeService.replaceData();
        }
        return invalidAuthResponse();
    }

    private boolean authenticate(HttpServletRequest request, String username, String password, String key) {
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        return new ApiError(getClass(), INTERNAL_ERROR);
    }
}
