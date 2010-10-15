package v2.connect;

import com.google.gson.annotations.Expose;

import model.PersistentObject;

public class SenateDistrict {
	@Expose @PrimaryKey String district;
	@Expose String districtUrl;
	
	@Expose @PersistentObject Senator senator;

	public SenateDistrict() {
		
	}
	
	public SenateDistrict(String district, String districtUrl, Senator senator) {
		super();
		this.district = district;
		this.districtUrl = districtUrl;
		this.senator = senator;
	}

	public String getDistrict() {
		return district;
	}

	public String getDistrictUrl() {
		return districtUrl;
	}

	public Senator getSenator() {
		return senator;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public void setDistrictUrl(String districtUrl) {
		this.districtUrl = districtUrl;
	}

	public void setSenator(Senator senator) {
		this.senator = senator;
	}
	
	
}
