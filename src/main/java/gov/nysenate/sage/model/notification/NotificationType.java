package gov.nysenate.sage.model.notification;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum NotificationType {

    ALL                         (null),
    EXCEPTION                   (ALL),
    REQUEST_EXCEPTION           (EXCEPTION),
    DISTRICT_PROCESS_EXCEPTION  (EXCEPTION),
    GEOCODE_PROCESS_EXCEPTION   (EXCEPTION),
    EVENT_BUS_EXCEPTION         (EXCEPTION),
    WARNING                     (ALL),
    NEW_API_KEY                 (ALL),
    ;

    private NotificationType parent;

    private Set<NotificationType> children = new HashSet<>();

    private static final ImmutableSet<NotificationType> ALL_NOTIFICATION_TYPES =
            ImmutableSet.copyOf(NotificationType.values());

    @SuppressWarnings("IncompleteCopyConstructor")
    NotificationType(NotificationType parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    /**
     * Returns true if this notification type encompasses the argument notification type
     * e.g. EXCEPTION covers REQUEST_EXCEPTION but not the other way around
     * @param other NotificationType
     * @return boolean
     */
    public boolean covers(NotificationType other) {
        return other != null && (this.equals(other) || covers(other.parent));
    }

    public Set<NotificationType> getChildren() {
        return children;
    }

    public NotificationType getParent() {
        return parent;
    }

    private EnumMap<NotificationType, Object> getTypeHierarchy() {
        EnumMap<NotificationType, Object> hierarchyMap = new EnumMap<>(NotificationType.class);
        children.forEach(child -> hierarchyMap.put(child, child.getTypeHierarchy()));
        return hierarchyMap;
    }

    /**
     * Returns a set of all notification types covered by the given notification type
     * @param type NotificationType
     * @return EnumSet<NotificationType>
     */
    public static EnumSet<NotificationType> getCoverage(NotificationType type) {
        if (type == null) return EnumSet.noneOf(NotificationType.class);
        EnumSet<NotificationType> result = EnumSet.of(type);
        type.children.stream()
                .map(NotificationType::getCoverage)
                .forEach(result::addAll);
        return result;
    }

    /**
     * Get all notification types covered by all types in the given collection
     * @param types Collection<NotificationType>
     * @return EnumSet<NotificationType>
     */
    public static EnumSet<NotificationType> getCoverage(Collection<NotificationType> types) {
        EnumSet<NotificationType> result = EnumSet.noneOf(NotificationType.class);
        for (NotificationType type : types) {
            if (!result.contains(type)) {
                result.addAll(getCoverage(type));
            }
        }
        return result;
    }

    public static NotificationType getValue(String string) {
        return NotificationType.valueOf(StringUtils.upperCase(string));
    }

    public static ImmutableSet<NotificationType> getAllNotificationTypes() {
        return ALL_NOTIFICATION_TYPES;
    }

    public static Map<NotificationType, Object> getHierarchy() {
        return ImmutableMap.of(ALL, ALL.getTypeHierarchy());
    }
}
