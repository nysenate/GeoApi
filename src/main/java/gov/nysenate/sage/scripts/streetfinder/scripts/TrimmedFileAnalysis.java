package gov.nysenate.sage.scripts.streetfinder.scripts;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility script to analyze data in the trimmed voter file.
 * args = [directory with trimmed voter file parts]
 */
public class TrimmedFileAnalysis {
    private static final AddressToDistrictsMap dataMap = new AddressToDistrictsMap();
    private static double badDataCount = 0;

    public static void main(String[] args) throws IOException {
        List<String> lines = new ArrayList<>(7640000);
        for (File file : new File(args[0]).listFiles()) {
            // These are formatted differently, so skip them.
            if (file.getName().contains("Alt")) {
                continue;
            }
            lines.addAll(Files.readAllLines(file.toPath()));
        }

        for (String line : lines) {
            line = line.toUpperCase();
            String[] parts = line.split("\t");
            if (parts.length != 19) {
                System.err.println("Problem with length of line: " + line);
            }
            else if (!dataMap.putData(parts)) {
                badDataCount++;
            }
        }
        System.err.println("Bad pairs:\n");
        for (var entry : AddressToDistrictsMap.badSenateMap.entrySet()) {
            System.err.println(entry.getKey());
            for (var thing : entry.getValue().entrySet()) {
                System.err.println(thing.getKey() + ": " + thing.getValue());
            }
            System.err.println();
        }
        System.err.println(badDataCount + " entries were bad.");
    }
}
