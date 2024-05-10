package gov.nysenate.sage.service.streetfile;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.usps.USPSAMSDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
public class StreetfileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StreetfileProcessor.class);
    private static final int THOUSAND = 1000, VALIDATION_BATCH_SIZE = 4 * THOUSAND,
            PRINT_BATCH_SIZE = 10 * THOUSAND, NUM_STREETS = 100 * THOUSAND;
    private final File sourceDir, resultsDir;
    private final Path streetfilePath, conflictPath, improperPath, invalidPath;
    private final USPSAMSDao amsDao;
    private final List<County> counties;

    @Autowired
    public StreetfileProcessor(@Value("${streetfile.dir}") String streetfileDir,
                              USPSAMSDao amsDao, CountyDao countyDao) {
        this.sourceDir = Path.of(streetfileDir, "sourceData").toFile();
        this.resultsDir = Path.of(streetfileDir, "results").toFile();
        this.streetfilePath = Path.of(resultsDir.getPath(), "streetfile.txt");
        this.conflictPath = Path.of(resultsDir.getPath(), "conflicts.txt");
        this.improperPath = Path.of(resultsDir.getPath(), "improper.txt");
        this.invalidPath = Path.of(resultsDir.getPath(), "invalid.txt");
        this.amsDao = amsDao;
        this.counties = countyDao.getCounties();
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

        DistrictingData fullData = new DistrictingData(NUM_STREETS);
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
        final var correctionMap = getCorrections(fullData);
        DistrictingData invalidData = fullData.removeInvalidAddresses(correctionMap);
        Files.write(invalidPath, List.of(invalidData.toString()), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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

    private Map<AddressWithoutNum, AddressWithoutNum> getCorrections(DistrictingData data) {
        final var correctionMap = new HashMap<AddressWithoutNum, AddressWithoutNum>(NUM_STREETS);
        Map<AddressWithoutNum, Queue<Integer>> toCorrectMap = data.rowToNumMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        while (!toCorrectMap.isEmpty()) {
            List<AddressWithoutNum> awns = toCorrectMap.keySet().stream().limit(VALIDATION_BATCH_SIZE).toList();
            List<Address> toValidate = awns.stream()
                    .map(awn -> getAddress(toCorrectMap.get(awn).poll(), awn)).toList();
            List<AddressResult> validationResults = amsDao.getValidatedAddressResults(toValidate);
            for (int addrIndex = 0; addrIndex < validationResults.size(); addrIndex++) {
                AddressResult result = validationResults.get(addrIndex);
                if (!result.isValidated()) {
                    continue;
                }
                final var currCorrectedAwn = new AddressWithoutNum(result.getAddress());
                AddressWithoutNum uncorrectedAwn = awns.get(addrIndex);
                AddressWithoutNum mapCorrectedAwn = correctionMap.get(uncorrectedAwn);
                if (mapCorrectedAwn == null) {
                    correctionMap.put(uncorrectedAwn, currCorrectedAwn);
                    toCorrectMap.remove(uncorrectedAwn);
                }
                else if (!currCorrectedAwn.equals(mapCorrectedAwn)) {
                    logger.error("Conflict between corrections: " + mapCorrectedAwn + " and " + currCorrectedAwn);
                }
            }
            // Removes AddressWithoutNums without any valid addresses.
            toCorrectMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isEmpty())
                    .map(Map.Entry::getKey).toList().forEach(toCorrectMap::remove);
        }
        return correctionMap;
    }

    private static Address getAddress(int num, AddressWithoutNum awn) {
        var addr = new Address(num + " " + awn.street());
        addr.setPostalCity(awn.postalCity());
        addr.setZip5(String.valueOf(awn.zip5()));
        addr.setState("NY");
        return addr;
    }
}
