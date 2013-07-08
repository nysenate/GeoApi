package gov.nysenate.sage.model.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class JobRequest
{
    protected JobUser requestor;
    protected List<JobProcess> processes;

    public JobRequest(JobUser requestor) {
        this.requestor = requestor;
        this.processes = new LinkedList<>();
    }

    public void clear()
    {
        if (this.processes != null) {
            processes.clear();
        }
    }

    public JobUser getRequestor() {
        return requestor;
    }

    public void setRequestor(JobUser requestor) {
        this.requestor = requestor;
    }

    public List<JobProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<JobProcess> processes) {
        this.processes = processes;
    }

    public void addProcess(JobProcess process) {
        if (this.processes == null) {
            this.processes = new ArrayList<>();
        }
        this.processes.add(process);
    }
}
