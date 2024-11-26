package gov.nysenate.sage.model.stats;

import java.sql.Timestamp;

public record Deployment(int id, Timestamp deployTime, int apiRequestsSince) {}
