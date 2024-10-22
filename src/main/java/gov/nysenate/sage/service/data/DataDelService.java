package gov.nysenate.sage.service.data;

import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.dao.data.SqlDataDelDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

@Service
public class DataDelService implements SageDataDelService {

    private static final Logger logger = LoggerFactory.getLogger(DataDelService.class);
    private final SqlDataDelDao sqlDataDelDao;

    @Autowired
    public DataDelService(SqlDataDelDao sqlDataDelDao) {
        this.sqlDataDelDao = sqlDataDelDao;
    }

    public Object cleanUpBadZips(Integer offset) {
        int limit = 2000;

        int total = sqlDataDelDao.getGeocacheDistinctZipCodesCount();

        logger.info("Geocache zip total record count: {}", total);

        //start from 0 and loop until the total number in batches of 2000
        while (total > offset) {
            //Get batch of 2000
            List<String> zip_codes = sqlDataDelDao.getGeocacheZipBatch(limit, offset);

            logger.info("At offset: {}", offset);
            offset = limit + offset;


            for (String zip : zip_codes) {
                //Validation for a proper zip code
                //if its not valid then delete it

                if (zip.matches("[0-9]+") && zip.length() == 5) {
                    continue;
                }
                else {
                    sqlDataDelDao.deleteZipInGeocache(zip);
                }
            }
        }

        logger.info("Geocache Zip clean Up completed");

        return new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
    }

    public Object cleanUpBadStates() {
        int offset = 0;
        int limit = 2000;

        //Get total number of addresses that will be used to update our geocache
        int total = sqlDataDelDao.getGeocacheDistinctStatesCount();

        logger.info("Geocache state total record count: " + total);

        //start from 0 and loop until the total number in batches of 2000
        while (total > offset) {
            //Get batch of 2000
            List<String> states = sqlDataDelDao.getGeocacheStateBatch(limit, offset);

            System.out.println("At offset: " + offset);
            offset = limit + offset;


            for (String state : states) {
                //Validation for a proper zip code
                //if its not valid then delete it

                if (state.isEmpty() || state.matches("([a-zA-Z]){2}") ) {
                    continue;
                }
                else {
//                    logger.info("Removing invalid state: " + state );
                    sqlDataDelDao.deleteStateInGeocache(state);
                }
            }
        }

        logger.info("Geocache State clean Up completed");

        return new GenericResponse(true,  SUCCESS.getCode() + ": " + SUCCESS.getDesc());
    }
}
