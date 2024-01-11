package gov.nysenate.sage.scripts.streetfinder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.model.address.Address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class PostOfficeParser {
    public static void main(String[] args) throws IOException {
        var data = zipToPostOfficeAddress(Path.of("/home/jacob/Downloads/ZIP_Physical_Addresses.tsv"));
        long multSize = data.asMap().entrySet().stream().filter(entry -> entry.getValue().size() > 1).distinct().count();
        System.out.println(100 * multSize / data.size());
    }

    public static Multimap<Integer, Address> zipToPostOfficeAddress(Path dataFilePath) throws IOException {
        Multimap<Integer, Address> multimap = ArrayListMultimap.create();
        var scanner = new Scanner(dataFilePath);
        // Skip column labels
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String[] lineData = scanner.nextLine().split("\t");
            if (!lineData[8].equals("NY")) {
                continue;
            }
            multimap.put(Integer.parseInt(lineData[4]),
                    new Address(lineData[6], "", lineData[7], "NY", lineData[9], lineData[10]));
        }
        return multimap;
    }
}
