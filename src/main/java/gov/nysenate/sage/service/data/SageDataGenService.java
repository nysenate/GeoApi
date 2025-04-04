package gov.nysenate.sage.service.data;

import java.io.IOException;

public interface SageDataGenService {
    /**
     * Generates the District metadata for the Senate, Assembly, and Congressional Candidates
     */
    void generateMetaData(String option) throws IOException;
}
