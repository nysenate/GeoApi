package gov.nysenate.sage.controller.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.model.AssemblyDao;
import gov.nysenate.sage.dao.model.CongressionalDao;
import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.model.district.Assembly;
import gov.nysenate.sage.model.district.Congressional;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.sage.util.ImageUtil;
import gov.nysenate.services.NYSenateClientService;
import gov.nysenate.services.NYSenateJSONClient;
import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;

@RestController
public class GenerateMetaDataController {

    @Autowired
    Environment env;

    private Logger logger = LogManager.getLogger(GenerateMetaDataController.class);

    @RequestMapping(value = "/gen-meta-data/senator-images", method = RequestMethod.GET)
    public void generateSenatorImages(@RequestParam String path, @RequestParam int height) {
        //Bad input return usage
        if (path == null || height > 0) {
            logger.error("Please provide the proper parameters to use this api funciton");
            return;
        }

        SenateDao senateDao = new SenateDao(env);
        Collection<Senator> senators = senateDao.getSenators();
        for (Senator senator : senators) {
            String filePath =  path + senator.getShortName() + ".png";
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                }
                catch (IOException e) {
                    logger.error("Failed to create new img file " + e.getMessage());
                    return;
                }
            }
            String baseImageDir = env.getNysenateDomain() + "/files/profile-pictures/";
            String url = baseImageDir + senator.getImageUrl().replace(baseImageDir, "").replace(" ", "%20");
            ImageUtil.saveResizedImage(url, "png", file, height);
        }
    }

    @RequestMapping(value = "/gen-meta-data/all", method = RequestMethod.GET)
    public void generateAll() {
        generateSenateData();
        generateAssemblyData();
        generateCongressData();
    }

    @RequestMapping(value = "/gen-meta-data/senate", method = RequestMethod.GET)
    public void generateSenateData() {
        NYSenateClientService senateClient;
        SenateDao senateDao = new SenateDao(env);

        logger.info("Generating senate data from NY Senate client services");

        /** Obtain the senate client service */
        String domain = env.getNysenateDomain();
        try {
            senateClient = new NYSenateJSONClient(domain);
            /** Retrieve the list of senators from the client API */
            List<Senator> senators = senateClient.getSenators();

            for (Senator senator : senators) {
                int district = senator.getDistrict().getNumber();
                if (district > 0) {
                    Senator existingSenator = senateDao.getSenatorByDistrict(district);
                    for (Office office : senator.getOffices()) {
                        getUpdatedGeocode(office);
                    }
                    if (existingSenator == null) {
                        senateDao.insertSenate(senator.getDistrict());
                        senateDao.insertSenator(senator);
                    } else {
                        senateDao.deleteSenator(district);
                        senateDao.insertSenator(senator);
                    }
                }
            }
        }
        catch (IOException e) {
            logger.error("Error updating senate meta data: \n" + e.getMessage());
        }



    }

    @RequestMapping(value = "/gen-meta-data/assembly", method = RequestMethod.GET)
    public void generateAssemblyData() {
        logger.info("Indexing NY Assembly by scraping its website...");
        AssemblyDao assemblyDao = new AssemblyDao();

        /** Retrieve the assemblies and insert into the database */
        List<Assembly> assemblies = AssemblyScraper.getAssemblies();
        for (Assembly assembly : assemblies) {
            int district = assembly.getDistrict();
            Assembly existingAssembly = assemblyDao.getAssemblyByDistrict(district);

            if (existingAssembly == null) {
                assemblyDao.insertAssembly(assembly);
            } else if (isAssemblyDataUpdated(existingAssembly, assembly)) {
                assemblyDao.deleteAssemblies(district);
                assemblyDao.insertAssembly(assembly);
            }
        }
    }

    @RequestMapping(value = "/gen-meta-data/congress", method = RequestMethod.GET)
    public void generateCongressData() {
        logger.info("Indexing NY Congress by scraping its website...");
        CongressionalDao congressionalDao = new CongressionalDao();

        /** Retrieve the congressional members and insert into the database */
        List<Congressional> congressionals = CongressScraper.getCongressionals();
        for (Congressional congressional : congressionals) {
            int district = congressional.getDistrict();
            Congressional existingCongressional = congressionalDao.getCongressionalByDistrict(district);
            if (existingCongressional == null) {
                congressionalDao.insertCongressional(congressional);
            } else if (isCongressionalDataUpdated(existingCongressional, congressional)) {
                congressionalDao.deleteCongressional(district);
                congressionalDao.insertCongressional(congressional);
            }
        }
    }

    private boolean isCongressionalDataUpdated(Congressional c1, Congressional c2) {
        if (c1 != null && c2 != null) {
            if (!(c1.getDistrict() == c2.getDistrict() &&
                    c1.getMemberName().equals(c2.getMemberName()) &&
                    c1.getMemberUrl().equals(c2.getMemberUrl()))) {
                logger.info("Congressional " + c1.getDistrict() + "updated.");
                return true;
            }
        } else if (c1 == null && c2 != null) {
            return true;
        }
        return false;
    }

    private boolean isAssemblyDataUpdated(Assembly a1, Assembly a2) {
        if (a1 != null && a2 != null) {
            if (!(a1.getDistrict() == a2.getDistrict() &&
                    a1.getMemberName().equals(a2.getMemberName()) &&
                    a1.getMemberUrl().equals(a2.getMemberUrl()))) {
                System.out.println("Assembly " + a1.getDistrict() + " updated.");
                return true;
            }
        } else if (a1 == null && a2 != null) {
            return true;
        }
        return false;
    }

    private void getUpdatedGeocode(Office senatorOffice) {
        String urlString = env.getBaseUrl() + "/api/v2/geo/geocode?addr1=" +
                senatorOffice.getStreet() + "&city=" + senatorOffice.getCity() +
                "&state=NY&zip5=" + senatorOffice.getPostalCode()  + "&bypassCache=false&useFallBack=true";
        urlString = urlString.replaceAll(" ", "%20");
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            String sageReponse = IOUtils.toString(is, "UTF-8");
            JsonNode jsonResonse = new ObjectMapper().readTree(sageReponse);
            IOUtils.closeQuietly(is);
            Geocode geocodedOffice = new ObjectMapper().readValue(jsonResonse.get("geocode").toString(), Geocode.class);
            senatorOffice.setLatitude( geocodedOffice.getLat() );
            senatorOffice.setLongitude( geocodedOffice.getLon() );
        }
        catch (IOException e) {
            logger.error("Unable to complete geocoding request to Senate Office " + senatorOffice.getStreet() +
                    ", " + senatorOffice.getCity() + ", NY " + senatorOffice.getPostalCode() + " " +e.getMessage());
        }
    }

}
