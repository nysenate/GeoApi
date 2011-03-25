package gov.nysenate.sage.util;

import gov.nysenate.sage.model.BulkProcessing.Boe3rdTsv;
import gov.nysenate.sage.model.BulkProcessing.BulkInterface;

public enum BulkFileType {
	BOE3RDTSV(Boe3rdTsv.class,"3rd Party TSV","id	first_name	middle_name	last_name	suffix_id	" +
    		"street_number	street_name	street_unit	supplemental_address_1	" +
    		"supplemental_address_2	city	state_province_id	postal_code	" +
    		"postal_code_suffix	birth_date	gender_id	phone	town_52	ward_53	" +
    		"election_district_49	congressional_district_46	ny_senate_district_47	" +
    		"ny_assembly_district_48	school_district_54	county_50	email	" +
    		"location_type_id	is_deleted	address_id	districtinfo_id	phone_id	email_id","\t");
	
	private String type;
	private Class<? extends BulkInterface> clazz;
	private String header;
	private String delimiter;
	
	private BulkFileType(Class<? extends BulkInterface> clazz, String type, String header, String delimiter) {
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


