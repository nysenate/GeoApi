package gov.nysenate.sage.dao.model.senate;

import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Senator;

import java.util.Collection;

public interface SenateDao {

    /**
     * Retrieve a collection of all Senators.
     * @return Collection of Senator
     */
    Collection<Senator> getSenators();

    /**
     * Retrieve a senator by the senate district number.
     * @param senateCode Senate district number
     * @return           Senator
     */
    Senator getSenatorByDistrict(int senateCode);

    /**
     * Inserts a Senator into the database.
     */
    void insertSenator(Senator senator);

    /**
     * Inserts a senate district and its associated url into the database.
     */
    void insertSenate(District district);

    /**
     * Clears the senator table.
     */
    void deleteSenators();

    /**
     * Deletes a senator entry with the given district.
     */
    void deleteSenator(int district);

    /**
     * Refresh Senator map cache
     */
    void updateSenatorCache();
}
