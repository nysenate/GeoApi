package gov.nysenate.sage.api.exceptions;

public class ApiCommandException extends ApiException {
	private static final long serialVersionUID = 1L;

	public ApiCommandException(String message) {
		super(message);
	}

	public ApiCommandException(String message, Throwable t) {
		super(message, t);
	}
}