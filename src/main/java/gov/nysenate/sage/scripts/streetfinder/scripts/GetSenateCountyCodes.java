package gov.nysenate.sage.scripts.streetfinder.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.scripts.streetfinder.County;
import gov.nysenate.sage.util.Config;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class GetSenateCountyCodes {

    static Config config;
    QueryRunner geoApiRun;
    private static Logger logger = LoggerFactory.getLogger(GetSenateCountyCodes.class);

    public GetSenateCountyCodes() {
        config = ApplicationFactory.getConfig();
        geoApiRun = new QueryRunner(ApplicationFactory.getDataSource());
    }

    public static void main(String[] args) {

        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        GetSenateCountyCodes getSenateCountyCodes = new GetSenateCountyCodes();

        String GET_SENATE_COUNTY_CODES = "select name, id from public.county";

        String directory = "/data/geoapi_data/street_finder/senate_counties.txt";


        try {
            File senateCounties = new File(directory);
            if (senateCounties.exists()) {
                logger.info("Senate county code already exists");
                System.exit(0);
            }

            senateCounties.createNewFile();

            ResultSetHandler<List<County>> h = new BeanListHandler<>(County.class);

            List<County> counties = getSenateCountyCodes.geoApiRun.query(GET_SENATE_COUNTY_CODES, h);

            FileWriter fileWriter = new FileWriter(directory);
            PrintWriter outputWriter = new PrintWriter(fileWriter);

            int count = 0;
            for (County county : counties) {
                count++;
                outputWriter.println(county.toString());
            }

            logger.info("Wrote " + count + " Senate county codes to file");
            fileWriter.close();
            outputWriter.close();


        } catch (SQLException ex) {
            logger.error("Error retrieving town codes from geoapi db", ex);
        } catch (IOException ex) {
            logger.error("Error creating town code file", ex);
        }


    }

}
