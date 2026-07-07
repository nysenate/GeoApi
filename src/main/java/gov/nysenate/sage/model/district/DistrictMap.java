package gov.nysenate.sage.model.district;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Contains district map geometry information.
 */
@Getter
public class DistrictMap implements Comparable<DistrictMap> {
    private final DistrictType districtType;
    private final String districtCode;
    private final String districtName;
    @Setter
    private DistrictMember member;
    // Only used for County maps.
    @Setter
    private String link;
    // TownCity maps also need to know the base name.
    @Setter
    private String baseName;
    // Raw GeoJSON geometry (a MultiPolygon).
    @Setter
    private String mapGeoJson;
    // Note that this is only an approximation.
    @Setter
    private BigDecimal area;

    public DistrictMap(DistrictType type, String name, String code) {
        this.districtType = type;
        this.districtName = name;
        this.districtCode = code;
    }

    @Override
    public String toString() {
        return mapGeoJson == null ? "" : mapGeoJson;
    }

    @Override
    public int compareTo(@Nonnull DistrictMap o) {
        if (districtType == DistrictType.TOWN_CITY) {
            int result = baseName.compareTo(o.baseName);
            if (result != 0) {
                return result;
            }
        }
        int i = Arrays.mismatch(districtName.toCharArray(), o.districtName.toCharArray());
        if (i < 0) {
            return 0;
        }
        // It's common for names to have a common form, e.g. District x, where x is the code.
        // These should be sorted by the code.
        String a = districtName.substring(i), b = o.districtName.substring(i);
        try {
            return Long.compare(Long.parseLong(a), Long.parseLong(b));
        } catch (NumberFormatException ex) {
            return a.compareTo(b);
        }
    }
}
