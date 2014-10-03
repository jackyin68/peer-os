package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.getParentTemplates command
 */
@Command(scope = "registry", name = "get-parent-templates", description = "Get all parent templates")
public class GetParentTemplatesCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "child template name", required = true, multiValued = false,
            description = "child template name")
    String childTemplateName;
    @Argument(index = 1, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64")
    String lxcArch;

    private TemplateRegistry templateRegistry;


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is NULL." );
        this.templateRegistry = templateRegistry;
    }


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<Template> templates =
                Strings.isNullOrEmpty( lxcArch ) ? templateRegistry.getParentTemplates( childTemplateName ) :
                templateRegistry.getParentTemplates( childTemplateName, lxcArch );

        if ( !templates.isEmpty() )
        {
            for ( Template template : templates )
            {
                System.out.println( template );
            }
        }
        else
        {
            System.out.println( String.format( "Parent templates of %s not found", childTemplateName ) );
        }

        return null;
    }
}
