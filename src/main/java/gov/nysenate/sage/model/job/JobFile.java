package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.*;

public class JobFile extends BaseJobFile<JobRecord>
{
    private static Logger logger = Logger.getLogger(JobFile.class);

    /** Represents the functional group a header column belongs to */
    protected enum Group {
        address, validateAddress, geocode, district
    }

    /** Types of header columns */
    protected enum Type {
        stringType, doubleType, intType, boolType, dateType;
    }

    /** All recognized column names are represented here */
    public enum Column {
        street(Arrays.asList("streetAddress", "street"), Group.address),
        city(Arrays.asList("city"), Group.address),
        state(Arrays.asList("stateProvinceId", "state"), Group.address),
        zip5(Arrays.asList("postalCode", "postal", "zip", "zip5"), Group.address),
        zip4(Arrays.asList("postalCodeSuffix", "postalSuffix", "zip4"), Group.address),

        uspsStreet(Arrays.asList("uspsStreetAddress", "uspsStreet"), Group.validateAddress),
        uspsCity(Arrays.asList("uspsCity"), Group.validateAddress),
        uspsState(Arrays.asList("uspsState"), Group.validateAddress),
        uspsZip5(Arrays.asList("uspsZip5", "uspsPostal", "uspsPostalCode"), Group.validateAddress),
        uspsZip4(Arrays.asList("uspsZip4", "uspsPostalSuffix", "uspsPostalCodeSuffix"), Group.validateAddress),

        lat(Arrays.asList("lat", "geoCode1", "latitude"), Group.geocode, Type.doubleType),
        lon(Arrays.asList("lon", "lng", "geoCode2", "longitude"), Group.geocode, Type.doubleType),
        geoMethod(Arrays.asList("geoMethod", "geoSource"), Group.geocode),
        geoQuality(Arrays.asList("geoQuality", "accuracy"), Group.geocode),

        town(Arrays.asList("town52", "townCode", "town"), Group.district),
        ward(Arrays.asList("ward53", "wardCode", "ward"), Group.district),
        election(Arrays.asList("electionDistrict49", "electionDistrict", "ed", "election"), Group.district),
        congressional(Arrays.asList("congressionalDistrict46", "cd", "congressionalDistrict", "congressional"), Group.district),
        senate(Arrays.asList("nySenateDistrict47", "sd", "senateDistrict", "senate"), Group.district),
        assembly(Arrays.asList("nyAssemblyDistrict48", "ad", "assemblyDistrict", "assembly"), Group.district),
        county(Arrays.asList("county50", "countyCode", "county"), Group.district),
        school(Arrays.asList("schoolDistrict54", "schoolDistrict", "school"), Group.district);

        Type type;
        Group group;
        List<String> aliases;
        Column(List<String> aliases, Group group) {
            this(aliases, group, Type.stringType);
        }
        Column(List<String> aliases, Group group, Type type) {
            this.aliases = aliases;
            this.group = group;
            this.type = type;
        }
        public static Column resolveColumn(String alias)
        {
            for (Column column : Column.values()) {
                if (column.aliases.contains(alias)) {
                    return column;
                }
            }
            return null;
        }
    }

    protected String[] header;
    protected List<Column> columns = new ArrayList<>();

    protected Map<Column, Integer> columnIndexMap = new HashMap<>();

    protected List<CellProcessor> processors = new ArrayList<>();

    public boolean hasAddress()
    {
        return checkColumnsForGroup(Group.address);
    }

    /** Indicates whether the job has USPS address columns to be filled in */
    public boolean requiresAddressValidation()
    {
        return checkColumnsForGroup(Group.validateAddress);
    }

    /** Indicates whether the job has geocoding columns to be filled in */
    public boolean requiresGeocode()
    {
        return checkColumnsForGroup(Group.geocode);
    }

    /** Indicates whether the job has district code columns to be filled in */
    public boolean requiresDistrictAssign()
    {
        return checkColumnsForGroup(Group.district);
    }

    /** Indicates whether the job has any columns to be filled in */
    public boolean requiresAny()
    {
        return requiresAddressValidation() || requiresGeocode() || requiresDistrictAssign();
    }

    /**
     * Returns a lost of district types that match the columns in the header.
     * @return List<DistrictType>
     */
    public List<DistrictType> getRequiredDistrictTypes()
    {
        List<DistrictType> reqTypes = new ArrayList<>();
        for (Column column : columns) {
            if (column.group.equals(Group.district)) {
                try {
                    DistrictType districtType = DistrictType.valueOf(column.name().toUpperCase());
                    if (districtType != null) {
                        reqTypes.add(districtType);
                    }
                }
                catch (IllegalArgumentException ex) {/*ignore*/}
            }
        }
        return reqTypes;
    }

    /**
     * Returns a list of Address objects that are stored as records
     * @return List<Address>
     */
    public List<Address> getAddresses()
    {
        List<Address> addresses = new ArrayList<>();
        for (JobRecord record : getRecords()) {
            addresses.add(record.address);
        }
        return addresses;
    }

    /** Returns true if the Column list contains an element belonging to the given Group */
    private boolean checkColumnsForGroup(Group group)
    {
        for (Column column : columns) { if (column.group.equals(group)) return true; }
        return false;
    }

    /**
     * Returns the modified header created by processHeader
     * @return String[]
     */
    public String[] getHeader() {
        return header;
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
     * @param header
     */
    public String[] processHeader(String[] header)
    {
        if (header != null && header.length > 0) {
            this.header = header.clone();
            for (int i = 0; i < header.length; i++ ) {
                /** Try to match column name to a Column */
                if (header[i] != null && !header[i].isEmpty()) {
                    String columnAlias = FormatUtil.toCamelCase(header[i]);
                    Column headerColumn = Column.resolveColumn(columnAlias);
                    if (headerColumn != null) {

                        /** Record the index for the column */
                        columns.add(headerColumn);
                        columnIndexMap.put(headerColumn, i);

                        /** Tell the processors to use the correct types */
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
        }
        return header;
    }
}
