package gov.nysenate.sage.model.abstracts;

import gov.nysenate.sage.model.Point;

public abstract interface AbstractGeocoder {
	public Point doParsing(String address);
	public Point doParsing(String addr, String city, String state, String zip4, String zip5);
	public String constructUrl(String address);
}
