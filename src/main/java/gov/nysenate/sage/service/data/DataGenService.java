package gov.nysenate.sage.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.controller.admin.DataGenController;
import gov.nysenate.sage.dao.data.SqlDataGenDao;
import gov.nysenate.sage.dao.model.assembly.SqlAssemblyDao;
import gov.nysenate.sage.dao.model.congressional.SqlCongressionalDao;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.Assembly;
import gov.nysenate.sage.model.district.Congressional;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.scripts.streetfinder.County;
import gov.nysenate.sage.scripts.streetfinder.TownCode;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.sage.util.ImageUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.controller.ConstantUtil;
import gov.nysenate.services.NYSenateClientService;
import gov.nysenate.services.NYSenateJSONClient;
import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;
import org.apache.commons.io.IOUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import static gov.nysenate.sage.model.result.ResultStatus.*;

@Service
public class DataGenService implements SageDataGenService {

    private Logger logger = LoggerFactory.getLogger(DataGenController.class);
    private SqlAssemblyDao sqlAssemblyDao;
    private SqlCongressionalDao sqlCongressionalDao;
    private SqlSenateDao sqlSenateDao;
    private SqlDataGenDao sqlDataGenDao;
    private Environment env;

    @Autowired
    public DataGenService(SqlSenateDao sqlSenateDao,
                          SqlAssemblyDao sqlAssemblyDao, SqlCongressionalDao sqlCongressionalDao,
                          Environment env, SqlDataGenDao sqlDataGenDao) {
        this.sqlSenateDao = sqlSenateDao;
        this.sqlAssemblyDao = sqlAssemblyDao;
        this.sqlCongressionalDao = sqlCongressionalDao;
        this.sqlDataGenDao = sqlDataGenDao;
        this.env = env;
    }

    public boolean ensureTownCodeFile() {
        try {
            File towns = new File(ConstantUtil.STREETFINDER_DIRECTORY + ConstantUtil.TOWN_FILE);
            if (towns.exists()) {
                logger.info("Town code file already exists");
                return true;
            }

            towns.createNewFile();
            List<TownCode> townCodes = sqlDataGenDao.getTownCodes();

            FileWriter fileWriter = new FileWriter(ConstantUtil.STREETFINDER_DIRECTORY + ConstantUtil.TOWN_FILE);
            PrintWriter outputWriter = new PrintWriter(fileWriter);

            int count = 0;
            for (TownCode townCode : townCodes) {
                count++;
                outputWriter.println(townCode.toString());
            }

            logger.info("Wrote " + count + " town codes to file");
            fileWriter.close();
            outputWriter.close();
            return true;

        } catch (IOException ex) {
            logger.error("Error creating town code file", ex);
            return false;
        }
    }

    public boolean ensureCountyCodeFile() {
        try {
            File senateCounties = new File(ConstantUtil.STREETFINDER_DIRECTORY + ConstantUtil.COUNTY_FILE);
            if (senateCounties.exists()) {
                logger.info("Senate county code file already exists");
                return true;
            }

            senateCounties.createNewFile();
            List<County> counties = sqlDataGenDao.getCountyCodes();

            FileWriter fileWriter = new FileWriter(ConstantUtil.STREETFINDER_DIRECTORY + ConstantUtil.COUNTY_FILE);
            PrintWriter outputWriter = new PrintWriter(fileWriter);

            int count = 0;
            for (County county : counties) {
                count++;
                outputWriter.println(county.toString());
            }

            logger.info("Wrote " + count + " Senate county codes to file");
            fileWriter.close();
            outputWriter.close();
            return true;

        }  catch (IOException ex) {
            logger.error("Error creating town code file", ex);
            return false;
        }
    }

    public Object generateSenatorImages(String path, int height) {
        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID );

        try {
            Collection<Senator> senators = sqlSenateDao.getSenators();
            for (Senator senator : senators) {
                String filePath =  path + senator.getShortName() + ".png";
                File file = new File(filePath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                String baseImageDir = "http://www.nysenate.gov/files/profile-pictures/";
                String url = baseImageDir + senator.getImageUrl()
                        .replace(baseImageDir, "").replace(" ", "%20");
                ImageUtil.saveResizedImage(url, "png", file, height);
                apiResponse = new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
            }
        }
        catch (IOException e) {
            apiResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        }

        return apiResponse;
    }


    public Object generateMetaData(String option) throws IOException, XmlRpcException {
        boolean updated = false;
        boolean processAssembly = false;
        boolean processCongress = false;
        boolean processSenate = false;

        if (option.equals("all")) {
            processAssembly = true;
            processSenate = true;
            processCongress = true;
        }
        else if (option.equals("assembly") || option.equals("a")) {
            processAssembly = true;
        }
        else if (option.equals("congress") || option.equals("c")) {
            processCongress = true;
        }
        else if (option.equals("senate") || option.equals("s")) {
            processSenate = true;
        }
        else {
            logger.error(option+": Invalid option");
            return new ApiError(this.getClass(), API_REQUEST_INVALID);
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
        boolean updated = false;

        logger.info("Indexing NY Congress by scraping its website...");

        /** Retrieve the congressional members and insert into the database */
        List<Congressional> congressionals = CongressScraper.getCongressionals();
        for (Congressional congressional : congressionals) {
            int district = congressional.getDistrict();
            Congressional existingCongressional = sqlCongressionalDao.getCongressionalByDistrict(district);

            if (existingCongressional == null) {
                updated = true;
                sqlCongressionalDao.insertCongressional(congressional);
            } else if (isCongressionalDataUpdated(existingCongressional, congressional)) {
                updated = true;
                sqlCongressionalDao.deleteCongressional(district);
                sqlCongressionalDao.insertCongressional(congressional);
            }
        }
        return updated;
    }

    /**
     * Retrieves Assembly member data from an external source and updates the
     * relevant data in the database.
     */
    private boolean generateAssemblyData() {
        logger.info("Indexing NY Assembly by scraping its website...");
        boolean updated = false;

        /** Retrieve the assemblies and insert into the database */
        List<Assembly> assemblies = AssemblyScraper.getAssemblies();
        for (Assembly assembly : assemblies) {
            int district = assembly.getDistrict();
            Assembly existingAssembly = sqlAssemblyDao.getAssemblyByDistrict(district);

            if (existingAssembly == null) {
                updated = true;
                sqlAssemblyDao.insertAssembly(assembly);
            } else if (isAssemblyDataUpdated(existingAssembly, assembly)) {
                updated = true;
                sqlAssemblyDao.deleteAssemblies(district);
                sqlAssemblyDao.insertAssembly(assembly);
            }
        }
        return updated;
    }

    /**
     * Retrieves senate data from the NY Senate API Client and stores it in
     * the database.
     *
     * @throws XmlRpcException
     */
    private boolean generateSenateData() throws XmlRpcException, IOException {
        boolean updated = false;

        NYSenateClientService senateClient;

        logger.info("Generating senate data from NY Senate client services");

        /** Obtain the senate client service */

        String domain = env.getNysenateDomain();
        senateClient = new NYSenateJSONClient(domain);

        /** Retrieve the list of senators from the client API */
        List<Senator> senators = senateClient.getSenators();

        for (Senator senator : senators) {
            int district = senator.getDistrict().getNumber();
            if (district > 0) {
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
                logger.info("Congressional District " + c1.getDistrict() + " [" + c1.getMemberName() + "] updated");
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

    private void getUpdatedGeocode(Office senatorOffice) throws UnsupportedEncodingException {
        //Convert Senator Object info into an address
        Address officeAddress = new Address(senatorOffice.getStreet(),senatorOffice.getCity(),
                "NY",senatorOffice.getPostalCode());
        officeAddress.setAddr1( senatorOffice.getStreet().toLowerCase()
                .replaceAll("avesuite", "ave suite").replaceAll("avenuesuite", "avenue suite"));
        //Reorder the address
        officeAddress = StreetAddressParser.parseAddress(officeAddress).toAddress();
        //URL Encode all of the address parts
        officeAddress.setAddr1( URLEncoder.encode(officeAddress.getAddr1(), StandardCharsets.UTF_8.toString())  );
        officeAddress.setAddr2( URLEncoder.encode(officeAddress.getAddr2(), StandardCharsets.UTF_8.toString()) );
        officeAddress.setCity( URLEncoder.encode(officeAddress.getCity(), StandardCharsets.UTF_8.toString()) );
        officeAddress.setZip5( URLEncoder.encode(officeAddress.getZip5(), StandardCharsets.UTF_8.toString()) );
        //Ensure Mixed Case
        StreetAddressParser.performInitCapsOnAddress(officeAddress);
        //Construct Url String
        String urlString = env.getBaseUrl() + "/api/v2/geo/geocode?addr1=" +
                officeAddress.getAddr1() + "&addr2=" + officeAddress.getAddr2() + "&city=" + officeAddress.getCity() +
                "&state=NY&zip5=" + officeAddress.getZip5();
        urlString = urlString.replaceAll(" ", "%20");
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            String sageResponse = IOUtils.toString(is, "UTF-8");
            JsonNode jsonResponse = new ObjectMapper().readTree(sageResponse);
            is.close();

            if (jsonResponse.get("status").toString().equals("\"SUCCESS\"")) {
                Geocode geocodedOffice = new ObjectMapper().readValue(jsonResponse.get("geocode").toString(), Geocode.class);
                if (geocodedOffice != null) {
                    senatorOffice.setLatitude( geocodedOffice.getLat() );
                    senatorOffice.setLongitude( geocodedOffice.getLon() );
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
            Double latitude = office.getLatitude();
            Double longitude = office.getLongitude();

            if (latitude == null || longitude == null
                    || latitude == 0.0 || longitude == 0.0
                    || latitude.isNaN() ||  longitude.isNaN()) {
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
    public Object generateZipCsv() throws Exception {
        Object generateResponse;
        boolean createZipCodesToGoFile = false;
        boolean createZipCodesFile = false;
        createZipCodesToGoFile = siteZipCodesToGoCsv();
        createZipCodesFile = siteZipCodesCsv();
        if (createZipCodesToGoFile && createZipCodesFile) {
            generateResponse = new GenericResponse(true, SUCCESS.getCode() + ": " + SUCCESS.getDesc());
            HashMap<String,String> mapZips = new HashMap<String,String>();
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
            ArrayList<String> finalList = new ArrayList<>();
            try {
                mapZips.entrySet().forEach(entry-> {
                    finalList.add(entry.getKey()+","+entry.getValue());
                });
                FileWriter finalCSV = new FileWriter(ConstantUtil.ZIPS_DIRECTORY + ConstantUtil.LAST_ZIPCODE_FILE);
                String collection = finalList.stream().collect(Collectors.joining("\n"));
                finalCSV.write(collection);
                finalCSV.close();
            }
            catch(IOException e) {
                logger.error("Unable to write " + ConstantUtil.LAST_ZIPCODE_FILE);
            }
        }
        else {
            generateResponse = new ApiError(this.getClass(), INTERNAL_ERROR);
        }
        return generateResponse;
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
            String collection = arrayZipCodesToGo.stream().collect(Collectors.joining("\n"));
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
            Document pageZipCodes;
            pageZipCodes = Jsoup.connect("https://www.zip-codes.com/state/ny.asp").get();
            Elements trs = pageZipCodes.select("table.statTable tr");
            trs.remove(0);
            ArrayList<String> arrayZipCodes = new ArrayList<>();
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
            FileWriter writerZipsCodes = new FileWriter(ConstantUtil.ZIPS_DIRECTORY + ConstantUtil.ZIPCODES_FILE);
            String collection = arrayZipCodes.stream().collect(Collectors.joining("\n"));
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
