package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import gov.nysenate.sage.model.district.DistrictType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AddressPointFileWriter {

    static void appendToStreetfile(String filename, String text) throws IOException {
        File file = new File(filename);
              FileUtils.writeStringToFile(file, text, StandardCharsets.UTF_8, true);
    }

    /**
     * For addresses which failed validation, just print the OID from the NYS Address Point file to an error file.
     */
    static void appendToUnsuccessfulFile(String filename, AddressPointValidationResult res) throws IOException {
        File file = new File(filename);
        StringBuilder sb = new StringBuilder();
        sb.append(res.addressPoint().oid);
        sb.append("\n");
        FileUtils.writeStringToFile(file, sb.toString(), StandardCharsets.UTF_8, true);
    }

    static String toStreetfileRow(AddressPointValidationResult ap) {
        StringBuilder sb = new StringBuilder();
        appendTsvColumn(sb, ap.streetAddress().getStreetName());
        appendTsvColumn(sb, ap.streetAddress().getLocation());
        appendTsvColumn(sb, ap.streetAddress().getState());
        appendTsvColumn(sb, ap.streetAddress().getZip5());
        appendTsvColumn(sb, String.valueOf(ap.streetAddress().getBldgNum()));
        appendTsvColumn(sb, ap.streetAddress().getBldgChar());
        appendTsvColumn(sb, String.valueOf(ap.streetAddress().getBldgNum()));
        appendTsvColumn(sb, ap.streetAddress().getBldgChar());
        appendTsvColumn(sb, ap.streetAddress().getBldgParity());
        appendTsvColumn(sb, "\\N"); // apt_lo_num
        appendTsvColumn(sb, "\\N"); // apt_lo_char
        appendTsvColumn(sb, "\\N"); // apt_hi_num
        appendTsvColumn(sb, "\\N"); // apt_hi_char
        appendTsvColumn(sb, "ALL"); // apt_parity
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.ELECTION)); // election code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.COUNTY)); // county_code
        appendTsvColumn(sb, ap.addressPoint().assemblyDistrict); // assembly_code
        appendTsvColumn(sb, ap.addressPoint().senateDistrict); // senate code
        appendTsvColumn(sb, ap.addressPoint().congressionalDistrict); // congressional code
        appendTsvColumn(sb, "\\N"); // boe town code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.TOWN)); // town code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.WARD)); // ward code
        appendTsvColumn(sb, "\\N"); // boe_school_code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.SCHOOL)); // school_code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.CLEG)); // cleg_code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.CITY_COUNCIL)); // cc_code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.FIRE)); // fire_code
        appendTsvColumn(sb, ap.getLookupCodeTsvValue(DistrictType.CITY)); // city_code
        // Don't add a tab after the last column
        sb.append(ap.getLookupCodeTsvValue(DistrictType.VILLAGE)); // vill_code
        sb.append("\n");
        return sb.toString();
    }

    static void writeStreetfileHeaders(String filename) throws IOException {
        File file = new File(filename);
        StringBuilder sb = new StringBuilder();
        appendTsvColumn(sb, "street");
        appendTsvColumn(sb, "town");
        appendTsvColumn(sb, "state");
        appendTsvColumn(sb, "zip5");
        appendTsvColumn(sb, "bldg_lo_num");
        appendTsvColumn(sb, "bldg_lo_chr");
        appendTsvColumn(sb, "bldg_hi_num");
        appendTsvColumn(sb, "bldg_hi_chr");
        appendTsvColumn(sb, "bldg_parity");
        appendTsvColumn(sb, "apt_lo_num"); // apt_lo_num
        appendTsvColumn(sb, "apt_lo_chr"); // apt_lo_char
        appendTsvColumn(sb, "apt_hi_num"); // apt_hi_num
        appendTsvColumn(sb, "apt_hi_chr"); // apt_hi_char
        appendTsvColumn(sb, "apt_parity"); // apt_parity
        appendTsvColumn(sb, "election_code"); // election code
        appendTsvColumn(sb, "county_code"); // county_code
        appendTsvColumn(sb, "assembly_code"); // assembly_code
        appendTsvColumn(sb, "senate_code"); // senate code
        appendTsvColumn(sb, "congressional_code"); // congressional code
        appendTsvColumn(sb, "boe_town_code"); // boe town code
        appendTsvColumn(sb, "town_code"); // town code
        appendTsvColumn(sb, "ward_code"); // ward code
        appendTsvColumn(sb, "boe_school_code"); // boe_school_code
        appendTsvColumn(sb, "school_code"); // school_code
        appendTsvColumn(sb, "cleg_code"); // cleg_code
        appendTsvColumn(sb, "cc_code"); // cc_code
        appendTsvColumn(sb, "fire_code"); // fire_code
        appendTsvColumn(sb, "city_code"); // city_code
        // Don't add a tab after the last column
        sb.append("vill_code"); // vill_code
        sb.append("\n");
        FileUtils.writeStringToFile(file, sb.toString(), StandardCharsets.UTF_8, true);
    }

    private static void appendTsvColumn(StringBuilder sb, String value) {
        sb.append(value);
        sb.append("\t");
    }
}
