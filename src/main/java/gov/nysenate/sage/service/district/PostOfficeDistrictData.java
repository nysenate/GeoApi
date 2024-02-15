package gov.nysenate.sage.service.district;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.model.address.DistrictedAddress;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores data about Post Office addresses in a single zipcode.
 */
public class PostOfficeDistrictData {
    private final Map<String, DistrictedAddress> townToDistAddrMap = new HashMap<>();
    private final DistrictedAddress consolidatedDistAddr;

    public PostOfficeDistrictData(Collection<DistrictedAddress> possibleDistAddrs) {
        // A town may have multiple Post Offices.
        Multimap<String, DistrictedAddress> townToDistAddrMultimap = ArrayListMultimap.create();
        for (DistrictedAddress addr : possibleDistAddrs) {
            townToDistAddrMultimap.put(addr.getGeocodedAddress().getAddress().getCity().toUpperCase(), addr);
        }
        for (var entry : townToDistAddrMultimap.asMap().entrySet()) {
            this.townToDistAddrMap.put(entry.getKey(), DistrictUtil.consolidateDistrictedAddress(entry.getValue()));
        }
        this.consolidatedDistAddr = DistrictUtil.consolidateDistrictedAddress(possibleDistAddrs);
    }

    /**
     * Attempts to match the city. Otherwise, just returns the consolidated districts.
     */
    public DistrictedAddress getDistrictedAddress(String city) {
        if (city == null) {
            return consolidatedDistAddr;
        }
        return townToDistAddrMap.getOrDefault(city.toUpperCase(), consolidatedDistAddr);
    }

    public boolean isEmpty() {
        return townToDistAddrMap.isEmpty();
    }
}
