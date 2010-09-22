package generated.geoserver.json;

import java.util.Collection;

public class GeoGeometry {
	String type;
	Collection<Collection<Collection<Collection<Double>>>> coordinates;
	
	public GeoGeometry() {
		
	}
	
	public String getType() {
		return type;
	}
	public Collection<Collection<Collection<Collection<Double>>>> getCoordinates() {
		return coordinates;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	public void setCoordinates(Collection<Collection<Collection<Collection<Double>>>> coordinates) 
	{
		this.coordinates = coordinates;
	}
}
