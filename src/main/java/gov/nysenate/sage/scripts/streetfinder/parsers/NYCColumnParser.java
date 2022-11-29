package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Parses a single column of a NYC streetfile, line by line.
 * Some state is needed to keep track of the current street.
 */
public class NYCColumnParser {
    private final String town;
    private String street = "", streetSuffix = "", preDir = "";

    public NYCColumnParser(String town) {
        this.town = town;
    }

    /**
     * Parses data from a single streetfile line.
     * @return an address, if one was parsed from the line.
     */
    public Optional<StreetFinderAddress> parse(String line) {
        String[] split = line.trim().split("\\s+");
        if (split.length <= 1) {
            return Optional.empty();
        }

        // Matches a data field
        var currPattern = "\\d+-?\\d*[A-Z]?";
        var nextPattern = "1/2|1/4|" + currPattern + "";
        if (split[0].matches(currPattern) && split[1].matches(nextPattern)) {
            // The data to make a new StreetFinderAddress has already been parsed.
            var streetFinderAddress = new StreetFinderAddress();
            streetFinderAddress.setStreet(street);
            streetFinderAddress.setStreetSuffix(streetSuffix);
            streetFinderAddress.setPreDirection(preDir);

            if (Column.handleDataPoints(streetFinderAddress, split) && street != null) {
                streetFinderAddress.setTown(town);
                return Optional.of(streetFinderAddress);
            }
        }

        else {
            LinkedList<String> streetParts = new LinkedList<>(List.of(split));
            if (NTSParser.checkForDirection(streetParts.getFirst())) {
                preDir = streetParts.removeFirst();
            }
            streetSuffix = streetParts.removeLast();
            street = String.join(" ", streetParts);
        }
        return Optional.empty();
    }
}
