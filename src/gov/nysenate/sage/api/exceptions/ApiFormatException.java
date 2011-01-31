package gov.nysenate.sage.api.exceptions;

public class ApiFormatException extends Exception {
	private static final long serialVersionUID = 1L;

	public ApiFormatException(String message) {
		super(message);
	}

	public ApiFormatException(String message, Throwable t) {
		super(message, t);
	}
}