package model;

import control.Connect;

public class OldAisCount {
	String ip;
	
	public static void main(String[] args) {
		Connect c = new Connect();
		
		c.deleteObjectById(OldAisCount.class, "ip", "hello!");
		
		c.close();
	}
	
	public OldAisCount() {
		
	}
	
	public OldAisCount(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
