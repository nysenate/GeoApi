package gov.nysenate.sage.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

/**
 * Utility class for performing JSON object serialization/de-serialization and mapping.
*/
public final class FormatUtil {
    private static final Logger logger = LoggerFactory.getLogger(FormatUtil.class);
    private static final ObjectMapper mapper = jackson2JsonMapper().build();
    private static final int PRINT_BATCH_SIZE = 10000;

    private FormatUtil() {}

    /**
     * Jackson 3 changed defaults that are visible in serialized output: property order, date
     * and enum formats, escaped forward slashes, and the names of properties whose getter
     * starts with a single capital letter. Mappers configured here keep the Jackson 2 output
     * that API clients and stored JSON were built against.
     */
    public static <M extends ObjectMapper, B extends MapperBuilder<M, B>> B withJackson2Defaults(B builder) {
        return builder.configureForJackson2()
                .disable(MapperFeature.FIX_FIELD_NAME_UPPER_CASE_PREFIX);
    }

    /** A JSON mapper builder with the Jackson 2 defaults of both the mapper and its factory. */
    public static JsonMapper.Builder jackson2JsonMapper() {
        return withJackson2Defaults(JsonMapper.builder(JsonFactory.builderWithJackson2Defaults().build()));
    }

    public static String cleanString(String string) {
        if (string != null && !string.isEmpty()) {
            return Jsoup.clean(string, Safelist.basic());
        }
        return string;
    }

    public static String toCamelCase(String s) {
        if (s != null && s.contains("_")) {
            return StringUtils.uncapitalize(WordUtils.capitalizeFully(s, '_').replace("_", ""));
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
        try {
            return mapper.writeValueAsString(o);
        }
        catch(Exception ex) {
            logger.error("Object to JSON Error: ".concat(ex.getMessage()));
            throw new RuntimeException(ex);
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
