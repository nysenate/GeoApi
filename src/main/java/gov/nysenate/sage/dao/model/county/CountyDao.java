package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.model.district.County;

import java.util.List;
import java.util.Map;

public interface CountyDao {

    /**
     * Get a list of all Counties
     * @return
     */
    public List<County> getCounties();

    /**
     * Creates a map of County's by their number
     * @return
     */
    public Map<Integer, County> getFipsCountyMap();

    /**
     * Get a county by its id
     * @param id
     * @return
     */
    public County getCountyById(int id);

    /**
     * Get a county by its name
     * @param name
     * @return
     */
    public County getCountyByName(String name);

    /**
     * Get a county by its fips code
     * @param fipsCode
     * @return
     */
    public County getCountyByFipsCode(int fipsCode);
}
