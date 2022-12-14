package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import com.google.common.collect.Lists;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NYSAddressPointProcessor {

    private static final String WORK_DIR = "/Users/nysenate/sage/2022_redistricting/address_validation";
    private static final String INPUT_FILE = WORK_DIR + "/input.tsv";
    private static final String OUTPUT_FILE = WORK_DIR + "/streetfile.tsv";
    private static final String NYC_OUTPUT_FILE = WORK_DIR + "/nyc-streetfile.tsv";
    private static final String ERRORS_FILE = WORK_DIR + "/errors.txt";
    private static final boolean USE_SEPARATE_FILE_FOR_NYC = true;

    private static final int BATCH_SIZE = 1000;

    private static List<DistrictType> LOOKUP_DISTRICT_TYPES = Arrays.asList(DistrictType.COUNTY, DistrictType.SCHOOL,
            DistrictType.TOWN, DistrictType.ELECTION);

    @Autowired
    private AddressServiceProvider addressServiceProvider;
    @Autowired
    private DistrictShapeFileDao districtShapeFileDao;

    public void processNYSAddressPoints() throws IOException {
        checkEnv();
        // Load the statewide address point file.
        List<NYSAddressPoint> addressPoints = parseNYSAddressPointsTsv(INPUT_FILE);
        // Group into batches for batch validation.
        List<List<NYSAddressPoint>> addressPointBatches = Lists.partition(addressPoints, BATCH_SIZE);
        int numOfBatches = addressPointBatches.size();
        int currBatch = 1;

        // Write headers in output files.
        AddressPointFileWriter.writeStreetfileHeaders(OUTPUT_FILE);
        if (USE_SEPARATE_FILE_FOR_NYC) {
            AddressPointFileWriter.writeStreetfileHeaders(NYC_OUTPUT_FILE);
        }

        // Process each batch
        for (List<NYSAddressPoint> batch : addressPointBatches) {
            System.out.println("Processing batch num " + currBatch + " of " + numOfBatches);

            List<AddressPointValidationResult> validationResults = validateBatch(batch);
            processValidatedAddresses(validationResults);
            processUnvalidatedAddresses(validationResults);
            currBatch++;
        }
    }

    /**
     * To prevent accidentally overwriting data, this ensures the output files do not already exist when starting the
     * script.
     */
    private void checkEnv() {
        List<File> files = Arrays.asList(new File(OUTPUT_FILE), new File(ERRORS_FILE), new File(NYC_OUTPUT_FILE));
        for (var file : files) {
            if (file.exists()) {
                System.err.println("A file named: '" + file.getAbsolutePath()
                        + "' already exists. Please rename or delete it before running this script.");
                System.exit(-1);
            }
        }
    }

    /**
     * Parses a tsv file of NYS address points into a list of NYSAddressPoint instances. Warning: NYSAddressPoint fields
     * and the parsing process is coupled to the tsv file columns and ordering.
     */
    private List<NYSAddressPoint> parseNYSAddressPointsTsv(String file) {
        List<String[]> parsedRows = null;
        try (Reader inputReader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            TsvParser parser = new TsvParser(new TsvParserSettings());
            parsedRows = parser.parseAll(inputReader);
        } catch (IOException e) {
            System.err.println("ERROR parsing TSV input file: " + INPUT_FILE);
            System.err.print(e);
            System.exit(-1);
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
    private void processValidatedAddresses(List<AddressPointValidationResult> validationResults) throws IOException {
        List<AddressPointValidationResult> validated = validationResults.stream()
                .filter(r -> r.validationResult().isValidated())
                .collect(Collectors.toList());

        System.out.println("Successfully validated " + validated.size() + " out of " + BATCH_SIZE);

        setStreetAddresses(validated);

        long startTime = System.nanoTime();
        // Lookup other district codes in SAGE.
        for (var v : validated) {
            Point p = new Point(v.addressPoint().latitude, v.addressPoint().longitude);
            DistrictInfo info = districtShapeFileDao.getDistrictInfo(p, LOOKUP_DISTRICT_TYPES, false, false);
            v.setLookedUpDistrictCodes(info.getDistrictCodes());
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("District lookup for batch completed in: " + (duration / 1000000) + " milliseconds");

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
            AddressPointFileWriter.appendToStreetfile(OUTPUT_FILE, tsvData);
        }

        // Write NYC addresses to tsv file
        for (var res : nycAddresses) {
            var nycFile = USE_SEPARATE_FILE_FOR_NYC ? NYC_OUTPUT_FILE : OUTPUT_FILE;
            var tsvData = AddressPointFileWriter.toStreetfileRow(res);
            AddressPointFileWriter.appendToStreetfile(nycFile, tsvData);
        }
    }

    // Process non-validated addresses.
    private void processUnvalidatedAddresses(List<AddressPointValidationResult> validationResults) throws
            IOException {
        List<AddressPointValidationResult> notValidated = validationResults.stream()
                .filter(r -> !r.validationResult().isValidated())
                .collect(Collectors.toList());

        for (var res : notValidated) {
            AddressPointFileWriter.appendToUnsuccessfulFile(ERRORS_FILE, res);
        }
    }
}
