package gov.nysenate.sage.model.datagen;

public class RubberBandedBoundary {

    private String geo;
    private String zipcode;

    public RubberBandedBoundary() {}

    public RubberBandedBoundary(String zipcode, String geo) {
        this.zipcode = zipcode;
        this.geo = geo;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public String toString() {
        return zipcode + "\t" + geo; }
}



