package org.safehaus.subutai.core.registry.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class TemplateRegistryPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "tree.png";
    public static final String MODULE_NAME = "Registry";
    private TemplateRegistry registryManager;


    public void setRegistryManager( final TemplateRegistry registryManager )
    {
        this.registryManager = registryManager;
    }


    @Override
    public String getId()
    {
        return MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new TemplateRegistryComponent( registryManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}