package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictInfo;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.model.district.DistrictType.VILLAGE;
import static gov.nysenate.sage.model.district.DistrictType.WARD;

public class MappedDistrictsView
{
    protected MappedSenateDistrictView senate;
    protected MappedMemberDistrictView congressional;
    protected MappedMemberDistrictView assembly;
    protected MappedDistrictView county;
    protected MappedDistrictView election;
    protected MappedDistrictView school;
    protected MappedDistrictView town;
    protected MappedDistrictView zip;
    protected MappedDistrictView cleg;
    protected MappedDistrictView ward;
    protected MappedDistrictView village;

    public MappedDistrictsView(DistrictInfo dInfo)
    {
        if (dInfo != null) {
            this.senate = new MappedSenateDistrictView(dInfo, dInfo.getSenator());
            this.congressional = new MappedMemberDistrictView(CONGRESSIONAL, dInfo, dInfo.getDistrictMember(CONGRESSIONAL));
            this.assembly = new MappedMemberDistrictView(ASSEMBLY, dInfo, dInfo.getDistrictMember(ASSEMBLY));
            this.county = new MappedDistrictView(COUNTY, dInfo);
            this.election = new MappedDistrictView(ELECTION, dInfo);
            this.school = new MappedDistrictView(SCHOOL, dInfo);
            this.town = new MappedDistrictView(TOWN, dInfo);
            this.zip = new MappedDistrictView(ZIP, dInfo);
            this.cleg = new MappedDistrictView(CLEG, dInfo);
            this.ward = new MappedDistrictView(WARD, dInfo);
            this.village = new MappedDistrictView(VILLAGE, dInfo);
        }
    }

    public MappedSenateDistrictView getSenate() {
        return senate;
    }

    public MappedMemberDistrictView getCongressional() {
        return congressional;
    }

    public MappedMemberDistrictView getAssembly() {
        return assembly;
    }

    public MappedDistrictView getCounty() {
        return county;
    }

    public MappedDistrictView getElection() {
        return election;
    }

    public MappedDistrictView getSchool() {
        return school;
    }

    public MappedDistrictView getTown() {
        return town;
    }

    public MappedDistrictView getZip() {
        return zip;
    }

    public MappedDistrictView getCleg() {
        return cleg;
    }

    public MappedDistrictView getWard() {
        return ward;
    }

    public MappedDistrictView getVillage() {
        return village;
    }
}
