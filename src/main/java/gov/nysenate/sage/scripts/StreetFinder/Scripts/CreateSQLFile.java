package gov.nysenate.sage.scripts.StreetFinder.Scripts;

import java.io.*;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Creates an SQL file (if it doesnt already exist) and adds the info from
 * the given tsv file. It also adds SET/DELETE/COPY statements to make the
 * .sql file ready to be imported to a database
 * args[0] = filename.tsv
 */
public class CreateSQLFile {

    /**
     * Creates sql file with all commands (if the file does not already exist), then appends new data from input file into file
     * args[0] = file
     * @param
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {

        BufferedWriter fileWriter;
        PrintWriter outputWriter;

        //check that a file was given as a commandline argument
        if(args.length > 0) {
            //create sql file with the current date in the title
            LocalDate date = LocalDate.now();
            File sqlFile = new File("/data/geoapi_data/street_finder/" + date.toString()+ "_streetfile.sql");
            //check if the sql file already exists
            if(!sqlFile.exists()) {
                //file does not exist so add all necessary statements
                fileWriter = new BufferedWriter(new FileWriter(sqlFile, true));
                outputWriter = new PrintWriter(fileWriter);

                outputWriter.print("SET statement_timeout = 0;\n");
                outputWriter.print("SET lock_timeout = 0;\n");
                outputWriter.print("SET idle_in_transaction_session_timeout = 0;\n");
                outputWriter.print("SET client_encoding = 'UTF8';\n");
                outputWriter.print("SET standard_conforming_strings = on;\n");
                outputWriter.print("SET check_function_bodies = false;\n");
                outputWriter.print("SET client_min_messages = warning;\n");
                outputWriter.print("SET row_security = off;\n");
                outputWriter.print("SET search_path = public, master, pg_catalog;\n");
                outputWriter.print("DELETE from public.streetfile;\n");
                outputWriter.print("COPY public.streetfile (street , town, state, zip5, bldg_lo_num, bldg_lo_chr, bldg_hi_num, bldg_hi_chr, bldg_parity, apt_lo_num, apt_lo_chr," +
                                "apt_hi_num, apt_hi_chr, apt_parity, election_code, county_code, assembly_code, senate_code, congressional_code, boe_town_code, " +
                        "town_code, ward_code, boe_school_code, school_code, cleg_code, cc_code, fire_code, city_code, vill_code) FROM stdin;\n");
            } else {
                //file exists so just create writers
                fileWriter = new BufferedWriter(new FileWriter(sqlFile, true));
                outputWriter = new PrintWriter(fileWriter);
            }

            Scanner scanner = new Scanner(new File(args[0]));
            //get all data from the tsv file and append to sql file
            while(scanner.hasNext()) {
                String line = scanner.nextLine();
                if(line.contains("bldg_lo_num") && line.contains("senate_code")) {
                    //skip over these lines because it is just a header and not data
                } else {
                    outputWriter.write(line + "\n");
                }
            }
            scanner.close();
            outputWriter.close();
            fileWriter.close();
        }
    }
}
