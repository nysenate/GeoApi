package model.BulkProcessing;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;

import model.annotations.Ignore;
import model.annotations.PrimaryKey;

import control.Connect;

public class JobProcess {
	String contact;
	String jobType;
	String fileName;
	
	Long requestTime;
	
	//when not null it is a multi-run request
	Integer segment;
	Integer lineCount;
	
	@Ignore @PrimaryKey Integer id;
	
	public JobProcess() {
		contact = "";
		jobType = "";
		fileName = "";
		
		requestTime = new Date().getTime();
		
		segment = -1;
		
		lineCount = 0;
	}
	
	public JobProcess(String contact, String type, String fileName, Integer lineCount) {
		this.contact = contact;
		this.jobType = type;
		this.fileName = fileName; 
		this.lineCount = lineCount;
		requestTime = new Date().getTime();
		segment = -1;
	}
	
	public JobProcess(String contact, String type, String fileName, Integer lineCount, Long requestTime) {
		this.contact = contact;
		this.jobType = type;
		this.fileName = fileName; 
		this.lineCount = lineCount;
		this.requestTime = requestTime;
		segment = -1;
	}
	
	public String getContact() {
		return contact;
	}

	public String getJobType() {
		return jobType;
	}

	public String getFileName() {
		return fileName;
	}

	public Long getRequestTime() {
		return requestTime;
	}

	public Integer getSegment() {
		return segment;
	}
	
	public Integer getLineCount() {
		return lineCount;
	}
	
	public Integer getId() {
		return id;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setJobType(String type) {
		this.jobType = type;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setRequestTime(Long timeStamp) {
		this.requestTime = timeStamp;
	}

	public void setSegment(Integer segment) {
		this.segment = segment;
	}
	
	public void setLineCount(Integer lineCount) {
		this.lineCount = lineCount;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public static class ByRequestTime implements Comparator<JobProcess> {
		public int compare(JobProcess o1, JobProcess o2) {
			int ret = o1.getRequestTime().compareTo(o2.getRequestTime());
			if(ret == 0) {
				ret = -1;
			}
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static TreeSet<JobProcess> getJobProcesses() {
		Connect connect = new Connect();
		ArrayList<JobProcess> list = new ArrayList<JobProcess>();
		TreeSet<JobProcess> set = new TreeSet<JobProcess>(new JobProcess.ByRequestTime());
		
		try {
			list = (ArrayList<JobProcess>)connect.getObjects(JobProcess.class);
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
