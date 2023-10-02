package gov.nysenate.sage.scripts.streetfinder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Class to store functions that add data to StreetFileAddresses.
 */
public class StreetFileFunctionList<T extends StreetFileAddressRange> {
    private final List<BiConsumer<T, String>> functions = new ArrayList<>();

    /**
     * We don't use some parts of the data.
     * @param num parts to skip
     */
    public StreetFileFunctionList<T> skip(int num) {
        for (int i = 0; i < num; i++) {
            functions.add((a, b) -> {});
        }
        return this;
    }

    /**
     * Data for the street itself is spread across multiple fields.
     * @param num the number of street part fields here.
     */
    public StreetFileFunctionList<T> addStreetParts(int num) {
        for (int i = 0; i < num; i++) {
            functions.add(StreetFileAddressRange::addToStreet);
        }
        return this;
    }

    /**
     * Useful helper method to add functions from a list of fields.
     * @param dashSplit if these values need to be split on the "-" character.
     * @param fields to be put into the address. Order matters here.
     */
    public StreetFileFunctionList<T> addFunctions(boolean dashSplit, StreetFileField... fields) {
        for (StreetFileField field : fields) {
            functions.add((streetFinderAddress, s) ->
                    streetFinderAddress.put(field, dashSplit ? dashSplit(s) : s));
        }
        return this;
    }

    public StreetFileFunctionList<T> addFunction(BiConsumer<T, String> functionToAdd) {
        functions.add(functionToAdd);
        return this;
    }

    public StreetFileFunctionList<T> addFunctions(List<BiConsumer<T, String>> functionsToAdd) {
        functions.addAll(functionsToAdd);
        return this;
    }

    public void addDataToAddress(T addr, String... dataParts) {
        for (int i = 0; i < Math.min(functions.size(), dataParts.length); i++) {
            functions.get(i).accept(addr, dataParts[i]);
        }
    }

    /**
     * Some data values are preceded by a label (e.g. SE-2) that needs to be skipped.
     * @param input the raw value.
     * @return properly formatted value.
     */
    private static String dashSplit(String input) {
        var split = input.split("-");
        return split.length > 1 ? split[1] : input;
    }
}
