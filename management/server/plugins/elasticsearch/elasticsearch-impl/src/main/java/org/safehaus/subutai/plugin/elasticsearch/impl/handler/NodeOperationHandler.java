package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;


public class NodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{

    private String clusterName;
    private UUID agentUUID;
    private OperationType operationType;


    public NodeOperationHandler( final ElasticsearchImpl manager, final String clusterName, final UUID agentUUID,
                                 OperationType operationType )
    {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker()
                                       .createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                                               String.format( "Checking %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        ElasticsearchClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();
        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getId().equals( agentUUID ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", agentUUID ) );
            return;
        }

        try
        {
            CommandResult result;
            switch ( operationType )
            {
                case START:
                    host.execute( new RequestBuilder( Commands.startCommand ) );
                    break;
                case STOP:
                    host.execute( new RequestBuilder( Commands.stopCommand ) );
                    break;
                case STATUS:
                    result = host.execute( new RequestBuilder( Commands.statusCommand ) );
                    logStatusResults( trackerOperation, result );
                    break;
            }
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    private void logStatusResults( TrackerOperation po, CommandResult result )
    {
        StringBuilder log = new StringBuilder();
        String status = "UNKNOWN";
        if ( result.getExitCode() == 0 )
        {
            status = "Cassandra is running";
        }
        else if ( result.getExitCode() == 768 )
        {
            status = "Cassandra is not running";
        }
        else
        {
            status = result.getStdOut();
        }
        log.append( String.format( "%s", status ) );
        po.addLogDone( log.toString() );
    }
}
