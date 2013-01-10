package gov.nysenate.sage.boe;

public class BluebirdAddress extends BOEStreetAddress {

    public String id;
    public double latitude;
    public double longitude;
    public int geo_accuracy;
    public boolean parse_failure;
    public String parse_message;
    public String geo_method;

    public BluebirdAddress(String id) { this.id = id; }
}
