package gov.nysenate.sage.model.BulkProcessing;

import gov.nysenate.sage.util.Connect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;


public class BulkRequest {
	Integer gRequests;
	Integer yRequests;
	Integer bRequests;
	
	Long requestTime;
	
	public BulkRequest() {
		gRequests = 0;
		yRequests = 0;
		bRequests = 0;
		requestTime = new Date().getTime();
	}
	
	public BulkRequest(int gRequests, int yRequests, int bRequests) {
		this.gRequests = gRequests;
		this.yRequests = yRequests;
		this.bRequests = bRequests;
		this.requestTime = new Date().getTime();
	}
	
	public Integer getGRequests() {
		return gRequests;
	}

	public Integer getYRequests() {
		return yRequests;
	}

	public Integer getBRequests() {
		return bRequests;
	}

	public Long getRequestTime() {
		return requestTime;
	}

	public void setGRequests(Integer gRequests) {
		this.gRequests = gRequests;
	}

	public void setYRequests(Integer yRequests) {
		this.yRequests = yRequests;
	}

	public void setBRequests(Integer bRequests) {
		this.bRequests = bRequests;
	}

	public void setRequestTime(Long requestTime) {
		this.requestTime = requestTime;
	}
	
	public void incrementGRequest(int i) {
		gRequests = i + gRequests;
	}
	public void incrementYRequest(int i) {
		yRequests = i + yRequests;
	}
	public void incrementBRequest(int i) {
		bRequests = i + bRequests;
	}
	

	public static class ByRequestTime implements Comparator<BulkRequest> {
		public int compare(BulkRequest o1, BulkRequest o2) {
			int ret = o1.getRequestTime().compareTo(o2.getRequestTime());
			if(ret == 0) {
				ret = -1;
			}
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static TreeSet<BulkRequest> getBulkRequests() {
		Connect connect = new Connect();
		ArrayList<BulkRequest> list = new ArrayList<BulkRequest>();
		TreeSet<BulkRequest> set = new TreeSet<BulkRequest>(new BulkRequest.ByRequestTime());
		
		try {
			list = (ArrayList<BulkRequest>)connect.getObjects(BulkRequest.class);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(list != null && !list.isEmpty()) {
			set.addAll(list);
		}
		
		connect.close();
		return set;
	}
}
