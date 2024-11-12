package gov.nysenate.sage.client.response.base;

import gov.nysenate.sage.model.result.ResultStatus;

// TODO: may need to adjust front-end for this
public class GenericResponse extends BaseResponse {
    public GenericResponse(boolean success, String message) {
        super(success ? ResultStatus.SUCCESS : ResultStatus.GENERAL_FAILURE);
        messages.add(message);
    }
}
