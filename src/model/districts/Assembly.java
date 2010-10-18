package model.districts;


import model.annotations.PersistentObject;
import model.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("assembly")
public class Assembly {
	@Expose @PrimaryKey String district;
	@Expose @PersistentObject Member member;
	
	public Assembly() {
		
	}
	
	public Assembly(String district, Member member) {
		this.district = district;
		this.member = member;
	}

	public String getDistrict() {
		return district;
	}

	public Member getMember() {
		return member;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public void setMember(Member member) {
		this.member = member;
	}
}
