package gov.nysenate.sage.service.district;

import com.google.common.collect.*;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.service.ImmutableCache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DistrictCodeCache<T> extends ImmutableCache<DistrictType, ImmutableMap<String, T>> {
    public DistrictCodeCache(Function<DistrictType, Map<String, T>> supplier) {
        super(() -> {
            var tempMap = new HashMap<DistrictType, ImmutableMap<String, T>>();
            for (DistrictType type : DistrictType.values()) {
                Map<String, T> data = supplier.apply(type);
                if (data != null) {
                    tempMap.put(type, ImmutableMap.copyOf(data));
                }
            }
            return tempMap;
        });
    }

    public T getData(DistrictType type, String code) {
        Map<String, T> tempMap = get(type);
        if (tempMap == null) {
            return null;
        }
        return tempMap.get(code);
    }
}
