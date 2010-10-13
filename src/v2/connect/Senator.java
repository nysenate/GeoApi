package v2.connect;

import java.util.List;

import com.google.gson.annotations.Expose;

import model.Ignore;
import model.PersistentObject;

public class Senator {
	@Expose String name;
	@Expose @PrimaryKey String contact;
	@Expose String url;
	@Expose String imageUrl;
	
	@Expose @PersistentObject Social social;
	@Expose @PersistentObject List<Office> offices;
	
	@Ignore @ForeignKey(SenateDistrict.class) String district;

	public Senator(String name, String contact, String url, String imageUrl,
			Social social, List<Office> offices, String district) {
		super();
		this.name = name;
		this.contact = contact;
		this.url = url;
		this.imageUrl = imageUrl;
		this.social = social;
		this.offices = offices;
		this.district = district;
	}

	public String getName() {
		return name;
	}

	public String getContact() {
		return contact;
	}

	public String getUrl() {
		return url;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public Social getSocial() {
		return social;
	}

	public List<Office> getOffice() {
		return offices;
	}

	public String getDistrict() {
		return district;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setSocial(Social social) {
		this.social = social;
	}

	public void setOffice(List<Office> offices) {
		this.offices = offices;
	}

	public void setDistrict(String district) {
		this.district = district;
	}
	
	
}
