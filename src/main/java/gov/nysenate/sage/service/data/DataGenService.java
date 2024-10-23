package gov.nysenate.sage.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.model.assembly.SqlAssemblyDao;
import gov.nysenate.sage.dao.model.congressional.SqlCongressionalDao;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.Assembly;
import gov.nysenate.sage.model.district.Congressional;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.controller.ConstantUtil;
import gov.nysenate.services.NYSenateClientService;
import gov.nysenate.services.NYSenateJSONClient;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static gov.nysenate.sage.model.result.ResultStatus.*;

@Service
public class DataGenService implements SageDataGenService {
    private static final Logger logger = LoggerFactory.getLogger(DataGenService.class);
    private final SqlAssemblyDao sqlAssemblyDao;
    private final SqlCongressionalDao sqlCongressionalDao;
    private final SqlSenateDao sqlSenateDao;
    private final Environment env;
    @Value("${nysenate.domain:http://www.nysenate.gov}")
    private String nysenateDomain;

    @Autowired
    public DataGenService(SqlSenateDao sqlSenateDao, SqlAssemblyDao sqlAssemblyDao,
                          SqlCongressionalDao sqlCongressionalDao, Environment env) {
        this.sqlSenateDao = sqlSenateDao;
        this.sqlAssemblyDao = sqlAssemblyDao;
        this.sqlCongressionalDao = sqlCongressionalDao;
        this.env = env;
    }

    public Object vacantizeSenateData() {
        boolean updated = false;
        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID );

        ArrayList<Senator> vacantSenatorsList = new ArrayList<>();
        //handle the empty ones
        for (int i=0; i < 64; i++) {
            Senator vacantSenator = new Senator();
            vacantSenator.setDistrict(new District(i,"https://www.nysenate.gov/district/" + i));
            vacantSenator.setShortName("Vacant");
            vacantSenator.setName("Vacant District " + i);
            vacantSenator.setFirstName("Vacant");
            vacantSenator.setLastName("District");
            vacantSenator.setImageUrl("https://www.nysenate.gov/themes/custom/nysenate_theme/dist/images/nys_logo_header240x240.jpg");
            vacantSenatorsList.add(vacantSenator);
        }

        try {
            //empty senator table in the database
            sqlSenateDao.deleteSenators();
            //insert new entries
            for (Senator vacantSenator : vacantSenatorsList) {
                sqlSenateDao.insertSenator(vacantSenator);
            }
            //Update Cache
            sqlSenateDao.updateSenatorCache();
            updated = true;
        }
        catch (Exception e) {
            logger.error("Failed to vacantize the Senator table " + e);
            apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        }

        if (updated) {
            return new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        }
        else {
            return apiResponse;
        }
    }


    public Object generateMetaData(String option) throws IOException {
        boolean updated = false;
        boolean processAssembly = false;
        boolean processCongress = false;
        boolean processSenate = false;

        switch (option) {
            case "all" -> {
                processAssembly = true;
                processSenate = true;
                processCongress = true;
            }
            case "assembly", "a" -> processAssembly = true;
            case "congress", "c" -> processCongress = true;
            case "senate", "s" -> processSenate = true;
            default -> {
                logger.error("{}: Invalid option", option);
                return new ApiError(this.getClass(), API_REQUEST_INVALID);
            }
        }

        if (processAssembly) {
            updated = generateAssemblyData();
        }

        if (processCongress) {
            updated = generateCongressionalData();
        }

        if (processSenate) {
            updated = generateSenateData();
        }

        if (updated) {
            return new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
        }
        else {
            return new ApiError(this.getClass(), INTERNAL_ERROR);
        }
    }

    public void updateSenatorCache() {
        sqlSenateDao.updateSenatorCache();
    }

    /**
     * Retrieves Congressional member data from an external source and updates the
     * relevant data in the database.
     */
    private boolean generateCongressionalData() {
        logger.info("Indexing NY Congress by scraping its website...");

        /** Retrieve the congressional members and insert into the database */
        List<Congressional> congressionals = CongressScraper.getCongressionals();
        for (Congressional congressional : congressionals) {
            int district = congressional.getDistrict();
            Congressional existingCongressional = sqlCongressionalDao.getCongressionalByDistrict(district);

            if (existingCongressional == null) {
                sqlCongressionalDao.insertCongressional(congressional);
            } else if (isCongressionalDataUpdated(existingCongressional, congressional)) {
                sqlCongressionalDao.deleteCongressional(district);
                sqlCongressionalDao.insertCongressional(congressional);
            }
        }
        return true;
    }

    /**
     * Retrieves Assembly member data from an external source and updates the
     * relevant data in the database.
     */
    private boolean generateAssemblyData() {
        logger.info("Indexing NY Assembly by scraping its website...");

        /** Retrieve the assemblies and insert into the database */
        List<Assembly> assemblies = AssemblyScraper.getAssemblies();
        for (Assembly assembly : assemblies) {
            int district = assembly.getDistrict();
            Assembly existingAssembly = sqlAssemblyDao.getAssemblyByDistrict(district);

            if (existingAssembly == null) {
                sqlAssemblyDao.insertAssembly(assembly);
            } else if (isAssemblyDataUpdated(existingAssembly, assembly)) {
                sqlAssemblyDao.deleteAssemblies(district);
                sqlAssemblyDao.insertAssembly(assembly);
            }
        }
        return true;
    }

    /**
     * Retrieves senate data from the NY Senate API Client and stores it in
     * the database.
     *
     */
    private boolean generateSenateData() throws IOException {
        boolean updated = false;

        NYSenateClientService senateClient;

        logger.info("Generating senate data from NY Senate client services");

        /** Obtain the senate client service */

        senateClient = new NYSenateJSONClient(nysenateDomain);

        /** Retrieve the list of senators from the client API */
        List<Senator> senators = senateClient.getSenators();

//        boolean[] emptySenators = new boolean[62];
//        for (int i=0; i < 62; i++) {
//            emptySenators[i] = true;
//        }

        for (Senator senator : senators) {
            int district = senator.getDistrict().getNumber();
            if (district > 0) {
//                emptySenators[district] = false;
                Senator existingSenator = sqlSenateDao.getSenatorByDistrict(district);
                for (Office office : senator.getOffices()) {
                    getUpdatedGeocode(office);
                }
                if (verifyOfficeGeocode(senator)) {
                    if (existingSenator == null) {
                        sqlSenateDao.insertSenate(senator.getDistrict());
                        sqlSenateDao.insertSenator(senator);
                    }
                    else {
                        sqlSenateDao.deleteSenator(district);
                        sqlSenateDao.insertSenator(senator);
                    }
                }
                else {
                    logger.info("Could not update Senator " + senator.getName() + " District: " + district);
                }
                updated = true;
            }
        }
        //handle the empty ones
//        for (int i=0; i < 62; i++) {
//            if (emptySenators[i] = true) {
//                Senator vacantSenator = new Senator();
//                vacantSenator.setDistrict(new District(i,"https://www.nysenate.gov/district/" + i));
//                vacantSenator.setShortName("Vacant");
//                vacantSenator.setName("Vacant");
//                vacantSenator.setFirstName("Empty");
//                vacantSenator.setLastName("District");
//                vacantSenator.setImageUrl("https://www.nysenate.gov/sites/all/themes/nysenate/images/nys_logo224x224.png");
//                updated = true;
//            }
//        }

        if (updated) {
            updateSenatorCache();
        }
        return updated;
    }

    private boolean isCongressionalDataUpdated(Congressional c1, Congressional c2) {
        if (c1 != null && c2 != null) {
            if (!(c1.getDistrict() == c2.getDistrict() &&
                    c1.getMemberName().equals(c2.getMemberName()) &&
                    c1.getMemberUrl().equals(c2.getMemberUrl()))) {
                logger.info("Congressional District {} [{}] updated", c1.getDistrict(), c1.getMemberName());
                return true;
            }
        } else if (c1 == null && c2 != null) {
            return true;
        }
        return false;
    }

    private boolean isAssemblyDataUpdated(Assembly a1, Assembly a2) { //Existing is A1, New is A2
        if (a1 != null && a2 != null) {
            if (!(a1.getDistrict() == a2.getDistrict() &&
                    a1.getMemberName().equals(a2.getMemberName()) &&
                    a1.getMemberUrl().trim().equals(a2.getMemberUrl().trim()))) {
                logger.info("Assembly District " + a1.getDistrict() + " [" + a1.getMemberName() + "] updated");
                return true;
            }
        } else if (a1 == null && a2 != null) {
            return true;
        }
        return false;
    }

    private void getUpdatedGeocode(Office senatorOffice) {
        //Convert Senator Object info into an address
        Address officeAddress = new Address(senatorOffice.getStreet(),senatorOffice.getCity(),
                "NY",senatorOffice.getPostalCode());
        officeAddress.setAddr1( senatorOffice.getStreet().toLowerCase()
                .replaceAll("avesuite", "ave suite").replaceAll("avenuesuite", "avenue suite"));
        //Reorder the address
        officeAddress = StreetAddressParser.parseAddress(officeAddress).toAddress();
        //URL Encode all of the address parts
        officeAddress.setAddr1( URLEncoder.encode(officeAddress.getAddr1(), StandardCharsets.UTF_8)  );
        officeAddress.setAddr2( URLEncoder.encode(officeAddress.getAddr2(), StandardCharsets.UTF_8) );
        officeAddress.setPostalCity( URLEncoder.encode(officeAddress.getPostalCity(), StandardCharsets.UTF_8) );
        officeAddress.setZip5(officeAddress.getZip5());
        //Ensure Mixed Case
        StreetAddressParser.performInitCapsOnAddress(officeAddress);
        //Construct Url String
        String urlString = env.getBaseUrl() + "/api/v2/geo/geocode?addr1=" +
                officeAddress.getAddr1() + "&addr2=" + officeAddress.getAddr2() + "&city=" + officeAddress.getPostalCity() +
                "&state=NY&zip5=" + officeAddress.getZip5();
        urlString = urlString.replaceAll(" ", "%20");
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            String sageResponse = IOUtils.toString(is, StandardCharsets.UTF_8);
            JsonNode jsonResponse = new ObjectMapper().readTree(sageResponse);
            is.close();

            if (jsonResponse.get("status").toString().equals("\"SUCCESS\"")) {
                Geocode geocodedOffice = new ObjectMapper().readValue(jsonResponse.get("geocode").toString(), Geocode.class);
                if (geocodedOffice != null) {
                    senatorOffice.setLatitude( geocodedOffice.lat() );
                    senatorOffice.setLongitude( geocodedOffice.lon() );
                }
            }
            else {
                logger.error("SAGE was unable to geocode the address in the url: " + urlString);
            }

        }
        catch (IOException e) {
            logger.error("Unable to complete geocoding request to Senate Office " + senatorOffice.getStreet() +
                    ", " + senatorOffice.getCity() + ", NY " + senatorOffice.getPostalCode() + " " +e.getMessage());
        }
    }

    private boolean verifyOfficeGeocode(Senator senator) {
        List<Office> offices = senator.getOffices();

        for (Office office : offices) {
            double latitude = office.getLatitude();
            double longitude = office.getLongitude();

            if (latitude == 0.0 || longitude == 0.0 || Double.isNaN(latitude) || Double.isNaN(longitude)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Connects to the following two services: createZipCodesToGoFile and createZipCodesFile,
     * creates and compares the two files that was created.
     * A file that results from the comparison called final_list_zipcodes.csv will be created.
     */
    public Object generateZipCsv() {
        if (!siteZipCodesToGoCsv() || !siteZipCodesCsv()) {
            return new ApiError(this.getClass(), INTERNAL_ERROR);
        }
        Map<String,String> mapZips = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(ConstantUtil.ZIPS_DIRECTORY
                + ConstantUtil.ZIPCODES_FILE))) {
            stream.forEach(line -> {
                String[] zipcodeType = line.split(",");
                String zip = zipcodeType[0];
                String type = zipcodeType[1].trim();
                mapZips.put(zip,type);
            });
        }
        catch (IOException e) {
            logger.error("Unable to read " + ConstantUtil.ZIPCODES_FILE);
        }
        try (Stream<String> stream = Files.lines(Paths.get(ConstantUtil.ZIPS_DIRECTORY
                + ConstantUtil.ZIPCODESTOGO_FILE))) {
            stream.forEach(line -> {
                if(!mapZips.containsKey(line)){
                    mapZips.put(line,"");
                }
            });
        }
        catch (IOException e) {
            logger.error("Unable to read " + ConstantUtil.ZIPCODESTOGO_FILE);
        }
        List<String> finalList = new ArrayList<>();
        try {
            mapZips.forEach((key, value) -> finalList.add(key + "," + value));
            FileWriter finalCSV = new FileWriter(ConstantUtil.ZIPS_DIRECTORY + ConstantUtil.LAST_ZIPCODE_FILE);
            String collection = String.join("\n", finalList);
            finalCSV.write(collection);
            finalCSV.close();
        }
        catch(IOException e) {
            logger.error("Unable to write " + ConstantUtil.LAST_ZIPCODE_FILE);
        }
        return new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());

    }


    /**
     * Connects to zipcodestogo.com and retrieves all the zip codes
     * present in the first table and writes to a csv file
     */
    private boolean siteZipCodesToGoCsv() {
        try {
            File zipCodesToGoFile = new File(ConstantUtil.ZIPS_DIRECTORY + ConstantUtil.ZIPCODESTOGO_FILE);
            if (zipCodesToGoFile.exists()) {
                logger.info("zipcodestogo.csv file already exists");
                return true;
            }
            Document pageZipCodesToGo;
            pageZipCodesToGo = Jsoup.connect("https://www.zipcodestogo.com/New%20York/").get();
            ArrayList<String> arrayZipCodesToGo = new ArrayList<>();
            for(Element row : pageZipCodesToGo.select("td[align=center]")) {
                String zip = row.select("a").first().text();
                arrayZipCodesToGo.add(zip);
            }
            FileWriter writerZipsCodesToGo = new FileWriter(ConstantUtil.ZIPS_DIRECTORY + ConstantUtil.ZIPCODESTOGO_FILE);
            String collection = String.join("\n", arrayZipCodesToGo);
            writerZipsCodesToGo.write(collection);
            writerZipsCodesToGo.close();
            return true;
        }
        catch (IOException excep) {
            logger.error("Error creating zipcodestogo.csv file", excep);
            return false;
        }
    }

    /**
     * Connects to zip-codes.com and retrieves all the zip codes
     * present in the first table and writes to a csv file
     */
    private boolean siteZipCodesCsv() {
        try {
            File zipCodesFile = new File(ConstantUtil.ZIPS_DIRECTORY + ConstantUtil.ZIPCODES_FILE);
            if (zipCodesFile.exists()) {
                logger.info("zipcodes.csv file already exists");
                return true;
            }
            Document pageZipCodes = Jsoup.connect("https://www.zip-codes.com/state/ny.asp").get();
            Elements trs = pageZipCodes.select("table.statTable tr");
            trs.remove(0);
            List<String> arrayZipCodes = new ArrayList<>();
            for(Element row : trs) {
                Elements tds = row.getElementsByTag("td");
                Element td = tds.first();
                Element tdType = tds.last();
                if(td.text().contains("ZIP Code")) {
                    String trimedzipCode = td.text();
                    String type = tdType.text();
                    trimedzipCode = trimedzipCode.substring(9);
                    String zipandType = trimedzipCode.concat(", ").concat(type);
                    arrayZipCodes.add(zipandType);
                }
            }
            var writerZipsCodes = new FileWriter(ConstantUtil.ZIPS_DIRECTORY + ConstantUtil.ZIPCODES_FILE);
            String collection = String.join("\n", arrayZipCodes);
            writerZipsCodes.write(collection);
            writerZipsCodes.close();
            return true;
        }
        catch (IOException excep) {
            logger.error("Error creating zipcodes.csv file", excep);
            return false;
        }
    }


}
