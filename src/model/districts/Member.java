package model.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("member")
public class Member {
	String name;
	String url;
	
	public Member() {
		
	}
	
	public Member(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
