package gov.nysenate.sage.scripts.streetfinder;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.PostOfficeAddress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Processes the Post Office data from a given file.
 * As of writing, said file can be found online at: <a href="https://postalpro.usps.com/ZIP_Locale_Detail">...</a>
 */
public final class PostOfficeParser {
    private PostOfficeParser() {}

    public static List<PostOfficeAddress> getData(File dataFile) throws IOException {
        var dataList = new ArrayList<PostOfficeAddress>();
        var scanner = new Scanner(dataFile);
        // Skip column labels
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String[] lineData = scanner.nextLine().split("\t");
            if (!lineData[8].equals("NY")) {
                continue;
            }
            var currAddr = new Address(lineData[6], lineData[7], "NY", lineData[9] + "-" + lineData[10]);
            dataList.add(new PostOfficeAddress(lineData[4], currAddr));
        }
        return dataList;
    }
}
