package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.College;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField;
import gov.nysenate.sage.util.AddressDictionary;
import gov.nysenate.sage.util.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.parsers.NonStandardAddressType.*;
import static gov.nysenate.sage.scripts.streetfinder.parsers.NonStandardAddressType.OTHER;

public class NonStandardAddress {
    private static final String collegeBoxes = "((ALUMNI|COLONIAL|DUTCH|EMPIRE|FREEDOM|INDIAN|STATE) (QUAD |COMMONS )?)?BO?X",
            aptNumRegex = "(?<normalAptType>%s)|(?<collegeBox>%s)".formatted(String.join("|", AddressDictionary.unitNumMap.keySet()), collegeBoxes);
    private static final Pattern
            // Some bldgNum examples: 10, 10A, 1/2, 4 1/2
            addressPattern = Pattern.compile("(?i)(?<bldgNum>([\\d /]+[A-Z]? ))?(?<preDir>[NSEW]{1,2} )?(?<street>" + streetRegex() + ")(?<postDir>[NSEW]{1,2} )?"),
            aptPattern = Pattern.compile("(%s) (?<aptNum>\\d+)|(?<noNumAptType>%s)"
                    .formatted(aptNumRegex, String.join("|", AddressDictionary.unitNoNumMap.keySet())));

    private static final Set<Integer> problemZips = Set.of(13021, 13215, 13104, 12118, 14623, 12804);

    private final EnumMap<VoterFileField, String> parsedFields = new EnumMap<>(VoterFileField.class);
    private NonStandardAddressType type = VALID;

    public NonStandardAddress(final String line, final String zip5) {
        Matcher addressMatcher = addressPattern.matcher(line);
        int endIndex;
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
            put(VoterFileField.RADDNUMBER, noHalf.trim());
            put(VoterFileField.RPREDIRECTION, addressMatcher.group("preDir"));
            put(VoterFileField.RSTREETNAME, addressMatcher.group("street"));
            put(VoterFileField.RPOSTDIRECTION, addressMatcher.group("postDir"));
            endIndex = addressMatcher.end();
        }
        else {
            this.type = College.getCollege(line, zip5) == null ? OTHER : COLLEGE;
            if (type == OTHER && problemZips.contains(Integer.parseInt(zip5))) {
                System.out.println(line + ", " + zip5);
            }
            return;
        }
        if (addressMatcher.find()) {
            // TODO: wrongly catches a lot of addresses, especially Broadway ones
            this.type =  MULTIPLE_ADDRESSES;
            return;
        }
        setAptFields(line.substring(endIndex).trim());
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

    private void setAptFields(String apt) {
        Matcher aptMatcher = aptPattern.matcher(apt);
        Pair<String> aptData;
        if (aptMatcher.find()) {
            if (aptMatcher.group("collegeBox") != null) {
                this.type = COLLEGE;
            }
            else if (aptMatcher.group("normalAptType") == null) {
                this.type = NON_STANDARD_APT_TYPE;
            }
            aptData = new Pair<>(
                    aptMatcher.group().replaceFirst("\\d+", "").trim(),
                    aptMatcher.group("aptNum"));
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
