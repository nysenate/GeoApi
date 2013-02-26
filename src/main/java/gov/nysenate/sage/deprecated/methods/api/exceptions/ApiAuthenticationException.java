package gov.nysenate.sage.deprecated.methods.api.exceptions;

public class ApiAuthenticationException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ApiAuthenticationException() {
		super();
	}

	public ApiAuthenticationException(String message, Throwable t) {
		super(message, t);
	}
}
