package gov.nysenate.sage.scripts.streetfinder;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.PostOfficeAddress;
import org.apache.commons.io.FilenameUtils;

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
        String extension = FilenameUtils.getExtension(dataFile.getName());
        String fileDelim = switch (extension) {
            case "csv" -> ",";
            case "tsv" -> "\t";
            default -> throw new IllegalArgumentException("Cannot process %s files".formatted(extension));
        };
        String delim = "\"?" + fileDelim + "\"?";

        // Different files put the delivery zip at different indices.
        String[] columns = scanner.nextLine().split(delim);
        int zipIndex = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].matches("DELIVERY ZIPCODE|ZIP CODE")) {
                zipIndex = i;
                break;
            }
        }

        while (scanner.hasNextLine()) {
            String[] lineData = scanner.nextLine().split(delim, -1);
            int numParts = lineData.length;
            if (!lineData[numParts - 3].equals("NY")) {
                continue;
            }
            var currAddr = new Address(lineData[numParts - 5], lineData[numParts - 4], "NY",
                    lineData[numParts - 2] + "-" + lineData[numParts - 1]);
            dataList.add(new PostOfficeAddress(lineData[zipIndex], currAddr));
        }
        return dataList;
    }
}
