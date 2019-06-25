package gov.nysenate.sage.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.text.WordUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CleanUpBadZips {

    private Config config;
    private QueryRunner tigerRun;
    private static final String table = "cache.geocache";


    public CleanUpBadZips() {
        config = ApplicationFactory.getConfig();
        tigerRun = new QueryRunner(ApplicationFactory.getTigerDataSource());
    }

    public Config getConfig() {
        return config;
    }

    public QueryRunner getTigerRun() {
        return tigerRun;
    }

    public String getTable() {
        return table;
    }

    public static void main(String[] args) {
        /* Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()) {
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        CleanUpBadZips cleanUpBadZips = new CleanUpBadZips();

        int offset = 0;
        int limit = 2000;

        String ZIP_COUNT_SQL = "select count(distinct zip5) from " + cleanUpBadZips.getTable();

        String ZIP_BATCH_SQL = "select distinct zip5 from " + cleanUpBadZips.getTable()
                + " limit ? offset ?;";

        String DELETE_ZIP_SQL = "Delete from " + cleanUpBadZips.getTable() + " where zip5 = ?;";

        BeanListHandler<String> zipBeanListHandler
                = new BeanListHandler<String>(String.class);

        try {
            //Get total number of addresses that will be used to update our geocache
            int total = cleanUpBadZips.getTigerRun().query(ZIP_COUNT_SQL, new ResultSetHandler<Integer>() {
                @Override
                public Integer handle(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getInt("count");
                }
            });


            System.out.println("Geocache zip total record count: " + total);

            //start from 0 and loop until the total number in batches of 2000
            while (total > offset) {
                //Get batch of 2000
                List<String> zip_codes = cleanUpBadZips.getTigerRun()
                        .query(
                                ZIP_BATCH_SQL,
                                new ResultSetHandler<List<String>>() {
                    @Override
                    public List<String> handle(ResultSet rs) throws SQLException {
                        ArrayList<String> zips = new ArrayList<>();
                        while (rs.next()) {
                            zips.add( rs.getString("zip5"));
                        }
                        return zips;
                    }
                },
                                limit,
                                offset);

                System.out.println("At offset: " + offset);
                offset = limit + offset;


                for (String zip : zip_codes) {
                    //Validation for a proper zip code
                    //if its not valid then delete it

                    boolean valid = false;

                    if (zip.matches("[0-9]+") && zip.length() == 5) {
                        valid = true;
                    }

                    if (!valid) {
//                        System.out.println( "Removing invalid zip code: " + zip);

                        cleanUpBadZips.getTigerRun().update(DELETE_ZIP_SQL, zip);
                    }

                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to clean up bad zips from cache");
        }

        ApplicationFactory.close();
        System.exit(0);
    }
}
