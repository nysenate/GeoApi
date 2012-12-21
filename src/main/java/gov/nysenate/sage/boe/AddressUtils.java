package gov.nysenate.sage.boe;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressUtils {
    public static boolean DEBUG = false;
    public static HashMap<String,String> suffixMap = null;
    public static HashMap<String,String> ordinals = null;
    public static HashMap<String,String> commonAbbreviations = null;

    public static Pattern addrPattern = null;

    public static BOEStreetAddress parseAddress(String address) {
        load_constants();
        Matcher m = addrPattern.matcher(address.toUpperCase());
        if (m.find()) {
            if (DEBUG) {
                for (int i=0; i<m.groupCount(); i++) {
                    System.out.println("Group "+i+": "+m.group(i));
                }
            }
            BOEStreetAddress ret = new BOEStreetAddress();
            ret.bldg_num = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
            ret.bldg_chr = m.group(3) != null ? m.group(3) : "";
            ret.street = m.group(4) != null ? m.group(4) : "";

            if( m.group(5) != null || m.group(6) != null) {
                ret.apt_num = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
                ret.apt_chr = m.group(6) != null ? m.group(6) : "";
            } else {
                ret.apt_chr = m.group(7) != null ? m.group(7) : "";
                ret.apt_num = m.group(8) != null ? Integer.parseInt(m.group(8)) : 0;
            }

            ret.town = m.group(9) != null ? m.group(9) : "";
            ret.state = m.group(10) != null ? m.group(10) : "";
            ret.zip5 = m.group(11) != null ? Integer.parseInt(m.group(11)) : 0;
            return (BOEStreetAddress)normalizeAddress(ret);

        } else {
            System.out.println("Could not match: "+address);
            return null;
        }
    }

    public static BOEAddress normalizeAddress(BOEAddress address) {
        load_constants();
        if (address.town != null && !address.town.equals("")) {
            address.town = address.town.toUpperCase().trim();
            // Fix up the towns
            if (address.town == "CITY/KNG") {
                address.town = "KINGSTON";
            } else if (address.town == "PORTJERVIS/CITY") {
                address.town = "PORT JERVIS";
            } else {
                address.town = address.town.replaceFirst("^(TOWN |TOWN OF |CITY |CITY OF |)", "");
                address.town = address.town.replaceFirst("(\\(CITY\\)|/CITY)$","");
            }
        }

        if (address.street != null) {
            address.street = address.street.toUpperCase().trim();
            if (!address.street.equals("")) {
                String[] parts = address.street.split(" ");

                // Replace directional suffix if present
                String possibleSuffix = parts[parts.length-1];
                if (parts.length > 2) {
                    if (ordinals.containsKey(parts[0])) {
                        address.street = address.street.replaceFirst("^"+parts[0], ordinals.get(parts[0]));
                    }
                }
                if (parts.length > 2) {
                    if (ordinals.containsKey(possibleSuffix)) {
                        address.street = address.street.replaceFirst(possibleSuffix+"$", ordinals.get(possibleSuffix));
                        possibleSuffix = parts[parts.length-2];
                    } else if (possibleSuffix.matches("[NEWS]|[0-9]+[A-Z]?|EXT")) {
                        // If it is an ordinal, number, or EXT, suffix is before that
                        possibleSuffix = parts[parts.length-2];
                    }
                }

                // Replace street suffix if present
                if (parts.length > 1 && suffixMap.containsKey(possibleSuffix)) {
                    address.street = address.street.replaceFirst(possibleSuffix+"( [NSEW]| [0-9]+[A-Z]?| EXT)?$",suffixMap.get(possibleSuffix)+"$1");
                }

                // Remove all numerical suffixes and special characters.
                address.street = address.street.replaceFirst("(?<=[0-9])(?:ST|ND|RD|TH)", "");
                address.street = address.street.replaceAll("[#:;.,]", "").replaceAll("'", "").replaceAll(" +", " ").replaceAll("-", " ");

                // Apply all the common street abbreviations.
                address.street = " "+address.street+" "; // Make regex easier
                for (Entry<String, String> entry : commonAbbreviations.entrySet()) {
                    address.street = address.street.replace(" "+entry.getKey()+" ", " "+entry.getValue()+" ");
                }
                address.street = address.street.trim();
//                // This was a little too dangerous/aggressive I think
//                for (Entry<String, String> entry : suffixMap.entrySet()) {
//                    address.street = address.street.replace(" "+entry.getKey()+" ", " "+entry.getValue()+" ");
//                }
//                address.street = address.street.replaceAll(" ", "");
            }
        }

        /* Don't know if we need these
        SET address = REPLACE( address, 'apt', '');
        SET address = REPLACE( address, 'floor', 'fl');
        */

        return address;
    }

    public static BOEAddressRange consolidateRanges(List<BOEAddressRange> ranges) {
        if (ranges.size() == 0) return null;

        BOEAddressRange base = ranges.get(0);
        int sd = base.senateCode;
        int ed = base.electionCode;
        int cong = base.congressionalCode;
        int ad = base.assemblyCode;
        int county = base.countyCode;
        String school = base.schoolCode;
        int ward = base.wardCode;
        String townCode = base.townCode;
        int ccCode = base.ccCode;
        int cleg = base.clegCode;

        for (int i=1; i < ranges.size(); i++) {
            BOEAddressRange range = ranges.get(i);
            // State wide
            if (range.senateCode != sd) { sd = 0; }
            if (range.congressionalCode != cong) { cong = 0; }
            if (range.assemblyCode != ad) { ad = 0; }
            if (range.countyCode != county) { county = 0; }

            // County Specific
            if (range.schoolCode==null || !range.schoolCode.equals(school) || range.countyCode!=base.countyCode) { school = ""; }
            if (range.townCode == null || !range.townCode.equals(townCode) || range.countyCode!=base.countyCode) { townCode = ""; }
            if (range.clegCode != cleg || range.countyCode!=base.countyCode) { cleg = 0; }

            // Town specific
            if (range.wardCode != ward || range.countyCode!=base.countyCode || !range.town.equals(base.town)) { ward = 0; }
            if (range.ccCode != ccCode || range.countyCode!=base.countyCode || !range.town.equals(base.town)) { ccCode = 0; }

            // Ward Specific (maybe)
            if (range.electionCode != ed || range.countyCode!=base.countyCode || !range.town.equals(base.town) || range.wardCode == base.wardCode) { ed = 0; }
        }

        if (sd != 0) {
            BOEAddressRange range = new BOEAddressRange();
            range.senateCode = sd;
            range.assemblyCode = ad;
            range.electionCode = ed;
            range.congressionalCode = cong;
            range.schoolCode = school;
            range.wardCode = ward;
            range.clegCode = cleg;
            range.ccCode = ccCode;
            range.townCode = townCode;
            range.countyCode = county;
            range.state = base.state;
            range.street = base.street;
            range.zip5 = base.zip5;
            return range;

        } else {
            return null;
        }

    }

    public static void load_constants() {
        if (addrPattern!=null && suffixMap != null)
            return;

        String zip = "([0-9]{5})(?:-([0-9]{4}))?";
        String sep = "(?:[ ]+)";
        String state = "([A-Z]{2})";
        String city = "([A-Z. '-]+?)";
        String street = "((?: ?[-.'A-Z0-9]){2,}?)";
        String building = "(?:([0-9]+)([A-Z]|-[0-9]+| 1/2)?)";
//        String building = "(?:([0-9]+)(?:[ -]*([A-Z]|[0-9]+|1/2)?))";
     // String apt_number = "([0-9]+)?(?:[ -]?([A-Z]))?"; // Old
        String apt_number = "(?:(?:(?:([0-9]+?)(?:ND|ST|RD|TH)?(?:[ -]*([A-Z0-9]+))?)|(?:([A-Z]+)(?:[ -]*([0-9]+))?)|BSMT|BSMNT|PH|PENTHOUSE)(?:FL)?)";
        String apartment = "(?:(?:#|APT|STE|UNIT|BLDG|LOWR|UPPR|LOT|BOX|LEFT|RIGHT|TRLR|RM)[. ]*(?:#|FL)?)"+apt_number+"?";

        // String addressee = "([^,]+)";
        addrPattern = Pattern.compile("()(?:"+building+sep+")?"+street+"(?:"+sep+apartment+")?"+"(?:[ ,]+"+city+")?"+"(?:"+sep+state+")?"+"(?:"+sep+zip+")?$");

        ordinals = new HashMap<String,String>();
        ordinals.put("NORTH", "N");
        ordinals.put("SOUTH", "S");
        ordinals.put("EAST", "E");
        ordinals.put("WEST", "W");

        commonAbbreviations = new HashMap<String, String>();
        commonAbbreviations.put("FIRST", "1");
        commonAbbreviations.put("SECOND", "2");
        commonAbbreviations.put("THIRD", "3");
        commonAbbreviations.put("FOURTH", "4");
        commonAbbreviations.put("FIFTH", "5");
//        commonAbbreviations.put("FIVE", "5");
        commonAbbreviations.put("SIXTH", "6");
        commonAbbreviations.put("SEVENTH", "7");
        commonAbbreviations.put("EIGHTH", "8");
        commonAbbreviations.put("NINETH", "9");
        commonAbbreviations.put("TENTH", "10");
        commonAbbreviations.put("ELEVENTH", "11");
        commonAbbreviations.put("TWELVETH", "12");
        commonAbbreviations.put("THIRTEENTH", "13");
        commonAbbreviations.put("FOURTEENTH", "14");
        commonAbbreviations.put("FIFTEENTH", "15");
        commonAbbreviations.put("SIXTEENTH", "16");
        commonAbbreviations.put("SEVENTEENTH", "17");

        // See http://www.semaphorecorp.com/cgi/abbrev.html
        commonAbbreviations.put("SAINT", "ST");
        commonAbbreviations.put("MOUNT", "MT");
        commonAbbreviations.put("MOUNTAIN", "MTN");
        commonAbbreviations.put("ROUTE", "RTE");
        commonAbbreviations.put("RT", "RTE");
        commonAbbreviations.put("HEIGHTS", "HGTS");
        commonAbbreviations.put("NO", "N");
        commonAbbreviations.put("SO", "N");




        suffixMap = new HashMap<String,String>();
        suffixMap.put("ALLEE","ALY");
        suffixMap.put("ALLEY","ALY");
        suffixMap.put("ALLY","ALY");
        suffixMap.put("ALY","ALY");
        suffixMap.put("ANEX","ANX");
        suffixMap.put("ANNEX","ANX");
        suffixMap.put("ANNEX","ANX");
        suffixMap.put("ANX","ANX");
        suffixMap.put("ARC","ARC");
        suffixMap.put("ARCADE","ARC");
        suffixMap.put("AV","AVE");
        suffixMap.put("AVE","AVE");
        suffixMap.put("AVEN","AVE");
        suffixMap.put("AVENU","AVE");
        suffixMap.put("AVENUE","AVE");
        suffixMap.put("AVN","AVE");
        suffixMap.put("AVNUE","AVE");
        suffixMap.put("BAYOO","BYU");
        suffixMap.put("BAYOU","BYU");
        suffixMap.put("BCH","BCH");
        suffixMap.put("BEACH","BCH");
        suffixMap.put("BEND","BND");
        suffixMap.put("BND","BND");
        suffixMap.put("BLF","BLF");
        suffixMap.put("BLUF","BLF");
        suffixMap.put("BLUFF","BLF");
        suffixMap.put("BLUFFS","BLFS");
        suffixMap.put("BOT","BTM");
        suffixMap.put("BOTTM","BTM");
        suffixMap.put("BOTTOM","BTM");
        suffixMap.put("BTM","BTM");
        suffixMap.put("BLVD","BLVD");
        suffixMap.put("BOUL","BLVD");
        suffixMap.put("BOULEVARD","BLVD");
        suffixMap.put("BOULV","BLVD");
        suffixMap.put("BR","BR");
        suffixMap.put("BRANCH","BR");
        suffixMap.put("BRNCH","BR");
        suffixMap.put("BRDGE","BRG");
        suffixMap.put("BRG","BRG");
        suffixMap.put("BRIDGE","BRG");
        suffixMap.put("BRK","BRK");
        suffixMap.put("BROOK","BRK");
        suffixMap.put("BROOKS","BRKS");
        suffixMap.put("BURG","BG");
        suffixMap.put("BURGS","BGS");
        suffixMap.put("BYP","BYP");
        suffixMap.put("BYPA","BYP");
        suffixMap.put("BYPAS","BYP");
        suffixMap.put("BYPASS","BYP");
        suffixMap.put("BYPS","BYP");
        suffixMap.put("CAMP","CP");
        suffixMap.put("CMP","CP");
        suffixMap.put("CP","CP");
        suffixMap.put("CANYN","CYN");
        suffixMap.put("CANYON","CYN");
        suffixMap.put("CNYN","CYN");
        suffixMap.put("CYN","CYN");
        suffixMap.put("CAPE","CPE");
        suffixMap.put("CPE","CPE");
        suffixMap.put("CAUSEWAY","CSWY");
        suffixMap.put("CAUSWAY","CSWY");
        suffixMap.put("CSWY","CSWY");
        suffixMap.put("CEN","CTR");
        suffixMap.put("CENT","CTR");
        suffixMap.put("CENTER","CTR");
        suffixMap.put("CENTR","CTR");
        suffixMap.put("CENTRE","CTR");
        suffixMap.put("CNTER","CTR");
        suffixMap.put("CNTR","CTR");
        suffixMap.put("CTR","CTR");
        suffixMap.put("CENTERS","CTRS");
        suffixMap.put("CIR","CIR");
        suffixMap.put("CIRC","CIR");
        suffixMap.put("CIRCL","CIR");
        suffixMap.put("CIRCLE","CIR");
        suffixMap.put("CRCL","CIR");
        suffixMap.put("CRCLE","CIR");
        suffixMap.put("CIRCLES","CIRS");
        suffixMap.put("CLF","CLF");
        suffixMap.put("CLIFF","CLF");
        suffixMap.put("CLFS","CLFS");
        suffixMap.put("CLIFFS","CLFS");
        suffixMap.put("CLB","CLB");
        suffixMap.put("CLUB","CLB");
        suffixMap.put("COMMON","CMN");
        suffixMap.put("COR","COR");
        suffixMap.put("CORNER","COR");
        suffixMap.put("CORNERS","CORS");
        suffixMap.put("CORS","CORS");
        suffixMap.put("COURSE","CRSE");
        suffixMap.put("CRSE","CRSE");
        suffixMap.put("COURT","CT");
        suffixMap.put("CRT","CT");
        suffixMap.put("CT","CT");
        suffixMap.put("COURTS","CTS");
        suffixMap.put("cts","cts");
        suffixMap.put("COVE","CV");
        suffixMap.put("CV","CV");
        suffixMap.put("COVES","CVS");
        suffixMap.put("CK","CRK");
        suffixMap.put("CR","CRK");
        suffixMap.put("CREEK","CRK");
        suffixMap.put("CRK","CRK");
        suffixMap.put("CRECENT","CRES");
        suffixMap.put("CRES","CRES");
        suffixMap.put("CRESCENT","CRES");
        suffixMap.put("CRESENT","CRES");
        suffixMap.put("CRSCNT","CRES");
        suffixMap.put("CRSENT","CRES");
        suffixMap.put("CRSNT","CRES");
        suffixMap.put("CREST","CRST");
        suffixMap.put("CROSSING","XING");
        suffixMap.put("CRSSING","XING");
        suffixMap.put("CRSSNG","XING");
        suffixMap.put("XING","XING");
        suffixMap.put("CROSSROAD","XRD");
        suffixMap.put("CURVE","CURV");
        suffixMap.put("DALE","DL");
        suffixMap.put("DL","DL");
        suffixMap.put("DAM","DM");
        suffixMap.put("DM","DM");
        suffixMap.put("DIV","DV");
        suffixMap.put("DIVIDE","DV");
        suffixMap.put("DV","DV");
        suffixMap.put("DVD","DV");
        suffixMap.put("DR","DR");
        suffixMap.put("DRIV","DR");
        suffixMap.put("DRIVE","DR");
        suffixMap.put("DRV","DR");
        suffixMap.put("DRIVES","DRS");
        suffixMap.put("EST","EST");
        suffixMap.put("ESTATE","EST");
        suffixMap.put("ESTATES","ESTS");
        suffixMap.put("ESTS","ESTS");
        suffixMap.put("EXP","EXPY");
        suffixMap.put("EXPR","EXPY");
        suffixMap.put("EXPRESS","EXPY");
        suffixMap.put("EXPRESSWAY","EXPY");
        suffixMap.put("EXPW","EXPY");
        suffixMap.put("EXPY","EXPY");
        suffixMap.put("EXT","EXT");
        suffixMap.put("EXTENSION","EXT");
        suffixMap.put("EXTN","EXT");
        suffixMap.put("EXTNSN","EXT");
        suffixMap.put("EXTENSIONS","EXTS");
        suffixMap.put("EXTS","EXTS");
        suffixMap.put("FALL","FALL");
        suffixMap.put("FALLS","FLS");
        suffixMap.put("FLS","FLS");
        suffixMap.put("FERRY","FRY");
        suffixMap.put("FRRY","FRY");
        suffixMap.put("FRY","FRY");
        suffixMap.put("FIELD","FLD");
        suffixMap.put("FLD","FLD");
        suffixMap.put("FIELDS","FLDS");
        suffixMap.put("FLDS","FLDS");
        suffixMap.put("FLAT","FLT");
        suffixMap.put("FLT","FLT");
        suffixMap.put("FLATS","FLTS");
        suffixMap.put("FLTS","FLTS");
        suffixMap.put("FORD","FRD");
        suffixMap.put("FRD","FRD");
        suffixMap.put("FORDS","FRDS");
        suffixMap.put("FOREST","FRST");
        suffixMap.put("FORESTS","FRST");
        suffixMap.put("FRST","FRST");
        suffixMap.put("FORG","FRG");
        suffixMap.put("FORGE","FRG");
        suffixMap.put("FRG","FRG");
        suffixMap.put("FORGES","FRGS");
        suffixMap.put("FORK","FRK");
        suffixMap.put("FRK","FRK");
        suffixMap.put("FORKS","FRKS");
        suffixMap.put("FRKS","FRKS");
        suffixMap.put("FORT","FT");
        suffixMap.put("FRT","FT");
        suffixMap.put("FT","FT");
        suffixMap.put("FREEWAY","FWY");
        suffixMap.put("FREEWY","FWY");
        suffixMap.put("FRWAY","FWY");
        suffixMap.put("FRWY","FWY");
        suffixMap.put("FWY","FWY");
        suffixMap.put("GARDEN","GDN");
        suffixMap.put("GARDN","GDN");
        suffixMap.put("GDN","GDN");
        suffixMap.put("GRDEN","GDN");
        suffixMap.put("GRDN","GDN");
        suffixMap.put("GARDENS","GDNS");
        suffixMap.put("GDNS","GDNS");
        suffixMap.put("GRDNS","GDNS");
        suffixMap.put("GATEWAY","GTWY");
        suffixMap.put("GATEWY","GTWY");
        suffixMap.put("GATWAY","GTWY");
        suffixMap.put("GTWAY","GTWY");
        suffixMap.put("GTWY","GTWY");
        suffixMap.put("GLEN","GLN");
        suffixMap.put("GLN","GLN");
        suffixMap.put("GLENS","GLNS");
        suffixMap.put("GREEN","GRN");
        suffixMap.put("GRN","GRN");
        suffixMap.put("GREENS","GRNS");
        suffixMap.put("GROV","GRV");
        suffixMap.put("GROVE","GRV");
        suffixMap.put("GRV","GRV");
        suffixMap.put("GROVES","GRVS");
        suffixMap.put("HARB","HBR");
        suffixMap.put("HARBOR","HBR");
        suffixMap.put("HARBR","HBR");
        suffixMap.put("HBR","HBR");
        suffixMap.put("HRBOR","HBR");
        suffixMap.put("HARBORS","HBRS");
        suffixMap.put("HAVEN","HVN");
        suffixMap.put("HAVN","HVN");
        suffixMap.put("HVN","HVN");
        suffixMap.put("HEIGHT","HTS");
        suffixMap.put("HEIGHTS","HTS");
        suffixMap.put("HGTS","HTS");
        suffixMap.put("HT","HTS");
        suffixMap.put("HTS","HTS");
        suffixMap.put("HIGHWAY","HWY");
        suffixMap.put("HIGHWY","HWY");
        suffixMap.put("HIWAY","HWY");
        suffixMap.put("HIWY","HWY");
        suffixMap.put("HWAY","HWY");
        suffixMap.put("HWY","HWY");
        suffixMap.put("HILL","HL");
        suffixMap.put("HL","HL");
        suffixMap.put("HILLS","HLS");
        suffixMap.put("HLS","HLS");
        suffixMap.put("HLLW","HOLW");
        suffixMap.put("HOLLOW","HOLW");
        suffixMap.put("HOLLOWS","HOLW");
        suffixMap.put("HOLW","HOLW");
        suffixMap.put("HOLWS","HOLW");
        suffixMap.put("INLET","INLT");
        suffixMap.put("INLT","INLT");
        suffixMap.put("IS","IS");
        suffixMap.put("ISLAND","IS");
        suffixMap.put("ISLND","IS");
        suffixMap.put("ISLANDS","ISS");
        suffixMap.put("ISLNDS","ISS");
        suffixMap.put("ISS","ISS");
        suffixMap.put("ISLE","ISLE");
        suffixMap.put("ISLES","ISLE");
        suffixMap.put("JCT","JCT");
        suffixMap.put("JCTION","JCT");
        suffixMap.put("JCTN","JCT");
        suffixMap.put("JUNCTION","JCT");
        suffixMap.put("JUNCTN","JCT");
        suffixMap.put("JUNCTON","JCT");
        suffixMap.put("JCTNS","JCTS");
        suffixMap.put("JCTS","JCTS");
        suffixMap.put("JUNCTIONS","JCTS");
        suffixMap.put("KEY","KY");
        suffixMap.put("KY","KY");
        suffixMap.put("KEYS","KYS");
        suffixMap.put("KYS","KYS");
        suffixMap.put("KNL","KNL");
        suffixMap.put("KNOL","KNL");
        suffixMap.put("KNOLL","KNL");
        suffixMap.put("KNLS","KNLS");
        suffixMap.put("KNOLLS","KNLS");
        suffixMap.put("LAKE","LK");
        suffixMap.put("LK","LK");
        suffixMap.put("LAKES","LKS");
        suffixMap.put("LKS","LKS");
        suffixMap.put("LAND","LAND");
        suffixMap.put("LANDING","LNDG");
        suffixMap.put("LNDG","LNDG");
        suffixMap.put("LNDNG","LNDG");
        suffixMap.put("LA","LN");
        suffixMap.put("LANE","LN");
        suffixMap.put("LANES","LN");
        suffixMap.put("LN","LN");
        suffixMap.put("LGT","LGT");
        suffixMap.put("LIGHT","LGT");
        suffixMap.put("LIGHTS","LGTS");
        suffixMap.put("LF","LF");
        suffixMap.put("LOAF","LF");
        suffixMap.put("LCK","LCK");
        suffixMap.put("LOCK","LCK");
        suffixMap.put("LCKS","LCKS");
        suffixMap.put("LOCKS","LCKS");
        suffixMap.put("LDG","LDG");
        suffixMap.put("LDGE","LDG");
        suffixMap.put("LODG","LDG");
        suffixMap.put("LODGE","LDG");
        suffixMap.put("LOOP","LOOP");
        suffixMap.put("LOOPS","LOOP");
        suffixMap.put("MALL","MALL");
        suffixMap.put("MANOR","MNR");
        suffixMap.put("MNR","MNR");
        suffixMap.put("MANORS","MNRS");
        suffixMap.put("MNRS","MNRS");
        suffixMap.put("MDW","MDW");
        suffixMap.put("MEADOW","MDW");
        suffixMap.put("MDWS","MDWS");
        suffixMap.put("MEADOWS","MDWS");
        suffixMap.put("MEDOWS","MDWS");
        suffixMap.put("MEWS","MEWS");
        suffixMap.put("MILL","ML");
        suffixMap.put("ML","ML");
        suffixMap.put("MILLS","MLS");
        suffixMap.put("MLS","MLS");
        suffixMap.put("MISSION","MSN");
        suffixMap.put("MISSN","MSN");
        suffixMap.put("MSN","MSN");
        suffixMap.put("MSSN","MSN");
        suffixMap.put("MOTORWAY","MTWY");
        suffixMap.put("MNT","MT");
        suffixMap.put("MOUNT","MT");
        suffixMap.put("MT","MT");
        suffixMap.put("MNTAIN","MTN");
        suffixMap.put("MNTN","MTN");
        suffixMap.put("MOUNTAIN","MTN");
        suffixMap.put("MOUNTIN","MTN");
        suffixMap.put("MTIN","MTN");
        suffixMap.put("MTN","MTN");
        suffixMap.put("MNTNS","MTNS");
        suffixMap.put("MOUNTAINS","MTNS");
        suffixMap.put("NCK","NCK");
        suffixMap.put("NECK","NCK");
        suffixMap.put("ORCH","ORCH");
        suffixMap.put("ORCHARD","ORCH");
        suffixMap.put("ORCHRD","ORCH");
        suffixMap.put("OVAL","OVAL");
        suffixMap.put("OVL","OVAL");
        suffixMap.put("OVERPASS","OPAS");
        suffixMap.put("PARK","PARK");
        suffixMap.put("PK","PARK");
        suffixMap.put("PRK","PARK");
        suffixMap.put("PARKS","PARK");
        suffixMap.put("PARKWAY","PKWY");
        suffixMap.put("PARKWY","PKWY");
        suffixMap.put("PKWAY","PKWY");
        suffixMap.put("PKWY","PKWY");
        suffixMap.put("PKY","PKWY");
        suffixMap.put("PARKWAYS","PKWY");
        suffixMap.put("PKWYS","PKWY");
        suffixMap.put("PASS","PASS");
        suffixMap.put("PASSAGE","PSGE");
        suffixMap.put("PTH", "PATH"); // TODO: Why was this one missing?
        suffixMap.put("PATH","PATH");
        suffixMap.put("PATHS","PATH");
        suffixMap.put("PIKE","PIKE");
        suffixMap.put("PIKES","PIKE");
        suffixMap.put("PINE","PNE");
        suffixMap.put("PINES","PNES");
        suffixMap.put("PNES","PNES");
        suffixMap.put("PL","PL");
        suffixMap.put("PLACE","PL");
        suffixMap.put("PLAIN","PLN");
        suffixMap.put("PLN","PLN");
        suffixMap.put("PLAINES","PLNS");
        suffixMap.put("PLAINS","PLNS");
        suffixMap.put("PLNS","PLNS");
        suffixMap.put("PLAZA","PLZ");
        suffixMap.put("PLZ","PLZ");
        suffixMap.put("PLZA","PLZ");
        suffixMap.put("POINT","PT");
        suffixMap.put("PT","PT");
        suffixMap.put("POINTS","PTS");
        suffixMap.put("PTS","PTS");
        suffixMap.put("PORT","PRT");
        suffixMap.put("PRT","PRT");
        suffixMap.put("PORTS","PRTS");
        suffixMap.put("PRTS","PRTS");
        suffixMap.put("PR","PR");
        suffixMap.put("PRAIRIE","PR");
        suffixMap.put("PRARIE","PR");
        suffixMap.put("PRR","PR");
        suffixMap.put("RAD","RADL");
        suffixMap.put("RADIAL","RADL");
        suffixMap.put("RADIEL","RADL");
        suffixMap.put("RADL","RADL");
        suffixMap.put("RAMP","RAMP");
        suffixMap.put("RANCH","RNCH");
        suffixMap.put("RANCHES","RNCH");
        suffixMap.put("RNCH","RNCH");
        suffixMap.put("RNCHS","RNCH");
        suffixMap.put("RAPID","RPD");
        suffixMap.put("RPD","RPD");
        suffixMap.put("RAPIDS","RPDS");
        suffixMap.put("RPDS","RPDS");
        suffixMap.put("REST","RST");
        suffixMap.put("RST","RST");
        suffixMap.put("RDG","RDG");
        suffixMap.put("RDGE","RDG");
        suffixMap.put("RIDGE","RDG");
        suffixMap.put("RDGS","RDGS");
        suffixMap.put("RIDGES","RDGS");
        suffixMap.put("RIV","RIV");
        suffixMap.put("RIVER","RIV");
        suffixMap.put("RIVR","RIV");
        suffixMap.put("RVR","RIV");
        suffixMap.put("RD","RD");
        suffixMap.put("ROAD","RD");
        suffixMap.put("RDS","RDS");
        suffixMap.put("ROADS","RDS");
        suffixMap.put("ROUTE","RTE");
        suffixMap.put("ROW","ROW");
        suffixMap.put("RUE","RUE");
        suffixMap.put("RUN","RUN");
        suffixMap.put("SHL","SHL");
        suffixMap.put("SHOAL","SHL");
        suffixMap.put("SHLS","SHLS");
        suffixMap.put("SHOALS","SHLS");
        suffixMap.put("SHOAR","SHR");
        suffixMap.put("SHORE","SHR");
        suffixMap.put("SHR","SHR");
        suffixMap.put("SHOARS","SHRS");
        suffixMap.put("SHORES","SHRS");
        suffixMap.put("SHRS","SHRS");
        suffixMap.put("SKYWAY","SKWY");
        suffixMap.put("SPG","SPG");
        suffixMap.put("SPNG","SPG");
        suffixMap.put("SPRING","SPG");
        suffixMap.put("SPRNG","SPG");
        suffixMap.put("SPGS","SPGS");
        suffixMap.put("SPNGS","SPGS");
        suffixMap.put("SPRINGS","SPGS");
        suffixMap.put("SPRNGS","SPGS");
        suffixMap.put("SPUR","SPUR");
        suffixMap.put("SPURS","SPUR");
        suffixMap.put("SQ","SQ");
        suffixMap.put("SQR","SQ");
        suffixMap.put("SQRE","SQ");
        suffixMap.put("SQU","SQ");
        suffixMap.put("SQUARE","SQ");
        suffixMap.put("SQRS","SQS");
        suffixMap.put("SQUARES","SQS");
        suffixMap.put("STA","STA");
        suffixMap.put("STATION","STA");
        suffixMap.put("STATN","STA");
        suffixMap.put("STN","STA");
        suffixMap.put("STRA","STRA");
        suffixMap.put("STRAV","STRA");
        suffixMap.put("STRAVE","STRA");
        suffixMap.put("STRAVEN","STRA");
        suffixMap.put("STRAVENUE","STRA");
        suffixMap.put("STRAVN","STRA");
        suffixMap.put("STRVN","STRA");
        suffixMap.put("STRVNUE","STRA");
        suffixMap.put("STREAM","STRM");
        suffixMap.put("STREME","STRM");
        suffixMap.put("STRM","STRM");
        suffixMap.put("ST","ST");
        suffixMap.put("STR","ST");
        suffixMap.put("STREET","ST");
        suffixMap.put("STRT","ST");
        suffixMap.put("STREETS","STS");
        suffixMap.put("SMT","SMT");
        suffixMap.put("SUMIT","SMT");
        suffixMap.put("SUMITT","SMT");
        suffixMap.put("SUMMIT","SMT");
        suffixMap.put("TER","TER");
        suffixMap.put("TERR","TER");
        suffixMap.put("TERRACE","TER");
        suffixMap.put("THROUGHWAY","TRWY");
        suffixMap.put("TRACE","TRCE");
        suffixMap.put("TRACES","TRCE");
        suffixMap.put("TRCE","TRCE");
        suffixMap.put("TRACK","TRAK");
        suffixMap.put("TRACKS","TRAK");
        suffixMap.put("TRAK","TRAK");
        suffixMap.put("TRK","TRAK");
        suffixMap.put("TRKS","TRAK");
        suffixMap.put("TRAFFICWAY","TRFY");
        suffixMap.put("TRFY","TRFY");
        suffixMap.put("TR","TRL");
        suffixMap.put("TRAIL","TRL");
        suffixMap.put("TRAILS","TRL");
        suffixMap.put("TRL","TRL");
        suffixMap.put("TRLS","TRL");
        suffixMap.put("TUNEL","TUNL");
        suffixMap.put("TUNL","TUNL");
        suffixMap.put("TUNLS","TUNL");
        suffixMap.put("TUNNEL","TUNL");
        suffixMap.put("TUNNELS","TUNL");
        suffixMap.put("TUNNL","TUNL");
        suffixMap.put("TPK","TPKE");
        suffixMap.put("TPKE","TPKE");
        suffixMap.put("TRNPK","TPKE");
        suffixMap.put("TRPK","TPKE");
        suffixMap.put("TURNPIKE","TPKE");
        suffixMap.put("TURNPK","TPKE");
        suffixMap.put("UNDERPASS","UPAS");
        suffixMap.put("UN","UN");
        suffixMap.put("UNION","UN");
        suffixMap.put("UNIONS","UNS");
        suffixMap.put("VALLEY","VLY");
        suffixMap.put("VALLY","VLY");
        suffixMap.put("VLLY","VLY");
        suffixMap.put("VLY","VLY");
        suffixMap.put("VALLEYS","VLYS");
        suffixMap.put("VLYS","VLYS");
        suffixMap.put("VDCT","VIA");
        suffixMap.put("VIA","VIA");
        suffixMap.put("VIADCT","VIA");
        suffixMap.put("VIADUCT","VIA");
        suffixMap.put("VIEW","VW");
        suffixMap.put("VW","VW");
        suffixMap.put("VIEWS","VWS");
        suffixMap.put("VWS","VWS");
        suffixMap.put("VILL","VLG");
        suffixMap.put("VILLAG","VLG");
        suffixMap.put("VILLAGE","VLG");
        suffixMap.put("VILLG","VLG");
        suffixMap.put("VILLIAGE","VLG");
        suffixMap.put("VLG","VLG");
        suffixMap.put("VILLAGES","VLGS");
        suffixMap.put("VLGS","VLGS");
        suffixMap.put("VILLE","VL");
        suffixMap.put("VL","VL");
        suffixMap.put("VIS","VIS");
        suffixMap.put("VIST","VIS");
        suffixMap.put("VISTA","VIS");
        suffixMap.put("VST","VIS");
        suffixMap.put("VSTA","VIS");
        suffixMap.put("WALK","WALK");
        suffixMap.put("WALKS","WALK");
        suffixMap.put("WALL","WALL");
        suffixMap.put("WAY","WAY");
        suffixMap.put("WY","WAY");
        suffixMap.put("WAYS","WAYS");
        suffixMap.put("WELL","WL");
        suffixMap.put("WELLS","WLS");
        suffixMap.put("WLS","WLS");
    }
}
