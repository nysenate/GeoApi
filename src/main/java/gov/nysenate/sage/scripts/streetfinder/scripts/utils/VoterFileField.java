package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileFieldType.*;

/**
 * List of all fields in the voter file. In practice, we will not use most of them.
 */
public enum VoterFileField {
    LASTNAME, FIRSTNAME, MIDDLENAME, NAMESUFFIX,
    RADDNUMBER(ADDRESS, true), RHALFCODE(ADDRESS),
    RPREDIRECTION(ADDRESS, true), RSTREETNAME(ADDRESS, true), RPOSTDIRECTION(ADDRESS, true),
    RAPARTMENTTYPE(ADDRESS), RAPARTMENT(ADDRESS),
    RADDRNONSTD(ADDRESS),
    RCITY(ADDRESS), RZIP5(ADDRESS, true), RZIP4(ADDRESS),
    MAILADD1, MAILADD2, MAILADD3, MAILADD4, DOB, GENDER, ENROLLMENT, OTHERPARTY,
    COUNTYCODE(DISTRICT), ED(DISTRICT), LD(DISTRICT), TOWNCITY(DISTRICT, false), WARD(DISTRICT),
    CD(DISTRICT), SD(DISTRICT), AD(DISTRICT),
    LASTVOTERDATE, PREVYEARVOTED, PREVCOUNTY, PREVADDRESS, PREVNAME,
    COUNTYVRNUMBER, REGDATE, VRSOURCE, IDREQUIRED, IDMET,
    STATUS, REASONCODE, INACT_DATE, PURGE_DATE, SBOEID, VoterHistory;

    // These fields will be put into a created streetfile.
    public static final List<VoterFileField> streetFileFields = Arrays.stream(values())
            .filter(field -> field.isStreetfileData).collect(Collectors.toList());

    private final VoterFileFieldType type;
    private final boolean isStreetfileData;

    VoterFileField() {
        this(OTHER, false);
    }

    // All districts should be included by default.
    VoterFileField(VoterFileFieldType type) {
        this(type, type == DISTRICT);
    }

    VoterFileField(VoterFileFieldType type, boolean isStreetfileData) {
        this.type = type;
        this.isStreetfileData = isStreetfileData;
    }

    public VoterFileFieldType getType() {
        return type;
    }
}
