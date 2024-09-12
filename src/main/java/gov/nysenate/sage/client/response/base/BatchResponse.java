package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.util.NonnullList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public class BatchResponse<T> {
    @Nonnull
    private final List<T> results;

    public <I> BatchResponse(List<I> inputList, Function<I, T> mapper) {
        this.results = NonnullList.of(inputList).stream().map(mapper).toList();
    }

    @Nonnull
    public List<T> getResults() {
        return results;
    }

    public int getTotal() {
        return this.results.size();
    }
}
