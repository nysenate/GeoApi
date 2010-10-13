package v2.connect;

import com.google.gson.annotations.Expose;

import model.Ignore;
import model.PersistentObject;

public class SenateDistrict {
	@Expose @PrimaryKey String district;
	@Expose String url;
	
	@Expose @PersistentObject Senator senator;

	public SenateDistrict(String district, String url, Senator senator) {
		super();
		this.district = district;
		this.url = url;
		this.senator = senator;
	}

	public String getDistrict() {
		return district;
	}

	public String getUrl() {
		return url;
	}

	public Senator getSenator() {
		return senator;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setSenator(Senator senator) {
		this.senator = senator;
	}
	
	
}
