package gov.nysenate.sage.client.view.street;

import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.district.DistrictId;
import gov.nysenate.sage.model.district.DistrictType;
import lombok.Getter;

import java.util.Map;

public class StreetRangeView {
    @Getter
    private int bldgLoNum;
    @Getter
    private int bldgHiNum;
    @Getter
    private String street;
    @Getter
    private String location;
    @Getter
    private String zip5;
    @Getter
    private String parity;
    private final Map<DistrictType, DistrictId> districts;

    public StreetRangeView(DistrictedStreetRange dsr) {
        var addressRange = dsr.streetAddressRange();
        if (addressRange != null) {
            this.bldgLoNum = addressRange.bldgLow();
            this.bldgHiNum = addressRange.bldgHigh();
            this.parity = addressRange.parity().name();
            this.street = addressRange.awn().street();
            this.location = addressRange.awn().postalCity();
            this.zip5 = String.valueOf(addressRange.awn().zip5());
        }
        this.districts = dsr.assignedDistricts().typeToDistrictMap();
    }

    public DistrictId getSenate() {
        return districts.get(DistrictType.SENATE);
    }

    public DistrictId getCongressional() {
        return districts.get(DistrictType.CONGRESSIONAL);
    }

    public DistrictId getAssembly() {
        return districts.get(DistrictType.ASSEMBLY);
    }

    public DistrictId getTown() {
        return districts.get(DistrictType.TOWN_CITY);
    }

    public DistrictId getCounty() {
        return districts.get(DistrictType.COUNTY);
    }
}
