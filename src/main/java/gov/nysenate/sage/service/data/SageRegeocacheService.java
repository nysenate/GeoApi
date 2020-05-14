package gov.nysenate.sage.service.data;

import java.util.ArrayList;

public interface SageRegeocacheService {

    /**
     * Regeocaches all of the Zip codes we have geometry for through google
     * @return A success or failure response
     */
    public Object updateZipsInGeocache();

    /**
     * This method is called by the mass geocache bash script. It provides a powerful and robust way to regeocache
     * a variety of different addresses.
     * @param offset
     * @param user_limit
     * @param typeList
     * @return
     */
    public Object massRegeoache(int offset, int user_limit, boolean useFallback, ArrayList<String> typeList);
}
