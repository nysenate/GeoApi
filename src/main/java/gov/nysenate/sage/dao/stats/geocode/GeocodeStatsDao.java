package gov.nysenate.sage.dao.stats.geocode;

import gov.nysenate.sage.model.stats.GeocodeStats;

import java.sql.Timestamp;

public interface GeocodeStatsDao {
    /**
     * Retrieve geocode stats within a specified time frame.
     * @return GeocodeStats
     */
    GeocodeStats getGeocodeStats(Timestamp from, Timestamp to);
}
