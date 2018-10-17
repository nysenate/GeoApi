package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InsertSenateCountyCode {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing file argument");
            System.exit(-1);
        }

        Map<String, String> countyCodeMap = new HashMap<>();

        ArrayList<String> updateFileLines = new ArrayList<>();

        String countyCode;

//        String testTsv = "/data/geoapi_data/street_finder/tsv_streetfiles/Street_Finder_Bronx_2018_02_21.tsv";

        try {
            File file = new File("/data/geoapi_data/street_finder/senate_counties.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int count = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("\t")) {
                    count++;
                    String[] sections = line.split("\t");
                    countyCodeMap.put(sections[0].trim(), sections[1].trim());
                }
            }
            fileReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
//            File file = new File(testTsv);
            File file = new File(args[0]);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String fileName = file.getName();
            String[] splitFileName = fileName.split("_");
            String county = "\\N";

            if (splitFileName[0].equals("St")) {
                county = "St. Lawrence";
            }
            else if (splitFileName[0].equals("Street")) {
                switch (splitFileName[2]) {
                    case "Bronx":
                        county = "Bronx";
                        break;
                    case "Brooklyn":
                        county = "Kings";
                        break;
                    case "Manhattan":
                        county = "New York";
                        break;
                    case "Queens":
                        county = "Queens";
                        break;
                    case "Staten":
                        county = "Richmond";
                        break;
                }

            }
            else {
                county = splitFileName[0];
            }

            countyCode = countyCodeMap.get(county.toUpperCase());

            String line;
            String updatedLine = null;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.contains("state") || line.contains("town")) {
                    updateFileLines.add(line);
                }
                if (line.contains("\t") && (!line.contains("state") || !line.contains("town"))) {
                    String[] sections = line.split("\t");

                    sections[15] = countyCode;

                    if (!sections[15].equals("\\N")) {
                        updatedLine = "";
                        for (int i = 0; i < sections.length; i++) {
                            updatedLine = updatedLine + sections[i] + "\t";
                        }
                        updatedLine = updatedLine.trim();
                    }


                    if (updatedLine != null) {
                        updateFileLines.add(updatedLine);
                    } else {
                        updateFileLines.add(line);
                    }
                    updatedLine = null;
                }
            }

//            FileWriter fileWriter = new FileWriter(testTsv);
            FileWriter fileWriter = new FileWriter(args[0]);
            PrintWriter outputWriter = new PrintWriter(fileWriter);

            for (String update : updateFileLines) {
                outputWriter.println(update);
            }

            fileReader.close();
            bufferedReader.close();
            fileWriter.close();
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
