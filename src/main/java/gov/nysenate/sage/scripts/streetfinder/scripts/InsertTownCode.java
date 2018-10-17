package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InsertTownCode {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing file argument");
            System.exit( -1);
        }

        Map<String,String> townCodeMap = new HashMap<>();

        ArrayList<String> updateFileLines = new ArrayList<>();

//        String testTsv = "/data/geoapi_data/street_finder/tsv_streetfiles/Greene_County_2018.tsv";

        try {
            File file = new File("/data/geoapi_data/street_finder/towns.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int count = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("\t")) {
                    count++;
                    String[] sections = line.split("\t");
                    townCodeMap.put(sections[0].trim(), sections[1].trim());
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

            String line;
            String updatedLine = null;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.contains("state") || line.contains("town")) {
                    updateFileLines.add(line);
                }

                if (line.contains("\t") && (!line.contains("state") || !line.contains("town"))) {
                    String[] sections = line.split("\t");

                    String cityTown = sections[1].toUpperCase();
                    if (cityTown.equals("town")) {
                        continue;
                    }
                    if (townCodeMap.containsKey(cityTown)) {
                        sections[20] = townCodeMap.get(cityTown);

                        if (sections[20] != null) {
                            updatedLine = "";
                            for (int i = 0; i < sections.length; i++) {
                                updatedLine = updatedLine + sections[i] + "\t";
                            }
                            updatedLine = updatedLine.trim();

                        }
                    }

                    if (updatedLine != null) {
                        updateFileLines.add(updatedLine);
                    }
                    else {
                        updateFileLines.add(line);
                    }
                    updatedLine = null;
                }
            }

//            FileWriter fileWriter = new FileWriter(testTsv);
            FileWriter fileWriter = new FileWriter(args[0]);
            PrintWriter outputWriter = new PrintWriter(fileWriter);

            for (String update: updateFileLines) {
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
