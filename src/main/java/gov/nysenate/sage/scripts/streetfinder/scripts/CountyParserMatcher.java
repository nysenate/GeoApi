package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileField;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.ApiHelper;
import gov.nysenate.sage.util.Pair;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * CountyParserMatcher file. Takes a file (.txt, .csv) and calls the correct
 * parser to create a tsv file
 */
public class CountyParserMatcher {
    private static final List<String> fieldLabels = List.of("street", "town", "state", "zip5",
            "bldg_lo_num", "bldg_lo_chr", "bldg_hi_num", "bldg_hi_chr", "bldg_parity",
            "election_code", "county_code", "assembly_code", "senate_code", "congressional_code",
            "boe_town_code", "town_code", "ward_code", "boe_school_code", "school_code",
            "cleg_code", "cc_code", "fire_code", "city_code", "vill_code");
    private static final String txtDir = "txt_streetfiles/", tsvDir = "tsv_streetfiles/",
            streetMap = "streetMap.txt", invalidAddressFile = "invalidAddresses.txt",
            errorAddressFile = "errorAddresses.txt";
    // A map from (street name, zip) -> USPS corrected street. Used to speed up correction.
    private static final Map<Pair<String>, String> streetCorrectionMap = new HashMap<>();
    private static final List<String> invalidAddresses = new ArrayList<>(), errorAddresses = new ArrayList<>();
    private static ImmutableMap<String, String> countyMap, townAbbrevMap;
    private static ApiHelper apiHelper;

    /**
     * @param args [sageUrl, SAGE admin key, streetfile data directory]
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Must pass in sageURL, admin key, and streetfile data directory");
            System.exit(-1);
        }
        if (args[2].endsWith("/")) {
            args[2] = args[2].substring(0, args[2].length() - 1);
        }
        apiHelper = new ApiHelper(args[0], args[1]);
        initializeData(args[2]);

        File[] txtFiles = new File(args[2] + txtDir).listFiles();
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
            processFile(new File(args[2], tsvDir + tsvFilename), countyId, nameEnum.getParser(file).parseFile());
            delete(file);
        }

        Files.write(Path.of(args[2], invalidAddressFile), invalidAddresses);
        Files.write(Path.of(args[2], errorAddressFile), errorAddresses);
        try (var streetMapWriter = new FileWriter(args[2] + "/" + streetMap)) {
            for (var entry : streetCorrectionMap.entrySet()) {
                streetMapWriter.write(entry.getKey().first() + "|~~|" + entry.getKey().second() + "|~~|" + entry.getValue());
                streetMapWriter.write("\n");
            }
            streetMapWriter.flush();
        }
    }

    /**
     * Initializes the maps and clears out old TSV files.
     */
    private static void initializeData(String dataDir) throws Exception {
        countyMap = ImmutableMap.copyOf(apiHelper.getCodes(false));
        townAbbrevMap = ImmutableMap.copyOf(apiHelper.getCodes(true));

        for (String line : Files.readAllLines(Path.of(dataDir, streetMap))) {
            if (line.isBlank()) {
                return;
            }
            String[] parts = line.split("\\|~~\\|");
            if (parts.length != 3) {
                System.err.println("Problem with line: " + line);
            }
            else {
                streetCorrectionMap.put(new Pair<>(parts[0], parts[1]), parts[2]);
            }
        }
        File[] tsvFiles = new File(dataDir, tsvDir).listFiles();
        if (tsvFiles == null) {
            throw new NullPointerException("Requires a tsv directory!");
        }
        delete(tsvFiles);
    }

    private static void delete(File... files) {
        for (File file : files) {
            file.delete();
        }
    }

    private static void processFile(File tsvFile, String countyId,
                                    List<? extends StreetFileAddress> addresses)
            throws FileNotFoundException {
        boolean isVoterFile = countyId == null;
        var outputWriter = new PrintWriter(tsvFile);
        outputWriter.println(String.join("\t", fieldLabels));
        Stopwatch st = Stopwatch.createStarted();
        Set<String> lines = new HashSet<>();
        for (var address : addresses) {
            if (isVoterFile) {
                // This field is initially a county ID, where the counties are sorted alphabetically.
                countyId = countyMap.get(StreetFileType.getDatabaseNameFromInt(address.get(StreetFileField.COUNTY_ID)));
            }
            address.put(StreetFileField.COUNTY_ID, countyId);
            if (correctAddress(address)) {
                String toAdd = address.toStreetFileForm();
                if (lines.contains(toAdd)) {
                    System.err.println("Line " + toAdd + " has a duplicate of ");
                }
                lines.add(toAdd);
            }
        }
        lines.forEach(outputWriter::println);
        st.stop();
        System.out.println(lines.size() + " took " + st.elapsed(TimeUnit.SECONDS) + " seconds");
        outputWriter.flush();
        outputWriter.close();
    }

    /**
     * USPS corrects the street to standardize the format.
     * @return true if the address could be verified, otherwise false.
     */
    private static boolean correctAddress(StreetFileAddress address) {
        String town = address.get(StreetFileField.TOWN);
        if (townAbbrevMap.containsKey(town)) {
            address.put(StreetFileField.SENATE_TOWN_ABBREV, townAbbrevMap.get(town));
        }
        // TODO: test without shortcut
        String originalStreet = address.get(StreetFileField.STREET);
        String zip = address.get(StreetFileField.ZIP);
        Pair<String> correctionKey = new Pair<>(originalStreet, zip);
        String correctedStreet = streetCorrectionMap.get(correctionKey);
        if (correctedStreet == null) {
            try {
                correctedStreet = apiHelper.getCorrectedStreet(address.getLowString(), originalStreet, zip);
                if (correctedStreet == null) {
                    // TODO: fix
                    invalidAddresses.add(address.toStreetFileForm());
                    return false;
                }
                correctedStreet = correctedStreet.replaceAll("\"", "").trim();
                streetCorrectionMap.put(correctionKey, correctedStreet);
            } catch (Exception ex) {
                // TODO: add num
                errorAddresses.add(originalStreet + ", " + zip);
                return false;
            }
        }
        address.put(StreetFileField.STREET, correctedStreet);
        return true;
    }
}
