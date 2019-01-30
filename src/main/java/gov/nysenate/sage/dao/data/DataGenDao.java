package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.scripts.streetfinder.County;
import gov.nysenate.sage.scripts.streetfinder.TownCode;

import java.util.List;

public interface DataGenDao {

    /**
     * Retrieves the county codes from the public schema in the streetfile model format
     * @return
     */
    public List<County> getCountyCodes();

    /**
     * Retreives the town codes from the district schema in the streetfile model format
     * @return
     */
    public List<TownCode> getTownCodes();
}
