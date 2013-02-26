package gov.nysenate.sage.deprecated.methods.api.exceptions;

public class ApiTypeException extends ApiException {
	private static final long serialVersionUID = 1L;

	public ApiTypeException(String message) {
		super(message);
	}

	public ApiTypeException(String message, Throwable t) {
		super(message, t);
	}
}