package gov.nysenate.sage.scripts.streetfinder.scripts;

import gov.nysenate.sage.scripts.streetfinder.model.CountyStreetfileName;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileField;
import gov.nysenate.sage.scripts.streetfinder.parsers.BaseParser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
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

    /**
     * @param args [sageUrl, SAGE admin key, txt/csv source directory, tsv destination directory]
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Must pass in sageURL, admin key, source directory, and destination directory");
            System.exit(-1);
        }
        Map<String, String> countyMap = GetCodes.getCodesHelper(args[0], args[1], false);
        Map<String, String> townAbbrevMap = GetCodes.getCodesHelper(args[0], args[1], true);

        for (File file : new File(args[2]).listFiles()) {
            String ext = FilenameUtils.getExtension(file.getName());
            if (ext.matches("txt|csv")) {
                Matcher matcher = CountyStreetfileName.getCountyPattern().matcher(file.getName());
                if (!matcher.matches()) {
                    throw new IOException("The filename " + file.getName() + " doesn't match any counties.");
                }
                String csvFilename = file.getName().replaceAll("\\.(txt|csv)", ".tsv");
                var fileWriter = new FileWriter(new File(args[3], csvFilename));
                var outputWriter = new PrintWriter(fileWriter);
                // add columns for the TSV file
                outputWriter.println(String.join("\t", fieldLabels));
                CountyStreetfileName name = CountyStreetfileName.valueOf(matcher.group(1));
                BaseParser<?> parser = name.getParser(file.getPath());
                String countyId = countyMap.get(name.getNameInCountyData());
                parser.parseFile();
                for (var address : parser.getAddresses()) {
                    address.put(StreetFileField.COUNTY_ID, countyId);
                    String town = address.get(StreetFileField.TOWN);
                    if (townAbbrevMap.containsKey(town)) {
                        address.put(StreetFileField.SENATE_TOWN_ABBREV, townAbbrevMap.get(town));
                    }
                    outputWriter.println(address.toStreetFileForm());
                }
                outputWriter.flush();
                outputWriter.close();
            }
            else {
                file.delete();
            }
        }
    }
}
