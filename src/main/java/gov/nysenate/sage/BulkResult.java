package gov.nysenate.sage;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BluebirdAddress;

public class BulkResult {
    public static enum STATUS { HOUSE, STREET, ZIP5, SHAPEFILE, INVALID, NOMATCH };

    public STATUS status_code;
    public String message;
    public String address_id;
    public int county_code;
    public int assembly_code;
    public int congressional_code;
    public int senate_code;
    public int election_code;
    public int council_code;
    public int ward_code;
    public String school_code;
    public String town_code;
    public int cleg_code;
    public String address;
    public double latitude;
    public double longitude;
    public int geo_accuracy;
    public String geo_method;

    public BluebirdAddress bluebird_address;

    public BulkResult(STATUS status, String message, BluebirdAddress address, BOEAddressRange match) {
        this.bluebird_address = address;
        this.address_id = address.id;
        this.status_code = status;
        this.message = message;
        this.address = address.toString();
        this.election_code = match.electionCode;
        this.senate_code = match.senateCode;
        this.congressional_code = match.congressionalCode;
        this.assembly_code = match.assemblyCode;
        this.county_code = match.countyCode;
        this.cleg_code = match.clegCode;
        this.council_code = match.ccCode;

        /**
         * Shape file lookups return the correct senate preferred town and school codes.
         * Street file data requires mapping to separate senate town and senate school codes.
         */

        if (status == STATUS.SHAPEFILE) {
            this.town_code = match.townCode;
            this.school_code = match.schoolCode;
        } else if (status == STATUS.HOUSE || status == STATUS.STREET || status == STATUS.ZIP5 ){
            this.town_code = match.senateTownCode;
            this.school_code = match.senateSchoolCode;
        } else if (county_code==14 || county_code==15 || county_code==28 || county_code==43 || county_code==47 || county_code==55 || county_code==56) {
            this.town_code = match.townCode;
            this.school_code = "";
        } else {
            this.town_code = "";
            this.school_code = "";
        }

        this.ward_code = match.wardCode;
        this.latitude = address.latitude;
        this.longitude = address.longitude;
        this.geo_accuracy = address.geo_accuracy;
        this.geo_method = address.geo_method;
    }
}
