package gov.nysenate.sage.service.district;

import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.model.address.DistrictedAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores data about Post Office addresses in a single zipcode.
 */
public class PostOfficeDistrictData {
    private static final Logger logger = LoggerFactory.getLogger(PostOfficeDistrictData.class);
    private final Map<String, DistrictedAddress> townToDistAddrMap;
    private final DistrictedAddress consolidatedDistAddr;

    public PostOfficeDistrictData(List<DistrictedAddress> possibleDistAddrs) {
        this.townToDistAddrMap = new HashMap<>();
        for (DistrictedAddress addr : possibleDistAddrs) {
            var result = townToDistAddrMap.put(addr.getGeocodedAddress().getAddress().getCity().toUpperCase(), addr);
            if (result != null) {
                logger.warn("There was already a Post Office entry: " + result);
            }
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
}
