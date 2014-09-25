package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;


public class StartClusterHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String clusterName;


    public StartClusterHandler( final CassandraImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        CassandraClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }
        Command startServiceCommand = Commands.getStartCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( startServiceCommand );

        if ( startServiceCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Start succeeded" );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
        }
    }
}