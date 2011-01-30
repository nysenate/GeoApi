package generated.geoserver.json;

public class GeoFeatures {
	String type;
	String id;
	GeoGeometry geometry;
	GeoProperty properties;
	
	public void setType(String type) {
		this.type = type;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setGeometry(GeoGeometry geometry) {
		this.geometry = geometry;
	}
	public void setProperties(GeoProperty properties) {
		this.properties = properties;
	}
	
	public String getType() {
		return type;
	}
	public String getId() {
		return id;
	}
	public GeoGeometry getGeometry() {
		return geometry;
	}
	public GeoProperty getProperties() {
		return properties;
	}
}
