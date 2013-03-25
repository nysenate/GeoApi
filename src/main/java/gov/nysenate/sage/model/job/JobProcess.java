package gov.nysenate.sage.model.job;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

public class JobProcess
{
    protected int id = -1;
    protected JobUser requestor;
    protected String sourcefileName = "";
    protected String systemFilename = "";
    protected String fileType = "";
    protected Timestamp requestTime;
    protected int recordCount = 0;

	public JobProcess()
    {
        this.requestTime = new Timestamp(new Date().getTime());
	}

    public void setSourceFileName(String fileName) {
        this.sourcefileName = fileName;
    }

    public String getSourceFileName() {
		return sourcefileName;
	}

	public String getFileType() {
		return fileType;
	}

	public Timestamp getRequestTime() {
		return requestTime;
	}

	public int getId() {
		return id;
	}

	public void setFileName(String fileName) {
		this.systemFilename = fileName;
	}

    public String getFileName() {
        return this.systemFilename;
    }

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setRequestTime(Timestamp timeStamp) {
		this.requestTime = timeStamp;
	}

    public void setId(int id) {
		this.id = id;
	}

    public JobUser getRequestor() {
        return requestor;
    }

    public void setRequestor(JobUser requestor) {
        this.requestor = requestor;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
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
}
