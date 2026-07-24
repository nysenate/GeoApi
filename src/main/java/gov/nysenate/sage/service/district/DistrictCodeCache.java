package gov.nysenate.sage.service.district;

import com.google.common.collect.*;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.service.ImmutableCache;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DistrictCodeCache<T> extends ImmutableCache<DistrictType, ImmutableMap<String, T>> {
    public DistrictCodeCache(Function<DistrictType, Map<String, T>> supplier) {
        super(() -> Arrays.stream(DistrictType.values()).collect(
                Collectors.toMap(Function.identity(), type -> ImmutableMap.copyOf(supplier.apply(type)))
        ));
    }

    public T getData(DistrictType type, String code) {
        Map<String, T> tempMap = get(type);
        if (tempMap == null) {
            return null;
        }
        return tempMap.get(code);
    }
}
