package gov.nysenate.geocoder.api.exceptions;

public class ApiInternalException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ApiInternalException() {
		super();
	}

	public ApiInternalException(String message, Throwable t) {
		super(message, t);
	}
}
