package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.address.BatchCityStateResponse;
import gov.nysenate.sage.client.response.address.BatchValidateResponse;
import gov.nysenate.sage.client.response.address.CityStateResponse;
import gov.nysenate.sage.client.response.address.ValidateResponse;
import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.address.AddressSource;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static gov.nysenate.sage.util.controller.ApiControllerUtil.getAddressFromParams;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.getAddressesFromJsonBody;

/**
 * Address API controller handles the various AddressService requests including
 *  - Address Validation
 *  - City State Lookup
 *  - ZipCode Lookup
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "address")
public final class AddressController {
    private final AddressService addressService;
    private final AddressSource defaultSource;

    @Autowired
    public AddressController(AddressService addressService, @Value("${usps.default:ams}") String defaultSource) {
        this.addressService = addressService;
        this.defaultSource = AddressSource.valueOf(defaultSource.toUpperCase());
    }

    /**
     * Address Validation Api
     * ---------------------------
     * Validates an address with USPS
     * Usage:
     * (GET)    /api/v2/address/validate
     */
    @GetMapping(value = "/validate")
    public BaseResponse addressValidate(
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) boolean punct,
            @RequestParam(required = false) String addr,
            @RequestParam(required = false) String addr1,
            @RequestParam(required = false) String addr2,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zip5,
            @RequestParam(required = false) String zip4) {
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        AddressSource source = AddressSource.fromString(provider, defaultSource);
        if (source == null) {
            return new ApiError(AddressController.class, ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
        }
        return new ValidateResponse(addressService.validate(address, source, punct));
    }

    /**
     * City State Validation Api
     * ---------------------------
     * Looks up a city state with USPS
     * Usage:
     * (GET)    /api/v2/address/citystate
     */
    @GetMapping(value = "/citystate")
    public BaseResponse addressCityState(@RequestParam String zip5, @RequestParam(required = false) String provider) {
        var validZip5 = new Zip5(Integer.parseInt(zip5));
        AddressSource source = AddressSource.fromString(provider, defaultSource);
        if (source == null) {
            return new ApiError(AddressController.class, ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
        }
        return new CityStateResponse(addressService.lookupCityState(validZip5, source));
    }

    /**
     * Batch Address Validation Api
     * ---------------------------
     * Batch address validation with USPS
     * Usage:
     * (GET)    /api/v2/address/validate/batch
     */
    @PostMapping(value = "/validate/batch")
    public BaseResponse addressBatchValidate(HttpServletRequest request,
                                     @RequestParam(required = false) String provider,
                                     @RequestParam(required = false) boolean punct) throws IOException {
        AddressSource source = AddressSource.fromString(provider, defaultSource);
        if (source == null) {
            return new ApiError(AddressController.class, ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
        }
        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        return new BatchValidateResponse(addressService.validate(addresses, source, punct));
    }

    /**
     * Batch City State Validation Api
     * ---------------------------
     * Batch city state validation with USPS
     * Usage:
     * (GET)    /api/v2/address/citystate/batch
     */
    @PostMapping(value = "/citystate/batch")
    public BaseResponse addressBatchCityState(HttpServletRequest request,
                                              @RequestParam(required = false) String provider) throws IOException {
        AddressSource source = AddressSource.fromString(provider, defaultSource);
        if (source == null) {
            return new ApiError(AddressController.class, ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
        }
        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        // TODO: parse if needed
        List<Zip5> zips = List.of();
        return new BatchCityStateResponse(addressService.lookupCityState(zips, source));
    }
}
