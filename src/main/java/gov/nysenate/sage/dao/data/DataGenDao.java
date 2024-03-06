package gov.nysenate.sage.dao.data;

import java.util.Map;

public interface DataGenDao {
    /**
     * Retrieves a mapping from town names to their abbreviations.
     */
    Map<String, String> getTownToAbbrevMap();

    /**
     * Retrieves a mapping from county names to their state Senate codes.
     */
    Map<String, String> getCountyToSenateCodeMap();
}
