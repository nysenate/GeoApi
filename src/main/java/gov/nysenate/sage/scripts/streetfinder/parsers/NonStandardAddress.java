package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.College;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField;
import gov.nysenate.sage.util.AddressDictionary;
import gov.nysenate.sage.util.Pair;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.parsers.NonStandardAddressType.*;

// TODO: "Standart Woods" and "John Burke Apts", for example, have valid data.
/**
 * Some entries in the voter files have their address in a single field.
 * This class attempts to parse out those pieces.
 */
public class NonStandardAddress {
    private static final String aptNumRegex = "(?<normalAptType>%s)".formatted(String.join("|", AddressDictionary.unitNumMap.keySet()));
    private static final Pattern
            // Some bldgNum examples: 10, 10A, 1/2, 4 1/2
            addressPattern = Pattern.compile("(?i)(?<bldgNum>([\\d /]+[A-Z]? ))?(?<preDir>[NSEW]{1,2} )?(?<street>" + streetRegex() + ")(?<postDir>[NSEW]{1,2} )?"),
            aptPattern = Pattern.compile("(?<numAptType>%s) (?<aptNum>\\d+)|(?<noNumAptType>%s)"
                    .formatted(aptNumRegex, String.join("|", AddressDictionary.unitNoNumMap.keySet())));

    private final EnumMap<VoterFileField, String> parsedFields = new EnumMap<>(VoterFileField.class);
    private final College college;
    private NonStandardAddressType type = NORMAL;

    public NonStandardAddress(final String line, final String zip5) {
        this.college = College.getCollege(line, zip5);
        if (college != null) {
            this.type = COLLEGE;
            return;
        }
        Matcher addressMatcher = addressPattern.matcher(line);
        if (!addressMatcher.find()) {
            this.type = OTHER;
            return;
        }
        String bldgNum = addressMatcher.group("bldgNum");
        if (bldgNum == null) {
            this.type = NO_STREET_NUM;
            return;
        }
        String noHalf = bldgNum.replaceFirst("1/2", "");
        if (!noHalf.equals(bldgNum)) {
            put(VoterFileField.RHALFCODE, "1/2");
        }
        put(VoterFileField.RADDNUMBER, noHalf.trim());
        put(VoterFileField.RPREDIRECTION, addressMatcher.group("preDir"));
        put(VoterFileField.RSTREETNAME, addressMatcher.group("street"));
        put(VoterFileField.RPOSTDIRECTION, addressMatcher.group("postDir"));
        int endIndex = addressMatcher.end();
        if (addressMatcher.find()) {
            this.type =  MULTIPLE_ADDRESSES;
        }
        else {
            setAptFields(line.substring(endIndex).trim());
        }
    }

    public EnumMap<VoterFileField, String> getParsedFields() {
        return parsedFields;
    }

    public College getCollege() {
        return college;
    }

    public NonStandardAddressType type() {
        return type;
    }

    // This method exists just to make declaration cleaner.
    private static String streetRegex() {
        var suffixSet = new TreeSet<>(AddressDictionary.streetTypeMap.keySet());
        var bigRoadList = new ArrayList<String>();
        for (var highwayEntry : AddressDictionary.highWayMap.entrySet()) {
            if (highwayEntry.getValue().matches(".*(Rte|Hwy)")) {
                bigRoadList.add(highwayEntry.getKey());
            }
        }
        return "BROADWAY|.+ (%s)|(%s) \\d+[A-Z]{0,1}".formatted(String.join("|", suffixSet), String.join("|", bigRoadList));
    }

    private void setAptFields(String apt) {
        Matcher aptMatcher = aptPattern.matcher(apt);
        Pair<String> aptData;
        if (aptMatcher.find()) {
            if (aptMatcher.group("numAptType") == null) {
                this.type = NON_STANDARD_APT_TYPE;
            }
            String groupName = type == NON_STANDARD_APT_TYPE ? "noNumAptType" : "numAptType";
            aptData = new Pair<>(aptMatcher.group(groupName), aptMatcher.group("aptNum"));
            if (aptMatcher.find()) {
                this.type = MULTIPLE_APT_TYPES;
            }
            else {
                put(VoterFileField.RAPARTMENTTYPE, aptData.first());
                put(VoterFileField.RAPARTMENT, aptData.second());
            }
        }
    }

    private void put(VoterFileField field, String value) {
        if (value != null && !value.isEmpty()) {
            parsedFields.put(field, value);
        }
    }
}
