package io.subutai.core.key.cli;


import io.subutai.common.peer.Host;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.key.api.KeyManager;
import io.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "km", name = "export-key" )
public class ExportKeyCommand extends SubutaiShellCommandSupport
{
    private KeyManager keyManager;
    private PeerManager peerManager;


    @Argument( index = 0, name = "key ID", multiValued = false, required = true, description = "Key ID" )
    private String keyId;


    public ExportKeyCommand( final KeyManager keyManager, final PeerManager peerManager )
    {
        this.keyManager = keyManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Host managementHost = peerManager.getLocalPeer().getManagementHost();
        String armor = keyManager.readKey( managementHost, keyId );

        System.out.println( String.format( "%s", armor ) );
        return null;
    }
}