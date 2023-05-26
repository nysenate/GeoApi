package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.scripts.streetfinder.parsers.*;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains some information relevant to each county, and the voter file.
 */
public enum StreetFileType {
    Albany, Allegany(SaratogaParser::new), Bronx(NYCParser::new),
    Brooklyn("Kings", NYCParser::new), Broome, Cattaraugus, Cayuga, Chautauqua, Chemung, Chenango,
    Clinton, Columbia(SaratogaParser::new), Cortland, Delaware, Dutchess, Franklin, Fulton,
    Erie(ErieParser::new), Essex(EssexParser::new), Genesee, Greene, Hamilton, Herkimer, Jefferson,
    Lewis, Livingston, Madison, Manhattan("New York", NYCParser::new), Monroe,
    Montgomery(MontgomeryParser::new), Nassau(NassauParser::new), Niagara, Oneida, Onondaga,
    Ontario, Orange, Orleans, Oswego, Otsego, Putnam, Queens(NYCParser::new), Rensselaer, Rockland,
    Saratoga(SaratogaParser::new), Schenectady, Schoharie(SchoharieParser::new), Schuyler, Seneca,
    St_Lawrence("St. Lawrence", NTSParser::new), Staten_Island("Richmond", NYCParser::new),
    Steuben, Suffolk(SuffolkParser::new), Sullivan, Tioga, Tompkins, Ulster, Warren, Washington,
    Wayne, Westchester(WestchesterParser::new), Wyoming(WyomingParser::new), Yates,
    VoterFile(VoterFileParser::new);

    public static final List<StreetFileType> sortedTypes = Arrays.stream(values())
            .sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
    private final static List<String> enumNames = Arrays.stream(values()).map(Enum::name)
            .collect(Collectors.toList());
    private static final Pattern countyPattern = Pattern.compile(".*(" + String.join("|", enumNames) + ").*");
    // The name of the county in our public.county table.
    private final String databaseName;
    private final Function<File, ? extends BaseParser<?>> parserConstructor;

    StreetFileType() {
        this(NTSParser::new);
    }

    StreetFileType(Function<File, ? extends BaseParser<?>> parserConstructor) {
        this.databaseName = name().toUpperCase();
        this.parserConstructor = parserConstructor;
    }

    StreetFileType(String name, Function<File, ? extends BaseParser<?>> parserConstructor) {
        this.databaseName = name.toUpperCase();
        this.parserConstructor = parserConstructor;
    }

    public static Pattern getCountyPattern() {
        return countyPattern;
    }

    public static String getDatabaseNameFromInt(String id) {
        return sortedTypes.get(Integer.parseInt(id)).getDatabaseName();
    }

    public BaseParser<?> getParser(File file) {
        return parserConstructor.apply(file);
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
