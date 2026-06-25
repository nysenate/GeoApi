package gov.nysenate.sage.service.data;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DataGenService implements SageDataGenService {
    private final DistrictMemberProvider memberProvider;

    @Autowired
    public DataGenService(DistrictMemberProvider memberProvider) {
        this.memberProvider = memberProvider;
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
