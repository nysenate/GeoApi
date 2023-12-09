package gov.nysenate.sage.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

import java.util.*;

public final class AddressDictionary {
    public static final ImmutableMap<String, String>
            stateMap = getMap(
            "AL","Alabama", "AK","Alaska", "AS","American Samoa", "AZ","Arizona",
            "AR","Arkansas", "CA","California", "CO","Colorado", "CT","Connecticut",
            "DE","Delaware", "DC","District of Columbia", "FM","Federated States of Micronesia", "FL","Florida",
            "GA","Georgia", "GU","Guam", "HI","Hawaii", "ID","Idaho",
            "IL","Illinois", "IN","Indiana", "IA","Iowa", "KS","Kansas",
            "KY","Kentucky", "LA","Louisiana", "ME","Maine", "MH","Marshall Islands",
            "MD","Maryland", "MA","Massachusetts", "MI","Michigan", "MN","Minnesota",
            "MS","Mississippi", "MO","Missouri", "MT","Montana", "NE","Nebraska",
            "NV","Nevada", "NH","New Hampshire", "NJ","New Jersey", "NM","New Mexico",
            "NY","New York", "NC","North Carolina", "ND","North Dakota", "MP","Northern Mariana Islands",
            "OH","Ohio", "OK","Oklahoma", "OR","Oregon", "PW","Palau",
            "PA","Pennsylvania", "PR","Puerto Rico", "RI","Rhode Island", "SC","South Carolina",
            "SD","South Dakota", "TN","Tennessee", "TX","Texas", "UT","Utah",
            "VT","Vermont", "VI","Virgin Islands", "VA","Virginia", "WA","Washington",
            "WV","West Virginia", "WI","Wisconsin", "WY","Wyoming"
    ),
            streetTypeMap = getMap(
                    "Aly", List.of("ALLEE", "ALLEY", "ALLY", "ALY"), "Anx", List.of("ANEX", "ANNEX", "ANNX", "ANX"),
                    "Arc", List.of("ARC", "ARCADE"), "Ave", List.of("AV", "AVE", "AVEN", "AVENU", "AVENUE", "AVN", "AVNUE"),
                    "Bch", List.of("BCH", "BEACH"), "Bg", List.of("BG", "BURG"),
                    "Bgs", List.of("BGS", "BURGS"), "Blf", List.of("BLF", "BLUF", "BLUFF"),
                    "Blfs", List.of("BLFS", "BLUFFS"), "Blvd", List.of("BLVD", "BOUL", "BOULEVARD", "BOULV"),
                    "Bnd", List.of("BEND", "BND"), "Br", List.of("BR", "BRANCH", "BRNCH"),
                    "Brg", List.of("BRDGE", "BRG", "BRIDGE"), "Brk", List.of("BRK", "BROOK"),
                    "Brks", List.of("BRKS", "BROOKS"), "Btm", List.of("BOT", "BOTTM", "BOTTOM", "BTM"),
                    "Byp", List.of("BYP", "BYPA", "BYPAS", "BYPASS", "BYPS"),
                    "Byu", List.of("BAYOO", "BAYOU", "BYU"),
                    "Cir", List.of("CIR", "CIRC", "CIRCL", "CIRCLE", "CRCL", "CRCLE"),
                    "Cirs", List.of("CIRCLES", "CIRS"), "Clb", List.of("CLB", "CLUB"),
                    "Clf", List.of("CLF", "CLIFF"), "Clfs", List.of("CLFS", "CLIFFS"),
                    "Cmn", List.of("CMN", "COMMON"), "Cor", List.of("COR", "CORNER"),
                    "Cors", List.of("CORNERS", "CORS"), "Cp", List.of("CAMP", "CMP", "CP"),
                    "Cpe", List.of("CAPE", "CPE"),
                    "Cres", List.of("CRECENT", "CRES", "CRESCENT", "CRESENT", "CRSCNT", "CRSENT", "CRSNT"),
                    "Crk", List.of("CK", "CR", "CREEK", "CRK"), "Crse", List.of("COURSE", "CRSE"),
                    "Crst", List.of("CREST", "CRST"), "Cswy", List.of("CAUSEWAY", "CAUSWAY", "CSWY"),
                    "Ct", List.of("COURT", "CRT", "CT"),
                    "Ctr", List.of("CEN", "CENT", "CENTER", "CENTR", "CENTRE", "CNTER", "CNTR", "CTR"),
                    "Ctrs", List.of("CENTERS", "CTRS"), "Cts", List.of("COURTS", "CTS"),
                    "Curv", List.of("CURV", "CURVE"), "Cv", List.of("COVE", "CV"),
                    "Cvs", List.of("COVES", "CVS"), "Cyn", List.of("CANYN", "CANYON", "CNYN", "CYN"),
                    "Dl", List.of("DALE", "DL"), "Dm", List.of("DAM", "DM"), "Dr", List.of("DR", "DRIV", "DRIVE", "DRV"),
                    "Drs", List.of("DRIVES", "DRS"), "Dv", List.of("DIV", "DIVIDE", "DV", "DVD"),
                    "Est", List.of("EST", "ESTATE"), "Ests", List.of("ESTATES", "ESTS"),
                    "Ext", List.of("EXT", "EXTENSION", "EXTN", "EXTNSN"), "Exts", List.of("EXTENSIONS", "EXTS"),
                    "Fall", List.of("FALL"), "Fld", List.of("FIELD", "FLD"), "Flds", List.of("FIELDS", "FLDS"),
                    "Fls", List.of("FALLS", "FLS"), "Flt", List.of("FLAT", "FLT"),
                    "Flts", List.of("FLATS", "FLTS"), "Frd", List.of("FORD", "FRD"),
                    "Frds", List.of("FORDS", "FRDS"), "Frg", List.of("FORG", "FORGE", "FRG"),
                    "Frgs", List.of("FORGES", "FRGS"), "Frk", List.of("FORK", "FRK"),
                    "Frks", List.of("FORKS", "FRKS"), "Fry", List.of("FERRY", "FRRY", "FRY"),
                    "Ft", List.of("FORT", "FRT", "FT"),
                    "Gdn", List.of("GARDEN", "GARDN", "GDN", "GRDEN", "GRDN"),
                    "Gdns", List.of("GARDENS", "GDNS", "GRDNS"), "Gln", List.of("GLEN", "GLN"),
                    "Glns", List.of("GLENS", "GLNS"), "Grn", List.of("GREEN", "GRN"),
                    "Grns", List.of("GREENS", "GRNS"), "Grv", List.of("GROV", "GROVE", "GRV"),
                    "Grvs", List.of("GROVES", "GRVS"), "Gtwy", List.of("GATEWAY", "GATEWY", "GATWAY", "GTWAY", "GTWY"),
                    "Hbr", List.of("HARB", "HARBOR", "HARBR", "HBR", "HRBOR"), "Hbrs", List.of("HARBORS", "HBRS"),
                    "Hl", List.of("HILL", "HL"), "Hls", List.of("HILLS", "HLS"),
                    "Holw", List.of("HLLW", "HOLLOW", "HOLLOWS", "HOLW", "HOLWS"),
                    "Hts", List.of("HEIGHT", "HEIGHTS", "HGTS", "HT", "HTS"), "Hvn", List.of("HAVEN", "HAVN", "HVN"),
                    "Inlt", List.of("INLET", "INLT"), "Is", List.of("IS", "ISLAND", "ISLND"),
                    "Isle", List.of("ISLE", "ISLES"), "Iss", List.of("ISLANDS", "ISLNDS", "ISS"),
                    "Jct", List.of("JCT", "JCTION", "JCTN", "JUNCTION", "JUNCTN", "JUNCTON"),
                    "Jcts", List.of("JCTNS", "JCTS", "JUNCTIONS"),
                    "Knl", List.of("KNL", "KNOL", "KNOLL"),
                    "Knls", List.of("KNLS", "KNOLLS"), "Ky", List.of("KEY", "KY"),
                    "Kys", List.of("KEYS", "KYS"),
                    "Land", List.of("LAND"), "Lck", List.of("LCK", "LOCK"),
                    "Lcks", List.of("LCKS", "LOCKS"), "Ldg", List.of("LDG", "LDGE", "LODG", "LODGE"),
                    "Lf", List.of("LF", "LOAF"), "Lgt", List.of("LGT", "LIGHT"),
                    "Lgts", List.of("LGTS", "LIGHTS"), "Lk", List.of("LAKE", "LK"),
                    "Lks", List.of("LAKES", "LKS"), "Ln", List.of("LA", "LANE", "LANES", "LN"),
                    "Lndg", List.of("LANDING", "LNDG", "LNDNG"), "Loop", List.of("LOOP", "LOOPS"),
                    "Mall", List.of("MALL"), "Mdw", List.of("MDW", "MEADOW"), "Mdws", List.of("MDWS", "MEADOWS", "MEDOWS"),
                    "Mews", List.of("MEWS"), "Ml", List.of("MILL", "ML"), "Mls", List.of("MILLS", "MLS"),
                    "Mnr", List.of("MANOR", "MNR"), "Mnrs", List.of("MANORS", "MNRS"),
                    "Msn", List.of("MISSION", "MISSN", "MSN", "MSSN"), "Mt", List.of("MNT", "MOUNT", "MT"),
                    "Mtn", List.of("MNTAIN", "MNTN", "MOUNTAIN", "MOUNTIN", "MTIN", "MTN"),
                    "Mtns", List.of("MNTNS", "MOUNTAINS", "MTNS"), "Mtwy", List.of("MOTORWAY", "MTWY"),
                    "Nck", List.of("NCK", "NECK"),
                    "Opas", List.of("OPAS", "OVERPASS"),
                    "Orch", List.of("ORCH", "ORCHARD", "ORCHRD"), "Oval", List.of("OVAL", "OVL"),
                    "Park", List.of("PARK", "PARKS", "PK", "PRK"), "Pass", List.of("PASS"),
                    "Path", List.of("PATH", "PATHS"), "Pike", List.of("PIKE", "PIKES"),
                    "Pkwy", List.of("PARKWAY", "PARKWAYS", "PARKWY", "PKWAY", "PKWY", "PKWYS", "PKY"),
                    "Pl", List.of("PL", "PLACE"), "Pln", List.of("PLAIN", "PLN"),
                    "Plns", List.of("PLAINES", "PLAINS", "PLNS"), "Plz", List.of("PLAZA", "PLZ", "PLZA"),
                    "Pne", List.of("PINE", "PNE"), "Pnes", List.of("PINES", "PNES"),
                    "Pr", List.of("PR", "PRAIRIE", "PRARIE", "PRR"), "Prt", List.of("PORT", "PRT"),
                    "Prts", List.of("PORTS", "PRTS"), "Psge", List.of("PASSAGE", "PSGE"),
                    "Pt", List.of("POINT", "POINTE", "PT"), "Pts", List.of("POINTS", "PTS"),
                    "Radl", List.of("RAD", "RADIAL", "RADIEL", "RADL"), "Ramp", List.of("RAMP"),
                    "Rd", List.of("RD", "ROAD"), "Rdg", List.of("RDG", "RDGE", "RIDGE"),
                    "Rdgs", List.of("RDGS", "RIDGES"), "Rds", List.of("RDS", "ROADS"),
                    "Riv", List.of("RIV", "RIVER", "RIVR", "RVR"), "Rnch", List.of("RANCH", "RANCHES", "RNCH", "RNCHS"),
                    "Row", List.of("ROW"), "Rpd", List.of("RAPID", "RPD"), "Rpds", List.of("RAPIDS", "RPDS"),
                    "Rst", List.of("REST", "RST"), "Rte", List.of("RTE"), "Rue", List.of("RUE"),
                    "Run", List.of("RUN"),
                    "Shl", List.of("SHL", "SHOAL"), "Shls", List.of("SHLS", "SHOALS"),
                    "Shr", List.of("SHOAR", "SHORE", "SHR"), "Shrs", List.of("SHOARS", "SHORES", "SHRS"),
                    "Skwy", List.of("SKWY", "SKYWAY"), "Smt", List.of("SMT", "SUMIT", "SUMITT", "SUMMIT"),
                    "Spg", List.of("SPG", "SPNG", "SPRING", "SPRNG"), "Spgs", List.of("SPGS", "SPNGS", "SPRINGS", "SPRNGS"),
                    "Spur", List.of("SPUR", "SPURS"), "Sq", List.of("SQ", "SQR", "SQRE", "SQU", "SQUARE"),
                    "Sqs", List.of("SQRS", "SQS", "SQUARES"), "St", List.of("ST", "STR", "STREET", "STRT"),
                    "Sta", List.of("STA", "STATION", "STATN", "STN"),
                    "Stra", List.of("STRA", "STRAV", "STRAVE", "STRAVEN", "STRAVENUE", "STRAVN", "STRVN", "STRVNUE"),
                    "Strm", List.of("STREAM", "STREME", "STRM"), "Sts", List.of("STREETS", "STS"),
                    "Svc Dr", List.of("SERVICE DR", "SERVICE DRIVE"), "Svc Rd", List.of("SERVICE RD", "SERVICE ROAD"),
                    "Ter", List.of("TER", "TERR", "TERRACE"), "Trak", List.of("TRACK", "TRACKS", "TRAK", "TRK", "TRKS"),
                    "Trce", List.of("TRACE", "TRACES", "TRCE"), "Trfy", List.of("TRAFFICWAY", "TRFY"),
                    "Trl", List.of("TR", "TRAIL", "TRAILS", "TRL", "TRLS"), "Trwy", List.of("THROUGHWAY", "TRWY"),
                    "Tunl", List.of("TUNEL", "TUNL", "TUNLS", "TUNNEL", "TUNNELS", "TUNNL"),
                    "Un", List.of("UN", "UNION"), "Uns", List.of("UNIONS", "UNS"),
                    "Upas", List.of("UNDERPASS", "UPAS"),
                    "Via", List.of("VDCT", "VIA", "VIADCT", "VIADUCT"),
                    "Vis", List.of("VIS", "VIST", "VISTA", "VST", "VSTA"), "Vl", List.of("VILLE", "VL"),
                    "Vlg", List.of("VILL", "VILLAG", "VILLAGE", "VILLG", "VILLIAGE", "VLG"),
                    "Vlgs", List.of("VILLAGES", "VLGS"), "Vly", List.of("VALLEY", "VALLY", "VLLY", "VLY"),
                    "Vlys", List.of("VALLEYS", "VLYS"), "Vw", List.of("VIEW", "VW"),
                    "Vws", List.of("VIEWS", "VWS"),
                    "Walk", List.of("WALK", "WALKS"), "Wall", List.of("WALL"),
                    "Way", List.of("WAY", "WY"), "Ways", List.of("WAYS"),
                    "Wl", List.of("WELL", "WL"), "Wls", List.of("WELLS", "WLS"),
                    "Xing", List.of("CROSSING", "CRSSING", "CRSSNG", "XING"), "Xrd", List.of("CROSSROAD", "XRD")
            ),
            unitMap = getMap(
                    "APT", List.of("APARTMENT", "APT"), "BLDG", List.of("BLDG", "BUILDING"),
                    "BSMT", List.of("BASEMENT", "BSMT"), "DEPT", List.of("DEPARTMENT", "DEPT"),
                    "FL", List.of("FL", "FLOOR"), "FRNT", List.of("FRNT"), "HNGR", List.of("HANGAR", "HNGR"),
                    "LBBY", List.of("LBBY", "LOBBY"), "LOT", List.of("LOT"), "LOWR", List.of("LOWER", "LOWR"),
                    "OFC", List.of("OFC", "OFFICE"), "PH", List.of("PENTHOUSE", "PH"),
                    "PIER", List.of("PIER"), "REAR", List.of("REAR"), "RM", List.of("RM", "ROOM"),
                    "SIDE", List.of("SIDE"), "SLIP", List.of("SLIP"), "SPC", List.of("SPACE", "SPC"),
                    "STE", List.of("STE", "SUITE"), "STOP", List.of("STOP"), "TRLR", List.of("TRAILER", "TRLR")
            ),
            highWayMap = getMap(
                    "Cam", List.of("CAM", "CAM.", "CAMINO"), "Co Hwy", List.of("CO HWY", "COUNTY HIGH WAY", "COUNTY HIGHWAY", "COUNTY HWY"),
                    "Co Rd", List.of("CO RD", "CORD", "COUNTY RD", "COUNTY ROAD"),
                    "Co Rte", List.of("CO RTE", "COUNTY ROUTE"), "Co St Aid Hwy", List.of("CO ST AID HWY"),
                    "Expy", List.of("EXP", "EXPR", "EXPRESS", "EXPRESSWAY", "EXPW", "EXPY"),
                    "Farm Rd", List.of("FARM RD"), "Fire Rd", List.of("FIRE RD"),
                    "Forest Rd", List.of("FOREST RD", "FOREST ROAD"), "Forest Rte", List.of("FOREST ROUTE", "FOREST RTE"),
                    "Fwy", List.of("FREEWAY", "FREEWY", "FRWAY", "FRWY", "FWY"),
                    "Hwy", List.of("HIGHWAY", "HIGHWY", "HIWAY", "HIWY", "HWAY", "HWY"),
                    "I-", List.of("I", "I-", "INTERSTATE", "INTERSTATE ROUTE", "INTERSTATE RT", "INTERSTATE RTE", "INTERSTATE RTE."),
                    "Rte", List.of("ROUTE", "RT"), "State Hwy", List.of("STATE HIGH WAY", "STATE HIGHWAY", "STATE HWY"),
                    "State Rd", List.of("STATE RD", "STATE ROAD"), "State Rte", List.of("STATE ROUTE", "STATE RT", "STATE RTE"),
                    "Tpke", List.of("TPK", "TPKE", "TRNPK", "TRPK", "TURNPIKE", "TURNPK"),
                    "US Hwy", List.of("U.S.", "US HIGH WAY", "US HIGHWAY", "US HWY"),
                    "US Rte", List.of("US ROUTE", "US RT", "US RTE"), "USFS Hwy", List.of("USFS HIGH WAY", "USFS HIGHWAY", "USFS HWY")
            ),
            streetPrefixMap = getMap(
                    "St", List.of("St"), "Ft", List.of("FORT", "FRT", "FT")
            ),
            directionMap = getDirectionMap();

    private AddressDictionary() {}

    private static ImmutableSortedMap<String, String> getMap(Object... data) {
        if (data.length%2 != 0) {
            throw new IllegalArgumentException("Data must come in pairs.");
        }

        var currMap = new HashMap<String, String>();
        for (int i = 0; i < data.length; i += 2) {
            try {
                var keys = (List<?>) data[i + 1];
                var value = (String) data[i];
                for (Object key : keys) {
                    currMap.put((String) key, value);
                }
            } catch (ClassCastException ex) {
                currMap.put((String) data[i], (String) data[i + 1]);
            }
        }
        return ImmutableSortedMap.copyOf(currMap);
    }

    private static ImmutableSortedMap<String, String> getDirectionMap() {
        var tempDirMap = new HashMap<String, String>();
        var cardinalDirections = List.of("NORTH", "SOUTH", "EAST", "WEST");
        var otherDirections = List.of("NORTH EAST", "NORTH WEST", "SOUTH EAST", "SOUTH WEST");
        for (String dir : cardinalDirections) {
            String abbr = String.valueOf(dir.charAt(0));
            tempDirMap.put(abbr, abbr);
            tempDirMap.put(dir, abbr);
        }

        for (String dir : otherDirections) {
            String[] parts = dir.split(" ");
            String abbr = String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
            tempDirMap.put(abbr, abbr);
            for (String delimiter : List.of("", " ", "-", "_")) {
                tempDirMap.put(dir.replaceFirst(" ", delimiter), abbr);
            }
        }
        return ImmutableSortedMap.copyOf(tempDirMap);
    }
}
