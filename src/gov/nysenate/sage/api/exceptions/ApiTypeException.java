package gov.nysenate.sage.api.exceptions;

public class ApiTypeException extends Exception {
	private static final long serialVersionUID = 1L;

	public ApiTypeException(String message) {
		super(message);
	}

	public ApiTypeException(String message, Throwable t) {
		super(message, t);
	}
}