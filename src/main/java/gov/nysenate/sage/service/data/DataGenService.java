package gov.nysenate.sage.service.data;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.model.assembly.SqlAssemblyDao;
import gov.nysenate.sage.dao.model.congressional.SqlCongressionalDao;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.Assembly;
import gov.nysenate.sage.model.district.Congressional;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.AddressUtil;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.services.NYSenateClientService;
import gov.nysenate.services.NYSenateJSONClient;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

@Service
public class DataGenService implements SageDataGenService {
    private static final Logger logger = LoggerFactory.getLogger(DataGenService.class);
    private final SqlAssemblyDao sqlAssemblyDao;
    private final SqlCongressionalDao sqlCongressionalDao;
    private final SqlSenateDao sqlSenateDao;
    private final GeocodeServiceProvider geocodeProvider;
    @Value("${nysenate.domain:https://www.nysenate.gov}")
    private String nysenateDomain;

    @Autowired
    public DataGenService(SqlSenateDao sqlSenateDao, SqlAssemblyDao sqlAssemblyDao,
                          SqlCongressionalDao sqlCongressionalDao, GeocodeServiceProvider geocodeProvider) {
        this.sqlSenateDao = sqlSenateDao;
        this.sqlAssemblyDao = sqlAssemblyDao;
        this.sqlCongressionalDao = sqlCongressionalDao;
        this.geocodeProvider = geocodeProvider;
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
            logger.error("Failed to vacantize the Senator table {}", String.valueOf(e));
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
        senateClient = new NYSenateJSONClient(nysenateDomain);
        List<Senator> senators = senateClient.getSenators();

        for (Senator senator : senators) {
            int district = senator.getDistrict().getNumber();
            if (district > 0) {
                for (Office office : senator.getOffices()) {
                    setUpdatedGeocode(office);
                }
                if (verifyOfficeGeocode(senator)) {
                    Senator existingSenator = sqlSenateDao.getSenatorByDistrict(district);
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
                    logger.info("Could not update Senator {} District: {}", senator.getName(), district);
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
                logger.info("Congressional District {} [{}] updated", c1.getDistrict(), c1.getMemberName());
                return true;
            }
        } else return c1 == null && c2 != null;
        return false;
    }

    private boolean isAssemblyDataUpdated(Assembly a1, Assembly a2) { //Existing is A1, New is A2
        if (a1 != null && a2 != null) {
            if (!(a1.getDistrict() == a2.getDistrict() &&
                    a1.getMemberName().equals(a2.getMemberName()) &&
                    a1.getMemberUrl().trim().equals(a2.getMemberUrl().trim()))) {
                logger.info("Assembly District {} [{}] updated", a1.getDistrict(), a1.getMemberName());
                return true;
            }
        } else return a1 == null && a2 != null;
        return false;
    }

    private void setUpdatedGeocode(Office senatorOffice) {
        //Convert Senator Object info into an address
        String street = senatorOffice.getStreet().replaceAll("(?i)Avesuite", "Ave Suite")
                .replaceAll("(?i)avenuesuite", "Avenue Suite");
        Address officeAddress = new Address(street, senatorOffice.getCity(), senatorOffice.getPostalCode());
        //Ensure Mixed Case
        AddressUtil.performInitCapsOnAddress(officeAddress);
        GeocodeResult result = geocodeProvider.geocode(officeAddress, List.of(Geocoder.NYSGEO, Geocoder.GOOGLE), false);

        if (result.isSuccess()) {
            Geocode geocodedOffice = result.getGeocode();
            senatorOffice.setLatitude(geocodedOffice.lat());
            senatorOffice.setLongitude(geocodedOffice.lon());
        }
        else {
            logger.error("SAGE was unable to geocode this office address: {}", officeAddress);
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
}
