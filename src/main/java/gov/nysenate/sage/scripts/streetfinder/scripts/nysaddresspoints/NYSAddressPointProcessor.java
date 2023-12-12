package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

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
import gov.nysenate.sage.util.StreetAddressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NYSAddressPointProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NYSAddressPointProcessor.class);

    private static List<DistrictType> LOOKUP_DISTRICT_TYPES = Arrays.asList(DistrictType.COUNTY, DistrictType.SCHOOL,
            DistrictType.TOWN, DistrictType.ELECTION);

    private AddressServiceProvider addressServiceProvider;
    private DistrictShapeFileDao districtShapeFileDao;
    private String directory;

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
    public synchronized void processNysSamAddressPoints(int batchSize, boolean saveNycToSeparateFile) throws IOException {
        // Init file names
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss_");
        String prefix = dt.format(formatter);
        final String inputFile = directory + "NYS_SAM_Address_Points.tsv";
        final String outputFile = directory + prefix + "NYS_SAM_Address_Points_streetfile.tsv";
        final String nycOutputFile = directory + prefix + "NYS_SAM_Address_Points_nyc_streetfile.tsv";
        final String errorFile = directory + prefix + "errors.txt";

        // Write headers in output files.
        AddressPointFileWriter.writeStreetfileHeaders(outputFile);
        if (saveNycToSeparateFile) {
            AddressPointFileWriter.writeStreetfileHeaders(nycOutputFile);
        }

        // Read from the input file in batches.
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setHeaderExtractionEnabled(true);
        TsvRoutines tsvRoutines = new TsvRoutines(parserSettings);

        Reader reader = new FileReader(inputFile);
        ResultIterator<NYSAddressPoint, ParsingContext> iterator = tsvRoutines.iterate(NYSAddressPoint.class, reader).iterator();

        int batchNum = 1;
        List<NYSAddressPoint> batch;
        do {
            batch = new ArrayList<>(batchSize);
            while (batch.size() < batchSize && iterator.hasNext()) {
                NYSAddressPoint point = iterator.next();
                batch.add(point);
            }
            // Process each batch
            List<AddressPointValidationResult> validationResults = processBatch(batch);
            saveResults(validationResults, outputFile, nycOutputFile, errorFile, saveNycToSeparateFile);
            logger.info("Done processing batch number {}", batchNum);
            batchNum++;
        } while (batch.size() == batchSize);
    }

    public List<AddressPointValidationResult> processBatch(List<NYSAddressPoint> batch) {
        // Run through usps address validator.
        List<AddressPointValidationResult> validationResults = validateBatch(batch);

        for (var result : validationResults) {
            if (result.validationResult().isValidated()) {
                // For all successfully validated addresses.
                //   - Set street addresses.
                result.setStreetAddress(StreetAddressParser.parseAddress(result.validationResult().getAddress()));
                //   - Lookup other district codes in SAGE.
                Point p = new Point(result.addressPoint().latitude, result.addressPoint().longitude);
                DistrictInfo info = districtShapeFileDao.getDistrictInfo(p, LOOKUP_DISTRICT_TYPES, false, false);
                result.setLookedUpDistrictCodes(info.getDistrictCodes());
            }
        }
        return validationResults;
    }

    private void saveResults(List<AddressPointValidationResult> results, String outputFile, String nycOutputFile,
                             String errorFile, boolean saveNycToSeparateFile) throws IOException {
        // Save errors/unable to validate.
        List<AddressPointValidationResult> notValidated = results.stream()
                .filter(r -> !r.validationResult().isValidated())
                .collect(Collectors.toList());

        for (var res : notValidated) {
            AddressPointFileWriter.appendToUnsuccessfulFile(errorFile, res);
        }

        List<AddressPointValidationResult> validated = results.stream()
                .filter(r -> r.validationResult().isValidated())
                .collect(Collectors.toList());

        logger.info("Successfully validated " + validated.size() + " out of " + results.size());

        // Split into NYC and non NYC addresses.
        List<AddressPointValidationResult> nycAddresses = validated.stream()
                .filter(r -> r.validationResult().getAddress().getCity()
                        .matches("(?i)Bronx|Brooklyn|Manhattan|Queens|Staten Island"))
                .collect(Collectors.toList());

        List<AddressPointValidationResult> nonNycAddresses = validated.stream()
                .filter(r -> !r.validationResult().getAddress().getCity()
                        .matches("(?i)Bronx|Brooklyn|Manhattan|Queens|Staten Island"))
                .collect(Collectors.toList());

        // Write non NYC addresses to tsv file
        for (var res : nonNycAddresses) {
            var tsvData = AddressPointFileWriter.toStreetfileRow(res);
            AddressPointFileWriter.appendToStreetfile(outputFile, tsvData);
        }

        // Write NYC addresses to tsv file
        for (var res : nycAddresses) {
            var nycFile = saveNycToSeparateFile ? nycOutputFile : outputFile;
            var tsvData = AddressPointFileWriter.toStreetfileRow(res);
            AddressPointFileWriter.appendToStreetfile(nycFile, tsvData);
        }
    }

    private List<AddressPointValidationResult> validateBatch(List<NYSAddressPoint> batch) {
        List<Address> batchAddresses = batch.stream().map(NYSAddressPoint::toAddress).collect(Collectors.toList());
        List<AddressResult> results = addressServiceProvider.validate(batchAddresses, "usps", false);
        return IntStream.range(0, results.size())
                .mapToObj(i -> new AddressPointValidationResult(batch.get(i), results.get(i)))
                .collect(Collectors.toList());
    }
}
