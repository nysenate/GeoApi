package gov.nysenate.sage.model.job.file;

import java.util.ArrayList;
import java.util.List;

public class BatchJobFile<T>
{
    protected List<T> records = new ArrayList<>();

    public void addRecord(T record) {
        this.records.add(record);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
