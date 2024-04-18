package gov.nysenate.sage.service.streetfile;

import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.usps.USPSAMSDao;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.parsers.*;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.CompactDistrictMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StreetfileProcessor {
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
        for (File streetfile : files) {
            BaseParser parser = getParser(streetfile);
            parser.parseFile(amsDao);
            result.copyFromAndClear(parser.getData());
        }
        Files.deleteIfExists(conflictPath);
        Files.createFile(conflictPath);
        Map<StreetfileAddressRange, CompactDistrictMap> fullData = result.consolidate(conflictPath);
        List<String> lines = fullData.entrySet().stream().map(entry -> entry.getKey().toString() + " " + entry.getValue()).toList();
        Files.write(streetfilePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private BaseParser getParser(File file) {
        County county = getCounty(file.getName());
        if (county == null) {
            if (file.getName().contains("Voter")) {
                var map = counties.stream().collect(Collectors.toMap(County::voterfileCode, County::fipsCode));
                return new VoterFileParser(file, map);
            }
            // AddressPoints
            else {
                var map = counties.stream().collect(Collectors.toMap(tempCounty -> tempCounty.name().toLowerCase(), County::fipsCode));
                return new AddressPointsParser(file, map);
            }
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
        for (County county : counties) {
            if (filename.contains(county.name())) {
                return county;
            }
        }
        return null;
    }
}
