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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.invalidAuthResponse;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAdminResponse;

@Controller
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
    public void generateStreetfile(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(defaultValue = "false") boolean voterFirst,
                                   @RequestParam(defaultValue = "0.8") double threshold,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {
        authenticateAndRun(request, response, username, password, key, () -> {
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
        });
    }

    /**
     * Generate Meta Data Api
     * -----------------------
     * <p>
     * Generate meta data related to Assembly, Senators and Congressional members
     * <p>
     * Usage:
     * (GET)    /admin/datagen/genmetadata/{option}
     * @param option   String value that can be either all, assembly, congress, senate, a, c, s
     */
    @RequestMapping(value = "/genmetadata/{option}", method = RequestMethod.GET)
    public void generateMetaData(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable String option,
                                 @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                 @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                 @RequestParam(required = false, defaultValue = "") String key) {
        authenticateAndRun(request, response, username, password, key, () -> dataGenService.generateMetaData(option));
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
    @RequestMapping(value = "/vacantize", method = RequestMethod.GET)
    public void vacantizeSenatorData(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                 @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                 @RequestParam(required = false, defaultValue = "") String key) {
        authenticateAndRun(request, response, username, password, key, dataGenService::vacantizeSenateData);
    }

    /**
     * Senator Cache Update Api
     * ------------------------
     * Updates the Senator Cache from GenMetaData Manually
     *  /admin/datagen/rebuild/sencache
     */
    @RequestMapping(value = "/rebuild/sencache", method = RequestMethod.GET)
    public void updateSenatorCache(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {
        authenticateAndRun(request, response, username, password, key, () -> {
            dataGenService.updateSenatorCache();
            return new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        });
    }

    /**
     * Generate Zip Code CSV File Api [test case: scope public]
     * ---------------------------
     * <p>
     * Creates a zip code csv file for use with the Street File parsing
     * <p>
     * Usage:
     * (GET)    /admin/datagen/zipcodes
     */

    @RequestMapping(value = "/zipcodes", method = RequestMethod.GET)
    public void generateZipCodeFiles(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                     @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                     @RequestParam(required = false, defaultValue = "") String key) {
        authenticateAndRun(request, response, username, password, key, dataGenService::generateZipCsv);
    }

    @RequestMapping(value = "/process/post-offices")
    public void processPostOffices(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {
        authenticateAndRun(request, response, username, password, key, postOfficeService::replaceData);
    }

    private void authenticateAndRun(HttpServletRequest request, HttpServletResponse response,
                                    String username, String password, String key,
                                    Callable<Object> responseSupplier) {
        Object apiResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            try {
                apiResponse = responseSupplier.call();
            } catch (Exception e) {
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        } else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }
}
