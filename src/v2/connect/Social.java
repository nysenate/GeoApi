package v2.connect;

import com.google.gson.annotations.Expose;

public class Social {
	@ForeignKey(Senator.class) String contact;
	@Expose String faceBook;
	@Expose String twitter;
	@Expose String youtube;
	@Expose String flickr;
	@Expose String rss;
	
	public Social() {
		
	}
	
	public Social(String contact, String faceBook, String twitter,
			String youtube, String flickr, String rss) {
		this.contact = contact;
		this.faceBook = faceBook;
		this.twitter = twitter;
		this.youtube = youtube;
		this.flickr = flickr;
		this.rss = rss;
	}

	public String getContact() {
		return contact;
	}

	public String getFaceBook() {
		return faceBook;
	}

	public String getTwitter() {
		return twitter;
	}

	public String getYoutube() {
		return youtube;
	}

	public String getFlickr() {
		return flickr;
	}

	public String getRss() {
		return rss;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setFaceBook(String faceBook) {
		this.faceBook = faceBook;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

	public void setYoutube(String youtube) {
		this.youtube = youtube;
	}

	public void setFlickr(String flickr) {
		this.flickr = flickr;
	}

	public void setRss(String rss) {
		this.rss = rss;
	}
	
	
}
