package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertSenateCountyCode {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing file argument");
            System.exit(-1);
        }

        Map<String, String> countyCodeMap = new HashMap<>();
        List<String> updateFileLines = new ArrayList<>();
        String countyCode;


        try {
            File file = new File("/data/geoapi_data/street_finder/senate_counties.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("\t")) {
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
                county = switch (splitFileName[2]) {
                    case "Bronx" -> "Bronx";
                    case "Brooklyn" -> "Kings";
                    case "Manhattan" -> "New York";
                    case "Queens" -> "Queens";
                    case "Staten" -> "Richmond";
                    default -> county;
                };

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
                        updatedLine = String.join("\t", List.of(sections)).trim();
                    }


                    if (updatedLine != null) {
                        updateFileLines.add(updatedLine);
                    } else {
                        updateFileLines.add(line);
                    }
                    updatedLine = null;
                }
            }

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
