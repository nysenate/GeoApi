package gov.nysenate.sage.model.district;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * A container for all assigned district names and codes.
 */
public record DistrictInfo(ImmutableMap<DistrictType, SingleDistrict> typeToDistrictMap, DistrictMatchLevel matchLevel) {
    public static final DistrictInfo empty = new DistrictInfo(Map.of(), DistrictMatchLevel.NOMATCH);

    public DistrictInfo(Map<DistrictType, SingleDistrict> typeToDistrictMap, DistrictMatchLevel matchLevel) {
        this(ImmutableMap.copyOf(typeToDistrictMap), matchLevel);
    }

    public String getDistName(DistrictType districtType) {
        SingleDistrict singleDistrict = typeToDistrictMap.get(districtType);
        return singleDistrict == null ? null : singleDistrict.name();
    }

    public String getDistCode(DistrictType districtType) {
        SingleDistrict singleDistrict = typeToDistrictMap.get(districtType);
        return singleDistrict == null ? null : singleDistrict.code();
    }

    public SingleDistrict getDistrict(DistrictType districtType) {
        return typeToDistrictMap.get(districtType);
    }

    @Nonnull
    @Override
    public String toString() {
        var out = new StringBuilder();
        for (DistrictType t : typeToDistrictMap.keySet()) {
            out.append(t).append(": name = ").append(getDistName(t)).append(" code = ").append(getDistCode(t)).append("\n");
        }
        return out.toString();
    }
}
