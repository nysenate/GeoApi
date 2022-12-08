package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.DistrictIndices;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.util.AddressDictionary;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses files in the base NTS format (see Albany and Broome Counties as an example)
 */
public class NTSParser extends BasicParser {
    private static final Pattern endOfPagePattern = Pattern.compile("TEAM SQL Version|r_strstd|Total No. of Street|r_ppstreet");
    private static final Predicate<String> isNotEndOfPage = line -> !endOfPagePattern.matcher(line).find();
    private static final Predicate<String> isValidLine = line -> line.length() >= 25 && line.trim().split("\\s+").length >= 5;
    protected DistrictIndices indices;
    //These are used to save the variables when the line is a continuation of an above street
    private StreetFileAddress streetFileAddressStorage = new StreetFileAddress();
    private String overloadStreetSuf = "";
    private final boolean isGreene;

    public NTSParser(String file) throws IOException {
        super(file);
        this.isGreene = file.contains("Greene");
    }

    /**
     * Some extraneous data must be removed, and some data stored from the start of the page.
     * @throws IOException
     */
    public void parseFile() throws IOException {
        Scanner scanner = new Scanner(new File(filename));
        Stream<String> lines =  Files.lines(Path.of(filename));
        while (lines.findAny().isPresent()) {
            lines = lines.dropWhile(line -> !line.contains("House Range"));
            parseStartOfPage(lines.findFirst().get());
            lines = lines.skip(1);
            parsePage(lines.takeWhile(isNotEndOfPage).filter(isValidLine).collect(Collectors.toList()));
            lines = lines.dropWhile(isNotEndOfPage).skip(1);
        }
        scanner.close();
        closeWriters();
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
            parseLine(String.join(",", cleanLine(currLine)), 3);
        }
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> funcList = new ArrayList<>();
        funcList.add(StreetFileAddress::setStreet);
        funcList.add(StreetFileAddress::setStreetSuffix);
        funcList.add(StreetFileAddress::setPostDirection);
        funcList.add(function(ZIP));
        funcList.addAll(buildingFunctions);
        funcList.addAll(functions(TOWN, BOE_TOWN_CODE, WARD, ELECTION_CODE, CONGRESSIONAL, SENATE,
                ASSEMBLY, SCHOOL, VILLAGE, CLEG, FIRE, CITY_COUNCIL, CITY));
        return funcList;
    }

    protected List<String> cleanLine(String line) {
        var streetFinderAddress = new StreetFileAddress();
        String[] splitLine = line.split("\\s+");

        int zipIndex = 0;
        for (int i = 1; i < splitLine.length; i++) {
            if (splitLine[i].matches("\\d{5}")) {
                zipIndex = i;
                break;
            }
            // Special case with wrong zipcode. TODO: check if still needed.
            if (splitLine[i].equals("1239") && filename.contains("Schenectady")) {
                zipIndex = i;
                splitLine[i] = "12309";
                break;
            }
        }
        String[] beforeZip = Arrays.copyOfRange(splitLine, 0, zipIndex);
        if (line.charAt(0) == ' ' && !line.contains("E. MAIN ST")) {
            beforeZip = new String[]{streetFileAddressStorage.getStreet(), streetFileAddressStorage.getStreetSuffix()};
        }
        else if (line.charAt(0) == '*' && !line.contains("E. MAIN ST")) {
            beforeZip = new String[]{};
        }
        List<String> cleanedData = new ArrayList<>(cleanBeforeZip(beforeZip));
        cleanedData.add(splitLine[zipIndex]);
        cleanedData.addAll(cleanAfterZip(Arrays.copyOfRange(splitLine, zipIndex + 1, splitLine.length)));
        cleanedData.addAll(indices.getPostAsmData(line));
        streetFileAddressStorage = streetFinderAddress;
        return cleanedData;
    }

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
        // TODO: should be used in SaratogaParser too
        if (!overloadStreetSuf.isEmpty()) {
            cleanedList.set(1, cleanedList.get(1) + cleanedList.get(2));
            cleanedList.set(2, overloadStreetSuf);
        }
        return cleanedList;
    }

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

    protected static String substringHelper(String str, int start, int end) {
        return str.substring(Math.min(start, str.length()), Math.min(end, str.length()));
    }
}
