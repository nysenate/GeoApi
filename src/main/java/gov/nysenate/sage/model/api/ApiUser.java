package gov.nysenate.sage.model.api;

import java.io.Serializable;

/**
 * Model class for representing a user and their assigned key.
 */
public class ApiUser implements Serializable {
	private final String apiKey;
	private final String name;
	private final String description;
	private final boolean admin;
	private int id;

	public ApiUser(String apiKey, String name, String description, boolean admin) {
		this.apiKey = apiKey;
		this.name = name;
		this.description = description;
		this.admin = admin;
	}

	public int getId() {
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

	public void setId(int id) {
		this.id = id;
	}

	public boolean isAdmin() {
		return admin;
	}
}
