package gov.nysenate.sage.model.district;

public record DistrictTypeInfo(String codeColumn, String nameColumn) {
    public DistrictTypeInfo {
        if (codeColumn == null) {
            throw new IllegalArgumentException("codeColumn cannot be null");
        }
        // Many DistrictTypes do not have separate names
        if (nameColumn == null) {
            nameColumn = codeColumn;
        }
    }
}
