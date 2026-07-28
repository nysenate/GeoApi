package gov.nysenate.sage.client.view.street;

import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.StreetAddressRange;
import gov.nysenate.sage.model.district.AssignedDistricts;
import gov.nysenate.sage.model.district.DistrictType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class StreetRangeView {
    @Getter
    protected int bldgLoNum;
    @Getter
    protected int bldgHiNum;
    @Getter
    protected String street;
    @Getter
    protected String location;
    @Getter
    protected String zip5;
    @Getter
    protected String parity;
    protected Map<String, String> districts;

    public StreetRangeView(DistrictedStreetRange districtedStreetRange) {
        if (districtedStreetRange == null) {
            return;
        }
        StreetAddressRange sar = districtedStreetRange.streetAddressRange();
        AssignedDistricts dInfo = districtedStreetRange.assignedDistricts();
        if (sar != null) {
            this.bldgLoNum = sar.bldgLow();
            this.bldgHiNum = sar.bldgHigh();
            this.parity = sar.parity().name();
            this.street = sar.awn().street();
            this.location = sar.awn().postalCity();
            this.zip5 = String.valueOf(sar.awn().zip5());
        }
        if (dInfo != null) {
            districts = new HashMap<>();
            districts.put("senate", dInfo.getDistCode(DistrictType.SENATE));
            districts.put("assembly", dInfo.getDistCode(DistrictType.ASSEMBLY));
            districts.put("congressional", dInfo.getDistCode(DistrictType.CONGRESSIONAL));
            districts.put("county", dInfo.getDistCode(DistrictType.COUNTY));
            districts.put("town", dInfo.getDistCode(DistrictType.TOWN_CITY));
            districts.put("election", dInfo.getDistCode(DistrictType.ELECTION));
        }
    }

    public String getSenate() {
        return districts.get("senate");
    }

    public String getCongressional() {
        return districts.get("congressional");
    }

    public String getAssembly() {
        return districts.get("assembly");
    }

    public String getTown() {
        return districts.get("town");
    }

    public String getCounty() {
        return districts.get("county");
    }

    public String getElection() {
        return districts.get("election");
    }

}
