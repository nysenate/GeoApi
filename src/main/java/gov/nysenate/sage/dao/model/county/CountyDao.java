package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.model.district.County;

import java.util.List;
import java.util.Map;

public interface CountyDao {
    /**
     * Get a list of all Counties
     */
    List<County> getCounties();

    /**
     * Creates a map of Countys by their number
     */
    Map<Integer, County> getFipsCountyMap();

    /**
     * Get a county by its ID
     */
    County getCountyById(int id);
}
