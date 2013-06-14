package gov.nysenate.sage.client.view.job;

import gov.nysenate.sage.model.job.JobProcessStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JobProcessStatusView
{
    protected int processId;
    protected JobProcessView process;

    protected int completedRecords = 0;
    protected Timestamp startTime = null;
    protected Timestamp completeTime = null;
    protected boolean completed = false;
    protected List<String> messages = new ArrayList<>();
    protected JobProcessStatus.Condition condition;

    public JobProcessStatusView(JobProcessStatus jobProcessStatus)
    {
        if (jobProcessStatus != null) {
            this.processId = jobProcessStatus.getProcessId();
            if (jobProcessStatus.getJobProcess() != null) {
                this.process = new JobProcessView(jobProcessStatus.getJobProcess());
            }
            this.completedRecords = jobProcessStatus.getCompletedRecords();
            this.startTime = jobProcessStatus.getStartTime();
            this.completeTime = jobProcessStatus.getCompleteTime();
            this.messages = jobProcessStatus.getMessages();
            this.condition = jobProcessStatus.getCondition();
        }
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public JobProcessView getProcess() {
        return process;
    }

    public void setProcess(JobProcessView process) {
        this.process = process;
    }

    public int getCompletedRecords() {
        return completedRecords;
    }

    public void setCompletedRecords(int completedRecords) {
        this.completedRecords = completedRecords;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Timestamp completeTime) {
        this.completeTime = completeTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public JobProcessStatus.Condition getCondition() {
        return condition;
    }

    public void setCondition(JobProcessStatus.Condition condition) {
        this.condition = condition;
    }
}
