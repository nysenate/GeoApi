package gov.nysenate.sage.model.job;

public enum JobFileType
{
	BLUEBIRDTSV(BlueBirdTsv.class, "BlueBird TSV", "id	first_name	middle_name	last_name	" +
			"prefix_id	suffix_id	full_name	street_number	street_name	street_unit	street_address	supplemental_address_1	" +
			"supplemental_address_2	city	state_province_id	postal_code	postal_code_suffix	" +
			"country_id	birth_date	gender_id	phone	town_52	ward_53	election_district_49	" +
			"congressional_district_46	ny_senate_district_47	ny_assembly_district_48	school_district_54	" +
			"county_50	email	boe_date_of_registration_24	current_employer	job_title	note	" +
			"keywords	geo_code_1	geo_code_2	is_deleted	address_id	email_id	phone_id	" +
			"districtinfo_id	constinfo_id	location_type_id	address_is_primary", "\t");
	
	private String type;
	private Class<? extends BulkInterface> clazz;
	private String header;
	private String delimiter;
	
	private JobFileType(Class<? extends BulkInterface> clazz, String type, String header, String delimiter) {
		this.type = type;
		this.clazz = clazz;
		this.header = header;
		this.delimiter = delimiter;
	}
	
	public String type() {
		return type;
	}
	
	public Class<? extends BulkInterface> clazz() {
		return clazz;
	}
	
	public String header() {
		return header;
	}
	
	public String delimiter() {
		return delimiter;
	}
}