package gov.nysenate.sage.scripts.streetfinder.parsers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;
import gov.nysenate.sage.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Base class for all parsers, with some common code for parsing data.
 */
public abstract class BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(BaseParser.class);
    private static final int BATCH_SIZE = 100000;
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

    public boolean isRangeData() {
        return true;
    }

    public void parseFile() throws IOException {
        var scanner = new Scanner(file);
        int lineNum = 0;
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            try {
                StreetfileLineData lineData = dataExtractor.getData(++lineNum, parseLine(nextLine));
                if (lineData != null) {
                    data.put(lineData);
                }
            } catch (Exception ex) {
                logger.error("Can't process line number {} in file {}:", lineNum, file.getName());
                logger.error(nextLine);
            }
            if (lineNum%BATCH_SIZE == 0) {
                System.out.println("Processed " + lineNum + " lines.");
            }
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

    protected List<String> parseLine(String line) {
        return new ArrayList<>(List.of(
                line.replaceAll("^\"|\"$", "").split(lineRegex, 0)
        ));
    }

    protected String delim() {
        return "\",\"";
    }

    public static Address getAddress(BuildingRange range, AddressWithoutNum addressWithoutNum, boolean isLow) {
        var addr = new Address((isLow ? range.low() : range.high()) + " " + addressWithoutNum.street());
        addr.setPostal(addressWithoutNum.postalCity());
        addr.setZip5(String.valueOf(addressWithoutNum.zip5()));
        addr.setState("NY");
        return addr;
    }
}
