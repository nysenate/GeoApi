package gov.nysenate.sage.client.view.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.nysenate.sage.client.view.district.BaseDistrictView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import lombok.Getter;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;

@Getter
public class DistrictMapView extends BaseDistrictView {
    // Only used for County maps.
    private String link;
    private final BigDecimal area;

    public DistrictMapView(DistrictMap districtMap) {
        super(districtMap.getDistrictType(), districtMap.getDistrictCode());
        this.area = districtMap.getArea();
    }

    // Explicit marker so this getter survives the @JsonIgnore on BaseDistrictView#type.
    @JsonProperty("type")
    public DistrictType getType() {
        return type;
    }

    public void setLink(URI link) {
        this.link = Objects.toString(link, null);
    }
}
