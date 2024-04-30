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
    private static final double batchPercent = 5;
    protected final File file;
    protected DistrictingData data;
    protected final Multimap<StreetfileLineType, String> improperLineMap = ArrayListMultimap.create();
    protected final StreetfileDataExtractor dataExtractor = getDataExtractor();
    private final String lineRegex = " *%s *".formatted(delim());

    public BaseParser(File file) {
        this.file = file;
        try {
            this.data = new DistrictingData(FileUtil.getLineCount(file), 1);
        } catch (Exception ex) {
            this.data = null;
        }
    }

    public void parseFile() throws IOException {
        int totalLines = FileUtil.getLineCount(file);
        int batchSize = (int) (batchPercent * totalLines/100);
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
            if (lineNum%batchSize == 0) {
                logger.info("Processed {}% of lines.", (int) (batchPercent * lineNum/batchSize));
            }
        }
        reader.close();
        if (lineNum != totalLines) {
            logger.error("Error! Only {} out of {} lines were read.", lineNum, totalLines);
        }
    }

    public Multimap<StreetfileLineType, String> getImproperLineMap() {
        return improperLineMap;
    }

    public DistrictingData getData() {
        return data;
    }

    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(getClass().getSimpleName(), this::parseLine);
    };

    protected List<String> parseLine(String line) {
        return new ArrayList<>(List.of(
                line.replaceAll("^\"|\"$", "").split(lineRegex, 0)
        ));
    }

    protected String delim() {
        return "\",\"";
    }
}
