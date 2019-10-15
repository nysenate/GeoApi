package gov.nysenate.sage.model.api;

import java.io.Serializable;

/**
 * Model class for representing a user and their assigned key.
 */
public class ApiUser implements Serializable
{
	private int id;
	private String apiKey;
	private String name;
	private String description;
	private boolean admin;
    private int monthlyRequestCap;

	public ApiUser() {}

	public ApiUser(String apiKey, String name, String description, boolean admin)
    {
		this.apiKey = apiKey;
		this.name = name;
		this.description = description;
		this.admin = admin;
	}

    public ApiUser(int id, String apiKey, String name, String description, boolean admin)
    {
        this(apiKey, name, description, admin);
        this.id = id;
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

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public int getMonthlyRequestCap() {
        return monthlyRequestCap;
    }

    public void setMonthlyRequestCap(int monthlyRequestCap) {
        this.monthlyRequestCap = monthlyRequestCap;
    }
}
