package org.safehaus.subutai.core.container.ui;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.server.ui.component.ConcurrentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;


public class ContainerTree extends ConcurrentComponent implements Disposable
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerTree.class.getName() );
    private final LocalPeer localPeer;
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<Host> selectedHosts = new HashSet<>();
    private final ScheduledExecutorService scheduler;


    public ContainerTree( LocalPeer localPeer )
    {

        this.localPeer = localPeer;
        setSizeFull();
        setMargin( true );

        tree = new Tree( "List of nodes" );
        tree.setContainerDataSource( getNodeContainer() );
        tree.setItemIconPropertyId( "icon" );
        tree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
        {

            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId )
            {
                String description = "";

                Item item = tree.getItem( itemId );
                if ( item != null )
                {
                    Host host = ( Host ) item.getItemProperty( "value" ).getValue();
                    if ( host != null )
                    {
                        description =
                                "Hostname: " + host.getHostname() + "<br>" + "MAC: " + host.getAgent().getMacAddress()
                                        + "<br>" + "UUID: " + host.getAgent().getUuid() + "<br>" + "IP: " + host
                                        .getAgent().getListIP();
                    }
                }

                return description;
            }
        } );
        tree.setMultiSelect( true );
        tree.setImmediate( true );
        tree.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() instanceof Set )
                {
                    Tree t = ( Tree ) event.getProperty();

                    Set<Host> selectedList = new HashSet<>();

                    for ( Object o : ( Iterable<?> ) t.getValue() )
                    {
                        if ( tree.getItem( o ).getItemProperty( "value" ).getValue() != null )
                        {
                            Host host = ( Host ) tree.getItem( o ).getItemProperty( "value" ).getValue();
                            selectedList.add( host );
                        }
                    }

                    selectedHosts = selectedList;
                }
            }
        } );
        addComponent( tree );
        scheduler = Executors.newScheduledThreadPool( 1 );

        scheduler.scheduleWithFixedDelay( new Runnable()
        {
            public void run()
            {
                LOG.info( "Refreshing containers state..." );
                refreshHosts();
                LOG.info( "Refreshing done." );
            }
        }, 5, 30, TimeUnit.SECONDS );
        try
        {
            tree.expandItem( localPeer.getManagementHost().getId() );
        }
        catch ( PeerException ignore )
        {

        }
    }


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( "value", Host.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        tree.removeAllItems();

        try
        {
            ManagementHost managementHost = localPeer.getManagementHost();
            if ( managementHost != null )
            {
                Item managementHostItem = container.addItem( managementHost.getId() );
                container.setChildrenAllowed( managementHost.getId(), true );
                managementHostItem.getItemProperty( "value" ).setValue( managementHost );
                tree.setItemCaption( managementHost.getId(),
                        String.format( localPeer.getPeerInfo().getName(), localPeer.getPeerInfo().getId() ) );
                for ( ResourceHost rh : managementHost.getResourceHosts() )
                {
                    Item resourceHostItem = container.addItem( rh.getId() );
                    tree.setItemCaption( rh.getId(), rh.getHostname() );
                    resourceHostItem.getItemProperty( "value" ).setValue( rh );
                    container.setParent( rh.getId(), managementHost.getId() );
                    if ( rh.getContainerHosts().size() > 0 )
                    {
                        container.setChildrenAllowed( rh.getId(), true );

                        for ( ContainerHost ch : rh.getContainerHosts() )
                        {
                            Item containerHostItem = container.addItem( ch.getId() );
                            if ( containerHostItem != null )
                            {
                                container.setChildrenAllowed( ch.getId(), false );
                                tree.setItemCaption( ch.getId(), ch.getHostname() );
                                containerHostItem.getItemProperty( "value" ).setValue( ch );
                                container.setParent( ch.getId(), rh.getId() );
                            }
                            else
                            {
                                LOG.error( "Error in ContainerTree" );
                            }
                        }
                    }
                    else
                    {
                        container.setChildrenAllowed( rh.getId(), false );
                    }
                }
            }
            else
            {
                LOG.warn( "ManagementHost is null" );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error on building container tree: " + e.toString() );
        }
        return container;
    }


    private void refreshHosts()
    {
        for ( Object itemObj : container.getItemIds() )
        {
            UUID itemId = ( UUID ) itemObj;
            Item item = container.getItem( itemId );
            Object o = item.getItemProperty( "value" ).getValue();
            if ( ( o instanceof Host ) && ( ( ( Host ) o ).isConnected() ) )
            {
                item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual.png" ) );
            }
            else
            {
                item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual-stopped.png" ) );
            }
        }
    }


    public Set<Host> getSelectedHosts()
    {
        return Collections.unmodifiableSet( selectedHosts );
    }


    public void dispose()
    {
        scheduler.shutdown();
    }
}
