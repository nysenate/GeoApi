package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField;
import gov.nysenate.sage.util.AddressDictionary;
import gov.nysenate.sage.util.Pair;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.parsers.NonStandardAddressType.*;
import static gov.nysenate.sage.scripts.streetfinder.parsers.NonStandardAddressType.OTHER;

public class NonStandardAddress {
    private static final String collegeBoxes = "((ALUMNI|COLONIAL|DUTCH|EMPIRE|FREEDOM|INDIAN|STATE) (QUAD |COMMONS )?)?BO?X",
            aptNumRegex = "(?<normalAptType>%s)|(?<collegeBox>%s)".formatted(String.join("|", AddressDictionary.unitNumMap.keySet()), collegeBoxes);
    private static final Pattern
            addressPattern = Pattern.compile("(?i)(?<bldgNum>([\\d /]+[A-Z]? )*)?(?<preDir>[NSEW]{1,2} )?(?<street>" + streetRegex() + ") "),
            aptPattern = Pattern.compile("(%s) (?<aptNum>\\d+)|(?<noNumAptType>%s)"
                    .formatted(aptNumRegex, String.join("|", AddressDictionary.unitNoNumMap.keySet())));

    private NonStandardAddressType type = VALID;
    private final EnumMap<VoterFileField, String> parsedFields = new EnumMap<>(VoterFileField.class);

    public NonStandardAddress(String line) {
        Matcher addressMatcher = addressPattern.matcher(line);
        int endIndex = -1;
        if (addressMatcher.find()) {
            String bldgNum = addressMatcher.group("bldgNum");
            if (bldgNum == null) {
                this.type = NO_STREET_NUM;
                return;
            }
            String noHalf = bldgNum.replaceFirst("1/2", "");
            if (!noHalf.equals(bldgNum)) {
                put(VoterFileField.RHALFCODE, "1/2");
            }
            // TODO: check isNum?
            put(VoterFileField.RADDNUMBER, noHalf.trim());
            put(VoterFileField.RPREDIRECTION, addressMatcher.group("preDir"));
            put(VoterFileField.RSTREETNAME, addressMatcher.group("street"));
            // TODO: post-direction?
            endIndex = addressMatcher.end();
        }
        else {
            this.type = OTHER;
            return;
        }
        if (addressMatcher.find()) {
            // TODO: sometimes, they're the same. Otherwise, invalid.
            endIndex = addressMatcher.end();
            this.type =  MULTIPLE_ADDRESSES;
            return;
        }
        String apt = line.substring(endIndex).trim();
        Matcher aptMatcher = aptPattern.matcher(apt);
        Pair<String> aptData;
        if (aptMatcher.find()) {
            if (aptMatcher.group("normalAptType") == null) {
                this.type = NON_STANDARD_APT_TYPE;
            }
            aptData = new Pair<>(aptMatcher.group().replaceFirst("\\d+", "").trim(),
                    aptMatcher.group("aptNum"));
        }
        else {
            return;
        }
        if (aptMatcher.find()) {
            this.type = MULTIPLE_APT_TYPES;
        }
        else {
            put(VoterFileField.RAPARTMENTTYPE, aptData.first());
            put(VoterFileField.RAPARTMENT, aptData.second());
        }
    }

    public NonStandardAddressType type() {
        return type;
    }

    public EnumMap<VoterFileField, String> getParsedFields() {
        return parsedFields;
    }

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

    private void put(VoterFileField field, String value) {
        if (value != null && !value.isEmpty()) {
            parsedFields.put(field, value);
        }
    }
}
