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

    public DistrictsView(DistrictInfo dInfo, Senator senator)
    {
        if (dInfo != null) {
            this.senate = new SenateDistrictView(dInfo, senator);
            this.congressional = new MemberDistrictView(CONGRESSIONAL, dInfo, dInfo.getDistrictMember(CONGRESSIONAL));
            this.assembly = new MemberDistrictView(ASSEMBLY, dInfo, dInfo.getDistrictMember(ASSEMBLY));
            this.county = new DistrictView(COUNTY, dInfo);
            this.election = new DistrictView(ELECTION, dInfo);
            this.school = new DistrictView(SCHOOL, dInfo);
            this.town = new DistrictView(TOWN, dInfo);
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
}
