package gov.nysenate.sage.service.data;

import java.util.List;

public interface SageRegeocacheService {
    /**
     * Re-geocaches all the Zip codes we have geometry for through google
     * @return A success or failure response
     */
    Object updateZipsInGeocache();

    /**
     * This method is called by the mass geocache bash script. It provides a powerful and robust way to regeocache
     * a variety of different addresses.
     */
    Object massRegeoache(int offset, int user_limit, boolean useFallback, List<String> typeList);
}
