package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.district.DistrictView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import lombok.Getter;
import org.apache.commons.text.CaseUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class DistrictResponse extends SourcedResponse {
    private AddressView address;
    private GeocodeView geocode;
    private boolean geocoded = false;
    private boolean districtAssigned = false;
    private boolean senateAssigned = false;
    private boolean uspsValidated = false;
    private String matchLevel;
    private final Map<String, DistrictView> districts = new LinkedHashMap<>();

    public DistrictResponse(DistrictResult districtResult, GeocodedAddress geoAddr, boolean usePunct) {
        super(districtResult);
        if (districtResult == null) {
            return;
        }
        this.districtAssigned = !districtResult.getAssignedDistrictTypes().isEmpty();
        this.senateAssigned = districtResult.getAssignedDistrictTypes().contains(DistrictType.SENATE);
        this.matchLevel = Objects.toString(districtResult.getAssignedDistricts().accuracy(), null);
        for (DistrictType districtType : DistrictType.values()) {
            String currCode = districtResult.getAssignedDistricts().getDistCode(districtType);
            districts.put(getFieldName(districtType),
                    currCode == null ? null : new DistrictView(districtType, currCode));
        }
        if (geoAddr == null) {
            return;
        }
        Address realAddress = geoAddr.getAddress();
        if (realAddress != null) {
            this.address = new AddressView(realAddress, usePunct);
            this.uspsValidated = realAddress.isUspsValidated();
        }
        Geocode realGeocode = geoAddr.getGeocode();
        this.geocode = GeocodeView.from(realGeocode);
        if (realGeocode != null) {
            this.geocoded = realGeocode.isValidGeocode();
        }
    }

    private static String getFieldName(DistrictType type) {
        return switch (type) {
            case TOWN_CITY -> "town";
            case COUNTY_LEGISLATURE -> "cleg";
            default -> CaseUtils.toCamelCase(type.name(), false, '_');
        };
    }

    public boolean getMultiMatch() {
        return false;
    }
}
