package model.abstracts;

import model.Point;

public abstract interface AbstractGeocoder {
	public Point doParsing(String address);
	public String constructUrl(String address);
}
