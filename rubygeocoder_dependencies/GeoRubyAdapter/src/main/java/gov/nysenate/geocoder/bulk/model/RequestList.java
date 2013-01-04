package gov.nysenate.geocoder.bulk.model;


import java.util.ArrayList;

public class RequestList {
	
	private ArrayList<RequestObject> requestObjects;
	
	public RequestList() {
		requestObjects = new ArrayList<RequestObject>();
	}
	
	public RequestList(ArrayList<RequestObject> requestObjects) {
		this.requestObjects = requestObjects;
	}

	public ArrayList<RequestObject> getRequestObjects() {
		return requestObjects;
	}

	public void setRequestObjects(ArrayList<RequestObject> requestObjects) {
		this.requestObjects = requestObjects;
	}
}
