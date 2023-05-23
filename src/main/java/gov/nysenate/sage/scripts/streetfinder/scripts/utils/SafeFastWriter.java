package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.function.Function;

/**
 * Utility class to write large Collections of Objects, a million lines at a time.
 */
public class SafeFastWriter {
    private static final int linesPerFlush = 1000000;

    private SafeFastWriter() {}

    public static <T> void writeLines(String filename, Collection<T> lines, Function<T, String> mapper) throws IOException {
        PrintWriter outputWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
        int lineCount = 0;
        for (T line : lines) {
            outputWriter.write(mapper.apply(line));
            outputWriter.write('\n');
            if (++lineCount == linesPerFlush) {
                outputWriter.flush();
                lineCount = 0;
            }
        }
        outputWriter.flush();
        outputWriter.close();
    }
}
