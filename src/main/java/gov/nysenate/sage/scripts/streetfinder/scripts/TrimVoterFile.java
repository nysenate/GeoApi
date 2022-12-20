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
        try (var outputWriter = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
             var scanner = new Scanner(Path.of(args[0]), StandardCharsets.US_ASCII)) {
            while (scanner.hasNextLine()){
                parseLine(scanner.nextLine());
            }
            for (String line : voterData) {
                outputWriter.write(line);
                outputWriter.write('\n');
            }
            outputWriter.flush();
        }
    }

    private static void parseLine(String line) {
        String[] fields = line.split("\",\"", -1);
        if (fields.length != 47) {
            System.err.println("This line had " + fields.length + " fields instead of 47: " + line);
            return;
        }
        // RADDNUMBER - RZIP4
        String[] relevantDataPart1 = Arrays.copyOfRange(fields, 4, 15);
        // COUNTYCODE - AD
        String[] relevantDataPart2 = Arrays.copyOfRange(fields, 23, 31);
        String csvLine = String.join("\t", relevantDataPart1) + "\t" +
                String.join("\t", relevantDataPart2);
        voterData.add(csvLine);
    }
}
