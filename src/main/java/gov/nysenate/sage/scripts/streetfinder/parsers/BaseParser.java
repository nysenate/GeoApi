package gov.nysenate.sage.scripts.streetfinder.parsers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.provider.usps.USPSAMSDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;
import gov.nysenate.sage.util.BatchSupplier;
import gov.nysenate.sage.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all parsers, with some common code for parsing data.
 */
public abstract class BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(BaseParser.class);
    private static final int BATCH_SIZE = 1000;
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

    public void parseFile(USPSAMSDao amsDao) throws IOException {
        var scanner = new Scanner(file);
        var lineNum = new AtomicInteger();
        BatchSupplier<StreetfileLineData> batchSupplier = new BatchSupplier<>(scanner::hasNextLine,
                () -> getData(lineNum.getAndIncrement(), parseLine(scanner.nextLine())), BATCH_SIZE, true);
        while (batchSupplier.hasNextBatch()) {
            List<StreetfileLineData> dataBatch = batchSupplier.getNextBatch();
            List<Address> addrBatch = new ArrayList<>(isRangeData() ? BATCH_SIZE * 2 : BATCH_SIZE);
            for (StreetfileLineData lineData : dataBatch) {
                List<Boolean> toAdd = isRangeData() ? List.of(false, true) : List.of(false);
                toAdd.forEach(isLow -> addrBatch.add(getAddress(lineData.range(), lineData.addressWithoutNum(), isLow)));
            }
            List<AddressWithoutNum> correctedAddrWithoutNum =
                    getCorrectedAddrWoNum(amsDao.getValidatedAddressResults(addrBatch));
            for (int i = 0; i < dataBatch.size(); i++) {
                AddressWithoutNum validAddrWoNum = correctedAddrWithoutNum.get(i);
                if (validAddrWoNum != null) {
                    data.put(dataBatch.get(i).with(validAddrWoNum));
                }
            }
            logger.info("Processed batch.");
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

    /**
     * Parses out data from a single streetfile line, and prints it to the file.
     * @param line raw data.
     */
    protected String[] parseLine(String line) {
        return line.replaceAll("^\"|\"$", "").split(lineRegex, 0);
    }

    protected StreetfileLineData getData(int lineNum, String... dataFields) {
        return getData(lineNum, List.of(dataFields));
    }

    protected StreetfileLineData getData(int lineNum, List<String> dataFields) {
        return dataExtractor.getData(lineNum, dataFields);
    }

    protected String delim() {
        return "\",\"";
    }

    private static Address getAddress(BuildingRange range, AddressWithoutNum addressWithoutNum, boolean isLow) {
        var addr = new Address((isLow ? range.low() : range.high()) + " " + addressWithoutNum.street());
        addr.setZip5(String.valueOf(addressWithoutNum.zip5()));
        addr.setState("NY");
        return addr;
    }

    /**
     * Converts AddressResults into AddressWithoutNums.
     * Since both the top and bottom of ranges are validated, some consolidation is needed.
     * Also, it's okay if only one part of the range validates: the other building may not exist yet.
     */
    private List<AddressWithoutNum> getCorrectedAddrWoNum(List<AddressResult> results) {
        var correctedAddrWithoutNum = new ArrayList<AddressWithoutNum>(BATCH_SIZE);
        for (int i = 0; i < results.size(); i++) {
            AddressResult low = results.get(i);
            AddressResult high = results.get(isRangeData() ? ++i : i);
            if (!low.isValidated() || !high.isValidated()) {
                // TODO: add invalid results somewhere
                correctedAddrWithoutNum.add(null);
                continue;
            }
            var lowAddrWoNum = new AddressWithoutNum(low.getAddress());
            var highAddrWoNum = new AddressWithoutNum(high.getAddress());
            if (!lowAddrWoNum.equals(highAddrWoNum)) {
                // TODO: can still get data
                correctedAddrWithoutNum.add(null);
                continue;
            }
            correctedAddrWithoutNum.add(lowAddrWoNum);
        }
        return correctedAddrWithoutNum;
    }
}
