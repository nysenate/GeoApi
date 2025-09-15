package gov.nysenate.sage;

import gov.nysenate.sage.model.job.Column;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DifferenceDataMap {
    private final LinkedHashMap<Column, Map<Difference, Integer>> internalMap = new LinkedHashMap<>();
    private int lineCount = 0;

    public void add(Column column, Difference difference) {
        internalMap.computeIfAbsent(column, k -> new EnumMap<>(Difference.class))
                .merge(difference, 1, Integer::sum);
    }

    public void incrementLineCount() {
        lineCount++;
    }

    public void addAll(DifferenceDataMap otherMap) {
        for (var colEntry : otherMap.internalMap.entrySet()) {
            for (var diffEntry : colEntry.getValue().entrySet()) {
                internalMap.computeIfAbsent(colEntry.getKey(), k -> new EnumMap<>(Difference.class))
                        .merge(diffEntry.getKey(), diffEntry.getValue(), Integer::sum);
            }
        }
        lineCount += otherMap.lineCount;
    }

    @Override
    public String toString() {
        var tempStr = new StringBuilder();
        tempStr.append("Total lines: ").append(lineCount).append("\n");
        for (var superEntry : internalMap.entrySet()) {
            // No need to clog output with perfect matches.
            if (superEntry.getValue().size() == 1 && superEntry.getValue().containsKey(Difference.SAME)) {
                continue;
            }
            tempStr.append(superEntry.getKey()).append(":").append('\n');
            for (var entry : superEntry.getValue().entrySet()) {
                tempStr.append("\t%s: %.2f%%%n".formatted(entry.getKey(), 100.0 * entry.getValue() / lineCount));
            }
            tempStr.append("\n");
        }
        return tempStr.toString();
    }
}
