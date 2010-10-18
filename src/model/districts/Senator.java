package model.districts;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import model.annotations.ForeignKey;
import model.annotations.ListType;
import model.annotations.PersistentObject;
import model.annotations.PrimaryKey;

public class Senator {
	@Expose String name;
	@Expose @PrimaryKey String contact;
	@Expose String url;
	@Expose String imageUrl;
	
	@Expose @PersistentObject Social social;
	@Expose @PersistentObject @ListType(Office.class) ArrayList<Office> offices;
	
	@XStreamOmitField @ForeignKey(Senate.class) String district;

	public Senator() {
		offices = new ArrayList<Office>();
	}

	public Senator(String name, String contact, String url, String imageUrl,
			Social social, ArrayList<Office> offices, String district) {
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

	public ArrayList<Office> getOffices() {
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

	public void setOffices(ArrayList<Office> offices) {
		this.offices = offices;
	}
	
	public void addOffices(Office office) {
		offices.add(office);
	}
	
	public void setDistrict(String district) {
		this.district = district;
	}
	
	
}
