package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvRoutines;
import gov.nysenate.sage.dao.provider.district.DistrictShapeFileDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.FileUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class NYSAddressPointProcessor {
    private static final Logger logger = LoggerFactory.getLogger(NYSAddressPointProcessor.class);
    private static final List<DistrictType> LOOKUP_DISTRICT_TYPES = List.of(
            DistrictType.COUNTY, DistrictType.SCHOOL, DistrictType.TOWN, DistrictType.ELECTION);
    private static final DateTimeFormatter dirFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm_ss");
    private static final File inputFile = new File("NYS_SAM_Address_Points.tsv"),
            outputFile = new File("streetfile.tsv"), errorFile = new File("errors.txt");
    private static final TsvRoutines tsvRoutines;
    static {
        var parserSettings = new TsvParserSettings();
        parserSettings.setHeaderExtractionEnabled(true);
        tsvRoutines = new TsvRoutines(parserSettings);
    }

    private final AddressServiceProvider addressServiceProvider;
    private final DistrictShapeFileDao districtShapeFileDao;
    private final String directory;

    @Autowired
    public NYSAddressPointProcessor(AddressServiceProvider addressServiceProvider,
                                    DistrictShapeFileDao districtShapeFileDao,
                                    @Value("${streetfile.nysaddresspoint.dir}") String directory) {
        this.addressServiceProvider = addressServiceProvider;
        this.districtShapeFileDao = districtShapeFileDao;
        this.directory = directory.endsWith("/") ? directory : directory + "/";
    }

    /**
     * Processes a tsv of NYS Address Point data into a tsv containing streetfile columns.
     */
    public synchronized void processNysSamAddressPoints(int batchSize) throws IOException {
        File currParentDir = new File(directory + LocalDateTime.now().format(dirFormatter));
        if (!currParentDir.mkdir()) {
            throw new IOException("Directory could not be created.");
        }

        AddressPointFileWriter.writeStreetfileHeaders(outputFile);
        // Read from the input file in batches.
        double totalLines = FileUtil.getLineCount(inputFile);
        double batchesPerFivePercent = Math.ceil(totalLines/batchSize)/20;
        Reader reader = new FileReader(inputFile);
        ResultIterator<NYSAddressPoint, ParsingContext> iterator = tsvRoutines.iterate(NYSAddressPoint.class, reader).iterator();

        int batchCount = 0;
        List<NYSAddressPoint> batch;
        while (iterator.hasNext()) {
            batch = new ArrayList<>(batchSize);
            while (batch.size() < batchSize && iterator.hasNext()) {
                batch.add(iterator.next());
            }
            // Process each batch
            saveResults(processBatch(batch));
            if (++batchCount % batchesPerFivePercent == 0) {
                logger.info(batchCount/batchesPerFivePercent + "% done");
            }
        }
    }

    /**
     * Returns a map with all valid and invalid addresses.
     * @param batch
     * @return
     */
    public ListMultimap<Boolean, AddressPointValidationResult> processBatch(List<NYSAddressPoint> batch) {
        // Run through usps address validator.
        List<AddressPointValidationResult> validationResults = validateBatch(batch);
        ListMultimap<Boolean, AddressPointValidationResult> validatedMap = ArrayListMultimap.create();
        validationResults.forEach(result -> validatedMap.put(result.validationResult().isValidated(), result));
        // For all successfully validated addresses.
        for (var result : validatedMap.get(true)) {
            result.setStreetAddress(StreetAddressParser.parseAddress(result.validationResult().getAddress()));
            //   - Lookup other district codes in SAGE.
            var p = new Point(result.addressPoint().latitude, result.addressPoint().longitude);
            DistrictInfo info = districtShapeFileDao.getDistrictInfo(p, LOOKUP_DISTRICT_TYPES, false, false);
            result.setLookedUpDistrictCodes(info.getDistrictCodes());
        }
        return validatedMap;
    }

    private void saveResults(ListMultimap<Boolean, AddressPointValidationResult> results) throws IOException {
        // Save errors/unable to validate.
        for (var res : results.get(false)) {
            AddressPointFileWriter.appendToUnsuccessfulFile(errorFile, res);
        }
        for (var result : results.get(true)) {
            String tsvData = AddressPointFileWriter.toStreetfileRow(result);
            AddressPointFileWriter.appendToStreetfile(outputFile, tsvData);
        }
        logger.info("Successfully validated " + results.get(true).size() + " out of " + results.size());
    }

    private List<AddressPointValidationResult> validateBatch(List<NYSAddressPoint> batch) {
        List<Address> batchAddresses = batch.stream().map(NYSAddressPoint::toAddress).toList();
        List<AddressResult> results = addressServiceProvider.validate(batchAddresses, "usps", false);
        return IntStream.range(0, results.size())
                .mapToObj(i -> new AddressPointValidationResult(batch.get(i), results.get(i)))
                .toList();
    }
}
