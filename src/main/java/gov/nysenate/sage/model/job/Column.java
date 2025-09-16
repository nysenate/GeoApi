package gov.nysenate.sage.model.job;

import com.fasterxml.jackson.dataformat.xml.util.CaseInsensitiveNameSet;
import gov.nysenate.sage.util.FormatUtil;

import java.util.Set;

/** All recognized column names are represented here */
public enum Column {
    street(Set.of("streetAddress", "street"), Group.address),
    city(Set.of("city"), Group.address),
    state(Set.of("stateProvinceId", "state"), Group.address),
    zip5(Set.of("postalCode", "postal", "zip", "zip5"), Group.address),
    zip4(Set.of("postalCodeSuffix", "postalSuffix", "zip4"), Group.address),

    uspsStreet(Set.of("uspsStreetAddress", "uspsStreet"), Group.validateAddress),
    uspsCity(Set.of("uspsCity"), Group.validateAddress),
    uspsState(Set.of("uspsState"), Group.validateAddress),
    uspsZip5(Set.of("uspsZip5", "uspsPostal", "uspsPostalCode"), Group.validateAddress),
    uspsZip4(Set.of("uspsZip4", "uspsPostalSuffix", "uspsPostalCodeSuffix"), Group.validateAddress),

    lat(Set.of("lat", "geoCode1", "latitude"), Group.geocode, Type.doubleType),
    lon(Set.of("lon", "lng", "geoCode2", "longitude"), Group.geocode, Type.doubleType),
    geoMethod(Set.of("geoMethod", "geoSource"), Group.geocode),
    geoQuality(Set.of("geoQuality", "accuracy"), Group.geocode),

    town_city(Set.of("town52", "townCode", "town"), Group.district),
    ward(Set.of("ward53", "wardCode", "ward"), Group.district),
    election(Set.of("electionDistrict49", "electionDistrict", "ed", "election"), Group.district),
    congressional(Set.of("congressionalDistrict46", "cd", "congressionalDistrict", "congressional"), Group.district),
    senate(Set.of("nySenateDistrict47", "sd", "senateDistrict", "senate"), Group.district),
    assembly(Set.of("nyAssemblyDistrict48", "ad", "assemblyDistrict", "assembly"), Group.district),
    county(Set.of("county50", "countyCode", "county"), Group.district),
    school(Set.of("schoolDistrict54", "schoolDistrict", "school"), Group.district);

    private final Type type;
    private final Group group;
    private final CaseInsensitiveNameSet aliases;

    Column(Set<String> aliases, Group group) {
        this(aliases, group, Type.stringType);
    }

    Column(Set<String> aliases, Group group, Type type) {
        this.aliases = CaseInsensitiveNameSet.construct(aliases);
        this.group = group;
        this.type = type;
    }

    public static Column resolveColumn(String alias) {
        // Standardizes the alias
        alias = FormatUtil.toCamelCase(alias);
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
