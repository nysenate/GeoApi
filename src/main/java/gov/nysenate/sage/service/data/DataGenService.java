package gov.nysenate.sage.service.data;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.CongressScraper;
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
    private final SqlSenateDao sqlSenateDao;
    private final MemberDao memberDao;
    private final GeocodeService geocodeService;
    @Value("${nysenate.domain:https://www.nysenate.gov}")
    private String nysenateDomain;

    @Autowired
    public DataGenService(SqlSenateDao sqlSenateDao, MemberDao memberDao, GeocodeService geocodeService) {
        this.sqlSenateDao = sqlSenateDao;
        this.memberDao = memberDao;
        this.geocodeService = geocodeService;
    }

    public Object vacantizeSenateData() {
        boolean updated = false;
        Object apiResponse = new ApiError(this.getClass(), API_REQUEST_INVALID);

        ArrayList<Senator> vacantSenatorsList = new ArrayList<>();
        //handle the empty ones
        for (int i = 0; i < 64; i++) {
            var vacantSenator = new Senator();
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
            updated = updateDistrictMembers(DistrictType.ASSEMBLY, AssemblyScraper.getAssemblies());
        }

        if (processCongress) {
            updated = updateDistrictMembers(DistrictType.CONGRESSIONAL, CongressScraper.getCongressionals());
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

    private static boolean verifyOfficeGeocode(Senator senator) {
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

    private boolean updateDistrictMembers(DistrictType districtType, List<DistrictMember> newMembers) {
        logger.info("Indexing NY {} by scraping its website...", districtType);

        for (DistrictMember newMember : newMembers) {
            int district = newMember.district();
            DistrictMember existingMember = memberDao.getMemberByDistrict(districtType, district);

            if (isMemberUpdated(existingMember, newMember)) {
                memberDao.deleteDistrictMember(districtType, district);
                memberDao.insertDistrictMember(newMember);
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
        logger.info("Generating senate data from NY Senate client services");
        List<Senator> senators = new NYSenateJSONClient(nysenateDomain).getSenators();

        for (Senator senator : senators) {
            int district = senator.getDistrict().getNumber();
            if (district <= 0) {
                continue;
            }
            for (Office office : senator.getOffices()) {
                String street = office.getStreet().replaceAll("(?i)Avesuite", "Ave Suite")
                        .replaceAll("(?i)avenuesuite", "Avenue Suite");
                var officeAddress = new BuildingAddress(street, office.getCity(), office.getPostalCode());
                Point point = setUpdatedGeocode(officeAddress);
                if (point != null && point.isValid()) {
                    office.setLatitude(point.lat().doubleValue());
                    office.setLongitude(point.lon().doubleValue());
                }
            }
            Senator existingSenator = sqlSenateDao.getSenatorByDistrict(district);
            if (existingSenator == null) {
                sqlSenateDao.insertSenate(senator.getDistrict());
            }
            else {
                sqlSenateDao.deleteSenator(district);
            }
            sqlSenateDao.insertSenator(senator);
            updated = true;
        }

        if (updated) {
            updateSenatorCache();
        }
        return updated;
    }

    private boolean isMemberUpdated(DistrictMember existingMember, DistrictMember newMember) {
        if (existingMember == null) {
            return newMember != null;
        }
        if (newMember == null) {
            return false;
        }
        return !existingMember.equals(newMember);
    }

    private Point setUpdatedGeocode(BuildingAddress officeAddress) {
        GeocodeResult result = geocodeService.geocode(null, officeAddress);
        if (result.isSuccess()) {
            Geocode geocodedOffice = result.getGeocode();
            return geocodedOffice.point();
        }
        else {
            logger.error("SAGE was unable to geocode this office address: {}", officeAddress);
            return null;
        }
    }
}
