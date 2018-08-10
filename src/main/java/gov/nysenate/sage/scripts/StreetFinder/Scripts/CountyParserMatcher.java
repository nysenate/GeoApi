package gov.nysenate.sage.scripts.StreetFinder.Scripts;

import gov.nysenate.sage.scripts.StreetFinder.Parsers.*;

import java.io.IOException;

/**
 * CountyParserMatcher file. Takes a file (.txt, .csv, .xlsx) and calls the correct
 * parser to create a tsv file
 */
public class CountyParserMatcher {
    /**
     * Takes the given file then determines the correct parser to call which creates a tsv file
     *  args[0] = file
     * @param args
     */
    public static void main(String args[]) throws IOException {

        if(args.length > 0) {
            if (args[0].contains("Street_Finder")) {
                //NYC Files
                NTSParser ntsParser = new NYCParser(args[0]);
                ntsParser.parseFile();

            } else if (args[0].contains("Allegany") || args[0].contains("Columbia") || args[0].contains("Saratoga")) {
                NTSParser ntsParser = new SaratogaParser(args[0]);
                ntsParser.parseFile();

            } else if (args[0].contains(".csv")) {

                if (args[0].contains("Erie")) {
                    NTSParser ntsParser = new ErieParser(args[0]);
                    ntsParser.parseFile();

                } else if (args[0].contains("Essex")) {
                    NTSParser ntsParser = new EssexParser(args[0]);
                    ntsParser.parseFile();

                } else if (args[0].contains("Nassau")) {
                    NTSParser ntsParser = new NassauParser(args[0]);
                    ntsParser.parseFile();

                } else if (args[0].contains("Westchester")) {
                    NTSParser ntsParser = new WestchesterParser(args[0]);
                    ntsParser.parseFile();
                }

            } else if (args[0].contains("Suffolk")) {
                NTSParser ntsParser = new SuffolkParser(args[0]);
                ntsParser.parseFile();

            } else if (args[0].contains("Wyoming")) {
                NTSParser ntsParser = new WyomingParser(args[0]);
                ntsParser.parseFile();

            } else if (args[0].contains("Schoharie")) {
                NTSParser ntsParser = new SchoharieParser(args[0]);
                ntsParser.parseFile();

            } else if (args[0].contains("Montgomery")) {
                NTSParser ntsParser = new MontgomeryParser(args[0]);
                ntsParser.parseFile();

            } else {
                //regular file
                NTSParser ntsParser = new NTSParser(args[0]);
                ntsParser.parseFile();
            }
        }
    }
}
