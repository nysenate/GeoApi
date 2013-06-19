package gov.nysenate.sage.model.stats;

import java.sql.Timestamp;
import java.util.Map;

public class ApiUsageStats
{
    private int intervalSizeInMinutes;
    private Timestamp intervalFrom;
    private Timestamp intervalTo;
    private Map<Timestamp, Integer> intervalUsageCounts;

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

    public Map<Timestamp, Integer> getIntervalUsageCounts() {
        return intervalUsageCounts;
    }

    public void setIntervalUsageCounts(Map<Timestamp, Integer> intervalUsageCounts) {
        this.intervalUsageCounts = intervalUsageCounts;
    }
}