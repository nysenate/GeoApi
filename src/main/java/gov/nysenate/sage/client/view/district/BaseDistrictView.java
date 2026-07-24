package gov.nysenate.sage.client.view.district;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseDistrictView {
    @JsonIgnore
    protected final DistrictType type;
    private final String district;
    private String name;
    private DistrictMember member;
    // Raw GeoJSON geometry, emitted directly to the client.
    @JsonRawValue
    private String map;

    protected BaseDistrictView(DistrictType type, String code) {
        this.type = type;
        this.district = code;
    }
}
