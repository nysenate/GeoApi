package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.scripts.streetfinder.CheckedNewParser;
import gov.nysenate.sage.scripts.streetfinder.parsers.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum CountyStreetfileName {
    Albany, Allegany(SaratogaParser::new), Bronx(NYCParser::new),
    Brooklyn("KINGS", NYCParser::new), Broome, Cattaraugus, Cayuga, Chautauqua, Chemung, Chenango,
    Clinton, Columbia(SaratogaParser::new), Cortland, Delaware, Dutchess, Franklin, Fulton,
    Erie(ErieParser::new), Essex(EssexParser::new), Genesee, Greene, Hamilton, Herkimer, Jefferson,
    Lewis, Livingston, Madison, Manhattan("NEW YORK", NYCParser::new), Monroe,
    Montgomery(MontgomeryParser::new), Nassau(NassauParser::new), Niagara, Oneida, Onondaga,
    Ontario, Orange, Orleans, Oswego, Otsego, Putnam, Queens(NYCParser::new), Rensselaer, Rockland,
    Saratoga(SaratogaParser::new), Schenectady, Schoharie(SchoharieParser::new), Schuyler, Seneca,
    St_Lawrence("ST. LAWRENCE", NTSParser::new), Staten_Island("RICHMOND", NYCParser::new), Steuben,
    Suffolk(SuffolkParser::new), Sullivan, Tioga, Tompkins, Ulster, Warren, Washington, Wayne,
    Westchester(WestchesterParser::new), Wyoming(WyomingParser::new), Yates, VoterFile(VoterFileParser::new);

    private final String nameInCountyData;
    private final CheckedNewParser<?> parserConstructor;
    private final static List<String> enumNames = Arrays.stream(values()).map(Enum::name)
            .collect(Collectors.toList());
    private static final Pattern countyPattern = Pattern.compile(".*(" + String.join("|", enumNames) + ").*");

    CountyStreetfileName() {
        this.nameInCountyData = name().toUpperCase();
        this.parserConstructor = NTSParser::new;
    }

    CountyStreetfileName(CheckedNewParser<?> parserConstructor) {
        this.nameInCountyData = name().toUpperCase();
        this.parserConstructor = parserConstructor;
    }

    CountyStreetfileName(String name, CheckedNewParser<?> parserConstructor) {
        this.nameInCountyData = name;
        this.parserConstructor = parserConstructor;
    }

    public static Pattern getCountyPattern() {
        return countyPattern;
    }

    public BaseParser<?> getParser(File file) throws IOException {
        return parserConstructor.apply(file);
    }

    public String getNameInCountyData() {
        return nameInCountyData;
    }
}
