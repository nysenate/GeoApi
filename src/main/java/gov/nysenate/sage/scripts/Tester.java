package gov.nysenate.sage.scripts;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class Tester {

    /**
     * @param args
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(Tester.class);
        GeoService geocoder = new GeoService();
        String adapter = args[0];
        logger.info("Running on: "+adapter);

        // Setup the destination Folder
        File srcDir = new File(args[1]);
        File destDir = new File(new File(args[2]),adapter);
        FileUtils.forceMkdir(destDir);
        logger.info("Writing to: "+destDir.getPath());

        FileWriter timesheet = new FileWriter(new File(destDir,"timesheet.txt"), true);

        // Handle each file in turn
        Collection<File> files = FileUtils.listFiles(srcDir, null, true);
        for (File in  : files) {
            // Check to verify that we actually need this file
            File destFile = new File(destDir, in.getName());
            if (destFile.exists()) {
                logger.info("Skipping file: "+in.getName());
                continue;
            } else {
                logger.info("Processing file: "+in.getName());
            }

            long start = System.nanoTime();

            // Build the address and record lists from file
            String line;
            ArrayList<Address> addresses = new ArrayList<Address>();
            ArrayList<ArrayList<String>> records = new ArrayList<ArrayList<String>>();
            BufferedReader input = new BufferedReader(new FileReader(in));
            while( (line = input.readLine()) != null ) {
                String[] parts = line.split("\\t");
                addresses.add(new Address(parts[1],parts[2],"NY",parts[3]));
                records.add(new ArrayList<String>(Arrays.asList(parts)));
            }

            // Geocode the addresses using the targeted service
            ArrayList<Result> results = geocoder.geocode(addresses, adapter, Address.TYPE.PARSED);

            // Combine the lat, long with the existing records and write
            FileWriter out = new FileWriter(destFile);
            for (int i=0; i < records.size(); i++) {
                ArrayList<String> record = records.get(i);
                Result result = results.get(i);

                record.add(""); // Just a spacer
                if (result.status_code.equals("0") && result.addresses.size() > 0) {
                    Address address = result.addresses.get(0);
                    record.add(String.valueOf(result.addresses.size()));
                    record.add(address.as_raw());
                    record.add(String.valueOf(address.geocode_quality));
                    record.add(String.valueOf(address.latitude));
                    record.add(String.valueOf(address.longitude));

                } else {
                    record.add("0");
                    record.add("NULL");
                    record.add("NULL");
                    record.add("NULL");
                    record.add("NULL");
                }


                for (String field : record) {
                    out.write(field+"\t");
                }
                out.write("\n");
            }
            out.close();

            timesheet.write(in.getName()+"\t"+(System.nanoTime()-start)+"\n");
            timesheet.flush();
        }
        timesheet.close();
    }

}
