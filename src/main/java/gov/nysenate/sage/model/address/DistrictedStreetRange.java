package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.district.AssignedDistricts;

public record DistrictedStreetRange(StreetAddressRange streetAddressRange, AssignedDistricts assignedDistricts) {}
