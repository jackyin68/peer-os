package org.safehaus.subutai.core.identity.api;


import java.util.Set;


/**
 * Created by timur on 1/21/15.
 */
public interface Role
{
    public String getName();

    public Set<Permission> getPermissions();

    public void addPermission( Permission permission );

    public void removePermission( Permission permission );

    public Set<PortalModuleScope> getAccessibleModules();

    public void clearPortalModules();

    public void addPortalModule( PortalModuleScope portalModule );

    public boolean canAccessModule( String moduleKey );
}
