package gov.nysenate.sage.client.view.street;

import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.StreetAddressRange;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.HashMap;
import java.util.Map;

public class StreetRangeView
{
    protected int bldgLoNum;
    protected int bldgHiNum;
    protected String street;
    protected String location;
    protected String zip5;
    protected String parity;
    protected Map<String, String> districts;

    public StreetRangeView(DistrictedStreetRange districtedStreetRange)
    {
        if (districtedStreetRange != null) {
            StreetAddressRange sar = districtedStreetRange.streetAddressRange();
            DistrictInfo dInfo = districtedStreetRange.districtInfo();
            if (sar != null) {
                this.bldgLoNum = sar.bldgLow();
                this.bldgHiNum = sar.bldgHigh();
                this.parity = sar.parity();
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
    }

    public int getBldgLoNum() {
        return bldgLoNum;
    }

    public int getBldgHiNum() {
        return bldgHiNum;
    }

    public String getStreet() {
        return street;
    }

    public String getLocation() {
        return location;
    }

    public String getZip5() {
        return zip5;
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

    public String getParity() {
        return parity;
    }
}
