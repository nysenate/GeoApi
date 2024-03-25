package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.DistrictIndices;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.BasicLineType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.util.AddressDictionary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses files in the base NTS format.
 */
public class NTSParser extends BaseParser {
    private static final Pattern endOfPagePattern = Pattern.compile("TEAM SQL Version|r_strstd|Total No. of Street|r_ppstreet");
    private static final Predicate<String> isValidLine = line -> line.length() >= 25 && line.trim().split("\\s+").length >= 5;
    protected DistrictIndices indices;
    // These are used to save the variables when the line is a continuation of an above street.
    private StreetfileAddressRange range = new StreetfileAddressRange();
    private String overloadStreetSuf = "";
    private final boolean isGreene;
    private final boolean isSchenectady;

    public NTSParser(File file) {
        super(file);
        this.isGreene = file.getName().contains("Greene");
        this.isSchenectady = file.getName().contains("Schenectady");
    }

    /**
     * Some extraneous data must be removed, and some data stored from the start of the page.
     */
    public void parseFile() throws IOException {
        Stream<String> lines = Files.lines(file.toPath());
        while (true) {
            Queue<String> lineQueue = new LinkedList<>(lines.dropWhile(line -> !line.contains("House Range")).toList());
            if (lineQueue.isEmpty()) {
                break;
            }
            parseStartOfPage(lineQueue.poll());
            var page = new ArrayList<String>();
            while (!lineQueue.isEmpty() && !endOfPagePattern.matcher(lineQueue.peek()).find()) {
                page.add(lineQueue.poll());
            }
            parsePage(page.stream().filter(isValidLine).toList());
            lines = lineQueue.stream().skip(1);
        }
        lines.close();
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // TODO: BOE_TOWN_CODE at index 5
        // TODO: the last 4 fields here vary depending on which county we're in.
        return new StreetfileDataExtractor(NTSParser.class.getSimpleName())
                .addBuildingIndices(1, 2, 3).addBuildingIndices(0)
                .addType(WARD, 6).addTypesInOrder(ELECTION, CONGRESSIONAL, SENATE, ASSEMBLY,
                        SCHOOL, VILLAGE, CLEG, FIRE, CITY_COUNCIL, CITY);
    }

    protected void parseStartOfPage(String startingLine) {
        indices = new DistrictIndices(isGreene, startingLine);
    }

    /**
     * Cannot be replaced with parseLine() because some data takes up multiple lines.
     * @param page to be processed.
     */
    private void parsePage(List<String> page) {
        for (int i = 0; i < page.size(); i++) {
            String currLine = page.get(i);
            Optional<String> nextLine = i == page.size() - 1 ? Optional.empty() : Optional.of(page.get(i + 1));
            // A special case is where the street suffix is bumped to its own line
            if (nextLine.isPresent() && nextLine.get().length() < 5) {
                overloadStreetSuf = nextLine.get();
                i++;
            }
            List<String> cleanLine = cleanLine(currLine);
            if (cleanLine.size() < 3) {
                improperLineMap.put(BasicLineType.ERROR, currLine);
                return;
            }
            parseLine(String.join(delim(), cleanLine));
        }
    }

    /**
     * Makes some data corrections to lines.
     */
    protected List<String> cleanLine(String line) {
        var streetFinderAddress = new StreetfileAddressRange();
        String[] splitLine = line.split("\\s+");

        int zipIndex = 0;
        for (int i = 1; i < splitLine.length; i++) {
            if (splitLine[i].matches("\\d{5}")) {
                zipIndex = i;
                break;
            }
            // Special case with wrong zipcode. TODO: check if still needed.
            if (splitLine[i].equals("1239") && isSchenectady) {
                zipIndex = i;
                splitLine[i] = "12309";
                break;
            }
        }
        String[] beforeZip = Arrays.copyOfRange(splitLine, 0, zipIndex);
        if (!line.contains("E. MAIN ST")) {
            if (line.charAt(0) == ' ') {
                beforeZip = new String[] {range.getStreet()};
            }
            else if (line.charAt(0) == '*') {
                beforeZip = new String[] {};
            }
        }
        List<String> cleanedData = new ArrayList<>(cleanBeforeZip(beforeZip));
        cleanedData.add(splitLine[zipIndex]);
        cleanedData.addAll(cleanAfterZip(Arrays.copyOfRange(splitLine, zipIndex + 1, splitLine.length)));
        cleanedData.addAll(indices.getPostAsmData(line));
        range = streetFinderAddress;
        return cleanedData;
    }

    /**
     * Helper method to clean data before the zipcode.
     * @return cleaned data section.
     */
    private List<String> cleanBeforeZip(String[] beforeZip) {
        // Simply return empty data.
        if (beforeZip.length == 0) {
            return List.of("", "", "");
        }
        var cleanedList = new LinkedList<String>();
        int currIndex = beforeZip.length - 1;
        // Either standardize the direction, or insert an empty one.
        String direction = AddressDictionary.directionMap.get(beforeZip[currIndex]);
        if (direction != null) {
            cleanedList.push(direction);
            currIndex--;
        }
        else {
            cleanedList.push("");
        }

        if (!beforeZip[currIndex].matches("\\d+")) {
            cleanedList.push(beforeZip[currIndex--]);
        }
        else {
            cleanedList.push("");
        }

        String[] streetNameAr = Arrays.copyOfRange(beforeZip,  0, currIndex + 1);
        cleanedList.push(String.join(" ", List.of(streetNameAr)).trim());
        // cleanedList now is [streetName, suffix, postDir]
        if (!overloadStreetSuf.isEmpty()) {
            cleanedList.set(1, cleanedList.get(1) + cleanedList.get(2));
            cleanedList.set(2, overloadStreetSuf);
        }
        return cleanedList;
    }

    /**
     * Helper method to clean data after the zipcode.
     * @return cleaned data section.
     */
    protected static List<String> cleanAfterZip(String[] splitLine) {
        LinkedList<String> cleanedList = new LinkedList<>(List.of(splitLine));
        // low could possibly be something like "1-" or just "1" and the "-" is its own index
        if (cleanedList.getFirst().contains("-")) {
            cleanedList.set(0, cleanedList.getFirst().replaceAll("-", ""));
        }
        else {
            cleanedList.remove(1);
        }
        if (splitLine.length > 3 && cleanedList.get(3).trim().equalsIgnoreCase("Inclusive")) {
            cleanedList.remove(3);
        }

        String townName = "";
        var tempTownName = new StringBuilder();
        // The townName could be multiple words.
        for (int i = 3; i < cleanedList.size(); i++) {
            String curr = cleanedList.get(i);
            tempTownName.append(curr).append(" ");
            // We know the town has stopped when we reach the town code.
            if (curr.matches("\\d{3}|[A-Z]{1,3}") && !curr.matches("LEE|AVA|ANN")
                    && cleanedList.get(i + 1).matches("\\d+")) {
                townName = tempTownName.toString().toUpperCase().replaceAll("(CITY|TOWN) OF", "").trim();
                break;
            }
        }
        cleanedList.set(3, townName);
        // Ignore the extra parts of the town name.
        return cleanedList.subList(0, 4);
    }

    public static String substringHelper(String str, int start, int end) {
        return str.substring(Math.min(start, str.length()), Math.min(end, str.length()));
    }
}
