package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;

import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictsView represents the structure of district information on the response end of the API.
 */
public class DistrictsView {
    protected SenateDistrictView senate;
    protected MemberDistrictView congressional;
    protected MemberDistrictView assembly;
    protected DistrictView county;
    protected DistrictView election;
    protected DistrictView school;
    protected DistrictView town;
    protected DistrictView zip;
    protected DistrictView cleg;
    protected DistrictView ward;
    protected DistrictView village;
    protected DistrictView cityCouncil;
    protected DistrictView electricUtility;

    public DistrictsView(DistrictResultWithMembers result, Map<DistrictType, DistrictMap> geomMap) {
        if (result == null) {
            return;
        }
        DistrictInfo dInfo = result.getDistrictInfo();
        this.senate = new SenateDistrictView(viewFrom(SENATE, dInfo, geomMap), result.getSenator());
        this.congressional = new MemberDistrictView(viewFrom(CONGRESSIONAL, dInfo, geomMap), result.getCongressionalMember());
        this.assembly = new MemberDistrictView(viewFrom(ASSEMBLY, dInfo, geomMap), result.getAssemblyMember());
        this.county = viewFrom(COUNTY, dInfo, geomMap);
        this.school = viewFrom(SCHOOL, dInfo, geomMap);
        this.town = viewFrom(TOWN_CITY, dInfo, geomMap);
        this.zip = viewFrom(ZIP, dInfo, geomMap);
        this.cleg = viewFrom(COUNTY_LEG, dInfo, geomMap);
        this.ward = viewFrom(WARD, dInfo, geomMap);
        this.village = viewFrom(VILLAGE, dInfo, geomMap);
        this.cityCouncil = viewFrom(CITY_COUNCIL, dInfo, geomMap);
        this.election = viewFrom(ELECTION, dInfo, geomMap);
        this.electricUtility = viewFrom(ELECTRIC_UTILITY, dInfo, geomMap);
    }

    private static DistrictView viewFrom(DistrictType type, DistrictInfo info, Map<DistrictType, DistrictMap> typeToGeom) {
        DistrictMap map = typeToGeom.get(type);
        return new DistrictView(info.getDistName(type), info.getDistCode(type), map == null ? null : new PolygonMapView(map));
    }

    public SenateDistrictView getSenate() {
        return getDistrictView(senate);
    }

    public MemberDistrictView getCongressional() {
        return getDistrictView(congressional);
    }

    public MemberDistrictView getAssembly() {
        return getDistrictView(assembly);
    }

    public DistrictView getCounty() {
        return getDistrictView(county);
    }

    public DistrictView getElection() {
        return getDistrictView(election);
    }

    public DistrictView getSchool() {
        return getDistrictView(school);
    }

    public DistrictView getTown() {
        return getDistrictView(town);
    }

    public DistrictView getZip() {
        return getDistrictView(zip);
    }

    public DistrictView getCleg() {
        return getDistrictView(cleg);
    }

    public DistrictView getWard() {
        return getDistrictView(ward);
    }

    public DistrictView getVillage() {
        return getDistrictView(village);
    }

    public DistrictView getCityCouncil() {
        return getDistrictView(cityCouncil);
    }

    public DistrictView getElectricUtility() {
        return getDistrictView(electricUtility);
    }

    private static <V extends DistrictView> V getDistrictView(V view) {
        return view != null && view.getDistrict() != null ? view : null;
    }
}
