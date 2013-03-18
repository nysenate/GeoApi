package gov.nysenate.sage.client.response;

import java.util.List;

public class BatchResponse<T>
{
    protected List<T> results;

    public BatchResponse( List<T> results)
    {
        this.results = results;
    }

    public List<T> getResults() {
        return results;
    }

    public int getTotal() {
        return (this.results != null) ? this.results.size() : 0;
    }
}
