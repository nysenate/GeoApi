package gov.nysenate.sage.model.stats;

import java.sql.Timestamp;
import java.util.List;

public record ApiUsageStats(Timestamp rangeFrom, Timestamp rangeTo, List<ApiHourlyUsage> usageCounts) {
    /**
     * Represents usage count at a given time slice
     */
    public static class ApiHourlyUsage {
        public Timestamp time;
        public int count;

        public ApiHourlyUsage(Timestamp time, int count) {
            this.time = time;
            this.count = count;
        }
    }
}
