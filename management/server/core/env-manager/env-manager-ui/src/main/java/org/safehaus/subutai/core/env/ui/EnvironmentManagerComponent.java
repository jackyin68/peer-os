package org.safehaus.subutai.core.env.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.ui.forms.BlueprintForm;
import org.safehaus.subutai.core.env.ui.forms.EnvironmentForm;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class EnvironmentManagerComponent extends CustomComponent implements Disposable
{

    private EnvironmentManager environmentManager;
    private PeerManager peerManager;


    public EnvironmentManagerComponent( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;

        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();

        BlueprintForm blueprintForm = new BlueprintForm( environmentManager );
        sheet.addTab( blueprintForm.getContentRoot(), "Blueprints" );
        sheet.getTab( 0 ).setId( "Blueprints" );
        EnvironmentForm environmentForm = new EnvironmentForm( environmentManager );
        sheet.addTab( environmentForm.getContentRoot(), "Environments" );
        sheet.getTab( 1 ).setId( "Environments" );

        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {
        //not ready yet
    }
}
