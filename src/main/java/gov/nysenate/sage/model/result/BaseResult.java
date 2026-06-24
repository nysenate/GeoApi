package gov.nysenate.sage.model.result;

import gov.nysenate.sage.provider.geocode.DataSource;

import java.util.*;

/**
 * Serves as a base class to provide common fields to sub-classed results. Result objects are
 * typically returned by the service layer classes and are used to wrap a data model object with
 * some status information.
 */
public abstract class BaseResult<S extends DataSource> {
    // A null source means the service was never actually hit for a result.
    private final LinkedHashSet<S> sources;
    private final ResultStatus statusCode;

    protected BaseResult(S source) {
        this(source, ResultStatus.SUCCESS);
    }

    protected BaseResult(S source, ResultStatus status) {
        this.sources = source == null ? null : new LinkedHashSet<>(Set.of(source));
        this.statusCode = status;
    }

    protected BaseResult(List<S> sources, ResultStatus status) {
        this.sources = sources == null  || sources.isEmpty() ? null : new LinkedHashSet<>(sources);
        this.statusCode = status;
    }

    public LinkedHashSet<S> getSources() {
        return sources;
    }

    public ResultStatus getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return statusCode == ResultStatus.SUCCESS;
    }
}
