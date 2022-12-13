package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileField;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.ELECTION_CODE;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.WARD;

/**
 * Base class for all parsers, with some common code for parsing data.
 * @param <T> Usually just the normal StreetFinderAddress, but some extra functionality may be needed.
 */
public abstract class BaseParser<T extends StreetFileAddress> {
    private final SortedMap<String, List<String>> badLines = new TreeMap<>();

    // We don't use some parts of the data
    protected final BiConsumer<T, String> skip = (streetFinderAddress, s) -> {};
    // Building data has a common form.
    protected final List<BiConsumer<T, String>> buildingFunctions =
            List.of((sfa, s) -> sfa.setBuilding(true, s),
                    (sfa, s) -> sfa.setBuilding(false, s),
                    StreetFileAddress::setBldgParity);
    protected final BiConsumer<T, String> handlePrecinct = BaseParser::handlePrecinct;

    private final FileWriter fileWriter;
    private final PrintWriter outputWriter;
    protected final String filename;

    /**
     * The file is assumed to be a text file, either txt or csv.
     */
    public BaseParser(String filename) throws IOException{
        this.filename = filename;
        String output = filename.replaceAll("[.](txt|csv)", ".tsv");
        this.fileWriter = new FileWriter(output);
        this.outputWriter = new PrintWriter(fileWriter);
        // add columns for the tsv file
        outputWriter.print("street\ttown\tstate\tzip5\tbldg_lo_num\tbldg_lo_chr\tbldg_hi_num\tbldg_hi_chr\tbldg_parity\tapt_lo_num\tapt_lo_chr\tapt_hi_num\tapt_hi_chr\tapt_parity\telection_code\tcounty_code\t" +
                "assembly_code\tsenate_code\tcongressional_code\tboe_town_code\ttown_code\tward_code\tboe_school_code\tschool_code\tcleg_code\tcc_code\tfire_code\tcity_code\tvill_code\n");
    }

    public void parseFile() throws IOException {
        Scanner scanner = new Scanner(new File(filename));
        scanner.nextLine();
        while (scanner.hasNext()) {
            parseLine(scanner.nextLine());
        }
        scanner.close();
        closeWriters();
    }

    /**
     * Necessary to ensure the address is the proper type.
     * @return a newly instantiated object of a class that is, or extends, StreetFileAddress.
     */
    protected abstract T getNewAddress();

    /**
     * Each function takes in data, and properly assigns it to the new street address.
     * @return a class-specific list of functions.
     */
    protected abstract List<BiConsumer<T, String>> getFunctions();

    protected void parseLine(List<String> dataList, int minLength) {
        parseLine(String.join(",", dataList), minLength);
    }

    protected void parseLine(String line, int minLength) {
        parseLine(line, minLength, ",");
    }

    protected void parseLine(List<String> dataList) {
        parseLine(String.join(",", dataList));
    }

    protected void parseLine(String line) {
        parseLine(line, 0);
    }

    /**
     * Parses out data from a single streetfile line, and prints it to the file.
     * @param line raw data.
     * @param minLength some lines need checks to make sure they have the necessary data.
     * @param delim to split the line on (could be tab, whitespace, or comma seperated).
     */
    protected void parseLine(String line, int minLength, String delim) {
        var sfa = getNewAddress();
        var functions = getFunctions();
        String[] split = line.split(delim);
        if (minLength > split.length) {
            System.err.println("Error parsing line " + line);
        }
        for (int i = 0; i < Math.min(functions.size(), split.length); i++) {
            functions.get(i).accept(sfa, split[i]);
        }
        writeToFile(sfa);
    }

    /**
     * Utility method that checks if the given string is equal to a direction
     * Only checks if equal to "N", "E", "S", and "W"
     * @param string
     * @return true if a direction, false otherwise
     */
    // TODO: should allow more directions
    protected static boolean checkForDirection(String string) {
        return string.toUpperCase().matches( "[NESW]{1,2}");
    }

    protected void closeWriters() throws IOException {
        fileWriter.close();
        outputWriter.close();
        if (!badLines.isEmpty()) {
            System.err.println("\nThe following data could not be parsed from " + filename);
            for (String street : badLines.keySet()) {
                System.err.println(street);
                for (String line : badLines.get(street)) {
                    System.err.println("\t" + line);
                }
            }
        }
    }

    /**
     * Writes the StreetFinderAddress to the file in StreetFileForm by using the PrintWriter
     * @param streetFinderAddress
     */
    protected void writeToFile(T streetFinderAddress) {
        streetFinderAddress.normalize();
        outputWriter.print(streetFinderAddress.toStreetFileForm());
        outputWriter.flush();
    }

    protected BiConsumer<T, String> function(StreetFileField field) {
        return functions(field).get(0);
    }

    protected List<BiConsumer<T, String>> functions(StreetFileField... fields) {
        return functions(false, fields);
    }

    /**
     * Useful helper method to generate functions from a list of fields.
     * @param dashSplit if these values need to be split.
     * @param fields to be put into the address. Order matters here.
     * @return the functions to use while parsing.
     */
    protected List<BiConsumer<T, String>> functions(boolean dashSplit, StreetFileField... fields) {
        var functions = new ArrayList<BiConsumer<T, String>>(fields.length);
        for (var field : fields) {
            functions.add((streetFinderAddress, s) -> streetFinderAddress.put(field, dashSplit ? split(s) : s));
        }
        return functions;
    }

    protected List<BiConsumer<T, String>> skip(int num) {
        return Collections.nCopies(num, skip);
    }

    protected void putBadLine(String street, String line) {
        List<String> currList = badLines.getOrDefault(street, new ArrayList<>());
        currList.add(line);
        badLines.put(street, currList);
    }

    /**
     * Some data values are preceded by a label (e.g. SE-2) that needs to be skipped.
     * @param input the raw value.
     * @return properly formatted value.
     */
    private static String split(String input) {
        var split = input.split("-");
        return split.length > 1 ? split[1] : input;
    }

    /**
     * In some files, multiple fields are in the same number.
     */
    private static void handlePrecinct(StreetFileAddress streetFileAddress, String precinct) {
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        streetFileAddress.setTownCode(precinct.substring(0, 2));
        streetFileAddress.put(WARD, precinct.substring(2, 4));
        streetFileAddress.put(ELECTION_CODE, precinct.substring(precinct.length() - 2));
    }
}
