package gov.nysenate.sage.model.abstracts;

import gov.nysenate.sage.model.Point;

public abstract interface AbstractGeocoder {
	public Point doParsing(String address);
	public String constructUrl(String address);
}
