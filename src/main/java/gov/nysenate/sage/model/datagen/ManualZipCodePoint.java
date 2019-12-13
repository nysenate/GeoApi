package gov.nysenate.sage.model.datagen;

import gov.nysenate.sage.model.geo.Point;


public class ManualZipCodePoint {
    private String zipcode;
    private String type;
    private Point point;
    private String source;


    public ManualZipCodePoint() {}

    public ManualZipCodePoint(String zipcode, String type, Point point, String source) {
        this.zipcode = zipcode;
        this.type = type;
        this.point = point;
        this.source = source;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLon() {
        return point.getLon();
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public double getLat() {
        return point.getLat();
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


}
