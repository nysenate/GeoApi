package gov.nysenate.sage.dao.model.senate;

import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Senator;

import java.util.Collection;

public interface SenateDao {

    /**
     * Retrieve a collection of all Senators.
     * @return Collection of Senator
     */
    public Collection<Senator> getSenators();

    /**
     * Retrieve a senator by the senate district number.
     * @param senateCode Senate district number
     * @return           Senator
     */
    public Senator getSenatorByDistrict(int senateCode);

    /**
     * Inserts a Senator into the database.
     * @param senator
     */
    public void insertSenator(Senator senator);

    /**
     * Inserts a senate district and it's associated url into the database.
     * @param district
     */
    public void insertSenate(District district);

    /**
     * Clears the senate table. Note that this method can only be called
     * after clearing the senator table since there is a foreign key constraint.
     */
    public void deleteSenateDistricts();

    /**
     * Clears the senator table.
     */
    public void deleteSenators();

    /**
     * Deletes a senator entry with the given district.
     * @param district
     */
    public void deleteSenator(int district);

    /**
     * Refresh Senator map cache
     */
    public void updateSenatorCache();
}
