package gov.nysenate.sage.model.district;

import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.model.Accuracy;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * A container for all assigned district names and codes.
 */
public record DistrictInfo(ImmutableMap<DistrictType, String> typeToDistrictMap, Accuracy accuracy) {
    public static final DistrictInfo empty = new DistrictInfo(Map.of(), null);

    public DistrictInfo(Map<DistrictType, String> typeToDistrictMap, Accuracy accuracy) {
        this(ImmutableMap.copyOf(typeToDistrictMap), accuracy);
    }

    public String getDistCode(DistrictType districtType) {
        return typeToDistrictMap.get(districtType);
    }

    public Set<DistrictType> getAssignedTypes() {
        return typeToDistrictMap.keySet();
    }

    @Nonnull
    @Override
    public String toString() {
        var out = new StringBuilder().append(accuracy).append('\n');
        for (DistrictType t : typeToDistrictMap.keySet()) {
            out.append(t).append(": ").append(getDistCode(t)).append("\n");
        }
        return out.toString();
    }
}
