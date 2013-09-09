package gov.nysenate.sage.model.stats;

import java.sql.Timestamp;
import java.util.List;

public class ApiUsageStats
{
    /**
     * Represents usage count at a given time slice
     */
    public static class IntervalUsage {
        public Timestamp time;
        public int count;

        public IntervalUsage(Timestamp time, int count) {
            this.time = time;
            this.count = count;
        }
    }

    private int intervalSizeInMinutes;
    private Timestamp intervalFrom;
    private Timestamp intervalTo;
    private List<IntervalUsage> intervalUsageCounts;

    public ApiUsageStats() {}

    public int getIntervalSizeInMinutes() {
        return intervalSizeInMinutes;
    }

    public void setIntervalSizeInMinutes(int intervalSizeInMinutes) {
        this.intervalSizeInMinutes = intervalSizeInMinutes;
    }

    public Timestamp getIntervalFrom() {
        return intervalFrom;
    }

    public void setIntervalFrom(Timestamp intervalFrom) {
        this.intervalFrom = intervalFrom;
    }

    public Timestamp getIntervalTo() {
        return intervalTo;
    }

    public void setIntervalTo(Timestamp intervalTo) {
        this.intervalTo = intervalTo;
    }

    public List<IntervalUsage> getIntervalUsageCounts() {
        return intervalUsageCounts;
    }

    public void setIntervalUsageCounts(List<IntervalUsage>intervalUsageCounts) {
        this.intervalUsageCounts = intervalUsageCounts;
    }
}