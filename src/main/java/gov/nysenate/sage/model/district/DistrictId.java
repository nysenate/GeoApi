package gov.nysenate.sage.model.district;

public record DistrictId(String id) {
    public DistrictId {
        if (id == null || !id.matches("(\\d+-)*\\d+")) {
            throw new IllegalArgumentException(id + " is not a valid DistrictId");
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
