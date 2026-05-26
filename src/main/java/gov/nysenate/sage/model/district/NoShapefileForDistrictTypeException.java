package gov.nysenate.sage.model.district;

public class NoShapefileForDistrictTypeException extends IllegalArgumentException {
    public NoShapefileForDistrictTypeException(DistrictType type) {
        super("There are no shapefiles for this type: " + type);
    }
}
