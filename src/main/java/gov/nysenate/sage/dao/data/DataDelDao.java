package gov.nysenate.sage.dao.data;

import java.util.List;

public interface DataDelDao {

    public Integer getGeocacheDistinctZipCodesCount();

    public List<String> getGeocacheZipBatch(int limit, int offset);

    public void deleteZipInGeocache(String zip);

    public Integer getGeocacheDistinctStatesCount();

    public List<String> getGeocacheStateBatch(int limit, int offset);

    public void deleteStateInGeocache(String state);
}
