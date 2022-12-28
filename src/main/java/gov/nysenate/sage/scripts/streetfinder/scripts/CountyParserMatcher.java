package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.scripts.streetfinder.model.CountyStreetfileName;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileField;
import gov.nysenate.sage.scripts.streetfinder.parsers.BaseParser;
import gov.nysenate.sage.util.Pair;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * CountyParserMatcher file. Takes a file (.txt, .csv) and calls the correct
 * parser to create a tsv file
 */
public class CountyParserMatcher {
    private static final List<String> fieldLabels = List.of("street", "town", "state", "zip5",
            "bldg_lo_num", "bldg_lo_chr", "bldg_hi_num", "bldg_hi_chr", "bldg_parity",
            "apt_lo_num", "apt_lo_chr", "apt_hi_num", "apt_hi_chr", "apt_parity",
            "election_code", "county_code", "assembly_code", "senate_code", "congressional_code",
            "boe_town_code", "town_code", "ward_code", "boe_school_code", "school_code",
            "cleg_code", "cc_code", "fire_code", "city_code", "vill_code");
    private static final Map<Pair<String>, String> streetCorrectionMap = new HashMap<>();
    private static final List<String> invalidAddresses = new ArrayList<>();
    private static final List<String> errorAddresses = new ArrayList<>();
    private static ImmutableMap<String, String> countyMap, townAbbrevMap;
    private static ApiHelper apiHelper;

    /**
     * @param args [sageUrl, SAGE admin key, txt/csv source directory, tsv destination directory]
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Must pass in sageURL, admin key, source directory, and destination directory");
            System.exit(-1);
        }
        apiHelper = new ApiHelper(args[0], args[1]);
        countyMap = ImmutableMap.copyOf(apiHelper.getCodes(false));
        townAbbrevMap = ImmutableMap.copyOf(apiHelper.getCodes(true));

        // Deletes any already existing CSV files
        File[] csvFiles = new File(args[3]).listFiles();
        if (csvFiles == null) {
            throw new NullPointerException("Requires a csv directory!");
        }
        for (File file : csvFiles) {
            file.delete();
        }

        File[] txtFiles = new File(args[2]).listFiles();
        if (txtFiles == null) {
            throw new NullPointerException("Requires a txt directory!");
        }
        for (File file : txtFiles) {
            String ext = FilenameUtils.getExtension(file.getName());
            if (!ext.matches("txt|csv")) {
                file.delete();
                continue;
            }
            Matcher matcher = CountyStreetfileName.getCountyPattern().matcher(file.getName());
            if (!matcher.matches()) {
                throw new IOException("The filename " + file.getName() + " doesn't match any counties.");
            }
            CountyStreetfileName nameEnum = CountyStreetfileName.valueOf(matcher.group(1));
            BaseParser<?> parser = nameEnum.getParser(file);
            parser.parseFile();

            String countyId = countyMap.get(nameEnum.getNameInCountyData());
            // County codes have only been inserted into the voter file beforehand.
            if (countyId == null && nameEnum != CountyStreetfileName.VoterFile) {
                System.err.println(file.getName() + " does not have a matching county! Skipping...");
                continue;
            }
            String tsvFilename = file.getName().replaceAll("\\.(txt|csv)", ".tsv");
            processFile(new File(args[3], tsvFilename), countyId, parser.getAddresses());
            file.delete();
        }

        Files.write(Path.of("/home/jacob/Documents/invalidAddresses.txt"), invalidAddresses);
        Files.write(Path.of("/home/jacob/Documents/errorAddresses.txt"), errorAddresses);
    }

    private static void processFile(File tsvFile, String countyId,
                                    List<? extends StreetFileAddress> addresses)
            throws FileNotFoundException {
        var outputWriter = new PrintWriter(tsvFile);
        outputWriter.println(String.join("\t", fieldLabels));
        Stopwatch st = Stopwatch.createStarted();
        int count = 0;
        for (var address : addresses) {
            if (correctAddress(address, countyId)) {
                outputWriter.println(address.toStreetFileForm());
            }
            if (++count%100000 == 0) {
                outputWriter.flush();
                System.out.printf("Processed 100k lines in %s seconds%n", st.elapsed(TimeUnit.SECONDS));
                st.reset();
                st.start();
            }
        }
        outputWriter.flush();
        outputWriter.close();
    }

    private static boolean correctAddress(StreetFileAddress address, String countyId) {
        address.put(StreetFileField.COUNTY_ID, countyId);
        String town = address.get(StreetFileField.TOWN);
        if (townAbbrevMap.containsKey(town)) {
            address.put(StreetFileField.SENATE_TOWN_ABBREV, townAbbrevMap.get(town));
        }
        return correctStreet(address);
    }

    private static boolean correctStreet(StreetFileAddress address) {
        String originalStreet = address.get(StreetFileField.STREET);
        String zip = address.get(StreetFileField.ZIP);
        Pair<String> correctionKey = new Pair<>(originalStreet, zip);
        String correctedStreet = streetCorrectionMap.get(correctionKey);
        if (correctedStreet == null) {
            // This may be null, which is fine: we want to try and validate other addresses on the street.
            try {
                correctedStreet = apiHelper.getCorrectedStreet(address.getLowString(), originalStreet, zip);
                streetCorrectionMap.put(correctionKey, correctedStreet);
            } catch (Exception ex) {
                errorAddresses.add(address.toStreetFileForm());
                return false;
            }
            if (correctedStreet == null) {
                invalidAddresses.add(address.toStreetFileForm());
                return false;
            }
        }
        address.put(StreetFileField.STREET, correctedStreet);
        return true;
    }
}
