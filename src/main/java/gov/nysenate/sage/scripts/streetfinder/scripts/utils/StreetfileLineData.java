package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;

public record StreetfileLineData(StreetFileAddressRange addressRange, CompactDistrictMap districts, CellId cellId) {}
