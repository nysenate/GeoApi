package gov.nysenate.sage.scripts.streetfinder.scripts;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.AddressToDistrictsMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterDistrictMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Scanner;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.*;

public class GetVoterAddressData {
    /**
     * Converts the full voter file into a streetfile, plus some extra data.
     * @param args the working directory, then the name of the voter file.
     */
    public static void main(String[] args) throws IOException {
        // TODO: add better timer
        System.out.println(LocalDateTime.now());
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

        var map = new AddressToDistrictsMap(4000000, STATUS, RHALFCODE, RPREDIRECTION, RAPARTMENTTYPE);
        int count = 0;
        try (var scanner = new Scanner(Path.of(args[0], args[1]), StandardCharsets.US_ASCII)) {
            while (scanner.hasNextLine()) {
                map.putData(new VoterFileLineMap(scanner.nextLine()));
                if (++count%1000000 == 0) {
                    System.out.println("Total lines parsed: " + count/1000000 + " million");
                    System.out.println("Interned district size: " + VoterDistrictMap.size());
                }
            }
        }
        map.writeData(args[0]);
        map.printSummary();
        System.out.println(LocalDateTime.now());
    }
}
