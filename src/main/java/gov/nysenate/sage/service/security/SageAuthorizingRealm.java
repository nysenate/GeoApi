package gov.nysenate.sage.service.security;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.realm.AuthorizingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * Base realm layer which sets up some convenience methods for resolving permissions.
 */
public abstract class SageAuthorizingRealm extends AuthorizingRealm
{
    private static final Logger logger = LoggerFactory.getLogger(SageAuthorizingRealm.class);

    protected static class SageRolePermissionResolver implements RolePermissionResolver
    {
        @Override
        public Collection<Permission> resolvePermissionsInRole(String roleString) {
            try {
                SageRole sageRole = SageRole.valueOf(roleString);
                return sageRole.getWildcardPermissions();
            }
            catch (IllegalArgumentException ex) {
                logger.warn("The role '{}' is not a known role! This needs to be addressed.", roleString);
            }
            return Collections.emptyList();
        }
    }

    protected static final RolePermissionResolver sageRolePermResolver = new SageRolePermissionResolver();

    @Override
    public void setRolePermissionResolver(RolePermissionResolver permissionRoleResolver) {
        throw new UnsupportedOperationException("Cannot set role resolvers.");
    }

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return sageRolePermResolver;
    }
}
