package gov.nysenate.geocoder.api.exceptions;

public class ApiException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public ApiException() {
		super();
	}
	
	public ApiException(String message) {
		super(message);
	}

	public ApiException(String message, Throwable t) {
		super(message, t);
	}
}
