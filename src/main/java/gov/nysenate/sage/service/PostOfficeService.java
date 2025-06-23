package gov.nysenate.sage.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.provider.PostOfficeCacheManager;
import gov.nysenate.sage.service.address.AddressService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

@Service
public class PostOfficeService {
    private static final Logger logger = LoggerFactory.getLogger(PostOfficeService.class);
    private final File dataDir;
    private final AddressService addressService;
    private final PostOfficeDao dao;

    @Autowired
    public PostOfficeService(@Value("${post.office.data.dir}") String postOfficeDataDir,
                             AddressService addressService, PostOfficeDao dao) {
        this.dataDir = new File(postOfficeDataDir);
        this.addressService = addressService;
        this.dao = dao;
    }

    /**
     * Clears out the database table, and replaces it with new data.
     * @return if the operation succeeded.
     * @throws IOException if there was a problem processing the file.
     */
    public synchronized boolean replaceData() throws IOException {
        File[] files = dataDir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        Multimap<Zip5, BuildingAddress> poAddrs = ArrayListMultimap.create();
        for (File file : files) {
            poAddrs.putAll(getData(file));
        }
        dao.replaceData(poAddrs);
        PostOfficeCacheManager.clearCaches();
        return true;
    }

    /**
     * Gets the Post Office data from a given file.
     * As of writing, said files can be found online at: <a href="https://postalpro.usps.com/ZIP_Locale_Detail">...</a>
     */
    private Multimap<Zip5, BuildingAddress> getData(File dataFile) throws IOException {
        Multimap<Zip5, BuildingAddress> dataMap = ArrayListMultimap.create();
        var scanner = new Scanner(dataFile);
        String extension = FilenameUtils.getExtension(dataFile.getName());
        String fileDelim = switch (extension) {
            case "csv" -> ",";
            case "tsv" -> "\t";
            default -> throw new IllegalArgumentException("Cannot process %s files".formatted(extension));
        };
        String delim = "\"?" + fileDelim + "\"?";

        // Different files put the delivery zip at different indices.
        String[] columns = scanner.nextLine().split(delim);
        int deliveryZipIndex = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].matches("DELIVERY ZIPCODE|ZIP CODE")) {
                deliveryZipIndex = i;
                break;
            }
        }

        while (scanner.hasNextLine()) {
            String[] lineData = scanner.nextLine().split(delim, -1);
            int numParts = lineData.length;
            if (!lineData[numParts - 3].equals("NY")) {
                continue;
            }
            var currAddr = new BuildingAddress(lineData[numParts - 5], lineData[numParts - 4], "NY",
                    lineData[numParts - 2], lineData[numParts - 1]);
            Address correctedAddr = addressService.validateOrDefault(currAddr);
            if (correctedAddr.isUspsValidated() && correctedAddr instanceof BuildingAddress correctedBldgAddr && correctedBldgAddr.getZip4() != null) {
                dataMap.put(new Zip5(lineData[deliveryZipIndex]), correctedBldgAddr);
            }
            else {
                logger.warn("Problem validating this Post Office: {}", currAddr);
            }
        }
        return dataMap;
    }
}
