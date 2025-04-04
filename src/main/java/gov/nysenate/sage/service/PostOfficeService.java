package gov.nysenate.sage.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.util.PostOfficeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PostOfficeService {
    private final File dataDir;
    private final PostOfficeDao dao;

    @Autowired
    public PostOfficeService(@Value("${post.office.data.dir}") String postOfficeDataDir,
                             PostOfficeDao dao) {
        this.dataDir = new File(postOfficeDataDir);
        this.dao = dao;
    }

    /**
     * Clears out the database table, and replaces it with new data.
     * @return if the operation succeeded.
     * @throws IOException if there was a problem processing the file.
     */
    public boolean replaceData() throws IOException {
        File[] files = dataDir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        Multimap<Zip5, BuildingAddress> poAddrs = ArrayListMultimap.create();
        for (File file : files) {
            poAddrs.putAll(PostOfficeParser.getData(file));
        }
        dao.replaceData(poAddrs);
        // TODO: refresh caches?
        return true;
    }
}
