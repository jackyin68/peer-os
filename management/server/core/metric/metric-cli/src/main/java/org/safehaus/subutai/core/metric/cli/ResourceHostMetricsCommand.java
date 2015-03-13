package org.safehaus.subutai.core.metric.cli;


import java.util.Set;

import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.metric.api.Monitor;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * ResourceHostMetricsCommand
 */
@Command( scope = "metric", name = "resource-host-metrics", description = "Lists resource host metrics" )
public class ResourceHostMetricsCommand extends SubutaiShellCommandSupport
{

    private final Monitor monitor;


    public ResourceHostMetricsCommand( final Monitor monitor )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );

        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<ResourceHostMetric> metrics = monitor.getResourceHostsMetrics();
        for ( ResourceHostMetric metric : metrics )
        {
            System.out.println( metric );
        }

        return null;
    }
}
