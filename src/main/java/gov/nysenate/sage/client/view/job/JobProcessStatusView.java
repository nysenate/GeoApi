package gov.nysenate.sage.client.view.job;

import gov.nysenate.sage.model.job.JobProcessStatus;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
public class JobProcessStatusView {
    private int processId;
    private JobProcessView process;
    private int completedRecords = 0;
    private Timestamp startTime = null;
    private Timestamp completeTime = null;
    private List<String> messages = new ArrayList<>();
    private JobProcessStatus.Condition condition;

    public JobProcessStatusView(JobProcessStatus jobProcessStatus) {
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
}
