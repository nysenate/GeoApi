package gov.nysenate.sage.model.abstracts;

import gov.nysenate.sage.model.geo.Point;

@Deprecated
public abstract interface AbstractGeocoder {
	public Point doParsing(String address);
	public Point doParsing(String addr, String city, String state, String zip4, String zip5);
	public String constructUrl(String address);
}
