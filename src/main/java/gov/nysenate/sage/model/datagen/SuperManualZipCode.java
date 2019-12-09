package gov.nysenate.sage.model.datagen;

public class SuperManualZipCode {
    private String zipcode;
    private String type;
    private String lon;
    private String lat;
    private String source;

    public SuperManualZipCode() {}

    public SuperManualZipCode(String zipcode, String type, String lon, String lat, String source) {
        this.zipcode = zipcode;
        this.type = type;
        this.lon = lon;
        this.lat = lat;
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

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


}
