package gov.nysenate.sage.service.district;

import com.google.common.collect.ArrayListMultimap;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.PostOfficeAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.scripts.streetfinder.PostOfficeParser;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostOfficeService {
    private final Map<Integer, PostOfficeDistrictData> postOfficeMap = new HashMap<>();
    private final PostOfficeDao dao;
    private final AddressServiceProvider addressProvider;
    // Autowired to prevent circular dependency
    @Autowired
    private TopLevelDistrictService districtService;
    @Value("${post.office.data.dir}")
    private String postOfficeDataDir;

    @Autowired
    public PostOfficeService(PostOfficeDao dao, AddressServiceProvider addressProvider) {
        this.dao = dao;
        this.addressProvider = addressProvider;
    }

    @PostConstruct
    private void init() {
        remakePostOfficeMap();
    }

    /**
     * Remakes the cached data based on the current database table.
     */
    private synchronized void remakePostOfficeMap() {
        postOfficeMap.clear();
        List<PostOfficeAddress> poAddresses = dao.getAllPostOffices();

        var request = new BatchDistrictRequest();
        request.setDistrictStrategy(DistrictServiceProvider.DistrictStrategy.shapeFallback);
        request.setAddresses(poAddresses.stream().map(PostOfficeAddress::getAddress).toList());
        // To speedup startup, we validated before inserting the data.
        request.setUspsValidate(false);
        List<DistrictResult> results = districtService.handleBatchDistrictRequest(request);

        if (poAddresses.size() != results.size()) {
            throw new RuntimeException("Post Office list and results are different sizes.");
        }
        ArrayListMultimap<Integer, DistrictedAddress> dataMultimap = ArrayListMultimap.create();
        for (int i = 0; i < poAddresses.size(); i++) {
            dataMultimap.put(poAddresses.get(i).getDeliveryZip(), results.get(i).getDistrictedAddress());
        }
        for (int poBoxZip5 : dataMultimap.keySet()) {
            postOfficeMap.put(poBoxZip5, new PostOfficeDistrictData(dataMultimap.get(poBoxZip5)));
        }
    }

    /**
     * Clears out the database table, and replaces it with new data.
     * @return a message String.
     * @throws IOException if there was a problem processing the file.
     */
    public String replaceData() throws IOException {
        var dataDir = new File(postOfficeDataDir);
        File[] files = dataDir.listFiles();
        if (files == null) {
            return dataDir + " is not a valid directory.";
        }
        if (files.length > 1) {
            return "Multiple data files found. No action taken.";
        }
        if (files.length == 1) {
            List<PostOfficeAddress> postOfficeData = PostOfficeParser.getData(files[0]);
            List<Address> currAddresses = postOfficeData.stream().map(PostOfficeAddress::getAddress).toList();
            currAddresses = addressProvider.validate(currAddresses, null, false).stream().map(AddressResult::getAddress).toList();
            if (postOfficeData.size() != currAddresses.size()) {
                return "Error validating addresses. No action taken.";
            }
            for (int i = 0; i < currAddresses.size(); i++) {
                postOfficeData.get(i).setAddress(currAddresses.get(i));
            }
            dao.replaceData(postOfficeData);
        }
        remakePostOfficeMap();
        return files.length == 0 ? "No data files found. Reloaded cache instead." : "Success.";
    }

    public DistrictedAddress getDistrictedAddress(String poBoxZip5, String city) {
        try {
            return postOfficeMap.get(Integer.parseInt(poBoxZip5)).getDistrictedAddress(city);
        } catch (Exception ex) {
            return new DistrictedAddress();
        }
    }
}
