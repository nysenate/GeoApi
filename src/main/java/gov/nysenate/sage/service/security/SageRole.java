package gov.nysenate.sage.service.security;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines roles and the permissions implied by them.
 */
public enum SageRole
{
    ADMIN(Collections.singletonList("admin:view, job:view, ui:view")),
    JOB_USER(Arrays.asList("job:view, ui:view")),
    API_USER(Arrays.asList("ui:view")),
    ;

    private List<String> permissions;
    private List<Permission> wildcardPermissions;

    SageRole(List<String> permissions) {
        this.permissions = permissions;
        this.wildcardPermissions =
                this.permissions.stream()
                        .map(WildcardPermission::new)
                        .collect(Collectors.toList());
    }

    public List<String> getPermissionStrings() {
        return permissions;
    }

    public List<Permission> getWildcardPermissions() {
        return wildcardPermissions;
    }
}
