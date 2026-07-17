package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.Map;

public class DistrictResultWithMembers extends DistrictResult {
    private final Map<DistrictType, DistrictMember> memberMap;

    public DistrictResultWithMembers(DistrictResult baseResult, Map<DistrictType, DistrictMember> memberMap) {
        super(baseResult.getSources() == null ? null : baseResult.getSources().stream().toList(),
                baseResult.getStatusCode(), baseResult.getDistrictInfo());
        this.memberMap = memberMap;
    }

    public DistrictMember getSenator() {
        return memberMap.get(DistrictType.SENATE);
    }

    public DistrictMember getAssemblyMember() {
        return memberMap.get(DistrictType.ASSEMBLY);
    }

    public DistrictMember getCongressionalMember() {
        return memberMap.get(DistrictType.CONGRESSIONAL);
    }
}
