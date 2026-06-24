package gov.nysenate.sage;

import com.google.common.collect.ImmutableList;
import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.job.Column;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatchTest {
    private static final double degPerFt = .00000274;
    private static final int feet = 50;
    private static final List<String> counties = List.of("Kings", "Queens", "Richmond", "Bronx", "New_York", "Suffolk", "Nassau"), districts;
    static {
        var tempList = new ArrayList<String>();
        for (int dist = 1; dist <= 63; dist++) {
            tempList.add("SD" + dist + '_');
        }
        districts = ImmutableList.copyOf(tempList);
    }

    private enum Option {
        ALL, COUNTY_ONLY, SENATE_DISTRICT_ONLY;

        public List<String> getStringsToMatch() {
            var tempList = new ArrayList<String>();
            if (this != COUNTY_ONLY) {
                tempList.addAll(districts);
            }
            if (this != SENATE_DISTRICT_ONLY) {
                tempList.addAll(counties);
            }
            if (this == ALL) {
                tempList.add("PO_Box");
            }
            return ImmutableList.copyOf(tempList);
        }
    }

    public static void main(String[] args) throws IOException {
        List<Path> files;
        try (var tempFiles = Files.list(Path.of(args[0]))) {
            files = new ArrayList<>(tempFiles.toList());
        }
        if (files.size()%2 != 0) {
            System.err.println("Should have pairs of files to compare!");
            return;
        }
        List<String> toMatch = Option.valueOf(args[1]).getStringsToMatch();
        var fullDataMap = new DifferenceDataMap();
        for (String currMatch : toMatch) {
            List<Path> currDistFiles = files.stream()
                    .filter(path -> path.getFileName().toString().contains(currMatch)).toList();
            if (currDistFiles.size() != 2) {
                continue;
            }
            Path oldFile = currDistFiles.stream()
                    .filter(path -> path.getFileName().toString().contains("old"))
                    .findFirst().orElseThrow();
            Path newFile = currDistFiles.stream()
                    .filter(path -> path.getFileName().toString().contains("new"))
                    .findFirst().orElseThrow();
            files.remove(oldFile);
            files.remove(newFile);
            System.out.printf("Old file: %s, New file: %s%n",
                    oldFile.getFileName().toString(), newFile.getFileName().toString());
            List<String> oldFileLines = Files.readAllLines(oldFile);
            List<String> newFileLines = Files.readAllLines(newFile);
            if (oldFileLines.size() != newFileLines.size()) {
                System.err.println("Files should be the same length!");
                continue;
            }

            var currDataMap = new DifferenceDataMap();
            List<Column> columns = Arrays.stream(oldFileLines.getFirst().split("\t"))
                    .map(Column::resolveColumn).toList();
            for (int lineNum = 0; lineNum < oldFileLines.size(); lineNum++) {
                String[] oldFileRow = oldFileLines.get(lineNum).split("\t");
                String[] newFileRow = newFileLines.get(lineNum).split("\t");
                for (int colNum = 0; colNum < Math.max(oldFileRow.length, newFileRow.length); colNum++) {
                    Column currCol = columns.get(colNum);
                    String oldFileCell = colNum >= oldFileRow.length ? "" : oldFileRow[colNum].trim();
                    String newFileCell = colNum >= newFileRow.length ? "" : newFileRow[colNum].trim();
                    currDataMap.add(currCol, getDiff(currCol, oldFileCell, newFileCell));
                }
                currDataMap.incrementLineCount();
            }
            System.out.println(currDataMap);
            fullDataMap.addAll(currDataMap);
        }
        System.out.println("Full summary results follow.");
        System.out.println(fullDataMap);
    }

    private static Difference getDiff(Column currCol, String oldFileCell, String newFileCell) {
        if (oldFileCell.equals(newFileCell)) {
            return Difference.SAME;
        }
        if (oldFileCell.isEmpty()) {
            return Difference.IMPROVEMENT;
        }
        if (newFileCell.isEmpty()) {
            return Difference.REDUCTION;
        }
        if (currCol == Column.lon || currCol == Column.lat) {
            double oldGeo = Double.parseDouble(oldFileCell);
            double newGeo = Double.parseDouble(newFileCell);
            if (Math.abs(oldGeo - newGeo) > degPerFt * feet) {
                return Difference.MISMATCH;
            }
            else {
                return Difference.SAME;
            }
        }
        if (currCol == Column.geoQuality) {
            Accuracy oldAccuracy = Accuracy.fromString(oldFileCell);
            Accuracy newAccuracy = Accuracy.fromString(newFileCell);
            if (oldAccuracy.ordinal() > newAccuracy.ordinal()) {
                return Difference.REDUCTION;
            }
            if (oldAccuracy.ordinal() == newAccuracy.ordinal()) {
                return Difference.SAME;
            }
            return Difference.IMPROVEMENT;
        }
        return Difference.MISMATCH;
    }
}
