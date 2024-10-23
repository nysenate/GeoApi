package gov.nysenate.sage.service.security;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines roles and the permissions implied by them.
 */
public enum SageRole {
    ADMIN(Collections.singletonList("admin:view, job:view, ui:view")),
    JOB_USER(List.of("job:view, ui:view")),
    API_USER(List.of("ui:view"));

    private final List<Permission> wildcardPermissions;

    SageRole(List<String> permissions) {
        this.wildcardPermissions =
                permissions.stream()
                        .map(WildcardPermission::new)
                        .collect(Collectors.toList());
    }

    public List<Permission> getWildcardPermissions() {
        return wildcardPermissions;
    }
}
