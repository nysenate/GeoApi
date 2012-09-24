package gov.nysenate.sage.model.BulkProcessing;

public class BlueBirdTsv implements BulkInterface {
	String id;
	String first_name;
	String middle_name;
	String last_name;
	String prefix_id;
	String suffix_id;
	String full_name;
	String street_number;
	String street_name;
	String street_unit;
	String street_address;
	String supplemental_address_1;
	String supplemental_address_2;
	String city;
	String state_province_id;
	String postal_code;
	String postal_code_suffix;
	String country_id;
	String birth_date;
	String gender_id;
	String phone;
	String town_52;
	String ward_53;
	String election_district_49;
	String congressional_district_46;
	String ny_senate_district_47;
	String ny_assembly_district_48;
	String school_district_54;
	String county_50;
	String email;
	String boe_date_of_registration_24;
	String current_employer;
	String job_title;
	String note;
	String keywords;
	String geo_code_1;
	String geo_code_2;
	String is_deleted;
	String address_id;
	String email_id;
	String phone_id;
	String districtinfo_id;
	String constinfo_id;
	String location_type_id;
	String address_is_primary;

	public BlueBirdTsv() {
		id="";
		first_name="";
		middle_name="";
		last_name="";
		prefix_id="";
		suffix_id="";
		full_name = "";
		street_number="";
		street_name="";
		street_unit="";
		supplemental_address_1="";
		supplemental_address_2="";
		city="";
		state_province_id="";
		postal_code="";
		postal_code_suffix="";
		country_id="";
		birth_date="";
		gender_id="";
		phone="";
		town_52="";
		ward_53="";
		election_district_49="";
		congressional_district_46="";
		ny_senate_district_47="";
		ny_assembly_district_48="";
		school_district_54="";
		county_50="";
		email="";
		boe_date_of_registration_24="";
		current_employer="";
		job_title="";
		note="";
		keywords="";
		geo_code_1="";
		geo_code_2="";
		is_deleted="";
		address_id="";
		email_id="";
		phone_id="";
		districtinfo_id="";
		constinfo_id="";
		location_type_id="";
		address_is_primary="";
	}

	public String getId() {
		return id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public String getMiddle_name() {
		return middle_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public String getPrefix_id() {
		return prefix_id;
	}

	public String getSuffix_id() {
		return suffix_id;
	}
	
	public String getFull_name() {
		return full_name;
	}

	public String getStreet_number() {
		return street_number;
	}

	public String getStreet_name() {
		return street_name;
	}

	public String getStreet_unit() {
		return street_unit;
	}
	
	public String getStreet_address(){
		return street_address;
	}
	
	public String getSupplemental_address_1() {
		return supplemental_address_1;
	}

	public String getSupplemental_address_2() {
		return supplemental_address_2;
	}

	@Override
	public String getCity() {
		return city;
	}

	public String getState_province_id() {
		return state_province_id;
	}

	public String getPostal_code() {
		return postal_code;
	}

	public String getPostal_code_suffix() {
		return postal_code_suffix;
	}

	public String getCountry_id() {
		return country_id;
	}

	public String getBirth_date() {
		return birth_date;
	}

	public String getGender_id() {
		return gender_id;
	}

	public String getPhone() {
		return phone;
	}

	public String getTown_52() {
		return town_52;
	}

	public String getWard_53() {
		return ward_53;
	}

	public String getElection_district_49() {
		return election_district_49;
	}

	public String getCongressional_district_46() {
		return congressional_district_46;
	}

	public String getNy_senate_district_47() {
		return ny_senate_district_47;
	}

	public String getNy_assembly_district_48() {
		return ny_assembly_district_48;
	}

	public String getSchool_district_54() {
		return school_district_54;
	}

	public String getCounty_50() {
		return county_50;
	}

	public String getEmail() {
		return email;
	}

	public String getBoe_date_of_registration_24() {
		return boe_date_of_registration_24;
	}

	public String getCurrent_employer() {
		return current_employer;
	}

	public String getJob_title() {
		return job_title;
	}

	public String getNote() {
		return note;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getGeo_code_1() {
		return geo_code_1;
	}

	public String getGeo_code_2() {
		return geo_code_2;
	}

	public String getIs_deleted() {
		return is_deleted;
	}

	public String getAddress_id() {
		return address_id;
	}

	public String getEmail_id() {
		return email_id;
	}

	public String getPhone_id() {
		return phone_id;
	}

	public String getDistrictinfo_id() {
		return districtinfo_id;
	}

	public String getConstinfo_id() {
		return constinfo_id;
	}

	public String getLocation_type_id() {
		return location_type_id;
	}
	
	public String getAddress_is_primary() {
		return address_is_primary;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setFirst_name(String firstName) {
		first_name = firstName;
	}

	public void setMiddle_name(String middleName) {
		middle_name = middleName;
	}

	public void setLast_name(String lastName) {
		last_name = lastName;
	}

	public void setPrefix_id(String prefixId) {
		prefix_id = prefixId;
	}

	public void setSuffix_id(String suffixId) {
		suffix_id = suffixId;
	}
	
	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public void setStreet_number(String streetNumber) {
		street_number = streetNumber;
	}

	public void setStreet_name(String streetName) {
		street_name = streetName;
	}

	public void setStreet_unit(String streetUnit) {
		street_unit = streetUnit;
	}
	
	public void setStreet_address(String street_address){
		this.street_address = street_address;
	}
	
	public void setSupplemental_address_1(String supplementalAddress_1) {
		supplemental_address_1 = supplementalAddress_1;
	}

	public void setSupplemental_address_2(String supplementalAddress_2) {
		supplemental_address_2 = supplementalAddress_2;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setState_province_id(String stateProvinceId) {
		state_province_id = stateProvinceId;
	}

	public void setPostal_code(String postalCode) {
		postal_code = postalCode;
	}

	public void setPostal_code_suffix(String postalCodeSuffix) {
		postal_code_suffix = postalCodeSuffix;
	}

	public void setCountry_id(String countryId) {
		country_id = countryId;
	}

	public void setBirth_date(String birthDate) {
		birth_date = birthDate;
	}

	public void setGender_id(String genderId) {
		gender_id = genderId;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setTown_52(String town_52) {
		this.town_52 = town_52;
	}

	public void setWard_53(String ward_53) {
		this.ward_53 = ward_53;
	}

	public void setElection_district_49(String electionDistrict_49) {
		election_district_49 = electionDistrict_49;
	}

	public void setCongressional_district_46(String congressionalDistrict_46) {
		congressional_district_46 = congressionalDistrict_46;
	}

	public void setNy_senate_district_47(String nySenateDistrict_47) {
		ny_senate_district_47 = nySenateDistrict_47;
	}

	public void setNy_assembly_district_48(String nyAssemblyDistrict_48) {
		ny_assembly_district_48 = nyAssemblyDistrict_48;
	}

	public void setSchool_district_54(String schoolDistrict_54) {
		school_district_54 = schoolDistrict_54;
	}

	public void setCounty_50(String county_50) {
		this.county_50 = county_50;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setBoe_date_of_registration_24(String boeDateOfRegistration_24) {
		boe_date_of_registration_24 = boeDateOfRegistration_24;
	}

	public void setCurrent_employer(String currentEmployer) {
		current_employer = currentEmployer;
	}

	public void setJob_title(String jobTitle) {
		job_title = jobTitle;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public void setGeo_code_1(String geoCode_1) {
		geo_code_1 = geoCode_1;
	}

	public void setGeo_code_2(String geoCode_2) {
		geo_code_2 = geoCode_2;
	}

	public void setIs_deleted(String isDeleted) {
		is_deleted = isDeleted;
	}

	public void setAddress_id(String addressId) {
		address_id = addressId;
	}

	public void setEmail_id(String emailId) {
		email_id = emailId;
	}

	public void setPhone_id(String phoneId) {
		phone_id = phoneId;
	}

	public void setDistrictinfo_id(String districtinfoId) {
		districtinfo_id = districtinfoId;
	}

	public void setConstinfo_id(String constinfoId) {
		constinfo_id = constinfoId;
	}

	public void setLocation_type_id(String locationTypeId) {
		location_type_id = locationTypeId;
	}
	
	public void setAddress_is_primary(String address_is_primary) {
		this.address_is_primary = address_is_primary;
	}
	
	@Override
	public String toString() {
		return 	(id + "\t" + first_name + "\t" + middle_name + "\t" + last_name 
				+ "\t" + prefix_id + "\t" + suffix_id + "\t" + full_name + "\t" + street_number 
				+ "\t" + street_name + "\t" + street_unit + "\t" + street_address + "\t" 
				+ supplemental_address_1 + "\t" + supplemental_address_2 
				+ "\t" + city + "\t" + state_province_id + "\t" + postal_code 
				+ "\t" + postal_code_suffix + "\t" + country_id + "\t" 
				+ birth_date + "\t" + gender_id + "\t" + phone + "\t" + town_52 
				+ "\t" + ward_53 + "\t" + election_district_49 + "\t" 
				+ congressional_district_46 + "\t" + ny_senate_district_47 
				+ "\t" + ny_assembly_district_48 + "\t" + school_district_54 
				+ "\t" + county_50 + "\t" + email + "\t" + boe_date_of_registration_24 
				+ "\t" + current_employer + "\t" + job_title + "\t" + note + "\t" 
				+ keywords + "\t" + geo_code_1 + "\t" + geo_code_2 + "\t" + is_deleted 
				+ "\t" + address_id + "\t" + email_id + "\t" + phone_id + "\t" 
				+ districtinfo_id + "\t" + constinfo_id + "\t" + location_type_id + "\t" 
				+ address_is_primary).replaceAll("\t \t", "\t\t");
	}
	
	@Override
	public String getAddress() {
		return this.getStreet() + ", " + this.getCity() + ", " + this.getState() + " " + this.getZip5();
	}

	@Override
	public String getState() {
		return this.getState_province_id();
	}

	//TODO
	@Override
	public String getStreet() {
		if(!this.getStreet_name().matches("\\s*")) {
			if(!this.getStreet_number().matches("\\s*")) {
				return this.getStreet_number() + " " + this.getStreet_name();
			}
			return this.getStreet_name();
		}
		return this.getStreet_address();
	}

	@Override
	public String getZip5() {
		return this.getPostal_code();
	}

	@Override
	public void setAD(String ad) {
		this.setNy_assembly_district_48(ad);
	}

	@Override
	public void setCD(String cd) {
		this.setCongressional_district_46(cd);
	}

	@Override
	public void setCounty(String county) {
		this.setCounty_50(county);
	}

	@Override
	public void setED(String ed) {
		this.setElection_district_49(ed);
	}

	@Override
    public void setTown(String town) {
	    this.setTown_52(town);
	}

	@Override
    public void setSchool(String school) {
	    this.setSchool_district_54(school);
	}

	@Override
	public void setLat(String lat) {
		this.setGeo_code_1(lat);
	}

	@Override
	public void setLon(String lon) {
		this.setGeo_code_2(lon);
	}

	@Override
	public void setSD(String sd) {
		this.setNy_senate_district_47(sd);
	}
}
