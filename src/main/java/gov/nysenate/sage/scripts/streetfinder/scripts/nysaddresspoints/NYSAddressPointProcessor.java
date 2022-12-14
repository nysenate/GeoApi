package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import gov.nysenate.sage.dao.provider.district.DistrictShapeFileDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.StreetAddressParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class NYSAddressPointProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NYSAddressPointProcessor.class);

    private static List<DistrictType> LOOKUP_DISTRICT_TYPES = Arrays.asList(DistrictType.COUNTY, DistrictType.SCHOOL,
            DistrictType.TOWN, DistrictType.ELECTION);
    private static final String DIR = "/data/geoapi_data/street_files/NYS_SAM_address_points/";

    @Autowired
    private AddressServiceProvider addressServiceProvider;
    @Autowired
    private DistrictShapeFileDao districtShapeFileDao;
    @Autowired
    private EventBus eventBus;

    @PostConstruct
    private void init() {
        eventBus.register(this);
    }

    @Subscribe
    public synchronized void processNysSamAddressPoints(NysAddressPointProcessEvent event) throws IOException {
        int batchSize = event.getBatchSize();
        boolean saveNycToSeparateFile = event.isSaveNycToSeparateFile();
        // Init file names
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss_");
        String prefix = dt.format(formatter);
        final String inputFile = DIR + "NYS_SAM_Address_Points.tsv";
        final String outputFile = DIR + prefix + "NYS_SAM_Address_Points_streetfile.tsv";
        final String nycOutputFile = DIR + prefix + "NYS_SAM_Address_Points_nyc_streetfile.tsv";
        final String errorFile = DIR + prefix + "errors.txt";

        // Load the statewide address point(input) file.
        List<NYSAddressPoint> addressPoints = parseNYSAddressPointsTsv(inputFile);
        // Group into batches for batch validation.
        List<List<NYSAddressPoint>> addressPointBatches = Lists.partition(addressPoints, batchSize);
        int numOfBatches = addressPointBatches.size();
        int currBatch = 1;

        // Write headers in output files.
        AddressPointFileWriter.writeStreetfileHeaders(outputFile);
        if (saveNycToSeparateFile) {
            AddressPointFileWriter.writeStreetfileHeaders(nycOutputFile);
        }

        // Process each batch
        for (List<NYSAddressPoint> batch : addressPointBatches) {
            logger.info("Processing batch num " + currBatch + " of " + numOfBatches);

            List<AddressPointValidationResult> validationResults = validateBatch(batch);
            processValidatedAddresses(validationResults, batchSize, saveNycToSeparateFile, outputFile, nycOutputFile);
            processUnvalidatedAddresses(validationResults, errorFile);
            currBatch++;
        }
    }

    /**
     * Parses a tsv file of NYS address points into a list of NYSAddressPoint instances. Warning: NYSAddressPoint fields
     * and the parsing process is coupled to the tsv file columns and ordering.
     */
    private List<NYSAddressPoint> parseNYSAddressPointsTsv(String file) throws IOException {
        List<String[]> parsedRows = null;
        try (Reader inputReader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            TsvParser parser = new TsvParser(new TsvParserSettings());
            parsedRows = parser.parseAll(inputReader);
        } catch (IOException e) {
            logger.error("Error trying to read the SAM NYS Address Point tsv file: " + file, e);
            throw e;
        }
        parsedRows.remove(0); // remove the header row
        List<NYSAddressPoint> nysAddressPoints = new ArrayList<>();
        for (String[] row : parsedRows) {
            nysAddressPoints.add(new NYSAddressPoint(row));
        }
        return nysAddressPoints;
    }

    private List<AddressPointValidationResult> validateBatch(List<NYSAddressPoint> batch) {
        List<Address> batchAddresses = batch.stream().map(NYSAddressPoint::toAddress).collect(Collectors.toList());
        List<AddressResult> results = addressServiceProvider.validate(batchAddresses, "usps", false);
        return IntStream.range(0, results.size())
                .mapToObj(i -> new AddressPointValidationResult(batch.get(i), results.get(i)))
                .collect(Collectors.toList());
    }

    /**
     * Set street address on each successfully validated result. This separates the address number field from the street,
     * it also gives us additional fields needed in the output tsv.
     */
    private void setStreetAddresses(List<AddressPointValidationResult> validationResults) {
        for (var res : validationResults) {
            if (res.validationResult().isValidated()) {
                res.setStreetAddress(StreetAddressParser.parseAddress(res.validationResult().getAddress()));
            }
        }
    }

    // Process successfully validated addresses.
    private void processValidatedAddresses(List<AddressPointValidationResult> validationResults, int batchSize,
                                           boolean saveNycToSeparateFile, String outputFile, String nycOutputFile) throws IOException {
        List<AddressPointValidationResult> validated = validationResults.stream()
                .filter(r -> r.validationResult().isValidated())
                .collect(Collectors.toList());

        logger.info("Successfully validated " + validated.size() + " out of " + batchSize);

        setStreetAddresses(validated);

        // Lookup other district codes in SAGE.
        for (var v : validated) {
            Point p = new Point(v.addressPoint().latitude, v.addressPoint().longitude);
            DistrictInfo info = districtShapeFileDao.getDistrictInfo(p, LOOKUP_DISTRICT_TYPES, false, false);
            v.setLookedUpDistrictCodes(info.getDistrictCodes());
        }

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

    // Process non-validated addresses.
    private void processUnvalidatedAddresses(List<AddressPointValidationResult> validationResults,
                                             String errorFileName) throws IOException {
        List<AddressPointValidationResult> notValidated = validationResults.stream()
                .filter(r -> !r.validationResult().isValidated())
                .collect(Collectors.toList());

        for (var res : notValidated) {
            AddressPointFileWriter.appendToUnsuccessfulFile(errorFileName, res);
        }
    }
}
