package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.address.*;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final AddressServiceProvider addressProvider;

    @Autowired
    public AddressController(AddressServiceProvider addressProvider) {
        this.addressProvider = addressProvider;
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
        return new ValidateResponse(addressProvider.validate(address, provider, punct));
    }

    /**
     * City State Validation Api
     * ---------------------------
     * Looks up a city state with USPS
     * Usage:
     * (GET)    /api/v2/address/citystate
     */
    @GetMapping(value = "/citystate")
    public BaseResponse addressCityState(
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String addr,
            @RequestParam(required = false) String addr1,
            @RequestParam(required = false) String addr2,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam String zip5,
            @RequestParam(required = false) String zip4) {
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        return new CityStateResponse(addressProvider.lookupCityState(address, provider));
    }

    /**
     * Zipcode validation Api
     * ---------------------------
     * Looks up a zipcode from an address input with USPS
     * Usage:
     * (GET)    /api/v2/address/zipcode
     */
    @GetMapping(value = "/zipcode")
    public BaseResponse addressZipcode(
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String addr,
            @RequestParam(required = false) String addr1,
            @RequestParam(required = false) String addr2,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zip5,
            @RequestParam(required = false) String zip4) {
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        return new ZipcodeResponse(addressProvider.lookupZipcode(address, provider));
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
        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        return new BatchValidateResponse(addressProvider.validate(addresses, provider, punct));
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
        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        return new BatchCityStateResponse(addressProvider.lookupCityState(addresses, provider));
    }
}
