package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.COUNTYCODE;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.STATUS;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.VALID;

/**
 * Stores lines from a voter file as a map from address data -> district data.
 * Also stores information about addresses matched to different districts, and some summary information.
 */
public class VoterFileData {
    // One address should have one set of districts, but may have many people.
    // Sometimes, there are conflicts of multiple districts for the same address.
    private final LongMultiTable<VoterAddress, VoterDistrictMap> districtConflictMap;
    private final Multimap<VoterFileLineType, VoterFileLineMap> invalidLines = ArrayListMultimap.create();
    // Counts every unique value of certain fields, to print at the end.
    private final Map<VoterFileField, Map<String, Integer>> summaryMaps = new HashMap<>();

    public VoterFileData(int initialSize, VoterFileField... statusFields) {
        this.districtConflictMap = new LongMultiTable<>(initialSize, 1);
        for (VoterFileField field : statusFields) {
            summaryMaps.put(field, new HashMap<>());
        }
    }

    /**
     * Stores a line of data into the maps.
     */
    public void putData(VoterFileLineMap fieldMap) {
        if (fieldMap.getType() != VALID) {
            invalidLines.put(fieldMap.getType(), fieldMap);
            return;
        }
        // Inactive and purged voters do not have updated address information.
        if (fieldMap.get(STATUS).matches("[IP]")) {
            return;
        }
        for (VoterFileField field : summaryMaps.keySet()) {
            summaryMaps.get(field).merge(fieldMap.get(field), 1, Integer::sum);
        }
        districtConflictMap.put(new VoterAddress(fieldMap), VoterDistrictMap.getDistrictMap(fieldMap), fieldMap.getId());
    }

    public void writeData(String dir) throws IOException {
        BufferedWriter streetfileWriter = getWriter(dir, "AddressVoterFile.txt");
        BufferedWriter multipleDistrictsWriter = getWriter(dir, "MultipleDistrictsList.txt");

        for (VoterAddress addr : districtConflictMap.rows()) {
            var currRow = districtConflictMap.getRow(addr);
            if (currRow.size() == 1) {
                streetfileWriter.write(addr.stringValues());
                streetfileWriter.write('\t');
                streetfileWriter.write(currRow.keySet().iterator().next().stringValues());
                streetfileWriter.newLine();
            }
            // If one address maps to multiple districts, the data has problems, and should be separated.
            else {
                multipleDistrictsWriter.write(addr.stringValues().replaceAll("\t", " ")
                        .replaceAll(" {2,}", " "));
                multipleDistrictsWriter.newLine();
                for (String line : consolidateDistricts(currRow)) {
                    multipleDistrictsWriter.write(line);
                    multipleDistrictsWriter.newLine();
                }
                multipleDistrictsWriter.newLine();
            }
        }

        streetfileWriter.flush();
        streetfileWriter.close();
        multipleDistrictsWriter.flush();
        multipleDistrictsWriter.close();
        writeBadData(dir);
    }

    /**
     * Converts data on district mismatches to String form.
     * @param input the mismatch data at a single address.
     * @return readable TSV data on mismatches.
     */
    private static List<String> consolidateDistricts(Map<VoterDistrictMap, LongList> input) {
        SortedSet<VoterFileField> hasMismatch = new TreeSet<>();
        for (VoterFileField field : VoterDistrictMap.districtFields) {
            if (input.keySet().stream().map(map -> map.get(field)).distinct().count() != 1) {
                hasMismatch.add(field);
            }
        }
        // BOE always wants the county, mismatch or no.
        hasMismatch.add(COUNTYCODE);
        
        var lines = new ArrayList<String>();
        // Adds the label. Ids need special treatment: each entry will have a list of them.
        lines.add(hasMismatch.stream().map(Enum::name).collect(Collectors.joining("\t")) + "\t" + "SBOEIDs");
        for (var districtAndIds : input.entrySet()) {
            // Each item will end up as an Excel cell.
            List<String> currCells = new ArrayList<>();
            for (VoterFileField field : hasMismatch) {
                currCells.add(districtAndIds.getKey().getString(field));
            }
            String ids = districtAndIds.getValue().longStream().mapToObj(VoterFileData::getId)
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
        BufferedWriter invalidLineWriter = getWriter(dir, "InvalidLines.tsv");
        for (VoterFileLineType type : invalidLines.keySet()) {
            invalidLineWriter.write(type.name() + ":");
            invalidLineWriter.write("\t" + type.getCsv(Enum::name));
            for (VoterFileLineMap line : invalidLines.get(type)) {
                invalidLineWriter.newLine();
                invalidLineWriter.write("\t" + type.getCsv(line::get));
            }
            invalidLineWriter.newLine();
            invalidLineWriter.newLine();
        }
        invalidLineWriter.flush();
        invalidLineWriter.close();
    }

    public void printSummary() {
        System.out.println("\nStatus data: ");
        for (var fieldStatusEntry : summaryMaps.entrySet()) {
            System.out.println(fieldStatusEntry.getKey());
            var sortedEntries = fieldStatusEntry.getValue().entrySet()
                    .stream().sorted((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()))
                    .collect(Collectors.toList());
            for (var dataEntry : sortedEntries) {
                System.out.println("\t" + dataEntry.getKey() + " -> " + dataEntry.getValue());
            }
        }
        System.out.println();
    }
}
