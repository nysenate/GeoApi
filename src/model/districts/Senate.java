package model.districts;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import model.annotations.PersistentObject;
import model.annotations.PrimaryKey;

@XStreamAlias("senate")
public class Senate {
	@Expose @PrimaryKey String district;
	@Expose String districtUrl;
	
	@Expose @PersistentObject Senator senator;

	public Senate() {
		
	}
	
	public Senate(String district, String districtUrl, Senator senator) {
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
