package gov.nysenate.sage.scripts;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CleanUpBadData {

    private Config config;
    private QueryRunner tigerRun;
    private static final String table = "cache.geocache";


    public CleanUpBadData() {
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

        CleanUpBadData cleanUpBadData = new CleanUpBadData();

        int offset = 0;
        int limit = 2000;

        String ZIP_COUNT_SQL = "select count(distinct zip5) from " + cleanUpBadData.getTable();

        String ZIP_BATCH_SQL = "select distinct zip5 from " + cleanUpBadData.getTable()
                + " limit ? offset ?;";

        String DELETE_ZIP_SQL = "Delete from " + cleanUpBadData.getTable() + " where zip5 = ?;";

        String STATE_COUNT_SQL = "select count(distinct state) from " + cleanUpBadData.getTable();

        String STATE_BATCH_SQL = "select distinct state from " + cleanUpBadData.getTable()
                + " limit ? offset ?;";

        String DELETE_STATE_SQL = "Delete from " + cleanUpBadData.getTable() + " where state = ?;";

        try {
            //Get total number of addresses that will be used to update our geocache
            int total = cleanUpBadData.getTigerRun().query(ZIP_COUNT_SQL, new ResultSetHandler<Integer>() {
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
                List<String> zip_codes = cleanUpBadData.getTigerRun()
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

                        cleanUpBadData.getTigerRun().update(DELETE_ZIP_SQL, zip);
                    }

                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to clean up bad zips from cache");
        }


//        Now handle the bad states. Good states are two characters and state that are empty for older cached zip codes
        offset = 0;

        try {
            //Get total number of addresses that will be used to update our geocache
            int total = cleanUpBadData.getTigerRun().query(STATE_COUNT_SQL, new ResultSetHandler<Integer>() {
                @Override
                public Integer handle(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getInt("count");
                }
            });


            System.out.println("Geocache state total record count: " + total);

            //start from 0 and loop until the total number in batches of 2000
            while (total > offset) {
                //Get batch of 2000
                List<String> states = cleanUpBadData.getTigerRun()
                        .query(
                                STATE_BATCH_SQL,
                                new ResultSetHandler<List<String>>() {
                                    @Override
                                    public List<String> handle(ResultSet rs) throws SQLException {
                                        ArrayList<String> states = new ArrayList<>();
                                        while (rs.next()) {
                                            states.add( rs.getString("state"));
                                        }
                                        return states;
                                    }
                                },
                                limit,
                                offset);

                System.out.println("At offset: " + offset);
                offset = limit + offset;


                for (String state : states) {
                    //Validation for a proper zip code
                    //if its not valid then delete it
                    boolean valid = false;

                    if (state.isEmpty() || state.matches("([a-zA-Z]){2}") ) {
                        valid = true;
                    }

                    if (!valid) {
                        System.out.println("Removing invalid state: " + state );
                        cleanUpBadData.getTigerRun().update(DELETE_STATE_SQL, state);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to clean up bad states from cache");
        }

        ApplicationFactory.close();
        System.exit(0);
    }
}
