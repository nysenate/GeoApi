package model.districts;

import model.PersistentObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("senate")
public class Senate {
	String district;
	String districtUrl;
	@PersistentObject
	Senator senator;
	
	public Senate() {
		
	}
	
	public Senate(String district, String districtUrl, Senator senator) {
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
