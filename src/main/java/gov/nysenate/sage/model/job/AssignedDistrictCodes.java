package gov.nysenate.sage.model.job;

import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.district.DistrictType;

public record AssignedDistrictCodes(ImmutableMap<DistrictType, String> codeMap, Accuracy accuracy) {}
