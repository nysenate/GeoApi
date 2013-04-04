package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.services.model.Senator;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictsView represents the structure of district information on the response end of the API.
 */
public class DistrictsView
{
    protected SenateDistrictView senate;
    protected MemberDistrictView congressional;
    protected MemberDistrictView assembly;
    protected DistrictView county;
    protected DistrictView election;
    protected DistrictView school;
    protected DistrictView town;
    protected DistrictView cleg;
    protected DistrictView ward;
    protected DistrictView village;

    public DistrictsView(DistrictInfo dInfo)
    {
        if (dInfo != null) {
            this.senate = new SenateDistrictView(dInfo, dInfo.getSenator());
            this.congressional = new MemberDistrictView(CONGRESSIONAL, dInfo, dInfo.getDistrictMember(CONGRESSIONAL));
            this.assembly = new MemberDistrictView(ASSEMBLY, dInfo, dInfo.getDistrictMember(ASSEMBLY));
            this.county = new DistrictView(COUNTY, dInfo);
            this.election = new DistrictView(ELECTION, dInfo);
            this.school = new DistrictView(SCHOOL, dInfo);
            this.town = new DistrictView(TOWN, dInfo);
            this.cleg = new DistrictView(CLEG, dInfo);
            this.ward = new DistrictView(WARD, dInfo);
            this.village = new DistrictView(VILLAGE, dInfo);
        }
    }

    public SenateDistrictView getSenate() {
        return senate;
    }

    public MemberDistrictView getCongressional() {
        return congressional;
    }

    public MemberDistrictView getAssembly() {
        return assembly;
    }

    public DistrictView getCounty() {
        return county;
    }

    public DistrictView getElection() {
        return election;
    }

    public DistrictView getSchool() {
        return school;
    }

    public DistrictView getTown() {
        return town;
    }

    public DistrictView getCleg() {
        return cleg;
    }

    public void setCleg(DistrictView cleg) {
        this.cleg = cleg;
    }

    public DistrictView getWard() {
        return ward;
    }

    public void setWard(DistrictView ward) {
        this.ward = ward;
    }

    public DistrictView getVillage() {
        return village;
    }

    public void setVillage(DistrictView village) {
        this.village = village;
    }
}
