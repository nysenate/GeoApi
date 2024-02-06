package gov.nysenate.sage.service.district;

import com.google.common.collect.ArrayListMultimap;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.PostOfficeAddress;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.scripts.streetfinder.PostOfficeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class PostOfficeService {
    private final PostOfficeDao dao;
    private final TopLevelDistrictService districtService;
    private final Map<Integer, PostOfficeDistrictData> postOfficeMap = new HashMap<>();
    @Value("${post.office.data.dir}")
    private String postOfficeDataDir;

    @Autowired
    public PostOfficeService(PostOfficeDao dao, TopLevelDistrictService districtService) {
        this.dao = dao;
        this.districtService = districtService;
        remakePostOfficeMap();
    }

    /**
     * Remakes the cached data based on the current database table.
     */
    private synchronized void remakePostOfficeMap() {
        postOfficeMap.clear();
        ArrayListMultimap<Integer, DistrictedAddress> dataMultimap = ArrayListMultimap.create();
        for (PostOfficeAddress address : dao.getAllPostOffices()) {
            var request = new DistrictRequest();
            request.setDistrictStrategy(DistrictServiceProvider.DistrictStrategy.shapeFallback);
            request.setAddress(address.fullAddress());
            dataMultimap.put(address.deliveryZip(), districtService.handleDistrictRequest(request, -1).getDistrictedAddress());
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
            return "Multiple data files found.";
        }
        if (files.length == 1) {
            dao.replaceData(PostOfficeParser.getData(files[0]));
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
