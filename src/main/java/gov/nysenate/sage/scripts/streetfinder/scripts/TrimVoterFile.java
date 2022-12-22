package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class TrimVoterFile {
    // There are often multiple voters at the same address, and these duplicates should be removed.
    private static final Set<String> voterData = new HashSet<>(10000000);
    private static int inactiveOrPurged = 0, noHouseNum = 0, duplicates = 0, fracAddresses = 0;

    /**
     * Converts the full voter file into a trimmed version,
     * which contains all the data we would use.
     * @param args the location of the voter file, then the location of the trimmed file.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Incorrect number of args!");
            return;
        }
        var output = new File(args[1]);
        output.delete();
        try (var scanner = new Scanner(Path.of(args[0]), StandardCharsets.US_ASCII)) {
            while (scanner.hasNextLine()) {
                parseLine(scanner.nextLine());
            }
            var outputWriter = getPrintWriter(args[1]);
            int lineCount = 0;
            int fileNum = 0;
            for (String line : voterData) {
                if (++lineCount > 1000000) {
                    lineCount = 0;
                    outputWriter.flush();
                    outputWriter.close();
                    outputWriter = getPrintWriter(args[1].replace(".txt", "_" + ++fileNum + ".txt"));
                }
                outputWriter.write(line);
                outputWriter.write('\n');
            }
            outputWriter.flush();
            outputWriter.close();
            System.out.println(inactiveOrPurged + " inactive or purged lines");
            System.out.println(noHouseNum + " alternate addresses were skipped");
            System.out.println(duplicates + " duplicate valid addresses were found");
            System.out.println(voterData.size() + " final entries");
            System.out.println(fracAddresses + " fractional addresses");
        }
    }

    private static PrintWriter getPrintWriter(String filename) throws IOException {
        return new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
    }

    private static void parseLine(String line) {
        String[] fields = line.split("\",\"", -1);
        if (fields.length != 47) {
            System.err.println("This line had " + fields.length + " fields instead of 47: " + line);
        }
        if (fields[41].matches("[IP]")) {
            inactiveOrPurged++;
            return;
        }
        if (fields[4].isBlank()) {
            noHouseNum++;
            return;
        }
        if (!fields[5].isBlank()) {
            fracAddresses++;
        }

        // RADDNUMBER - RZIP4
        String[] relevantDataPart1 = Arrays.copyOfRange(fields, 4, 15);
        // COUNTYCODE - AD
        String[] relevantDataPart2 = Arrays.copyOfRange(fields, 23, 31);
        String tsvLine = String.join("\t", relevantDataPart1) + "\t" +
                String.join("\t", relevantDataPart2);
        if (!voterData.add(tsvLine.trim())) {
            duplicates++;
        }
    }
}
