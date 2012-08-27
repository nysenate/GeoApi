package gov.nysenate.sage;

/**
 *
 * @author Graylin Kim
 *
 * A generic address structure supplied to all services.
 *
 * Encapsulates all aspects of an address to allow an address to be augmented
 * through successful calls to 3rd party services for specific address details.
 *
 * Supports both raw and fully parsed address styles and supplies a helper
 * function for services to distinguish between the two.
 *
 */
public class Address implements Cloneable {
    public enum TYPE { RAW, MIXED, PARSED };

    // Geo-coordinates
    public double latitude = 0;
    public double longitude = 0;

    // Ranges from 0 for not geocoded to 100 for user input
    // geocodes from services are never 100% accurate.
    public int geocode_quality = 0;

    // District Information
    public String congressional_name = "";
    public int congressional_code = 0;
    public String county_name = "";
    public int county_code = 0;
    public String senate_name = "";
    public int senate_code = 0;
    public String assembly_name = "";
    public int assembly_code = 0;
    public String town_name = "";
    public String town_code = "";
    public String election_name = "";
    public int election_code = 0;
    public String school_name = "";
    public int school_code = 0;
    public String ward_name = "";
    public int ward_code = 0;

    // Raw street address
    public String raw = "";

    // Address Parts
    public String addr1 = "";
    public String addr2 = "";
    public String city = "";
    public String state = "";
    public String zip5 = "";
    public String zip4 = "";
    public boolean validated = false;

    @Override
    public Address clone() {
        try {
            return (Address)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Address() {}

    public Address(String address) {
        this.raw = address;
    }

    public Address(String addr2, String city, String state, String zip5) {
        this("", addr2, city, state, zip5, "");
    }

    public Address(String addr1, String addr2, String city, String state, String zip5, String zip4) {
        this.addr1 = (addr1==null) ? "" : addr1;
        this.addr2 = (addr2==null) ? "" : addr2;
        this.city = (city==null) ? "" : city;
        this.state = (state==null) ? "" : state;
        this.zip5 = (zip5==null) ? "" : zip5;
        this.zip4 = (zip4==null) ? "" : zip4;
    }

    public boolean is_parsed() {
        return !(addr2.equals("") && city.equals("") && state.equals("") && zip5.equals(""));
    }

    public boolean is_geocoded() {
        return geocode_quality != 0;
    }

    public String as_raw() {
        if (is_parsed()) {
            return ( !addr1.equals("") ? addr1+" " : "")
                 + ( !addr2.equals("") ? addr2+" " : "")
                 + ( !city.equals("")  ? city+", "   : "")
                 + ( !state.equals("") ? state+" " : "")
                 + ( !zip5.equals("")  ? zip5 : "")
                 + ( !zip4.equals("")  ? "-"+zip4 : "").trim();
        } else {
            return raw;
        }
    }

    @Override
    public String toString() {
        String str = this.as_raw();
        if (this.is_geocoded()) {
            str += " ("+latitude+","+longitude+")~"+geocode_quality;
        }
        return str;
    }

    public void setGeocode(double lat, double lng, int quality) {
        this.latitude = lat;
        this.longitude = lng;
        this.geocode_quality = quality;
    }
}
