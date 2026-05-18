package gov.nysenate.sage.model.notification;

import java.time.LocalDateTime;

/**
 * The data portion of a notification.
 */
public record Notification(LocalDateTime occurred, String summary, String message) {}
