package gov.nysenate.sage.model;

import gov.nysenate.sage.model.annotations.Ignore;

public class ApiUser {
	@Ignore Integer id;
	String apiKey;
	String name;
	String description;
	
	public ApiUser() {
		
	}

	public ApiUser(String apiKey, String name, String description) {
		this.apiKey = apiKey;
		this.name = name;
		this.description = description;
	}

	public Integer getId() {
		return id;
	}
	
	public String getApiKey() {
		return apiKey;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
