package gov.nysenate.sage.service.district;

import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.PostOfficeAddress;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.scripts.streetfinder.PostOfficeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PostOfficeService {
    private final ConcurrentMap<Integer, PostOfficeDistrictData> cache = new ConcurrentHashMap<>();
    private final File dataDir;
    private final PostOfficeDao dao;
    // Autowired to prevent circular dependency
    @Autowired
    private TopLevelDistrictService districtService;

    @Autowired
    public PostOfficeService(@Value("${post.office.data.dir}") String postOfficeDataDir,
                             PostOfficeDao dao) {
        this.dataDir = new File(postOfficeDataDir);
        this.dao = dao;
    }

    /**
     * Clears out the database table, and replaces it with new data.
     * @return a message String.
     * @throws IOException if there was a problem processing the file.
     */
    public String replaceData() throws IOException {
        File[] files = dataDir.listFiles();
        if (files == null) {
            return dataDir + " is not a valid directory.";
        }
        if (files.length > 1) {
            return "Multiple data files found. No action taken.";
        }
        if (files.length == 1) {
            dao.replaceData(PostOfficeParser.getData(files[0]));
        }
        cache.clear();
        return files.length == 0 ? "No data files found. Cleared cache instead." : "Success.";
    }

    public DistrictedAddress getDistrictedAddress(String poBoxZip5, String city) {
        int deliveryZip;
        try {
            deliveryZip = Integer.parseInt(poBoxZip5);
        } catch (NumberFormatException ex) {
            return null;
        }
        PostOfficeDistrictData result = cache.get(deliveryZip);
        if (result == null) {
            result = new PostOfficeDistrictData(
                    dao.getPostOffices(deliveryZip).stream().map(this::getDistrictedAddress).toList()
            );
            cache.put(deliveryZip, result);
        }
        return result.isEmpty() ? null : result.getDistrictedAddress(city);
    }

    private DistrictedAddress getDistrictedAddress(PostOfficeAddress poAddress) {
        var request = new DistrictRequest();
        request.setDistrictStrategy(DistrictServiceProvider.DistrictStrategy.shapeFallback);
        request.setAddress(poAddress.address());
        request.setUspsValidate(true);
        DistrictResult results = districtService.handleDistrictRequest(request, -1);
        return results.getDistrictedAddress();
    }
}
