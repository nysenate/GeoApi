package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.FormatUtil;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobFile extends BaseJobFile<JobRecord> {
    /** Represents the functional group a header column belongs to */
    protected enum Group {
        address, validateAddress, geocode, district
    }

    /** Types of header columns */
    protected enum Type {
        stringType, doubleType, intType
    }

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

        town(List.of("town52", "townCode", "town"), Group.district),
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
    }

    private final List<Column> columns = new ArrayList<>();
    private final Map<Column, Integer> columnIndexMap = new HashMap<>();
    private final List<CellProcessor> processors = new ArrayList<>();

    public boolean hasAddress() {
        return checkColumnsForGroup(Group.address);
    }

    /** Indicates whether the job has USPS address columns to be filled in */
    public boolean requiresAddressValidation() {
        return checkColumnsForGroup(Group.validateAddress);
    }

    /** Indicates whether the job has geocoding columns to be filled in */
    public boolean requiresGeocode() {
        return checkColumnsForGroup(Group.geocode);
    }

    /** Indicates whether the job has district code columns to be filled in */
    public boolean requiresDistrictAssign() {
        return checkColumnsForGroup(Group.district);
    }

    /** Indicates whether the job has any columns to be filled in */
    public boolean requiresAny() {
        return requiresAddressValidation() || requiresGeocode() || requiresDistrictAssign();
    }

    /**
     * Returns a lost of district types that match the columns in the header.
     * @return List<DistrictType>
     */
    public List<DistrictType> getRequiredDistrictTypes() {
        List<DistrictType> reqTypes = new ArrayList<>();
        for (Column column : columns) {
            if (column.group.equals(Group.district)) {
                try {
                    reqTypes.add(DistrictType.valueOf(column.name().toUpperCase()));
                }
                catch (IllegalArgumentException ignored) {}
            }
        }
        return reqTypes;
    }

    /** Returns true if the Column list contains an element belonging to the given Group */
    private boolean checkColumnsForGroup(Group group) {
        for (Column column : columns) { if (column.group.equals(group)) return true; }
        return false;
    }

    /**
     * Returns a list of the cell processors created by <code>processHeader</code>
     * @return List<CellProcessor>
     */
    public List<CellProcessor> getProcessors() {
        return processors;
    }

    /**
     * Returns a map of resolved columns to indices
     * @return Map<Column, Integer>
     */
    public Map<Column, Integer> getColumnIndexMap() {
        return columnIndexMap;
    }

    /**
     * Given a header (array of column names) create a custom cell processor to parse values properly
     */
    public String[] processHeader(String[] header) {
        if (header == null || header.length == 0) {
            return header;
        }
        for (int i = 0; i < header.length; i++) {
            // Try to match column name to a Column
            if (header[i] != null && !header[i].isEmpty()) {
                String columnAlias = FormatUtil.toCamelCase(header[i]);
                Column headerColumn = Column.resolveColumn(columnAlias);
                if (headerColumn != null) {

                    // Record the index for the column
                    columns.add(headerColumn);
                    columnIndexMap.put(headerColumn, i);

                    // Tell the processors to use the correct types
                    if (headerColumn.type.equals(Type.doubleType)) {
                        this.processors.add(new Optional(new ParseDouble()));
                    }
                    else if (headerColumn.type.equals(Type.intType)) {
                        this.processors.add(new Optional(new ParseInt()));
                    }
                    else {
                        this.processors.add(new Optional());
                    }
                }
                else {
                    this.processors.add(new Optional());
                }
            }
            else {
                this.processors.add(new Optional());
            }
        }
        return header;
    }
}
