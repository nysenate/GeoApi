package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;

public record StreetfileLineData(StreetfileAddressRange addressRange, CompactDistrictMap districts, CellId cellId) {}
