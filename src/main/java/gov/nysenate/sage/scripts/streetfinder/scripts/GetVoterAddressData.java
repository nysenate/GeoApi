package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.google.common.collect.ArrayListMultimap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.SBOEID;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileFieldType.DISTRICT;

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
                map.putData(new VoterFileLineMap(scanner.nextLine()));
                if (++count%1000000 == 0) {
                    System.out.println("Total lines parsed: " + count/1000000 + " million");
                }
            }
        }
        System.out.println("Finished parsing lines. Consolidating matches...");
        count = 0;
        ArrayListMultimap<String, VoterFileLineMap> mulMatch = ArrayListMultimap.create();
        try (var scanner = new Scanner(Path.of(args[0], args[1]), StandardCharsets.US_ASCII)) {
            while (scanner.hasNextLine()) {
                var lineMap = new VoterFileLineMap(scanner.nextLine());
                if (++count%1000000 == 0) {
                    System.out.println("Total lines parsed: " + count/1000000 + " million");
                }
                if (lineMap.getType() != VoterFileLineType.VALID || lineMap.get(VoterFileField.STATUS).matches("[IP]")) {
                    continue;
                }
                if (map.multipleDistrictMatch.containsKey(lineMap.getAddress())) {
                    mulMatch.put(lineMap.getAddress(), lineMap);
                }
            }
        }
        List<String> multipleLineMatches = new ArrayList<>();
        for (var entry : mulMatch.asMap().entrySet()) {
            multipleLineMatches.add(entry.getKey());
            List<VoterFileField> relevantFields = new ArrayList<>();
            for (var field : VoterFileField.streetFileFields) {
                // Shouldn't display districts in common.
                if (field.getType() == DISTRICT &&
                        entry.getValue().stream().map(lineMap -> lineMap.get(field)).distinct().count() != 1) {
                    relevantFields.add(field);
                }
            }

            ArrayListMultimap<String, String> districtsToIds = ArrayListMultimap.create();
            for (VoterFileLineMap lineMap : entry.getValue()) {
                var districts = relevantFields.stream().map(lineMap::get).collect(Collectors.joining("\",\""));
                districtsToIds.put(districts, lineMap.get(SBOEID));
            }

            // To label the fields
            multipleLineMatches.add("\"" + relevantFields.stream().map(Enum::name).collect(Collectors.joining("\",\"")) + "\": SBOEIDs");
            for (var districtAndIds : districtsToIds.asMap().entrySet()) {
                multipleLineMatches.add("\t" + districtAndIds.getKey() + ": " + String.join(",", districtAndIds.getValue()));
            }
        }

        SafeFastWriter.writeLines(args[0] + "AddressVoterFile.txt", map.entrySet(),
                entry -> entry.getKey() + '\t' + entry.getValue());
        SafeFastWriter.writeLines(args[0] + "MultipleDistrictsList.txt", multipleLineMatches, Function.identity());
        map.printSummary();
        map.printBadLines();
    }
}
