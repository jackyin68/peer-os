package org.safehaus.subutai.core.hostregistry.impl;


import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Notifies listener on host heartbeat
 */
public class HostNotifier implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( HostNotifier.class.getName() );

    private HostListener listener;
    private HostInfo info;


    public HostNotifier( final HostListener listener, final HostInfo info )
    {
        this.listener = listener;
        this.info = info;
    }


    @Override
    public void run()
    {
        try
        {
            listener.onHeartbeat( info );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in run", e );
        }
    }
}
