package gov.nysenate.sage.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

/**
 * Utility class for performing JSON object serialization/de-serialization and mapping.
*/
public final class FormatUtil {
    private static final Logger logger = LoggerFactory.getLogger(FormatUtil.class);
    private static final int PRINT_BATCH_SIZE = 10000;

    private FormatUtil() {}

    public static String cleanString(String string) {
        if (string != null && string.isEmpty()) {
            return Jsoup.clean(string, Safelist.basic());
        }
        return string;
    }

    public static String toCamelCase(String s) {
        if (s != null && s.contains("_")) {
            return StringUtils.uncapitalize(WordUtils.capitalizeFully(s, '_').replaceAll("_", ""));
        }
        return s;
    }

    /**
     * Returns JSON representation of object.
     * Failure to map object results in empty string.
     *
     * @return String   JSON string
     * */
    public static String toJsonString(Object o) {
        var om = new ObjectMapper();
        try {
            return om.writeValueAsString(o);
        }
        catch(Exception ex) {
            logger.error("Object to JSON Error: ".concat(ex.getMessage()));
            return "";
        }
    }

    /**
     * Converts each T into a line, writes each line, flushing periodically.
     */
    public static <T> void writeLines(BufferedWriter writer, Collection<T> data, Function<T, String> toLine)
            throws IOException {
        int lineCount = 0;
        for (T lineData : data) {
            writer.write(toLine.apply(lineData));
            writer.newLine();
            if (++lineCount % PRINT_BATCH_SIZE == 0) {
                writer.flush();
            }
        }
        writer.flush();
    }
}
