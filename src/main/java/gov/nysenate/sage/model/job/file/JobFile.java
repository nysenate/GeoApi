package gov.nysenate.sage.model.job.file;

import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.ArrayList;
import java.util.List;

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
        street(Group.address),
        city(Group.address),
        state(Group.address),
        zip5(Group.address),
        zip4(Group.address),

        streetNumber(Type.intType, Group.address),
        streetName(Group.address),
        streetUnit(Group.address),

        geocode1(Type.doubleType, Group.geocode),
        geocode2(Type.doubleType, Group.geocode),
        lat(Type.doubleType, Group.geocode),
        lon(Type.doubleType, Group.geocode),
        geoMethod(Group.geocode),
        geoQuality(Group.geocode),

        town(Group.district),
        ward(Group.district),
        election(Group.district),
        congressional(Group.district),
        senate(Group.district),
        assembly(Group.district),
        county(Group.district),
        school(Group.district);

        Type type;
        Group group;
        Column(Group group) {
            this.type = Type.stringType;
            this.group = group;
        }
        Column(Type type, Group group) {
            this.type = type;
            this.group = group;
        }
    }

    protected String[] header;
    protected List<Column> columns = new ArrayList<>();
    protected List<CellProcessor> processors = new ArrayList<>();

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
     * Given a header (array of column names) create a custom cell processor and header
     * @param header
     */
    public String[] processHeader(String[] header)
    {
        this.header = header.clone();
        for (int i = 0; i < header.length; i++ ) {
            /** Try to match column name to a Column */
            try {
                Column headerColumn = Column.valueOf(FormatUtil.toCamelCase(header[i]));
                columns.add(headerColumn);

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
            /** Unmatched column headers are ignored */
            catch (IllegalArgumentException ex) {
                this.header[i] = null;
                this.processors.add(null);
            }
        }
        return this.header;
    }
}
