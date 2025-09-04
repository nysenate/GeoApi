package gov.nysenate.sage.service.streetfile;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.TownCity;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.scripts.streetfinder.model.ResolveConflictConfiguration;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.parsers.*;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.CompactDistrictMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.io.FileUtils;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StreetfileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StreetfileProcessor.class);
    private final File sourceDir, resultsDir;
    private final Path streetfilePath, conflictPath, improperPath, invalidPath;
    private final ShapefileService shapefileService;
    private final StreetfileAddressCorrectionService correctionService;
    private final StreetfileDao streetfileDao;

    @Autowired
    public StreetfileProcessor(@Value("${streetfile.dir}") String streetfileDir, ShapefileService shapefileService,
                               StreetfileAddressCorrectionService correctionService, StreetfileDao streetfileDao) throws IOException {
        this.sourceDir = Path.of(streetfileDir, "text_files").toFile();
        FileUtils.forceMkdir(sourceDir);
        this.resultsDir = Path.of(streetfileDir, "results").toFile();
        FileUtils.forceMkdir(resultsDir);
        this.streetfilePath = Path.of(resultsDir.getPath(), "streetfile.txt");
        this.conflictPath = Path.of(resultsDir.getPath(), "conflicts.txt");
        this.improperPath = Path.of(resultsDir.getPath(), "improper.txt");
        this.invalidPath = Path.of(resultsDir.getPath(), "invalid.txt");
        this.shapefileService = shapefileService;
        this.correctionService = correctionService;
        this.streetfileDao = streetfileDao;
    }

    public Path regenerateStreetfile(ResolveConflictConfiguration config) throws IOException {
        File[] dataFiles = sourceDir.listFiles();
        File[] resultFiles = resultsDir.listFiles();
        if (dataFiles == null || resultFiles == null) {
            throw new IOException("The necessary directories do not exist.");
        }
        if (dataFiles.length == 0) {
            logger.warn("No streetfile data to process.");
            return null;
        }

        var fullData = new DistrictingData(config);
        Multimap<StreetfileLineType, String> fullImproperLineMap = ArrayListMultimap.create();
        Multimap<County, TownCity> countyTownCityMap = shapefileService.getCountyToTownCityMap();
        TownCity nyc = countyTownCityMap.values().stream().filter(tc -> "New York".equals(tc.baseName()))
                .findFirst().orElse(null);
        for (File dataFile : dataFiles) {
            if (dataFile.isFile()) {
                BaseParser parser = getParser(dataFile, countyTownCityMap, nyc);
                fullData.putSource(dataFile.getName(), parser.type());
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
        FormatUtil.writeLines(bufferedWriter, consolidatedData.entrySet(), this::toCsvLine);
        bufferedWriter.close();

        bufferedWriter = new BufferedWriter(new PrintWriter(improperPath.toFile()));
        for (StreetfileLineType type : StreetfileLineType.values()) {
            if (!fullImproperLineMap.containsKey(type)) {
                continue;
            }
            bufferedWriter.write(type.name());
            bufferedWriter.newLine();
            FormatUtil.writeLines(bufferedWriter, fullImproperLineMap.get(type), line -> '\t' + line);
        }
        bufferedWriter.close();
        logger.info("Finished writing streetfile data.");
        return streetfilePath;
    }

    private String toCsvLine(Map.Entry<StreetfileAddressRange, CompactDistrictMap> entry) {
        List<String> districts = streetfileDao.order().stream()
                .map(type -> getString(type, entry.getValue().get(type))).toList();
        List<String> fullParts = entry.getKey().parts();
        fullParts.addAll(districts);
        // Null strings do not have quotes.
        return ('"' + String.join("\",\"", fullParts) + '"')
                .replaceAll("\"%s\"".formatted(streetfileDao.nullString()), streetfileDao.nullString()).toUpperCase();
    }

    private String getString(DistrictType type, short num) {
        if (num == 0) {
            return streetfileDao.nullString();
        }
        if (type == DistrictType.TOWN_CITY) {
            return StreetfileDataExtractor.codeToIdBiMap.inverse().get(num);
        }
        return String.valueOf(num);
    }

    private BaseParser getParser(File file, Multimap<County, TownCity> countyToTownCityMap, TownCity nyc) {
        String filename = file.getName().toLowerCase();
        County county = getCounty(countyToTownCityMap.keySet(), filename);
        if (county == null) {
            if (filename.contains("voter")) {
                return new VoterFileParser(file, countyToTownCityMap, nyc);
            }
            // AddressPoints
            else if (filename.contains("address_points")) {
                var map = countyToTownCityMap.keySet().stream().collect(
                        Collectors.toMap(tempCounty -> tempCounty.name().toLowerCase(), County::senateCode)
                );
                return new AddressPointsParser(file, map);
            }
            else throw new IllegalArgumentException(file.getName() + " could not be matched with a parser.");
        }
        return switch (county.name()) {
            case "Bronx", "New York", "Queens", "Kings", "Richmond" -> new NYCParser(file, county, nyc);
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

    private County getCounty(Set<County> counties, String filename) {
        filename = filename.replaceAll("_", " ");
        for (County county : counties) {
            if (filename.contains(county.streetfileName().toLowerCase())) {
                return county;
            }
        }
        return null;
    }
}
