package gov.nysenate.sage.model.abstracts;

import gov.nysenate.sage.model.Point;

import java.util.List;

public abstract interface AbstractReverseGeocoder {
	public List<Point> doReverseParsing(String address);
	public String constructReverseUrl(String address);
}
