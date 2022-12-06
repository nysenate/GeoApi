package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFileField;
import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.ELECTION_CODE;
import static gov.nysenate.sage.model.address.StreetFileField.WARD;

public abstract class BaseParser<T extends StreetFinderAddress> {
    private final BiConsumer<T, String> skip = (streetFinderAddress, s) -> {};
    protected final List<BiConsumer<T, String>> buildingFunctions =
            List.of((sfa, s) -> sfa.setBuilding(true, s),
                    (sfa, s) -> sfa.setBuilding(false, s),
                    StreetFinderAddress::setBldgParity);
    protected final BiConsumer<T, String> handlePrecinct = BaseParser::handlePrecinct;

    private final FileWriter fileWriter;
    private final PrintWriter outputWriter;
    protected final String file;

    public BaseParser(String file) throws IOException {
        this.file = file;
        String output = file.replaceAll("[.](txt|csv)", ".tsv");
        this.fileWriter = new FileWriter(output);
        this.outputWriter = new PrintWriter(fileWriter);
        //add columns for the tsv file
        outputWriter.print("street\ttown\tstate\tzip5\tbldg_lo_num\tbldg_lo_chr\tbldg_hi_num\tbldg_hi_chr\tbldg_parity\tapt_lo_num\tapt_lo_chr\tapt_hi_num\tapt_hi_chr\tapt_parity\telection_code\tcounty_code\t" +
                "assembly_code\tsenate_code\tcongressional_code\tboe_town_code\ttown_code\tward_code\tboe_school_code\tschool_code\tcleg_code\tcc_code\tfire_code\tcity_code\tvill_code\n");
    }

    public void parseFile() throws IOException {
        Scanner scanner = new Scanner(new File(file));
        scanner.nextLine();
        while (scanner.hasNext()) {
            parseLine(scanner.nextLine());
        }
        scanner.close();
        closeWriters();
    }

    protected abstract T getNewAddress();
    protected abstract List<BiConsumer<T, String>> getFunctions();

    protected void parseLine(String line) {
        parseLine(line, 0);
    }

    protected void parseLine(String line, int minLength) {
        var sfa = getNewAddress();
        var functions = getFunctions();
        String[] split = line.split(",");
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
        outputWriter.close();
        fileWriter.close();
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

    private static String split(String input) {
        var split = input.split("-");
        return split.length > 1 ? split[1] : input;
    }

    private static void handlePrecinct(StreetFinderAddress streetFinderAddress, String precinct) {
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        streetFinderAddress.setTownCode(precinct.substring(0, 2));
        streetFinderAddress.put(WARD, precinct.substring(2, 4));
        streetFinderAddress.put(ELECTION_CODE, precinct.substring(precinct.length() - 2));
    }
}
