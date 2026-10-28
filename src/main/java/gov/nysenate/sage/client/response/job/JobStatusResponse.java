package gov.nysenate.sage.client.response.job;

import gov.nysenate.sage.client.view.job.JobProcessStatusView;
import gov.nysenate.sage.model.job.JobProcessStatus;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class JobStatusResponse {
    private final boolean success;
    private final boolean processorRunning;
    private final List<JobProcessStatusView> statuses = new ArrayList<>();

    public JobStatusResponse(List<JobProcessStatus> jobProcessStatuses, boolean processorRunning) {
        this.success = jobProcessStatuses != null;
        if (jobProcessStatuses != null) {
            for (JobProcessStatus jps: jobProcessStatuses) {
                statuses.add(new JobProcessStatusView(jps));
            }
        }
        this.processorRunning = processorRunning;
    }
}
