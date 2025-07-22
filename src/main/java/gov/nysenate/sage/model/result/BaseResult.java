package gov.nysenate.sage.model.result;

import gov.nysenate.sage.provider.geocode.DataSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Serves as a base class to provide common fields to sub-classed results. Result objects are
 * typically returned by the service layer classes and are used to wrap a data model object with
 * some status information.
 */
public abstract class BaseResult<S extends DataSource> {
    // A null source means the service was never actually hit for a result.
    private final LinkedHashSet<S> sources;
    protected List<String> messages = new ArrayList<>();
    protected ResultStatus statusCode = ResultStatus.SUCCESS;

    protected BaseResult(S source) {
        this.sources = source == null ? null : new LinkedHashSet<>(Set.of(source));
    }

    protected BaseResult(LinkedHashSet<S> sources) {
        this.sources = sources;
    }

    public LinkedHashSet<S> getSources() {
        return sources;
    }

    public ResultStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(ResultStatus statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return statusCode == ResultStatus.SUCCESS;
    }
}
