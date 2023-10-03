package gov.nysenate.sage.scripts.streetfinder.scripts;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterDistrictMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Scanner;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.*;

public class ConvertVoterfileToStreetfile {
    /**
     * Converts the full voter file into a streetfile, and prints some status data.
     * @param args the working directory, then the name of the voter file.
     */
    public static void main(String[] args) throws IOException {
        System.out.println(LocalTime.now());
        if (args.length != 2) {
            System.err.println("Incorrect number of args!");
            return;
        }
        // Adds trailing slash.
        if (!args[0].matches(".*/$")) {
            args[0] = args[0] + "/";
        }
        args[1] = args[1].trim();

        var map = new VoterFileData(4000000, RHALFCODE, RPREDIRECTION, RAPARTMENTTYPE);
        int count = 0;
        VoterFileLineMap currLineMap;
        try (var scanner = new Scanner(Path.of(args[0], args[1]), StandardCharsets.US_ASCII)) {
            while (scanner.hasNextLine()) {
                currLineMap = new VoterFileLineMap(scanner.nextLine());
                map.putData(currLineMap);
                if (++count%1000000 == 0) {
                    System.out.println("Total lines parsed: " + count/1000000 + " million");
                    System.out.println("Interned district size: " + VoterDistrictMap.internedSize());
                }
            }
        }
        // Clears out old files.
        for (File file : Objects.requireNonNull(new File(args[0]).listFiles())) {
            if (!args[1].equals(file.getName())) {
                file.delete();
            }
        }
        map.writeData(args[0]);
        map.printSummary();
        System.out.println(LocalTime.now());
    }
}
