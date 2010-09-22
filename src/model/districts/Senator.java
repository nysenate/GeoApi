package model.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("senator")
public class Senator {
	String name;
	String contact;
	String url;
	
	public Senator() {
		
	}
	
	public Senator(String name, String contact, String url) {
		this.name = name;
		this.contact = contact;
		this.url = url;
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

	public void setName(String name) {
		this.name = name;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
