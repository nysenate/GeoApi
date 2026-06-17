package gov.nysenate.sage.service.data;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Senator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class DataGenService implements SageDataGenService {
    private static final int NUM_SENATE_SEATS = 63;
    private final DistrictMemberProvider memberProvider;

    @Autowired
    public DataGenService(DistrictMemberProvider memberProvider) {
        this.memberProvider = memberProvider;
    }

    // TODO: clarify this, handle it, and update caches properly
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
//            sqlSenateDao.insertOrReplaceSenator(vacantSenator);
        }
    }

    public synchronized void generateMetaData(String option) throws IOException {
        option = option.toLowerCase();
        if (option.matches("s|senate|all")) {
            memberProvider.updateDistrictMembers(DistrictType.SENATE);
        }

        if (option.matches("a|assembly|all")) {
            memberProvider.updateDistrictMembers(DistrictType.ASSEMBLY);
        }

        if (option.matches("c|congress|all")) {
            memberProvider.updateDistrictMembers(DistrictType.CONGRESSIONAL);
        }
    }

    @Scheduled(cron = "${member.refresh.cron:0 0 0/12 * * *}")
    private void autoRefresh() throws IOException {
        generateMetaData("all");
    }
}
