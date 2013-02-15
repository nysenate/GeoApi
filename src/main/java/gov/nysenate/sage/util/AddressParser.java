package gov.nysenate.sage.util;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.boe.BOEAddress;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.BluebirdAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser
{
    public static HashMap<String,String> suffixMap = null;
    public static HashMap<String,String> ordinals = null;
    public static HashMap<String,String> commonAbbreviations = null;

    public static Pattern addrPattern = null;

    public static void main(String[] args)
    {
        loadConstants();
        for(String s : commonAbbreviations.keySet()){
            System.out.println("(\""+s+"\",\""+commonAbbreviations.get(s)+"\"),");
        }
    }

    public static BOEStreetAddress parseAddress(String address)
    {
        loadConstants();
        Matcher m = addrPattern.matcher(address.toUpperCase());
        if (m.find()) {

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

        }
        else {
            System.out.println("Could not match: "+address);
            return null;
        }
    }

    public static BOEAddress normalizeAddress(BOEAddress address)
    {
        loadConstants();

        if (address.town != null && !address.town.equals("")) {
            address.town = address.town.toUpperCase().trim();
            // Fix up the towns
            if (address.town == "CITY/KNG") {
                address.town = "KINGSTON";
            }
            else if (address.town == "PORTJERVIS/CITY") {
                address.town = "PORT JERVIS";
            }
            else {
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
                for (Map.Entry<String, String> entry : commonAbbreviations.entrySet()) {
                    address.street = address.street.replace(" "+entry.getKey()+" ", " "+entry.getValue()+" ");
                }
                address.street = address.street.trim();
            }
        }
        return address;
    }

    public static void loadConstants()
    {
        if (addrPattern!=null && suffixMap != null)
            return;

        ordinals = new HashMap<String,String>();
        ordinals.put("NORTH", "N");
        ordinals.put("SOUTH", "S");
        ordinals.put("EAST", "E");
        ordinals.put("WEST", "W");
    }

    /**
     *
     */
    public static void loadRegexes()
    {
        String zip = "([0-9]{5})(?:-([0-9]{4}))?";
        String sep = "(?:[ ]+)";
        String state = "([A-Z]{2})";
        String city = "([A-Z. '-]+?)";
        String street = "((?: ?[-.'A-Z0-9]){2,}?)";
        String building = "(?:([0-9]+)([A-Z]|-[0-9]+| 1/2)?)";
        String apt_number = "(?:(?:(?:([0-9]+?)(?:ND|ST|RD|TH)?(?:[ -]*([A-Z0-9]+))?)|(?:([A-Z]+)(?:[ -]*([0-9]+))?)|BSMT|BSMNT|PH|PENTHOUSE)(?:FL)?)";
        String apartment = "(?:(?:#|APT|STE|UNIT|BLDG|LOWR|UPPR|LOT|BOX|LEFT|RIGHT|TRLR|RM)[. ]*(?:#|FL)?)"+apt_number+"?";

        addrPattern = Pattern.compile("()(?:" + building + sep + ")?" + street + "(?:" + sep + apartment + ")?" + "(?:[ ,]+" + city + ")?" + "(?:" + sep + state + ")?" + "(?:" + sep + zip + ")?$");
    }

}
