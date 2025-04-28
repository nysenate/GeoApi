package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.services.model.Senator;

public class DistrictResultWithMembers extends DistrictResult {
    private final Senator senator;
    private final DistrictMember assemblyMember;
    private final DistrictMember congressionalMember;

    public DistrictResultWithMembers(DistrictResult baseResult, Senator senator, DistrictMember assemblyMember,
                                     DistrictMember congressionalMember) {
        super(baseResult.getSource(), baseResult.getDistrictInfo());
        this.senator = senator;
        this.assemblyMember = assemblyMember;
        this.congressionalMember = congressionalMember;
    }

    public Senator getSenator() {
        return senator;
    }

    public DistrictMember getAssemblyMember() {
        return assemblyMember;
    }

    public DistrictMember getCongressionalMember() {
        return congressionalMember;
    }
}
