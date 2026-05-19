package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.district.DistrictView;
import gov.nysenate.sage.client.view.district.MemberDistrictView;
import gov.nysenate.sage.client.view.district.SenateDistrictView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;
import org.apache.commons.text.CaseUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class DistrictResponse extends SourcedResponse {
    protected AddressView address;
    protected GeocodeView geocode;
    protected boolean geocoded = false;
    protected boolean districtAssigned = false;
    protected boolean senateAssigned = false;
    protected boolean uspsValidated = false;
    protected boolean isMultiMatch = false;
    protected String matchLevel;
    protected Map<String, DistrictView> districts = new LinkedHashMap<>();

    public DistrictResponse(DistrictResultWithMembers districtResult, GeocodedAddress geoAddr,
                            boolean usePunct) {
        this(districtResult, geoAddr, usePunct, Map.of());
    }

    public DistrictResponse(DistrictResultWithMembers districtResult, GeocodedAddress geoAddr,
                            boolean usePunct, Map<DistrictType, DistrictMap> geomMap) {
        super(districtResult);
        if (districtResult == null) {
            return;
        }
        this.districtAssigned = !districtResult.getAssignedDistricts().isEmpty();
        this.senateAssigned = districtResult.getAssignedDistricts().contains(DistrictType.SENATE);
        this.matchLevel = districtResult.getDistrictInfo().matchLevel().name();
        for (DistrictType districtType : DistrictType.values()) {
            districts.put(getFieldName(districtType), viewFrom(districtType, districtResult, geomMap.get(districtType)));
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
        if (realGeocode != null) {
            this.geocode = new GeocodeView(realGeocode);
            this.geocoded = realGeocode.isValidGeocode();
        }
    }

    private static String getFieldName(DistrictType type) {
        return switch (type) {
            case TOWN_CITY -> "town";
            case COUNTY_LEG -> "cleg";
            default -> CaseUtils.toCamelCase(type.name(), false, '_');
        };
    }

    private static DistrictView viewFrom(DistrictType type, DistrictResultWithMembers result, DistrictMap districtMap) {
        DistrictInfo info = result.getDistrictInfo();
        if (!info.getAssignedTypes().contains(type)) {
            return null;
        }
        var baseView = new DistrictView(info.getDistName(type), info.getDistCode(type),
                type.getDisplayName(), districtMap == null ? null : new PolygonMapView(districtMap));
        return switch (type) {
            case SENATE -> new SenateDistrictView(baseView, result.getSenator());
            case ASSEMBLY -> new MemberDistrictView(baseView, result.getAssemblyMember());
            case CONGRESSIONAL -> new MemberDistrictView(baseView, result.getCongressionalMember());
            default -> baseView;
        };
    }

    public AddressView getAddress() {
        return address;
    }

    public GeocodeView getGeocode() {
        return geocode;
    }

    public boolean isGeocoded() {
        return geocoded;
    }

    public boolean isSenateAssigned() {
        return senateAssigned;
    }

    public boolean isDistrictAssigned() {
        return districtAssigned;
    }

    public boolean isUspsValidated() {
        return uspsValidated;
    }

    public String getMatchLevel() {
        return matchLevel;
    }

    public boolean getMultiMatch() {
        return isMultiMatch;
    }

    public Map<String, DistrictView> getDistricts() {
        return districts;
    }
}
