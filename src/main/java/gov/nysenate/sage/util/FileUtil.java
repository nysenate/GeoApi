package gov.nysenate.sage.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.prefs.CsvPreference;

import java.io.*;

public final class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {}

    /**
     * Determines the CsvPreference based on the delimiter used in the header.
     * The delimiter is assumed to be the character (`\t', `,`, `:`) that occurs most often in the header.
     * It is also assumed that the header is the first line of the file.
     * @param file
     * @return
     */
    public static CsvPreference getCsvPreference(File file) {
        try {
            BufferedReader sourceReader = new BufferedReader(new FileReader(file));
            String firstLine = sourceReader.readLine();

            sourceReader.close();
            int [] delimiterCounts = new int[3];
            delimiterCounts[0] = StringUtils.countMatches(firstLine, "\t");
            delimiterCounts[1] = StringUtils.countMatches(firstLine, ",");
            delimiterCounts[2] = StringUtils.countMatches(firstLine, ";");

            int max = 0, index = -1;
            for (int i = 0; i < delimiterCounts.length; i++) {
                if (delimiterCounts[i] > max) {
                    index = i;
                    max = delimiterCounts[i];
                }
            }

            return switch (index) {
                case 0 -> {
                    logger.debug("Tab delimited");
                    yield CsvPreference.TAB_PREFERENCE;
                }
                case 1 -> {
                    logger.debug("Comma delimited");
                    yield CsvPreference.STANDARD_PREFERENCE;
                }
                case 2 -> {
                    logger.debug("Semi-colon delimited");
                    yield CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
                }
                default -> null;
            };
        }
        catch (IOException ex) {
            logger.error(ex.getClass().getSimpleName() + " when trying to determine csv preference.", ex);
            return null;
        }
    }

    public static int getLineCount(File file) throws IOException {
        try (var reader = new LineNumberReader(new FileReader(file))) {
            reader.skip(Long.MAX_VALUE);
            return reader.getLineNumber();
        }
    }
}
