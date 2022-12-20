package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.stream.Stream;

/**
 * Creates an SQL file (if it doesn't already exist) and adds the info from
 * the given tsv file. It also adds SET/DELETE/COPY statements to make the
 * .sql file ready to be imported to a database
 * args[0] = filename.tsv
 */
public class CreateSQLFile {
    private static final String sqlHeading = """
            SET statement_timeout = 0;
            SET lock_timeout = 0;
            SET idle_in_transaction_session_timeout = 0;
            SET idle_in_transaction_session_timeout = 0;
            SET client_encoding = 'UTF8';
            SET standard_conforming_strings = on;
            SET check_function_bodies = false;
            SET client_min_messages = warning;
            SET row_security = off;
            SET search_path = public, master, pg_catalog;
            SET search_path = public, master, pg_catalog;
            DELETE from public.streetfile;
            """;


    /**
     * Creates a SQL with a proper heading, plus all the data to be added.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            return;
        }
        File sqlFile = new File("/data/geoapi_data/street_finder/" + LocalDate.now() + "_streetfile.sql");
        try (var fileWriter = new BufferedWriter(new FileWriter(sqlFile, true));
             var outputWriter = new PrintWriter(fileWriter);
             Stream<String> lines = Files.lines(Path.of(args[0])).skip(1)) {
            if (!sqlFile.exists()) {
                outputWriter.println(sqlHeading);
                outputWriter.println("COPY public.streetfile " +
                        "(street, town, state, zip5, bldg_lo_num, bldg_lo_chr, " +
                        "bldg_hi_num, bldg_hi_chr, bldg_parity, apt_lo_num, apt_lo_chr, " +
                        "apt_hi_num, apt_hi_chr, apt_parity, election_code, county_code, " +
                        "assembly_code, senate_code, congressional_code, boe_town_code, " +
                        "town_code, ward_code, boe_school_code, school_code, cleg_code, " +
                        "cc_code, fire_code, city_code, vill_code) FROM stdin;\n");
            }
            lines.forEach(outputWriter::println);
        }
    }
}
