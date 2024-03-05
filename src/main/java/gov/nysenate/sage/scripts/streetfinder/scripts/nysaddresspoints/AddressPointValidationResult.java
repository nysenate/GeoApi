package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddressPointValidationResult {

    private NYSAddressPoint addressPoint;
    private AddressResult validationResult;
    private StreetAddress streetAddress;
    private Map<DistrictType, String> lookedUpDistrictCodes;

    public AddressPointValidationResult(NYSAddressPoint addressPoint, AddressResult validationResult) {
        this.addressPoint = addressPoint;
        this.validationResult = validationResult;
    }

    /**
     * Returns the code for the given district if it exits, otherwise
     * returns the string "\\N" to indicate a null value in a streetfile.
     */
    public String getLookupCodeTsvValue(DistrictType districtType) {
        if (lookedUpDistrictCodes.containsKey(districtType)) {
            return lookedUpDistrictCodes.get(districtType);
        }
        return "\\N";
    }

    public String fullStreetName() {
        return Stream.of(streetAddress.getPreDir(), streetAddress.getStreet(), streetAddress.getPostDir())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    public NYSAddressPoint addressPoint() {
        return addressPoint;
    }

    public AddressResult validationResult() {
        return validationResult;
    }

    public StreetAddress streetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(StreetAddress streetAddress) {
        this.streetAddress = streetAddress;
    }

    public Map<DistrictType, String> lookedUpDistrictCodes() {
        return lookedUpDistrictCodes;
    }

    public void setLookedUpDistrictCodes(Map<DistrictType, String> lookedUpDistrictCodes) {
        this.lookedUpDistrictCodes = lookedUpDistrictCodes;
    }
}
