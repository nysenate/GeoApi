package gov.nysenate.sage.service.streetfile;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.parsers.*;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.CompactDistrictMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StreetfileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StreetfileProcessor.class);
    private static final int PRINT_BATCH_SIZE = 10000;

    private final File sourceDir, resultsDir;
    private final Path streetfilePath, conflictPath, improperPath, invalidPath;
    private final List<County> counties;
    private final StreetfileAddressCorrectionService correctionService;

    @Autowired
    public StreetfileProcessor(@Value("${streetfile.dir}") String streetfileDir, CountyDao countyDao,
                               StreetfileAddressCorrectionService correctionService) {
        this.sourceDir = Path.of(streetfileDir, "text_files").toFile();
        this.resultsDir = Path.of(streetfileDir, "results").toFile();
        this.streetfilePath = Path.of(resultsDir.getPath(), "streetfile.txt");
        this.conflictPath = Path.of(resultsDir.getPath(), "conflicts.txt");
        this.improperPath = Path.of(resultsDir.getPath(), "improper.txt");
        this.invalidPath = Path.of(resultsDir.getPath(), "invalid.txt");
        this.counties = countyDao.getCounties();
        this.correctionService = correctionService;
    }

    public synchronized void regenerateStreetfile() throws IOException {
        File[] dataFiles = sourceDir.listFiles();
        File[] resultFiles = resultsDir.listFiles();
        if (dataFiles == null || resultFiles == null) {
            throw new IOException("Couldn't access directories.");
        }
        if (dataFiles.length == 0) {
            logger.info("No streetfile data to process.");
            return;
        }

        var fullData = new DistrictingData();
        Multimap<StreetfileLineType, String> fullImproperLineMap = ArrayListMultimap.create();
        for (File dataFile : dataFiles) {
            if (dataFile.isFile()) {
                BaseParser parser = getParser(dataFile);
                parser.parseFile(fullData);
                fullImproperLineMap.putAll(parser.getImproperLineMap());
            }
        }

        // Clears out old results files.
        for (File resultFile : resultFiles) {
            if (resultFile.isFile()) {
                Files.deleteIfExists(resultFile.toPath());
            }
        }
        logger.info("Beginning address validation. This may take some time.");
        final var correctionMap = correctionService.getCorrections(fullData);
        Multimap<String, String> invalidData = fullData.removeInvalidAddresses(correctionMap);
        for (String source : invalidData.keySet()) {
            var currData = invalidData.get(source);
            String toPrint = source + "\n\t" + String.join("\n\t", currData) + "\n";
            Files.writeString(invalidPath, toPrint, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        logger.info("Validation completed. Consolidating...");

        Map<StreetfileAddressRange, CompactDistrictMap> consolidatedData = fullData.consolidate(conflictPath);
        var bufferedWriter = new BufferedWriter(new PrintWriter(streetfilePath.toFile()));
        int lineCount = 0;
        for (var entry : consolidatedData.entrySet()) {
            bufferedWriter.write(entry.getKey() + " " + entry.getValue());
            bufferedWriter.newLine();
            if (++lineCount % PRINT_BATCH_SIZE == 0) {
                bufferedWriter.flush();
            }
        }
        bufferedWriter.close();

        bufferedWriter = new BufferedWriter(new PrintWriter(improperPath.toFile()));
        lineCount = 0;
        for (StreetfileLineType type : StreetfileLineType.values()) {
            if (!fullImproperLineMap.containsKey(type)) {
                continue;
            }
            bufferedWriter.write(type.name());
            bufferedWriter.newLine();
            for (String line : fullImproperLineMap.get(type)) {
                bufferedWriter.write('\t' + line);
                bufferedWriter.newLine();
                if (++lineCount % PRINT_BATCH_SIZE == 0) {
                    bufferedWriter.flush();
                }
            }
        }
        bufferedWriter.close();
    }

    private BaseParser getParser(File file) {
        String filename = file.getName().toLowerCase();
        County county = getCounty(filename);
        if (county == null) {
            if (filename.contains("voter")) {
                var map = counties.stream().collect(Collectors.toMap(County::voterfileCode, County::fipsCode));
                return new VoterFileParser(file, map);
            }
            // AddressPoints
            else if (filename.contains("address_points")) {
                var map = counties.stream().collect(Collectors.toMap(tempCounty -> tempCounty.name().toLowerCase(), County::fipsCode));
                return new AddressPointsParser(file, map);
            }
            else throw new IllegalArgumentException(file.getName() + " could not be matched with a parser.");
        }
        return switch (county.name()) {
            case "Bronx", "New York", "Queens", "Kings", "Richmond" -> new NYCParser(file, county);
            case "Allegany", "Columbia", "Saratoga" -> new SaratogaParser(file, county);
            case "Erie" -> new ErieParser(file, county);
            case "Essex" -> new EssexParser(file, county);
            case "Montgomery" -> new MontgomeryParser(file, county);
            case "Nassau" -> new NassauParser(file, county);
            case "Schoharie" -> new SchoharieParser(file, county);
            case "Suffolk" -> new SuffolkParser(file, county);
            case "Westchester" -> new WestchesterParser(file, county);
            case "Wyoming" -> new WyomingParser(file, county);
            default -> new NTSParser(file, county);
        };
    }

    private County getCounty(String filename) {
        filename = filename.replaceAll("_", " ");
        for (County county : counties) {
            if (filename.contains(county.streetfileName().toLowerCase())) {
                return county;
            }
        }
        return null;
    }
}
