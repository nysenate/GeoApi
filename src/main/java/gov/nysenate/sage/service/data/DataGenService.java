package gov.nysenate.sage.service.data;

import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.services.NYSenateJSONClient;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataGenService implements SageDataGenService {
    private static final Logger logger = LoggerFactory.getLogger(DataGenService.class);
    private static final int NUM_SENATE_SEATS = 63;
    private static final Address LOB = new Address("198 State St", "Albany", "NY", "12247");
    private final SqlSenateDao sqlSenateDao;
    private final DistrictMemberProvider memberProvider;
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    @Value("${nysenate.domain:https://www.nysenate.gov}")
    private String nysenateDomain;

    @Autowired
    public DataGenService(SqlSenateDao sqlSenateDao, DistrictMemberProvider memberProvider,
                          AddressService addressService, GeocodeService geocodeService) {
        this.sqlSenateDao = sqlSenateDao;
        this.memberProvider = memberProvider;
        this.addressService = addressService;
        this.geocodeService = geocodeService;
    }

    public synchronized void vacantizeSenateData() {
        var vacantSenatorsList = new ArrayList<Senator>();
        //handle the empty ones
        for (int i = 1; i <= NUM_SENATE_SEATS; i++) {
            var vacantSenator = new Senator();
            vacantSenator.setDistrict(new District(i, "https://www.nysenate.gov/district/" + i));
            vacantSenator.setShortName("Vacant");
            vacantSenator.setName("Vacant District " + i);
            vacantSenator.setFirstName("Vacant");
            vacantSenator.setLastName("District");
            vacantSenator.setImageUrl("https://www.nysenate.gov/themes/custom/nysenate_theme/dist/images/nys_logo_header240x240.jpg");
            vacantSenatorsList.add(vacantSenator);
        }

        for (Senator vacantSenator : vacantSenatorsList) {
            sqlSenateDao.insertOrReplaceSenator(vacantSenator);
        }
        memberProvider.recreateCaches();
    }

    public synchronized void generateMetaData(String option) throws IOException {
        if (option.matches("a|assembly|all")) {
            memberProvider.updateDistrictMembers(DistrictType.ASSEMBLY);
        }

        if (option.matches("c|congress|all")) {
            memberProvider.updateDistrictMembers(DistrictType.CONGRESSIONAL);
        }

        if (option.matches("s|senate|all")) {
            generateSenateData();
        }
        memberProvider.recreateCaches();
    }

    @Scheduled(cron = "${senator.refresh.cron:0 0 0/12 * * *}")
    private void autoRefresh() throws IOException {
        generateMetaData("all");
    }

    /**
     * Retrieves senate data from the NY Senate API Client and stores it in the database.
     */
    private void generateSenateData() throws IOException {
        boolean updated = false;
        logger.info("Generating senate data from NY Senate client services");
        List<Senator> senators = new NYSenateJSONClient(nysenateDomain).getSenators();

        for (Senator senator : senators) {
            int district = senator.getDistrict().getNumber();
            if (district <= 0) {
                continue;
            }
            for (Office office : senator.getOffices()) {
                String addr1 = office.getStreet().replaceAll("(?i)Avesuite", "Ave Suite")
                        .replaceAll("(?i)avenuesuite", "Avenue Suite");
                String[] zips = office.getPostalCode().split("-");
                Address address;
                if (addr1.matches("(\\d+ )?Legislative Office (Bldg|Building).*")) {
                    address = LOB;
                }
                else {
                    address = new Address(addr1, "", office.getCity(), office.getProvince(),
                            zips[0], zips.length > 1 ? zips[1] : null);
                }
                // Offices within the Capitol are corrected poorly by AMS, since it's a unique zipcode.
                if (!"12247".equals(zips[0])) {
                    address = addressService.validateOrDefault(address);
                }

                Point point = getPoint(address);
                if (point != null && point.isValid()) {
                    office.setLatitude(point.lat().doubleValue());
                    office.setLongitude(point.lon().doubleValue());
                }
            }
            sqlSenateDao.insertOrReplaceSenator(senator);
            updated = true;
        }

        if (!updated) {
            throw new RuntimeException("No Senators found!");
        }
    }

    private Point getPoint(Address officeAddress) {
        GeocodeResult result = geocodeService.geocode(null, officeAddress);
        if (result.isSuccess()) {
            Geocode geocodedOffice = result.getGeocode();
            return geocodedOffice.point();
        }
        else {
            logger.error("Unable to geocode this office address: {}", officeAddress);
            return null;
        }
    }
}
