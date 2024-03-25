package gov.nysenate.sage.model.district;

public record County(int senateCode, int fipsCode, int voterfileCode, String name, String link, String streetfileName) {}
