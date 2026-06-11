package gov.nysenate.sage.controller.admin;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.POST_OFFICE_REFRESH_FAILURE;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.invalidAuthResponse;

@RestController
@RequestMapping(value = ConstantUtil.ADMIN_REST_PATH + "/api/datagen")
public class DataGenController extends BaseAdminApiController {
    private final DataGenService dataGenService;
    private final PostOfficeService postOfficeService;
    private final StreetfileProcessor streetfileProcessor;
    private final StreetfileDao streetfileDao;

    @Autowired
    public DataGenController(AdminUserAuth adminUserAuth, ApiUserAuth apiUserAuth,
                             DataGenService dataGenService, StreetfileProcessor streetfileProcessor,
                             PostOfficeService postOfficeService, StreetfileDao streetfileDao) {
        super(adminUserAuth, apiUserAuth);
        this.dataGenService = dataGenService;
        this.postOfficeService = postOfficeService;
        this.streetfileProcessor = streetfileProcessor;
        this.streetfileDao = streetfileDao;
    }

    @GetMapping("/streetfile")
    public BaseResponse generateStreetfile(HttpServletRequest request,
                                     @RequestParam(defaultValue = "false") boolean voterFirst,
                                     @RequestParam(defaultValue = "0.8") double threshold,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                     @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                     @RequestParam(required = false, defaultValue = "") String key)
            throws SQLException, IOException {
        if (!authenticate(request, username, password, key)) {
            return invalidAuthResponse;
        }
        List<StreetfileType> priorityList = voterFirst ? List.of(StreetfileType.VOTER, StreetfileType.COUNTY) :
                List.of(StreetfileType.COUNTY, StreetfileType.VOTER);
        Path streetfilePath = streetfileProcessor.regenerateStreetfile(
                new ResolveConflictConfiguration(priorityList, threshold));
        if (streetfilePath == null) {
            return new BaseResponse(ResultStatus.NO_STREETFILES_TO_PROCESS);
        }
        streetfileDao.replaceStreetfile(streetfilePath);
        return ApiControllerUtil.successResponse;
    }

    /**
     * Generate Meta Data Api
     * -----------------------
     * <p>
     * Generate metadata related to Assembly, Senators and Congressional members
     * <p>
     * Usage:
     * (GET)    /admin/api/datagen/genmetadata/{option}
     * @param option   String value that can be either all, assembly, congress, senate, a, c, s
     */
    @GetMapping(value = "/genmetadata/{option}")
    public BaseResponse generateMetaData(HttpServletRequest request, @PathVariable String option,
                                   @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) throws IOException {
        if (authenticate(request, username, password, key)) {
            dataGenService.generateMetaData(option);
            return ApiControllerUtil.successResponse;
        }
        return invalidAuthResponse;
    }

    /**
     * Vacantize Senator Data Api
     * -----------------------
     * <p>
     * Generates and replaces the Senator table with vacant senator data
     * <p>
     * Usage:
     * (GET)    /admin/api/datagen/vacantize
     */
    @GetMapping(value = "/vacantize")
    public BaseResponse vacantizeSenatorData(HttpServletRequest request,
                                       @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                       @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                       @RequestParam(required = false, defaultValue = "") String key) {
        if (authenticate(request, username, password, key)) {
            dataGenService.vacantizeSenateData();
            return ApiControllerUtil.successResponse;
        }
        return invalidAuthResponse;
    }

    @GetMapping(value = "/post-offices")
    public BaseResponse processPostOffices(HttpServletRequest request,
                                     @RequestParam(required = false, defaultValue = "defaultUser") String username,
                                   @RequestParam(required = false, defaultValue = "defaultPass") String password,
                                   @RequestParam(required = false, defaultValue = "") String key) throws IOException {
        if (authenticate(request, username, password, key)) {
            if (postOfficeService.replaceData()) {
                return ApiControllerUtil.successResponse;
            }
            return new ApiError(POST_OFFICE_REFRESH_FAILURE);
        }
        return invalidAuthResponse;
    }
}
