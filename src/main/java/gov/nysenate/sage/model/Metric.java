package gov.nysenate.sage.model;

import java.util.Date;

public class Metric {
	int userId;
	String command;
	String date;
	String host;
	
	public Metric () {
		this.date = new Date().toString();
	}
	
	public Metric(int userId, String command, String host) {
		this.userId = userId;
		this.command = command;
		this.host = host;
		this.date = new Date().toString();
	}

	public int getUserId() {
		return userId;
	}

	public String getCommand() {
		return command;
	}

	public String getDate() {
		return date;
	}
	public String getHost() {
		return host;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setDate(String date) {
		this.date = date;
	}
	public void setHost(String host) {
		this.host = host;
	}
}
