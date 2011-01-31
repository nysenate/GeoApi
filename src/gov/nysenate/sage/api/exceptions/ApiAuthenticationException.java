package gov.nysenate.sage.api.exceptions;

public class ApiAuthenticationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ApiAuthenticationException() {
		super();
	}

	public ApiAuthenticationException(String message, Throwable t) {
		super(message, t);
	}
}
