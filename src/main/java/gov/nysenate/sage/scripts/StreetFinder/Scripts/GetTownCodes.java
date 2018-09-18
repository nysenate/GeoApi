package gov.nysenate.sage.scripts.StreetFinder.Scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.scripts.StreetFinder.TownCode;
import gov.nysenate.sage.util.Config;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class GetTownCodes {

    static Config config;
    QueryRunner geoApiRun;
    private static Logger logger = Logger.getLogger(GetTownCodes.class);

    public GetTownCodes() {
        config = ApplicationFactory.getConfig();
        geoApiRun = new QueryRunner(ApplicationFactory.getDataSource());
    }

    public static void main(String[] args) {

        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        GetTownCodes getTownCodes = new GetTownCodes();

        String GET_TOWN_CODES_SQL = "select name, abbrev from districts.town;";

        String directory = "/data/geoapi_data/street_finder/towns.txt";


        try {
            File towns = new File(directory);
            if (towns.exists()) {
                logger.info("File already exists");
                System.exit(0);
            }

            towns.createNewFile();

            ResultSetHandler<List<TownCode>> h = new BeanListHandler<>(TownCode.class);

            List<TownCode> townCodes = getTownCodes.geoApiRun.query(GET_TOWN_CODES_SQL, h);

            FileWriter fileWriter = new FileWriter(directory);
            PrintWriter outputWriter = new PrintWriter(fileWriter);

            for (TownCode townCode : townCodes) {
                outputWriter.print(townCode.toString());
            }

            logger.info("Wrote town codes to file");


        } catch (SQLException ex) {
            logger.error("Error retrieving town codes from geoapi db", ex);
        } catch (IOException ex) {
            logger.error("Error creating town code file", ex);
        }



    }

}
