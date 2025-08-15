package gov.nysenate.sage.model.job;

import java.util.List;

/** All recognized column names are represented here */
public enum Column {
    street(List.of("streetAddress", "street"), Group.address),
    city(List.of("city"), Group.address),
    state(List.of("stateProvinceId", "state"), Group.address),
    zip5(List.of("postalCode", "postal", "zip", "zip5"), Group.address),
    zip4(List.of("postalCodeSuffix", "postalSuffix", "zip4"), Group.address),

    uspsStreet(List.of("uspsStreetAddress", "uspsStreet"), Group.validateAddress),
    uspsCity(List.of("uspsCity"), Group.validateAddress),
    uspsState(List.of("uspsState"), Group.validateAddress),
    uspsZip5(List.of("uspsZip5", "uspsPostal", "uspsPostalCode"), Group.validateAddress),
    uspsZip4(List.of("uspsZip4", "uspsPostalSuffix", "uspsPostalCodeSuffix"), Group.validateAddress),

    lat(List.of("lat", "geoCode1", "latitude"), Group.geocode, Type.doubleType),
    lon(List.of("lon", "lng", "geoCode2", "longitude"), Group.geocode, Type.doubleType),
    geoMethod(List.of("geoMethod", "geoSource"), Group.geocode),
    geoQuality(List.of("geoQuality", "accuracy"), Group.geocode),

    town_city(List.of("town52", "townCode", "town"), Group.district),
    ward(List.of("ward53", "wardCode", "ward"), Group.district),
    election(List.of("electionDistrict49", "electionDistrict", "ed", "election"), Group.district),
    congressional(List.of("congressionalDistrict46", "cd", "congressionalDistrict", "congressional"), Group.district),
    senate(List.of("nySenateDistrict47", "sd", "senateDistrict", "senate"), Group.district),
    assembly(List.of("nyAssemblyDistrict48", "ad", "assemblyDistrict", "assembly"), Group.district),
    county(List.of("county50", "countyCode", "county"), Group.district),
    school(List.of("schoolDistrict54", "schoolDistrict", "school"), Group.district);

    private final Type type;
    private final Group group;
    private final List<String> aliases;

    Column(List<String> aliases, Group group) {
        this(aliases, group, Type.stringType);
    }

    Column(List<String> aliases, Group group, Type type) {
        this.aliases = aliases;
        this.group = group;
        this.type = type;
    }

    public static Column resolveColumn(String alias) {
        for (Column column : Column.values()) {
            if (column.aliases.contains(alias)) {
                return column;
            }
        }
        return null;
    }

    public Group group() {
        return group;
    }

    public Type type() {
        return type;
    }

    /** Represents the functional group a header column belongs to */
    public enum Group {
        address, validateAddress, geocode, district
    }

    /** Types of header columns */
    public enum Type {
        stringType, doubleType, intType
    }
}
