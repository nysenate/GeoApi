package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.FormatUtil;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.*;

public class JobFile {
    private final List<Column> columns = new ArrayList<>();
    private final Map<Column, Integer> columnIndexMap = new HashMap<>();
    private final List<CellProcessor> processors = new ArrayList<>();
    private final List<JobRecord> records = new ArrayList<>();

    /**
     * Given a header (array of column names), create a custom cell processor to properly parse values.
     */
    public JobFile(String[] header) {
        if (header == null) {
            return;
        }
        for (int i = 0; i < header.length; i++) {
            Optional toAdd = null;
            // Try to match column name to a Column
            String columnAlias = FormatUtil.toCamelCase(header[i]);
            Column headerColumn = Column.resolveColumn(columnAlias);
            if (headerColumn != null) {
                // Record the index for the column
                columns.add(headerColumn);
                columnIndexMap.put(headerColumn, i);

                // Tell the processors to use the correct types
                if (headerColumn.type() ==Column.Type.doubleType) {
                    toAdd = new Optional(new ParseBigDecimal());
                }
                else if (headerColumn.type() == Column.Type.intType) {
                    toAdd = new Optional(new ParseInt());
                }
            }
            processors.add(toAdd);
        }
    }

    public void addRecord(JobRecord record) {
        records.add(record);
    }

    public List<JobRecord> getRecords() {
        return records;
    }

    public int recordCount() {
        return records.size();
    }

    public boolean hasAddress() {
        return checkColumnsForGroup(Column.Group.address);
    }

    /** Indicates whether the job has USPS address columns to be filled in */
    public boolean requiresAddressValidation() {
        return checkColumnsForGroup(Column.Group.validateAddress);
    }

    /** Indicates whether the job has geocoding columns to be filled in */
    public boolean requiresGeocode() {
        return checkColumnsForGroup(Column.Group.geocode);
    }

    /** Indicates whether the job has district code columns to be filled in */
    public boolean requiresDistrictAssign() {
        return checkColumnsForGroup(Column.Group.district);
    }

    /**
     * Returns a lost of district types that match the columns in the header.
     * @return List<DistrictType>
     */
    public Set<DistrictType> getRequiredDistrictTypes() {
        Set<DistrictType> reqTypes = new HashSet<>();
        for (Column column : columns) {
            if (column.group() == Column.Group.district) {
                try {
                    reqTypes.add(DistrictType.valueOf(column.name().toUpperCase()));
                }
                catch (IllegalArgumentException ignored) {}
            }
        }
        return reqTypes;
    }

    /** Returns true if the Column list contains an element belonging to the given Group */
    private boolean checkColumnsForGroup(Column.Group group) {
        for (Column column : columns) {
            if (column.group() == group) {
                return true;
            }
        }
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
}
