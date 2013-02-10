package gov.nysenate.sage.deprecated.abstracts;

import gov.nysenate.sage.model.geo.Point;

import java.util.List;

@Deprecated
public abstract interface AbstractReverseGeocoder {
	public List<Point> doReverseParsing(String address);
	public String constructReverseUrl(String address);
}
