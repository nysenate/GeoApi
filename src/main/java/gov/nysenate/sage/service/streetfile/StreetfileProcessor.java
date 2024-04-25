package gov.nysenate.sage.service.streetfile;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StreetfileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StreetfileProcessor.class);
    private static final int BATCH_SIZE = 1000;
    private final File streetfileDir;
    private final Path streetfilePath, conflictPath;
    private final USPSAMSDao amsDao;
    private final List<County> counties;

    @Autowired
    public StreetfileProcessor(@Value("${streetfile.dir}") String streetfileDir,
                              USPSAMSDao amsDao, CountyDao countyDao) {
        this.streetfileDir = Path.of(streetfileDir, "sourceData").toFile();
        this.streetfilePath = Path.of(streetfileDir, "results", "streetfile.txt");
        this.conflictPath = Path.of(streetfileDir, "results", "conflicts.txt");
        this.amsDao = amsDao;
        this.counties = countyDao.getCounties();
    }

    public synchronized void regenerateStreetfile() throws IOException {
        File[] files = streetfileDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        DistrictingData result = new DistrictingData(4000000, files.length);
        final var correctionMap = new HashMap<AddressWithoutNum, AddressWithoutNum>(100000);
        logger.info("Starting streetfile processing...");
        for (File streetfile : files) {
            if (streetfile.isDirectory()) {
                continue;
            }
            BaseParser parser = getParser(streetfile);
            parser.parseFile();
            logger.info("Parsed " + streetfile.getName() + ". Validating addresses...");
            addCorrections(parser.getData(), correctionMap);
            result.copyFromAndClear(parser.getData());
            logger.info("Validated " + streetfile.getName() + " addresses.");
        }
        correctionMap.forEach(result::replace);
        Files.deleteIfExists(conflictPath);
        Files.createFile(conflictPath);
        logger.info("Done parsing streetfiles. Consolidating...");
        Map<StreetfileAddressRange, CompactDistrictMap> fullData = result.consolidate(conflictPath);
        List<String> lines = fullData.entrySet().stream().map(entry -> entry.getKey() + " " + entry.getValue()).toList();
        Files.write(streetfilePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
            case "Allegany", "Columbia", "Saratoga" -> new SaratogaParser(file, county);
            case "Bronx", "New York" -> new NYCParser(file, county, county.name());
            case "Kings", "Richmond" -> new NYCParser(file, county, county.streetfileName());
            case "Queens" -> new NYCParser(file, county, "");
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

    private void addCorrections(DistrictingData data, final Map<AddressWithoutNum, AddressWithoutNum> correctionMap) {
        Map<AddressWithoutNum, List<Integer>> toCorrectMap = data.rowToNumMap().entrySet().stream()
                .filter(entry -> !correctionMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (int currIndex = 0; !toCorrectMap.isEmpty(); currIndex++) {
            final int finalCurrIndex = currIndex;
            // Removed AddressWithoutNums that weren't validated this time around.
            toCorrectMap.entrySet().stream()
                    .filter(entry -> finalCurrIndex >= entry.getValue().size())
                    .map(Map.Entry::getKey).toList().forEach(toCorrectMap::remove);
            List<AddressWithoutNum> awns = new ArrayList<>(toCorrectMap.keySet());
            List<Address> toValidate = awns.stream().limit(BATCH_SIZE)
                    .map(awn -> getAddress(toCorrectMap.get(awn).get(finalCurrIndex), awn)).toList();
            List<AddressResult> validatedAddrs = amsDao.getValidatedAddressResults(toValidate);
            for (int addrIndex = 0; addrIndex < validatedAddrs.size(); addrIndex++) {
                AddressResult result = validatedAddrs.get(addrIndex);
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
                // Note: this doesn't guarantee no conflicts
                else if (!currCorrectedAwn.equals(mapCorrectedAwn)) {
                    logger.error("Conflict between corrections: " + mapCorrectedAwn + " and " + currCorrectedAwn);
                }
            }
        }
    }

    private static Address getAddress(int num, AddressWithoutNum awn) {
        var addr = new Address(num + " " + awn.street());
        addr.setPostal(awn.postalCity());
        addr.setZip5(String.valueOf(awn.zip5()));
        addr.setState("NY");
        return addr;
    }
}
