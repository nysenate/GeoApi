package gov.nysenate.sage.model.job;

import gov.nysenate.sage.deprecated.annotations.*;
import gov.nysenate.sage.util.Connect;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;



public class JobProcess {
	String contact;
	String fileName;
	String className;

	Long requestTime;

	@Ignore @PrimaryKey Integer id;

	public JobProcess() {
		contact = "";
		fileName = "";
		className = "";

		requestTime = new Date().getTime();
	}

	public JobProcess(String contact, String fileName, String className) {
		this.contact = contact;
		this.fileName = fileName;
		this.className = className;
		requestTime = new Date().getTime();
	}

	public String getContact() {
		return contact;
	}

	public String getFileName() {
		return fileName;
	}

	public String getClassName() {
		return className;
	}

	public Long getRequestTime() {
		return requestTime;
	}

	public Integer getId() {
		return id;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setRequestTime(Long timeStamp) {
		this.requestTime = timeStamp;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public static class ByRequestTime implements Comparator<JobProcess> {
		@Override
        public int compare(JobProcess o1, JobProcess o2) {
			int ret = o1.getRequestTime().compareTo(o2.getRequestTime());
			if(ret == 0) {
				ret = -1;
			}
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static TreeSet<JobProcess> getJobProcesses() throws IOException {
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
