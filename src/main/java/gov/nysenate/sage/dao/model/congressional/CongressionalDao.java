package gov.nysenate.sage.dao.model.congressional;

import gov.nysenate.sage.model.district.Congressional;

import java.util.List;

public interface CongressionalDao {

    /**
     * @return a list of all current congressional members
     */
    public List<Congressional> getCongressionals();


    /**
     * @param district number of the district
     * @return Congressional member for the specified district
     */
    public Congressional getCongressionalByDistrict(int district);

    /**
     * Inserts a congressional object into the database
     * @param congressional
     */
    public void insertCongressional(Congressional congressional);

    /**
     * Clears the congressional table.
     */
    public void deleteCongressionals();

    /**
     * Removes a congressional by district.
     */
    public void deleteCongressional(int district);
}
