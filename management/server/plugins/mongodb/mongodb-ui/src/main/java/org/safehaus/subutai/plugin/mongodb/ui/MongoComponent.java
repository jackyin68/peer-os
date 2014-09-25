/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.mongodb.ui.manager.Manager;
import org.safehaus.subutai.plugin.mongodb.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class MongoComponent extends CustomComponent
{

    public MongoComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet mongoSheet = new TabSheet();
        mongoSheet.setSizeFull();
        Manager manager = new Manager( executorService, serviceLocator );
        Wizard wizard = new Wizard( executorService, serviceLocator );
        mongoSheet.addTab( wizard.getContent(), "Install" );
        mongoSheet.addTab( manager.getContent(), "Manage" );
        verticalLayout.addComponent( mongoSheet );

        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}