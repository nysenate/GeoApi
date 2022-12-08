package gov.nysenate.sage.scripts.streetfinder.scripts;

import gov.nysenate.sage.scripts.streetfinder.CheckedNewParser;
import gov.nysenate.sage.scripts.streetfinder.parsers.*;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CountyParserMatcher file. Takes a file (.txt, .csv, .xlsx) and calls the correct
 * parser to create a tsv file
 */
public class CountyParserMatcher {
    private static final Map<String, CheckedNewParser<?>> parserTypeMap = Map.of(
            "Erie", ErieParser::new,
            "Essex", EssexParser::new,
            "Montgomery", MontgomeryParser::new,
            "Nassau", NassauParser::new,
            "Bronx|Brooklyn|Manhattan|Queens|Staten", NYCParser::new,
            "Allegany|Columbia|Saratoga", SaratogaParser::new,
            "Schoharie", SchoharieParser::new,
            "Suffolk", SuffolkParser::new,
            "Westchester", WestchesterParser::new,
            "Wyoming", WyomingParser::new
            );

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Need to pass in filename as argument.");
        }
        BaseParser<?> parser = null;
        for (var entry : parserTypeMap.entrySet()) {
            if (Pattern.compile(entry.getKey()).matcher(args[0]).find()) {
                parser = entry.getValue().apply(args[0]);
                break;
            }
        }
        if (parser == null) {
            parser = new NTSParser(args[0]);
        }
        parser.parseFile();
    }
}
