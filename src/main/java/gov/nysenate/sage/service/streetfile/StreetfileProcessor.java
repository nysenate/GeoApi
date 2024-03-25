package gov.nysenate.sage.service.streetfile;

import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.usps.USPSAMSDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.parsers.*;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.CellId;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.CompactDistrictMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class StreetfileProcessor {
    private static final int batchSize = 2000;
    private final File streetfileDir;
    private final USPSAMSDao amsDao;
    private final List<County> counties;

    @Autowired
    public StreetfileProcessor(@Value("${streetfile.dir}") String streetfileDir,
                              USPSAMSDao amsDao, CountyDao countyDao) {
        this.streetfileDir = new File(streetfileDir);
        this.amsDao = amsDao;
        this.counties = countyDao.getCounties();
    }

    public synchronized void regenerateStreetfile() throws IOException {
        DistrictingData result = null;
        for (File streetfile : streetfileDir.listFiles()) {
            if (result == null) {
                result = getDataFromFile(streetfile);
            }
            else {
                result.copyFromAndClear(getDataFromFile(streetfile));
            }
        }
        // TODO: add data to SQL streetfile table
    }

    public DistrictingData getDataFromFile(File file) throws IOException {
        BaseParser parser = getParser(file);
        parser.parseFile();
        DistrictingData data = parser.getData();
        Queue<StreetfileAddressRange> ranges = new LinkedList<>(data.rows());
        while (!ranges.isEmpty()) {
            List<Address> batch = new ArrayList<>();
            // Need to keep ordered track of the relevant data
            var districtDataList = new ArrayList<Multimap<CompactDistrictMap, CellId>>();
            for (int i = 0; i < batchSize && !ranges.isEmpty(); i++) {
                StreetfileAddressRange originalRange = ranges.poll();
                batch.add(originalRange.getAddress(true));
                // TODO: add AddressCorrectionHandler logic
                if (parser.isRangeData()) {
                    batch.add(originalRange.getAddress(false));
                }
                // Note: this list will have size <= batchSize, but for range data, batch size is doubled.
                districtDataList.add(data.remove(originalRange));
            }
            List<AddressResult> results = amsDao.getValidatedAddressResults(batch);
            for (int i = 0; i < results.size(); i++) {
                Multimap<CompactDistrictMap, CellId> districtData = districtDataList.get(i);
                AddressResult low = results.get(i);
                AddressResult high = results.get(parser.isRangeData() ? ++i : i);
                if (!low.isValidated() || !high.isValidated()) {
                    // TODO: add invalid results somewhere
                    continue;
                }
                var validatedRange = new StreetfileAddressRange(low.getAddress(), high.getAddress());
                for (var entry : districtData.entries()) {
                    data.put(new StreetfileLineData(validatedRange, entry.getKey(), entry.getValue()));
                }
            }
        }
        return data;
    }

    private BaseParser getParser(File file) {
        County fileCounty = null;
        for (County county : counties) {
            if (file.getName().contains(county.name())) {
                fileCounty = county;
                break;
            }
        }
        if (fileCounty != null) {
            return switch (fileCounty.name()) {
                case "Allegany", "Columbia", "Saratoga" -> new SaratogaParser(file);
                case "Bronx", "Kings", "New York", "Queens", "Richmond" -> new NYCParser(file);
                case "Erie" -> new ErieParser(file);
                case "Essex" -> new EssexParser(file);
                case "Montgomery" -> new MontgomeryParser(file);
                case "Nassau" -> new NassauParser(file);
                case "Schoharie" -> new SchoharieParser(file);
                case "Suffolk" -> new SuffolkParser(file);
                case "Westchester" -> new WestchesterParser(file);
                case "Wyoming" -> new WyomingParser(file);
                default -> new NTSParser(file);
            };
        }
        if (file.getName().contains("Voter")) {
            return new VoterFileParser(file);
        }
        return new AddressPointsParser(file);
    }
}
