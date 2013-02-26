package gov.nysenate.sage.deprecated.methods.api.exceptions;

public class ApiInternalException extends ApiException {
	private static final long serialVersionUID = 1L;

    public ApiInternalException() {
        super();
    }
    public ApiInternalException(String message) {
        super(message);
    }
    public ApiInternalException(String message, Throwable t) {
        super(message, t);
    }

    public ApiInternalException(Throwable t) {
        super(t.getMessage(),t);
    }
}
