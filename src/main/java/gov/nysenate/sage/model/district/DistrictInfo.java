package gov.nysenate.sage.model.district;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * A container for all assigned district names and codes.
 */
public record DistrictInfo(ImmutableMap<DistrictType, SingleDistrict> typeToDistrictMap, DistrictMatchLevel matchLevel) {
    public static final DistrictInfo empty = new DistrictInfo(Map.of(), DistrictMatchLevel.NOMATCH);

    public DistrictInfo(Map<DistrictType, SingleDistrict> typeToDistrictMap, DistrictMatchLevel matchLevel) {
        this(ImmutableMap.copyOf(typeToDistrictMap), matchLevel);
    }

    public String getDistCode(DistrictType districtType) {
        SingleDistrict singleDistrict = typeToDistrictMap.get(districtType);
        return singleDistrict == null ? null : singleDistrict.code();
    }

    public SingleDistrict getDistrict(DistrictType districtType) {
        return typeToDistrictMap.get(districtType);
    }

    public Set<DistrictType> getAssignedTypes() {
        return typeToDistrictMap.keySet();
    }

    @Nonnull
    @Override
    public String toString() {
        var out = new StringBuilder().append(matchLevel).append('\n');
        for (DistrictType t : typeToDistrictMap.keySet()) {
            out.append(t).append(": ").append(getDistrict(t)).append("\n");
        }
        return out.toString();
    }
}
