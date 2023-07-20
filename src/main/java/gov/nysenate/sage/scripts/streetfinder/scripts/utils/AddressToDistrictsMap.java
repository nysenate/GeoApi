package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.util.Tuple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.COUNTYCODE;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.STATUS;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.NON_STANDARD_ADDRESS;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.VALID;

/**
 * Stores lines from a voter file as a map from address data -> district data.
 * Also stores information about addresses matched to different districts, and some summary information.
 */
public class AddressToDistrictsMap {
    // One address should have one set of districts, but may have many people.
    private final Map<VoterAddress, Tuple<VoterDistrictMap, List<Long>>> dataMap;
    // Contains all problematic data where the same address maps to multiple district sets.
    // Could theoretically contain all voter data, but this would waste a lot of memory.
    private final MultiTable<VoterAddress, VoterDistrictMap, Long> districtConflictMap = new MultiTable<>();
    private final Multimap<VoterFileLineType, VoterFileLineMap> invalidLines = ArrayListMultimap.create();
    private final Map<VoterFileField, Map<String, Integer>> summaryMaps = new HashMap<>();
    private int duplicates = 0;

    public AddressToDistrictsMap(int initialSize, VoterFileField... statusFields) {
        this.dataMap = new HashMap<>(initialSize);
        for (VoterFileField field : statusFields) {
            summaryMaps.put(field, new HashMap<>());
        }
    }

    /**
     * Stores a line of data into the maps.
     */
    public void putData(VoterFileLineMap fieldMap) {
        for (VoterFileField field : summaryMaps.keySet()) {
            summaryMaps.get(field).merge(fieldMap.get(field), 1, Integer::sum);
        }
        if (fieldMap.getType() != VALID) {
            invalidLines.put(fieldMap.getType(), fieldMap);
            return;
        }
        // Inactive and purged voters do not have updated address information.
        if (fieldMap.get(STATUS).matches("[IP]")) {
            return;
        }

        // Data is good to be inserted.
        var addr = new VoterAddress(fieldMap);
        var districts = VoterDistrictMap.getDistrictMap(fieldMap);
        var mappedTuple = dataMap.get(addr);
        if (districtConflictMap.containsRow(addr)) {
            districtConflictMap.put(addr, districts, fieldMap.getId());
        }
        else if (mappedTuple == null) {
            dataMap.put(addr, new Tuple<>(districts, new ArrayList<>(List.of(fieldMap.getId()))));
        }
        else {
            // No conflict, add the ID.
            if (mappedTuple.first().equals(districts)) {
                mappedTuple.second().add(fieldMap.getId());
                duplicates++;
            }
            else {
                var currTuple = dataMap.remove(addr);
                districtConflictMap.put(addr, currTuple.first(), currTuple.second());
                districtConflictMap.put(addr, districts, fieldMap.getId());
            }
        }
    }

    public void writeData(String dir) throws IOException {
        BufferedWriter streetfileWriter = getWriter(dir, "AddressVoterFile.txt");
        for (VoterAddress addr : dataMap.keySet()) {
            streetfileWriter.write(addr.toString());
            streetfileWriter.write('\t');
            streetfileWriter.write(dataMap.get(addr).toString());
            streetfileWriter.newLine();
        }
        streetfileWriter.flush();
        streetfileWriter.close();

        BufferedWriter multipleDistrictsWriter = getWriter(dir, "MultipleDistrictsList.txt");
        for (VoterAddress addr : districtConflictMap.rows()) {
            multipleDistrictsWriter.write(addr.toString().replaceAll("\t", " ")
                    .replaceAll(" {2,}", " "));
            multipleDistrictsWriter.newLine();
            for (String line : consolidateDistricts(districtConflictMap.getRow(addr))) {
                multipleDistrictsWriter.write(line);
                multipleDistrictsWriter.newLine();
            }
            multipleDistrictsWriter.newLine();
        }
        multipleDistrictsWriter.flush();
        multipleDistrictsWriter.close();

        // TODO: finish
        Writer nonStandardAddressWriter = getWriter(dir, "AltAddresses.txt");
        writeBadData(dir);
    }


    // TODO: more consolidation, on types?
    /**
     * Converts data on district mismatches to String form.
     * @param input the mismatch data at a single address.
     * @return readable TSV data on mismatches.
     */
    private static List<String> consolidateDistricts(Map<VoterDistrictMap, List<Long>> input) {
        SortedSet<VoterFileField> hasMismatch = new TreeSet<>();
        for (VoterFileField field : VoterDistrictMap.districtFields) {
            if (input.keySet().stream().map(map -> map.get(field)).distinct().count() != 1) {
                hasMismatch.add(field);
            }
        }
        // BOE always wants the county, mismatch or no.
        hasMismatch.add(COUNTYCODE);
        
        var lines = new ArrayList<String>();
        // Adds the label
        lines.add(hasMismatch.stream().map(Enum::name).collect(Collectors.joining("\t")) + "\t" + "SBOEIDs");
        for (var districtAndIds : input.entrySet()) {
            // Each item will end up as an Excel cell.
            List<String> currCells = new ArrayList<>();
            for (VoterFileField field : hasMismatch) {
                currCells.add(districtAndIds.getKey().get(field));
            }
            String ids = districtAndIds.getValue().stream().map(AddressToDistrictsMap::getId)
                    .collect(Collectors.joining(","));
            currCells.add(ids);
            lines.add(String.join("\t", currCells));
        }
        return lines;
    }

    private static String getId(Long id) {
        String idStr = id.toString();
        return "NY" + "0".repeat(18 - idStr.length()) + idStr;
    }

    private static BufferedWriter getWriter(String dir, String filename) throws IOException {
        return new BufferedWriter(new FileWriter(dir + filename, true));
    }

    private void writeBadData(String dir) throws IOException {
        BufferedWriter invalidLineWriter = getWriter(dir, "InvalidLines.txt");
        for (VoterFileLineType type : invalidLines.keySet()) {
            invalidLineWriter.write(type.name() + ":");
            invalidLineWriter.write("\t" + type.getCsv(Enum::name));
            for (VoterFileLineMap line : invalidLines.get(type)) {
                invalidLineWriter.write("\t" + type.getCsv(line::get));
                invalidLineWriter.newLine();
            }
            invalidLineWriter.newLine();
        }
        invalidLineWriter.flush();
        invalidLineWriter.close();
    }

    public void printSummary() {
        System.out.println("\nStatus data: ");
        for (var fieldStatusEntry : summaryMaps.entrySet()) {
            System.out.println(fieldStatusEntry.getKey());
            for (var dataEntry : fieldStatusEntry.getValue().entrySet()) {
                System.out.println("\t" + dataEntry.getKey() + " -> " + dataEntry.getValue());
            }
        }
        System.out.println();
        System.out.println(dataMap.size() + " standard entries");
        System.out.println(invalidLines.get(NON_STANDARD_ADDRESS).size() + " alternate addresses");
        System.out.println(duplicates + " duplicate entries.");
    }
}
