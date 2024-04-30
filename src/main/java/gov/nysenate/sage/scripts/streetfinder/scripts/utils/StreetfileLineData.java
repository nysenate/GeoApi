package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;
import org.apache.commons.lang3.ObjectUtils;

public record StreetfileLineData(BuildingRange range, AddressWithoutNum addressWithoutNum,
                                 CompactDistrictMap districts, CellId cellId, StreetfileLineType type) {
    public StreetfileLineData {
        if (ObjectUtils.anyNull(range(), addressWithoutNum(), districts(), cellId()) &&
                type() == StreetfileLineType.PROPER) {
            throw new IllegalArgumentException("PROPER line data must have no null elements.");
        }
    }

    public StreetfileLineData with(AddressWithoutNum addressWithoutNum) {
        return new StreetfileLineData(range, addressWithoutNum, districts, cellId, type);
    }

    public StreetfileLineData(StreetfileLineType type) {
        this(null, null, null, null, type);
    }
}
