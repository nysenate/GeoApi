package gov.nysenate.sage.scripts.streetfinder.scripts;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.AddressToDistrictsMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.SafeFastWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;

public class GetVoterAddressData {
    /**
     * Converts the full voter file into a trimmed version,
     * which contains all the data we would use.
     * @param args the working directory, then the name of the voter file.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Incorrect number of args!");
            return;
        }
        // Adds trailing slash.
        if (!args[0].matches(".*/$")) {
            args[0] = args[0] + "/";
        }
        args[1] = args[1].trim();
        // Clears out old files.
        for (File file : Objects.requireNonNull(new File(args[0]).listFiles())) {
            if (!args[1].equals(file.getName())) {
                file.delete();
            }
        }

        var map = new AddressToDistrictsMap(10000000);
        int count = 0;
        try (var scanner = new Scanner(Path.of(args[0], args[1]), StandardCharsets.US_ASCII)) {
            while (scanner.hasNextLine()) {
                map.putData(scanner.nextLine());
                if (++count%1000000 == 0) {
                    System.out.println("Total lines parsed: " + count/1000000 + " million");
                }
            }
        }
        SafeFastWriter.writeLines(args[0] + "AddressVoterFile.txt", map.entrySet(),
                entry -> entry.getKey() + '\t' + entry.getValue());
        map.printSummary();
    }
}
