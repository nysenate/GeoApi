package gov.nysenate.sage.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.supercsv.prefs.CsvPreference;

import java.io.*;

public class JobFileUtil
{
    private static Logger logger = Logger.getLogger(JobFileUtil.class);

    /**
     * Determines the CsvPreference based on the delimiter used in the header.
     * The delimiter is assumed to be the character (`\t', `,`, `:`) that occurs most often in the header.
     * It is also assumed that the header is the first line of the file.
     * @param file
     * @return
     */
    public static CsvPreference getCsvPreference(File file)
    {
        try {
            BufferedReader sourceReader = new BufferedReader(new FileReader(file));
            String firstLine = sourceReader.readLine();

            IOUtils.closeQuietly(sourceReader);
            IOUtils.closeQuietly(sourceReader);
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

            switch (index) {
                case -1 : return null;
                case  0 : logger.debug("Tab delimited"); return CsvPreference.TAB_PREFERENCE;
                case  1 : logger.debug("Comma delimited"); return CsvPreference.STANDARD_PREFERENCE;
                case  2 : logger.debug("Semi-colon delimited"); return CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
                default : return null;
            }
        }
        catch (FileNotFoundException ex) {
            logger.error("File not found when trying to determine csv preference.", ex);
        }
        catch (IOException ex) {
            logger.error("IO Exception when trying to determine csv preference", ex);
        }
        return null;
    }
}
