package gov.nysenate.sage.dao.data;

import java.util.List;

public interface DataDelDao {
    Integer getGeocacheDistinctZipCodesCount();

    List<String> getGeocacheZipBatch(int limit, int offset);

    void deleteZipInGeocache(String zip);

    Integer getGeocacheDistinctStatesCount();

    List<String> getGeocacheStateBatch(int limit, int offset);

    void deleteStateInGeocache(String state);
}
