package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictInfo;

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

    public DistrictsView(DistrictInfo dInfo) {
        if (dInfo == null) {
            return;
        }
        this.senate = new SenateDistrictView(dInfo, dInfo.getSenator());
        this.congressional = new MemberDistrictView(CONGRESSIONAL, dInfo, dInfo.getDistrictMember(CONGRESSIONAL));
        this.assembly = new MemberDistrictView(ASSEMBLY, dInfo, dInfo.getDistrictMember(ASSEMBLY));
        this.county = new DistrictView(COUNTY, dInfo);
        this.election = new DistrictView(ELECTION, dInfo);
        this.school = new DistrictView(SCHOOL, dInfo);
        this.town = new DistrictView(TOWN_CITY, dInfo);
        this.zip = new DistrictView(ZIP, dInfo);
        this.cleg = new DistrictView(CLEG, dInfo);
        this.ward = new DistrictView(WARD, dInfo);
        this.village = new DistrictView(VILLAGE, dInfo);
        this.cityCouncil = new DistrictView(CITY_COUNCIL, dInfo);
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

    private static <V extends DistrictView> V getDistrictView(V view) {
        return view != null && view.district != null ? view : null;
    }
}
