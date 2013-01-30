package gov.nysenate.sage.scripts;

import gov.nysenate.sage.util.DB;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;

public class RebuildTriggers {

    public static void main(String[] args) throws Exception{

        DataSource db = DB.INSTANCE.getDataSource();
        QueryRunner runner = new QueryRunner(db);

        System.out.println("Rebuilding Triggers");
        try {
            rebuildSchoolTownTrigger(runner);
        }
        catch( SQLException sqlException){
            System.err.println("Failed to rebuild triggers with error: " + sqlException.getMessage());
        }
    }

    /**
     * When inserting street data, perform senate school code and senate town code assignments
     * using mapping data in the `street_data_map` table. The values in the `from_code` column
     * have been trimmed prior to insertion.
     *
     * @param runner    QueryRunner with data source set
     */

    public static void rebuildSchoolTownTrigger( QueryRunner runner ) throws SQLException {

       runner.update("DROP TRIGGER IF EXISTS senate_code_check;");
       runner.update(
            "CREATE TRIGGER senate_code_check BEFORE INSERT ON `street_data`\n" +
            "FOR EACH ROW BEGIN \n" +
            "SET NEW.town_code = \n" +
            "(SELECT `to_code` FROM `street_data_map`\n" +
            " WHERE `map_col` = 'town_code' \n" +
            " AND `county_code` = NEW.county_code\n" +
            " AND (`from_code` = TRIM(LEADING '0' FROM NEW.boe_town_code) OR `to_code` = NEW.boe_town_code)\n" +
            ");\n" +
            "SET NEW.school_code = \n" +
            "(SELECT `to_code` FROM `street_data_map`\n" +
            " WHERE `map_col` = 'school_code'\n" +
            " AND `county_code` = NEW.county_code\n" +
            " AND (`from_code` = TRIM(LEADING '0' FROM NEW.boe_school_code) OR `to_code` = TRIM(LEADING '0' FROM NEW.boe_school_code))\n" +
            ");\n" +
            "END;\n");

        System.out.println("Rebuilt senate school and town check trigger.");
    }


}
