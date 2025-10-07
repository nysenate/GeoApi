package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.ResultStatus;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class SourcedResponse extends BaseResponse {
    private LinkedHashSet<?> sources = null;

    public SourcedResponse(BaseResult<?> baseResult) {
        super(baseResult == null ? ResultStatus.RESPONSE_ERROR : baseResult.getStatusCode());
        if (baseResult != null) {
            this.sources = baseResult.getSources();
        }
    }

    public String getSources() {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
