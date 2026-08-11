package gov.nysenate.sage.service.district;

import com.google.common.collect.*;
import gov.nysenate.sage.model.district.DistrictId;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.service.ImmutableCache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DistrictIdCache<T> extends ImmutableCache<DistrictType, ImmutableMap<DistrictId, T>> {
    public DistrictIdCache(Function<DistrictType, Map<DistrictId, T>> supplier) {
        super(() -> {
            var tempMap = new HashMap<DistrictType, ImmutableMap<DistrictId, T>>();
            for (DistrictType type : DistrictType.values()) {
                Map<DistrictId, T> data = supplier.apply(type);
                if (data != null) {
                    tempMap.put(type, ImmutableMap.copyOf(data));
                }
            }
            return tempMap;
        });
    }

    public T getData(DistrictType type, DistrictId id) {
        Map<DistrictId, T> tempMap = get(type);
        if (tempMap == null) {
            return null;
        }
        return tempMap.get(id);
    }
}
