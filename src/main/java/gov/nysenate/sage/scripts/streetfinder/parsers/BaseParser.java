package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.SortedStringMultiMap;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.ELECTION_CODE;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.WARD;

/**
 * Base class for all parsers, with some common code for parsing data.
 * Converts a text file (usually CSV) to a SQL TSV ready to import.
 * @param <T> Usually just the normal StreetFinderAddress, but some extra functionality may be needed.
 */
public abstract class BaseParser<T extends StreetFileAddressRange> {
    private static final String[] dummyArray = {};
    // The two variables below can't be static, due to use of generics.
    // Building data has a common form.
    protected final List<BiConsumer<T, String>> buildingFunctions =
            List.of((range, s) -> range.setBuilding(true, s),
                    (range, s) -> range.setBuilding(false, s),
                    StreetFileAddressRange::setBldgParity);

    protected final File file;
    protected final List<T> addresses = new ArrayList<>();
    protected final SortedStringMultiMap badLines = new SortedStringMultiMap();
    private final StreetFileFunctionList<T> functions = getFunctions();

    public BaseParser(File file) {
        this.file = file;
    }

    public void parseFile() throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lines.forEach(this::parseLine);
        }
    }

    public List<T> getParsedAddresses() {
        return addresses;
    }

    public SortedStringMultiMap getBadLines() {
        return badLines;
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

    protected void finalize(T range) {
        addresses.add(range);
    }

    /**
     * Only checks if equal to letter abbreviations.
     * @return true if a direction, false otherwise
     */
    protected static boolean isNotDirection(String part) {
        return !part.matches("(?i)[NESW]{1,2}");
    }

    /**
     * In some files, multiple fields are in the same number.
     */
    protected static void handlePrecinct(StreetFileAddressRange range, String precinct) {
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        range.setTownCode(precinct.substring(0, 2));
        range.put(WARD, precinct.substring(2, 4));
        range.put(ELECTION_CODE, precinct.substring(precinct.length() - 2));
    }
}
