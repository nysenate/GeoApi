package gov.nysenate.sage.model;

import gov.nysenate.sage.Response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("error")
public class ErrorResponse extends Response {
	public String message;

	public ErrorResponse(String message) {
		this.message = message;
	}
}
