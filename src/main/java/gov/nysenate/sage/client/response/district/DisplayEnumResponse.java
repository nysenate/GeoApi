package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.util.HasDisplayName;
import lombok.Getter;

@Getter
public class DisplayEnumResponse {
    private final String enumName;
    private final String displayName;

    public <E extends Enum<E> & HasDisplayName> DisplayEnumResponse(E type) {
        this.enumName = type.name();
        this.displayName = type.getDisplayName();
    }
}
