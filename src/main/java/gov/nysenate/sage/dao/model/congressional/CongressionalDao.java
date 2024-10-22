package gov.nysenate.sage.dao.model.congressional;

import gov.nysenate.sage.model.district.Congressional;

import java.util.List;

public interface CongressionalDao {
    /**
     * @return a list of all current congressional members
     */
    List<Congressional> getCongressionals();


    /**
     * @param district number of the district
     * @return Congressional member for the specified district
     */
    Congressional getCongressionalByDistrict(int district);

    /**
     * Inserts a congressional object into the database
     */
    void insertCongressional(Congressional congressional);

    /**
     * Removes a congressional by district.
     */
    void deleteCongressional(int district);
}
