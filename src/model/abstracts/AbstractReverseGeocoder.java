package model.abstracts;

import java.util.List;
import model.Point;

public abstract interface AbstractReverseGeocoder {
	public List<Point> doReverseParsing(String address);
	public String constructReverseUrl(String address);
}
