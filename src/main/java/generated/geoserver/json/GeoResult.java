package generated.geoserver.json;

import java.util.List;

public class GeoResult {
	String type;
	List<GeoFeatures> features;
	
	public void setType(String type) {
		this.type = type;
	}
	public void setFeatures(List<GeoFeatures> features) {
		this.features = features;
	}
	
	public String getType() {
		return type;
	}
	public List<GeoFeatures> getFeatures() {
		return features;
	}
}
