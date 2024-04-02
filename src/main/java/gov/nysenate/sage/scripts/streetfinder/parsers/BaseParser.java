package gov.nysenate.sage.scripts.streetfinder.parsers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;
import gov.nysenate.sage.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Base class for all parsers, with some common code for parsing data.
 * Pareses data from a text file.
 */
public abstract class BaseParser {
    protected final File file;
    protected DistrictingData data;
    protected final Multimap<StreetfileLineType, String> improperLineMap = ArrayListMultimap.create();
    protected final StreetfileDataExtractor dataExtractor = getDataExtractor();
    private final String lineRegex = "\\s*\"%s\"\\s*".formatted(delim());

    public BaseParser(File file) {
        this.file = file;
        try {
            this.data = new DistrictingData(FileUtil.getLineCount(file), 1);
        } catch (Exception ex) {
            this.data = null;
        }
    }

    public boolean isRangeData() {
        return true;
    }

    public void parseFile() throws IOException {
        var scanner = new Scanner(file);
        int lineNum = 0;
        while (scanner.hasNextLine()) {
            addData(lineNum++, parseLine(scanner.nextLine()));
        }
        scanner.close();
    }

    public Multimap<StreetfileLineType, String> getImproperLineMap() {
        return improperLineMap;
    }

    public DistrictingData getData() {
        return data;
    }

    protected abstract StreetfileDataExtractor getDataExtractor();

    /**
     * Parses out data from a single streetfile line, and prints it to the file.
     * @param line raw data.
     */
    protected String[] parseLine(String line) {
        line = line.replaceAll("^\"|\"$", "");
        return line.split(lineRegex, 0);
    }

    protected void addData(int lineNum, String... dataFields) {
        if (dataFields != null) {
            addData(lineNum, List.of(dataFields));
        }
    }

    protected void addData(int lineNum, List<String> dataFields) {
        data.put(dataExtractor.getData(lineNum, dataFields));
    }

    protected String delim() {
        return ",";
    }
}
