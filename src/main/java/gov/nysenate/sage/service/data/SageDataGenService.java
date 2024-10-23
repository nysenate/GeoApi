package gov.nysenate.sage.service.data;

import java.io.IOException;

public interface SageDataGenService {
    /**
     * Manually Updates the senator cache used by the front end
     */
    void updateSenatorCache();

    /**
     * Generates the District metadata for the Senate, Assembly, and Congressional Candidates
     */
    Object generateMetaData(String option) throws IOException;
}
