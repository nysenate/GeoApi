package model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("error")
public class ErrorResponse {
	public String message;

	public ErrorResponse(String message) {
		this.message = message;
	}
}
