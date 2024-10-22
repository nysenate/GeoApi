package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.district.DistrictInfo;

public record DistrictedStreetRange(StreetAddressRange streetAddressRange, DistrictInfo districtInfo) {}
