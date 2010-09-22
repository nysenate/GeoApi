package model.districts;

import model.PersistentObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("assembly")
public class Assembly {
	String district;
	@PersistentObject
	Member member;
	
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
