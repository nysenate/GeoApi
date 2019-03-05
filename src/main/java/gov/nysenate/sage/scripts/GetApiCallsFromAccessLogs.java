package gov.nysenate.sage.scripts;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetApiCallsFromAccessLogs {

    public static void main(String[] args) {
        String outputFileName = "single_api_paths.csv";

        // Access logs should be in /data/geoapi_data
        // Access logs are named like: sage_access.2019-02-25.log
        final Pattern accessLogPattern =
                Pattern.compile("^sage_access\\.\\d{4}-\\d{2}-\\d{2}\\.log$");

        List<File> trueFilesInData_Geoapi_data = new ArrayList<>();


        File[] files = new File("/data/geoapi_data").listFiles();

        if (files == null) {
            System.err.println("Error: /data/geoapi_data is missing. The access logs go inside this folder");
            System.exit(1);
        }

        for (File file : files) {
            if (file.isFile()) {
                trueFilesInData_Geoapi_data.add(file);
            }
        }
        //create file that while have all of the api calls
        File pathsCSV = new File("/data/geoapi_data/" + outputFileName);

        // If 1+ then append the paths of all of them into a new file in the output directory /data/geoapi_data
        for (File file: trueFilesInData_Geoapi_data) {
            Matcher accessLogMatcher = accessLogPattern.matcher(file.getName());

            //If the file is an access log file
            if (accessLogMatcher.matches()) {
                try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                    PrintWriter writer = new PrintWriter(pathsCSV, "UTF-8");
                    writer.println("\"path\"");
                    for(String line; (line = br.readLine()) != null; ) {
                        // process the line.
                        String[] splitLine = line.split(" ");
                        line = "";
                        //splitline[6] is the actual api call we need to examine
                        if (splitLine[6].contains("api/v2")) {
                            writer.println("\"" + splitLine[6] + "\"");
                        }
                    }
                    br.close();
                    writer.close();
                }
                catch (IOException e) {
                    System.err.println("Could not read file " + e);
                    System.exit(1);
                }
            }
        }
        // Exit Successful
        System.exit(0);
    }
}