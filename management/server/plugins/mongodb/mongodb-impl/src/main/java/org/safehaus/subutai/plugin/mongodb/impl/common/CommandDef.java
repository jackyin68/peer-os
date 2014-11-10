package org.safehaus.subutai.plugin.mongodb.impl.common;


import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.peer.api.Host;


/**
 * Created by timur on 11/7/14.
 */
public class CommandDef
{
    private String command;
    private String description;
    private int timeout;


    public CommandDef( String description, String command, int timeout )
    {
        this.description = description;
        this.command = command;
        this.timeout = timeout;
    }


    public RequestBuilder build()
    {
        return new RequestBuilder( command ).withTimeout( timeout );
    }


    public String getCommand()
    {
        return command;
    }


    public String getDescription()
    {
        return description;
    }


    public int getTimeout()
    {
        return timeout;
    }


    public void execute( Host host ) throws CommandException
    {
        host.execute( build() );
    }
}
