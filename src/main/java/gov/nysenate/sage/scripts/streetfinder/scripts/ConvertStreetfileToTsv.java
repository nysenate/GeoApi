package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.scripts.streetfinder.SortedStringMultiMap;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileField;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileType;
import gov.nysenate.sage.scripts.streetfinder.parsers.BaseParser;
import gov.nysenate.sage.scripts.streetfinder.parsers.NYCParser;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.AddressCorrectionHandler;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.AddressCorrectionMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.ApiHelper;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.SENATE_TOWN_ABBREV;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.TOWN;

/**
 * Takes a file (.txt, .csv) and calls the matching parser to create a TSV file.
 */
public class ConvertStreetfileToTsv {
    private static final List<String> fieldLabels = List.of("street", "town", "state", "zip5",
            "bldg_lo_num", "bldg_lo_chr", "bldg_hi_num", "bldg_hi_chr", "bldg_parity",
            "election_code", "county_code", "assembly_code", "senate_code", "congressional_code",
            "boe_town_code", "town_code", "ward_code", "boe_school_code", "school_code",
            "cleg_code", "cc_code", "fire_code", "city_code", "vill_code");
    private static final String fieldLabelString = String.join("\t", fieldLabels),
            txtDir = "txt_streetfiles/", tsvDir = "tsv_streetfiles/",
            correctionMapFilename = "correctionMap.txt", invalidAddressFilename = "unvalidatedAddresses.txt",
            errorAddressFile = "errorAddresses.txt";
    private static final String divider = "-".repeat(20);
    // Used to speed up correction.
    private static AddressCorrectionHandler correctionHandler;
    private static ImmutableMap<String, String> countyMap, townAbbrevMap;
    private static final Map<String, SortedStringMultiMap> badLineMap = new HashMap<>();

    /**
     * @param args [sageUrl, SAGE admin key, USPS AMS URL, streetfile data directory]
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Must pass in sageURL, admin key, USPS AMS URL, and streetfile data directory");
            System.exit(-1);
        }
        if (!args[3].endsWith("/")) {
            args[3] = args[3] + "/";
        }
        initializeData(args[3], new ApiHelper(args[0], args[1], args[2]));

        File[] txtFiles = new File(args[3], txtDir).listFiles();
        if (txtFiles == null) {
            throw new NullPointerException("Requires a txt directory!");
        }
        for (File file : txtFiles) {
            String ext = FilenameUtils.getExtension(file.getName());
            if (!ext.matches("txt|csv")) {
                delete(file);
                continue;
            }
            Matcher matcher = StreetFileType.getCountyPattern().matcher(file.getName());
            if (!matcher.matches()) {
                System.err.println("The filename " + file.getName() + " doesn't match any StreetFileType. Skipping...");
                continue;
            }
            StreetFileType nameEnum = StreetFileType.valueOf(matcher.group(1));

            String countyId = countyMap.get(nameEnum.getDatabaseName());
            // County codes have only been inserted into the voter file beforehand.
            if (countyId == null && nameEnum != StreetFileType.VoterFile) {
                System.err.println(file.getName() + " does not have a matching county! Skipping...");
                continue;
            }
            String tsvFilename = file.getName().replaceAll("\\.(txt|csv)", ".tsv");
            processFile(args[3] + tsvDir + tsvFilename, countyId, nameEnum.getParser(file));
        }
        printBadLines(args[3]);

        var mapWriter = new ObjectOutputStream(new FileOutputStream(new File(args[3], correctionMapFilename)));
        mapWriter.writeObject(correctionHandler.getCorrectionMap());
        mapWriter.flush();
        mapWriter.close();

        try (var invalidAddressWriter = new PrintWriter(new File(args[3], invalidAddressFilename))) {
            for (var entry : correctionHandler.getCorrectionMap().entrySet()) {
                if (!entry.getValue().isValid()) {
                    invalidAddressWriter.println(entry.getKey());
                }
            }
            invalidAddressWriter.flush();
        }
    }

    /**
     * Clears out old TSV files and initializes the maps.
     */
    private static void initializeData(String dataDir, ApiHelper apiHelper) throws Exception {
        File[] tsvFiles = new File(dataDir, tsvDir).listFiles();
        if (tsvFiles == null) {
            throw new NullPointerException("Requires a tsv directory!");
        }
        delete(tsvFiles);

        countyMap = ImmutableMap.copyOf(apiHelper.getCodes(false));
        townAbbrevMap = ImmutableMap.copyOf(apiHelper.getCodes(true));

        var correctionMapFile = new File(dataDir, correctionMapFilename);
        AddressCorrectionMap correctionMap;
        if (correctionMapFile.exists()) {
            var mapReader = new ObjectInputStream(new FileInputStream(correctionMapFile));
            correctionMap = (AddressCorrectionMap) mapReader.readObject();
            mapReader.close();
        }
        else {
            correctionMap = new AddressCorrectionMap();
        }
        correctionHandler = new AddressCorrectionHandler(correctionMap, apiHelper);
    }

    private static void delete(File... files) {
        for (File file : files) {
            file.delete();
        }
    }

    private static void processFile(String tsvFilename, String countyId,
                                    BaseParser<?> parser) throws IOException {
        boolean isVoterFile = countyId == null;
        Stopwatch st = Stopwatch.createStarted();
        parser.parseFile();
        Set<String> lines = new HashSet<>();
        for (var baseSfa : parser.getParsedAddresses()) {
            if (isVoterFile) {
                // This field is initially a county ID, where the counties are sorted alphabetically.
                countyId = countyMap.get(StreetFileType.getDatabaseNameFromInt(baseSfa.get(StreetFileField.COUNTY_ID)));
            }
            baseSfa.put(StreetFileField.COUNTY_ID, countyId);
            String town = baseSfa.get(TOWN);
            if (townAbbrevMap.containsKey(town)) {
                baseSfa.put(SENATE_TOWN_ABBREV, townAbbrevMap.get(town));
            }
            List<StreetFileAddressRange> newSfaList;
            try {
                newSfaList = correctionHandler.getCorrectedAddressRanges(baseSfa);
                for (StreetFileAddressRange newSfa : newSfaList) {
                    // NYC data does not have parity, it needs to be derived from the range.
                    if (parser.getClass().equals(NYCParser.class)) {
                        newSfa.getBuildingRange().setParityFromRange();
                    }
                    String toAdd = newSfa.toStreetFileForm();
                    if (lines.contains(toAdd)) {
                        System.err.println("Duplicate of line: " + toAdd);
                    }
                    else {
                        lines.add(toAdd);
                    }
                }
            }
            catch (IOException ex) {
                System.err.println("Problem accessing the API for " + baseSfa.toStreetFileForm() + ". Skipping.");
            }
        }
        // TODO: write one voter file, and one file for everything else
        var outputWriter = new PrintWriter(tsvFilename);
        outputWriter.println(fieldLabelString);
        lines.forEach(outputWriter::println);
        st.stop();
        System.out.printf("Took %d seconds to process %d lines.%n", st.elapsed(TimeUnit.SECONDS), lines.size());
        outputWriter.flush();
        outputWriter.close();
        if (!parser.getBadLines().isEmpty()) {
            badLineMap.put(tsvFilename, parser.getBadLines());
        }
    }

    private static void printBadLines(String dir) throws FileNotFoundException {
        try (var badLineWriter = new PrintWriter(new File(dir, errorAddressFile))) {
            for (String filename : badLineMap.keySet()) {
                badLineWriter.println(filename);
                badLineWriter.println(divider);
                badLineWriter.println(badLineMap.get(filename));
            }
        }
    }
}
