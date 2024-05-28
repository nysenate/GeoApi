package gov.nysenate.sage.scripts.streetfinder.parsers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;
import gov.nysenate.sage.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType.*;

/**
 * Base class for all parsers, with some common code for parsing data.
 */
public abstract class BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(BaseParser.class);
    private static final int SECONDS_PER_PRINT = 15;
    protected final File file;
    protected final Multimap<StreetfileLineType, String> improperLineMap = ArrayListMultimap.create();
    protected final StreetfileDataExtractor dataExtractor;
    private final String lineRegex = " *%s *".formatted(delim());

    public BaseParser(File file) {
        this.file = file;
        this.dataExtractor = getDataExtractor();
    }

    public void parseFile(DistrictingData data) throws IOException {
        logger.info("Parsing streetfile {}", file.getName());
        long lastPrintTime = System.currentTimeMillis();
        int totalLines = FileUtil.getLineCount(file);
        // A Scanner does not properly read in the voterfile.
        var reader = new BufferedReader(new FileReader(file));
        int lineNum = 0;
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
            try {
                StreetfileLineData lineData = dataExtractor.getData(++lineNum, nextLine);
                if (lineData.type() == PROPER) {
                    data.put(lineData);
                }
                else if (lineData.type() != SKIP) {
                    improperLineMap.put(lineData.type(), nextLine);
                }
            } catch (Exception ex) {
                improperLineMap.put(UNKNOWN_ERROR, nextLine);
            }
            long currTime = System.currentTimeMillis();
            if (currTime - lastPrintTime >= SECONDS_PER_PRINT * 1000) {
                logger.info("Processed {}% of lines.", Math.round(100.0*lineNum / totalLines));
                lastPrintTime = currTime;
            }
        }
        reader.close();
        if (lineNum != totalLines) {
            logger.error("Error! Only {} out of {} lines were read.", lineNum, totalLines);
        }
        logger.info("Parsed {}", file.getName());
    }

    public Multimap<StreetfileLineType, String> getImproperLineMap() {
        return improperLineMap;
    }

    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(file.getName(), this::parseLine);
    }

    protected List<String> parseLine(String line) {
        String[] tempLine = line.split(lineRegex, -1);
        tempLine[0] = tempLine[0].replaceFirst("^\"", "");
        tempLine[tempLine.length - 1] = tempLine[tempLine.length - 1].replaceFirst("\"$", "");
        return new ArrayList<>(List.of(tempLine));
    }

    protected String delim() {
        return "\",\"";
    }
}
