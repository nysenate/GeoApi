package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Base class for all parsers, with some common code for parsing data.
 * Converts a text file (usually CSV) to a SQL TSV ready to import.
 * @param <T> Usually just the normal StreetFinderAddress, but some extra functionality may be needed.
 */
public abstract class BaseParser<T extends StreetFileAddress> {
    private static final String[] dummyArray = {};
    // The two variables below can't be static, due to use of generics.
    // Building data has a common form.
    protected final List<BiConsumer<T, String>> buildingFunctions =
            List.of((sfa, s) -> sfa.setBuilding(true, s),
                    (sfa, s) -> sfa.setBuilding(false, s),
                    StreetFileAddress::setBldgParity);

    protected final File file;
    protected final List<T> addresses = new ArrayList<>();
    private final SortedMap<String, List<String>> badlyFormattedLines = new TreeMap<>();
    private final StreetFileFunctionList<T> functions = getFunctions();

    public BaseParser(File file) {
        this.file = file;
    }

    public List<T> parseFile() throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lines.forEach(this::parseLine);
        }
        endProcessing();
        return addresses;
    }

    /**
     * Necessary to ensure the address is the proper type.
     * @return a newly instantiated object of a class that is, or extends, StreetFileAddress.
     */
    protected abstract T getNewAddress();

    /**
     * Each function takes in a String, and properly assigns it to the new street address.
     * @return a class-specific list of functions.
     */
    protected abstract StreetFileFunctionList<T> getFunctions();

    protected void parseLine(String line) {
        parseLine(line, ",");
    }

    /**
     * Parses out data from a single streetfile line, and prints it to the file.
     * @param line raw data.
     * @param delim to split the line on (tab, whitespace, or comma seperated).
     */
    protected void parseLine(String line, String delim) {
        parseData(line.split(delim));
    }

    protected void parseData(String... lineParts) {
        T addr = getNewAddress();
        functions.addDataToAddress(addr, lineParts);
        finalize(addr);
    }

    protected void parseData(List<String> lineParts) {
        parseData(lineParts.toArray(dummyArray));
    }

    protected void finalize(T sfa) {
        addresses.add(sfa);
    }

    /**
     * Only checks if equal to letter abbreviations.
     * @return true if a direction, false otherwise
     */
    protected static boolean isNotDirection(String part) {
        return !part.toUpperCase().matches("[NESW]{1,2}");
    }

    protected void endProcessing() {
        if (badlyFormattedLines.isEmpty()) {
            return;
        }
        System.err.println("\nThe following data could not be parsed from " + file.getName());
        for (String street : badlyFormattedLines.keySet()) {
            System.err.println(street);
            for (String line : badlyFormattedLines.get(street)) {
                System.err.println("\t" + line);
            }
        }
    }

    protected void putBadLine(String street, String line) {
        if (!badlyFormattedLines.containsKey(street)) {
            badlyFormattedLines.put(street, new ArrayList<>());
        }
        badlyFormattedLines.get(street).add(line);
    }

    /**
     * Sets the street name and street suffix from a single String.
     * Also checks for pre-direction.
     */
    protected static void setStreetAndSuffix(StreetFileAddress streetFileAddress, String streetNameAndSuffix) {
        LinkedList<String> splitList = new LinkedList<>(List.of(streetNameAndSuffix.split("\\s+")));
        streetFileAddress.setStreetSuffix(splitList.removeLast());
        streetFileAddress.put(STREET, String.join(" ", splitList));
    }

    /**
     * In some files, multiple fields are in the same number.
     */
    protected static void handlePrecinct(StreetFileAddress streetFileAddress, String precinct) {
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        streetFileAddress.setTownCode(precinct.substring(0, 2));
        streetFileAddress.put(WARD, precinct.substring(2, 4));
        streetFileAddress.put(ELECTION_CODE, precinct.substring(precinct.length() - 2));
    }
}
