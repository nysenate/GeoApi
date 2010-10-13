package model.districts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("senator")
public class Senator {
	@Expose	String name;
	@Expose	String contact;
	@Expose	String url;
	@Expose	String imageUrl;
	
	@XStreamOmitField String district;
	
	public static void main(String[] args) {
		Senator s = new Senator("jared","williams@nysenate.gov","jaredwilliams.net");
		s.imageUrl = "jared.gif";
		
		Gson g1 = new Gson();
				
		Gson g2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		
		System.out.println(g1.toJson(s));
		
		System.out.println(g2.toJson(s));
		
	}
	
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
	
	public String getImageUrl() {
		return imageUrl;
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
}
