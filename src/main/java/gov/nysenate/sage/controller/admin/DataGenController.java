package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints.NYSAddressPointProcessor;
import gov.nysenate.sage.service.data.DataGenService;
import gov.nysenate.sage.service.district.PostOfficeService;
import gov.nysenate.sage.util.auth.AdminUserAuth;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.controller.ApiControllerUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.invalidAuthResponse;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setAdminResponse;

@Controller
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/datagen")
public class DataGenController {

    private static final Logger logger = LoggerFactory.getLogger(DataGenController.class);
    private final AdminUserAuth adminUserAuth;
    private final DataGenService dataGenService;
    private final ApiUserAuth apiUserAuth;
    private final NYSAddressPointProcessor nysAddressPointProcessor;
    private final PostOfficeService postOfficeService;

    @Autowired
    public DataGenController(AdminUserAuth adminUserAuth, ApiUserAuth apiUserAuth,
                             DataGenService dataGenService, NYSAddressPointProcessor nysAddressPointProcessor,
                             PostOfficeService postOfficeService) {
        this.adminUserAuth = adminUserAuth;
        this.apiUserAuth = apiUserAuth;
        this.dataGenService = dataGenService;
        this.nysAddressPointProcessor = nysAddressPointProcessor;
        this.postOfficeService = postOfficeService;
    }

    /**
     * Generate Meta Data Api
     * -----------------------
     * <p>
     * Generate meta data related to Assembly, Senators and Congressional members
     * <p>
     * Usage:
     * (GET)    /admin/datagen/genmetadata/{option}
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param option   String value that can be either all, assembly, congress, senate, a, c, s
     * @param username String
     * @param password String
     */
    @RequestMapping(value = "/genmetadata/{option}", method = RequestMethod.GET)
    public void generateMetaData(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable String option,
                                 @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                 @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                 @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            try {
                apiResponse = dataGenService.generateMetaData(option);
            } catch (Exception e) {
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        } else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * Vacantize Senator Data Api
     * -----------------------
     * <p>
     * Generates and replaces the Senator table with vacant senator data
     * <p>
     * Usage:
     * (GET)    /admin/datagen/vacantize
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param username String
     * @param password String
     */
    @RequestMapping(value = "/vacantize", method = RequestMethod.GET)
    public void vacantizeSenatorData(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                 @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                 @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            try {
                apiResponse = dataGenService.vacantizeSenateData();
            } catch (Exception e) {
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        } else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * Senator Cache Update Api
     * ------------------------
     * Updates the Senator Cache from GenMetaData Manually
     *
     *  /admin/datagen/rebuild/sencache
     *
     * @param request
     * @param response
     * @param username
     * @param password
     * @param key
     */
    @RequestMapping(value = "/rebuild/sencache", method = RequestMethod.GET)
    public void updateSenatorCache(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {

        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            dataGenService.updateSenatorCache();
            apiResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        }

        setAdminResponse(apiResponse, response);

    }

    /**
     * Generate County Code File Api
     * -----------------------------
     * <p>
     * Creates a county code file for use with the Street File parsing
     * <p>
     * Usage:
     * (GET)    /admin/datagen/countycodes
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/countycodes", method = RequestMethod.GET)
    public void ensureCountyCodeFileExists(HttpServletRequest request, HttpServletResponse response,
                                           @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                           @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                           @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            if (dataGenService.ensureCountyCodeFile()) {
                apiResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
            }
        }

        setAdminResponse(apiResponse, response);
    }

    /**
     * Generate Town Code File Api
     * ---------------------------
     * <p>
     * Creates a town code file for use with the Street File parsing
     * <p>
     * Usage:
     * (GET)    /admin/datagen/towncodes
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/towncodes", method = RequestMethod.GET)
    public void ensureTownCodeFileExists(HttpServletRequest request, HttpServletResponse response,
                                         @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                         @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                         @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            if (dataGenService.ensureTownCodeFile()) {
                apiResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
            }
        }
        setAdminResponse(apiResponse, response);
    }

    /**
     * Generate Zip Code CSV File Api [test case: scope public]
     * ---------------------------
     * <p>
     * Creates a zip code csv file for use with the Street File parsing
     * <p>
     * Usage:
     * (GET)    /admin/datagen/zipcodes
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */

    @RequestMapping(value = "/zipcodes", method = RequestMethod.GET)
    public void generateZipCodeFiles(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                     @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                     @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            try {
                apiResponse = dataGenService.generateZipCsv();
            } catch (Exception e) {
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        } else {
            apiResponse = invalidAuthResponse();
        }

        setAdminResponse(apiResponse, response);
    }

    /**
     * Processes a SAM NYS Address file into tsv streetfiles.
     * ---------------------------
     * Usage:
     * (GET)    /admin/datagen/process/sam-nys-statewide-addresses
     *
     * @param request
     * @param response
     * @param username
     * @param password
     * @param batchSize
     * @param saveNycToSeparateFile
     */
    @RequestMapping(value = "/process/sam-nys-statewide-addresses")
    public void processSamStatewideAddresses(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                             @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                             @RequestParam(required = false, defaultValue = "") String key,
                                             @RequestParam(required = false, defaultValue = "1000") int batchSize,
                                             @RequestParam(required = false, defaultValue = "false") boolean saveNycToSeparateFile) {
        Object apiResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            try {
                nysAddressPointProcessor.processNysSamAddressPoints(batchSize, saveNycToSeparateFile);
                apiResponse = "Processing sucessfully started";
            } catch (Exception e) {
                logger.error("Error trying to process the sam-nys-statewide-address file", e);
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        } else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }

    @RequestMapping(value = "/process/post-offices")
    public void processPostOffices(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) {
        Object apiResponse;
        String ipAddr = ApiControllerUtil.getIpAddress(request);
        Subject subject = SecurityUtils.getSubject();

        if (subject.hasRole("ADMIN") ||
                adminUserAuth.authenticateAdmin(request, username, password, subject, ipAddr) ||
                apiUserAuth.authenticateAdmin(request, subject, ipAddr, key)) {
            try {
                apiResponse = postOfficeService.replaceData();
            } catch (IOException ex) {
                logger.error("Error trying to process post office data", ex);
                apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
            }
        } else {
            apiResponse = invalidAuthResponse();
        }
        setAdminResponse(apiResponse, response);
    }
}
