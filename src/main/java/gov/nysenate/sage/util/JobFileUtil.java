package gov.nysenate.sage.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.supercsv.prefs.CsvPreference;

import java.io.*;

public class JobFileUtil
{
    private CsvPreference getCsvPreference(File file)
    {
        try {
            BufferedReader sourceReader = new BufferedReader(new FileReader(file));
            String firstLine = sourceReader.readLine();

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
                case  0 : return CsvPreference.TAB_PREFERENCE;
                case  1 : return CsvPreference.STANDARD_PREFERENCE;
                case  2 : return CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
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
