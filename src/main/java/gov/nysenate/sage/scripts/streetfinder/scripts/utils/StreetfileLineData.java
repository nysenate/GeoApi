package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;

public record StreetfileLineData(BuildingRange range, AddressWithoutNum addressWithoutNum, CompactDistrictMap districts, CellId cellId) {
    public StreetfileLineData with(AddressWithoutNum addressWithoutNum) {
        return new StreetfileLineData(range, addressWithoutNum, districts, cellId);
    }
}
