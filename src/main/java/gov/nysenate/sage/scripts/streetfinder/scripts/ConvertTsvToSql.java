package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

/**
 * Creates an SQL file (if it doesn't already exist) and adds the info from
 * the given tsv file. It also adds SET/DELETE/COPY statements to make the
 * .sql file ready to be imported to a database
 * args[0] = filename.tsv
 */
public class ConvertTsvToSql {
    private static final String sqlHeading = """
            SET statement_timeout = 0;
            SET lock_timeout = 0;
            SET idle_in_transaction_session_timeout = 0;
            SET client_encoding = 'UTF8';
            SET standard_conforming_strings = on;
            SET check_function_bodies = false;
            SET client_min_messages = warning;
            SET row_security = off;
            SET search_path = public, master, pg_catalog;
            """;
    // TODO: is repeated elsewhere
    private static final List<String> fieldNames = List.of("street", "town", "state", "zip5",
            "bldg_lo_num", "bldg_lo_chr", "bldg_hi_num", "bldg_hi_chr", "bldg_parity",
            "election_code", "county_code", "assembly_code", "senate_code", "congressional_code",
            "boe_town_code", "town_code", "ward_code", "boe_school_code", "school_code",
            "cleg_code", "cc_code", "fire_code", "city_code", "vill_code");

    /**
     * Creates a SQL with a proper heading, plus all the data to be added.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            return;
        }
        // TODO: generalize to make multiple files for multiple databases
        File sqlFile = new File("/data/geoapi_data/street_finder/" + LocalDate.now() + "_streetfile.sql");
        File voterSqlFile = new File("/data/geoapi_data/street_finder/" + LocalDate.now() + "_voter_streetfile.sql");
        sqlFile.delete();
        voterSqlFile.delete();
        try (var outputWriter = new PrintWriter(new BufferedWriter(new FileWriter(sqlFile, true)));
             var voterOutputWriter = new PrintWriter(new BufferedWriter(new FileWriter(voterSqlFile, true)))) {
            outputWriter.println(sqlHeading);
            outputWriter.println("DELETE from public.nyc_streetfile;");
            outputWriter.println("COPY public.nyc_streetfile (" + String.join(", ", fieldNames) + ") FROM stdin;");
            voterOutputWriter.println(sqlHeading);
            voterOutputWriter.println("DELETE from public.voter_streetfile;");
            voterOutputWriter.println("COPY public.voter_streetfile (" + String.join(", ", fieldNames) + ") FROM stdin;");
            for (File dataFile : new File(args[0]).listFiles()) {
                var currWriter = dataFile.getName().toLowerCase().contains("voter") ? voterOutputWriter : outputWriter;
                Files.lines(dataFile.toPath()).skip(1).forEach(line -> currWriter.println(line.toUpperCase()));
                currWriter.flush();
            }
        }
    }
}
