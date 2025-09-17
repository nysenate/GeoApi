package gov.nysenate.sage;

import gov.nysenate.sage.model.job.Column;
import gov.nysenate.sage.util.CountMap;

import java.util.LinkedHashMap;

public class DifferenceDataMap {
    private final LinkedHashMap<Column, CountMap<Difference>> internalMap = new LinkedHashMap<>();
    private int lineCount = 0;

    public void add(Column column, Difference difference) {
        internalMap.computeIfAbsent(column, k -> new CountMap<>()).put(difference);
    }

    public void incrementLineCount() {
        lineCount++;
    }

    public void addAll(DifferenceDataMap otherMap) {
        for (var colEntry : otherMap.internalMap.entrySet()) {
            internalMap.computeIfAbsent(colEntry.getKey(), k -> new CountMap<>())
                    .addAll(colEntry.getValue());
        }
        lineCount += otherMap.lineCount;
    }

    @Override
    public String toString() {
        var tempStr = new StringBuilder();
        tempStr.append("Total lines: ").append(lineCount).append("\n");
        for (var superEntry : internalMap.entrySet()) {
            // No need to clog output with perfect matches.
            if (superEntry.getValue().containsOnly(Difference.SAME)) {
                continue;
            }
            tempStr.append(superEntry.getKey()).append(":").append('\n');
            tempStr.append(superEntry.getValue().toString(lineCount, true));
            tempStr.append("\n");
        }
        return tempStr.toString();
    }
}
