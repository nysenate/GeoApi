package gov.nysenate.sage.model.districts;

import gov.nysenate.sage.model.annotations.Ignore;
import gov.nysenate.sage.model.annotations.PersistentObject;
import gov.nysenate.sage.model.annotations.PrimaryKey;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("senate")
public class Senate {
	@Expose @PrimaryKey String district;
	@Expose String districtUrl;
	
	@Expose @PersistentObject Senator senator;
	
	@Expose @Ignore List<Senate> nearbyDistricts;

	public Senate() {
		
	}
	
	public Senate(String district) {
		this.district = district;
	}
	
	public Senate(String district, String districtUrl, Senator senator) {
		super();
		this.district = district;
		this.districtUrl = districtUrl;
		this.senator = senator;
	}
	
	public Senate(String district, String districtUrl, Senator senator, List<Senate> nearbyDistricts) {
		super();
		this.district = district;
		this.districtUrl = districtUrl;
		this.senator = senator;
		this.nearbyDistricts = nearbyDistricts;
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
	
	public List<Senate> getNearbyDistricts() {
		return nearbyDistricts;
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
	
	public void setNearbyDistricts(List<Senate> nearbyDistricts) {
		this.nearbyDistricts = nearbyDistricts;
	}
}
