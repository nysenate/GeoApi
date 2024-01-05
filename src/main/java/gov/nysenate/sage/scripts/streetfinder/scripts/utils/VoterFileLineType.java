package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.*;

/**
 * Allows for thorough sorting of lines for QA purposes and error reporting.
 */
public enum VoterFileLineType {
    // TODO: has_tabs should have full line printed
    VALID, HAS_TABS, WRONG_FIELD_LENGTH(VoterFileField.values()),
    BAD_ID, MISSING_ZIP_5, NO_ADDRESS,
    INVALID_NON_STANDARD_ADDRESS(RADDRNONSTD),
    TWO_ADDRESS_FORMATS(displayAddressFields());

    private final SortedSet<VoterFileField> displayFields;

    VoterFileLineType(VoterFileField singleField) {
        this.displayFields = new TreeSet<>();
        displayFields.add(singleField);
    }

    // By default, the county ID and BOE ID are added.
    VoterFileLineType(VoterFileField... displayFields) {
        this.displayFields = new TreeSet<>(List.of(displayFields));
        this.displayFields.add(COUNTYCODE);
        this.displayFields.add(SBOEID);
    }

    private static VoterFileField[] displayAddressFields() {
        List<VoterFileField> fullFields = new ArrayList<>(VoterFileField.standardAddressFields);
        fullFields.add(VoterFileField.RADDRNONSTD);
        return fullFields.toArray(new VoterFileField[0]);
    }

    public String getCsv(Function<VoterFileField, String> toString) {
        return displayFields.stream().map(toString).collect(Collectors.joining(", "));
    }
}
