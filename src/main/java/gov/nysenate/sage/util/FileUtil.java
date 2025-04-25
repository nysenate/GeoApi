package gov.nysenate.sage.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.List;
import java.util.TreeMap;

public final class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {}

    /**
     * Determines the CsvPreference based on the delimiter used in the header.
     * The delimiter is assumed to be the character (`\t`, `,`, `:`) that occurs most often in the header.
     * It is also assumed that the header is the first line of the file.
     */
    public static CsvPreference getCsvPreference(File file) {
        String firstLine;
        try {
            var sourceReader = new BufferedReader(new FileReader(file));
            firstLine = sourceReader.readLine();
            sourceReader.close();
        } catch (IOException ex) {
            logger.error("{} when trying to determine csv preference.", ex.getClass().getSimpleName(), ex);
            return null;
        }
        var countMap = new TreeMap<Integer, String>();
        for (String delim : List.of("\t", ",", ";")) {
            countMap.put(StringUtils.countMatches(firstLine, delim), delim);
        }

        // Find the delimiter with the highest count
        return switch(countMap.lastEntry().getValue()) {
            case "\t" -> {
                logger.debug("Tab delimited");
                yield CsvPreference.TAB_PREFERENCE;
            }
            case "," -> {
                logger.debug("Comma delimited");
                yield CsvPreference.STANDARD_PREFERENCE;
            }
            case ";" -> {
                logger.debug("Semi-colon delimited");
                yield CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
            }
            default -> null;
        };
    }

    public static int getLineCount(File file) throws IOException {
        try (var reader = new LineNumberReader(new FileReader(file))) {
            reader.skip(Long.MAX_VALUE);
            return reader.getLineNumber();
        }
    }
}
