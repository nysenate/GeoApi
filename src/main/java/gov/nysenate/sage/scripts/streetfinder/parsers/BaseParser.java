package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFileField;
import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.WARD;

public abstract class BaseParser {
    private static final BiConsumer<StreetFinderAddress, String> skip = (streetFinderAddress, s) -> {};
    protected static final List<BiConsumer<StreetFinderAddress, String>> buildingFunctions = List.of(
            (sfa, s) -> sfa.setBuilding(true, s), (sfa, s) -> sfa.setBuilding(false, s),
            StreetFinderAddress::setBldgParity
    );
    protected static final BiConsumer<StreetFinderAddress, String> handlePrecinct = BaseParser::handlePrecinct;

    private final FileWriter fileWriter;
    private final PrintWriter outputWriter;
    protected final String file;

    public BaseParser(String file) throws IOException {
        this.file = file;
        String output = file.replace(".txt", ".tsv");
        output = output.replace(".csv", ".tsv");            //in case its a csv file
        this.fileWriter = new FileWriter(output);
        this.outputWriter = new PrintWriter(fileWriter);
        //add columns for the tsv file
        outputWriter.print("street\ttown\tstate\tzip5\tbldg_lo_num\tbldg_lo_chr\tbldg_hi_num\tbldg_hi_chr\tbldg_parity\tapt_lo_num\tapt_lo_chr\tapt_hi_num\tapt_hi_chr\tapt_parity\telection_code\tcounty_code\t" +
                "assembly_code\tsenate_code\tcongressional_code\tboe_town_code\ttown_code\tward_code\tboe_school_code\tschool_code\tcleg_code\tcc_code\tfire_code\tcity_code\tvill_code\n");
    }

    public void parseFile() throws IOException {
        readFile();
    }

    /**
     * Utility method that scans through a file and calls the child classes version of
     * parseLine(String). This method is only intended for classes that extend this class and should not be used within
     * the NTSParser class
     * @throws IOException
     */
    protected void readFile() throws IOException {
        Scanner scanner = new Scanner(new File(file));
        String currentLine = scanner.nextLine();
        //While there is more lines in the file
        while (scanner.hasNext()) {
            currentLine = scanner.nextLine();
            parseLine(currentLine);
        }
        //close all writers/readers
        scanner.close();
        closeWriters();
    }

    protected abstract void parseLine(String line);

    /**
     * Utility method that checks if the given string is equal to a direction
     * Only checks if equal to "N", "E", "S", and "W"
     * @param string
     * @return true if a direction, false otherwise
     */
    protected static boolean checkForDirection(String string) {
        return string.toUpperCase().matches("[NESW]");
    }

    protected void parseLineFun(List<BiConsumer<StreetFinderAddress, String>> functions, String line) {
        var sfa = new StreetFinderAddress();
        String[] split = line.split(",");
        for (int i = 0; i < functions.size(); i++) {
            functions.get(i).accept(sfa, split[i]);
        }
        writeToFile(sfa);
    }

    protected void closeWriters() throws IOException {
        outputWriter.close();
        fileWriter.close();
    }

    /**
     * Writes the StreetFinderAddress to the file in StreetFileForm by using the PrintWriter
     * @param streetFinderAddress
     */
    protected void writeToFile(StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.normalize();
        outputWriter.print(streetFinderAddress.toStreetFileForm());
        outputWriter.flush();
    }

    protected BiConsumer<StreetFinderAddress, String> function(StreetFileField field) {
        return function(field, false);
    }

    protected BiConsumer<StreetFinderAddress, String> function(StreetFileField field, boolean dashSplit) {
        return (streetFinderAddress, s) -> streetFinderAddress.put(field, dashSplit ? split(s) : s);
    }

    protected List<BiConsumer<StreetFinderAddress, String>> skip(int num) {
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
        streetFinderAddress.setED(precinct.substring(precinct.length() - 2));
    }
}
