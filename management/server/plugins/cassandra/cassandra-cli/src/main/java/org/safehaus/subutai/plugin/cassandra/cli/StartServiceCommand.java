package org.safehaus.subutai.plugin.cassandra.cli;


import java.io.IOException;
import java.util.UUID;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "service-cassandra-start", description = "Command to start Cassandra service")
public class StartServiceCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "clusterName", description = "Name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;
    @Argument(index = 1, name = "agentUUID", description = "UUID of the agent.", required = true, multiValued = false)
    String agentUUID = null;
    private Cassandra cassandraManager;
    private Tracker tracker;


    public Cassandra getCassandraManager()
    {
        return cassandraManager;
    }


    public void setCassandraManager( Cassandra cassandraManager )
    {
        this.cassandraManager = cassandraManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    protected Object doExecute() throws IOException
    {

        UUID uuid = cassandraManager.startService( clusterName, agentUUID );
        tracker.printOperationLog( CassandraClusterConfig.PRODUCT_KEY, uuid, 30000 );

        return null;
    }
}