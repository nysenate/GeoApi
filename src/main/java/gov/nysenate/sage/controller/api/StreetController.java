package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.street.StreetResponse;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.StreetResult;
import gov.nysenate.sage.provider.district.StreetLookupService;
import gov.nysenate.sage.provider.district.Streetfile;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "street")
public class StreetController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(StreetController.class);
    private final StreetLookupService streetfile;

    @Autowired
    public StreetController(Streetfile streetfile) {
        this.streetfile = streetfile;
    }

    /**
     * Street Lookup Api
     * ---------------------------
     * Look up street data for a zip5 code
     * Usage:
     * (GET)    /api/v2/street/lookup
     */
    @GetMapping(value = "/lookup")
    public BaseResponse addressBatchCityState(@RequestParam int zip5) {
        logger.info("Getting street data for zip5 {}", zip5);
        var zip5Obj = new Zip5(zip5);
        List<DistrictedStreetRange> streets = streetfile.streetLookup(zip5Obj);
        return new StreetResponse(new StreetResult(streets));
    }
}
