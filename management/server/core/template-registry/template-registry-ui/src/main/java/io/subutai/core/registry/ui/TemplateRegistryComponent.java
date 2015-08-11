/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.core.registry.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.subutai.common.protocol.Template;
import io.subutai.core.git.api.GitChangedFile;
import io.subutai.core.registry.api.RegistryException;
import io.subutai.core.registry.api.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class TemplateRegistryComponent extends CustomComponent
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateRegistryComponent.class );
    private static final String VALUE_PROPERTY = "value";
    private static final String ICON = "icon";
    private static final String CAPTION = "caption";
    private static final String PHYSICAL_IMG = "img/lxc/physical.png";
    private static final String TEMPLATE_PROPERTY = "Template Property";
    private static final String TEMPLATE_VALUE = "Value";
    private final TemplateRegistry registryManager;

    private HierarchicalContainer container;
    private Tree templateTree;


    private BeanContainer<String, Template> templatesBeanContainer;


    private BeanContainer<String, GitChangedFile> changedFilesBeanContainer;


    private ComboBox templateA;

    private ComboBox templateB;

    private Map<String, TemplateValue> templatePropertiesMap;


    private interface TemplateValue
    {
        String getTemplateProperty( Template template );
    }


    public TemplateRegistryComponent( final TemplateRegistry registryManager )
    {
        setHeight( 100, Unit.PERCENTAGE );

        this.registryManager = registryManager;

        initializeControls();
        setupView();
    }


    private void initializeControls()
    {
        templatesBeanContainer = new BeanContainer<>( Template.class );
        templatesBeanContainer.setBeanIdProperty( "templateName" );

        changedFilesBeanContainer = new BeanContainer<>( GitChangedFile.class );
        changedFilesBeanContainer.setBeanIdProperty( "gitFilePath" );

        templateA = new ComboBox( "Template A" );
        templateA.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );
        templateA.setItemCaptionPropertyId( "templateName" );
        templateA.setNullSelectionAllowed( false );
        templateA.setImmediate( true );
        templateA.setContainerDataSource( templatesBeanContainer );
        templateA.setTextInputAllowed( false );
        templateA.setRequired( true );

        templateB = new ComboBox( "Template B" );
        templateB.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );
        templateB.setItemCaptionPropertyId( "templateName" );
        templateB.setNullSelectionAllowed( false );
        templateB.setImmediate( true );
        templateB.setContainerDataSource( templatesBeanContainer );
        templateB.setTextInputAllowed( false );
        templateB.setRequired( true );

        templatePropertiesMap = new HashMap<String, TemplateValue>();
        templatePropertiesMap.put( "Template Name", new TemplateValue()
        {
            @Override
            public String getTemplateProperty( final Template template )
            {
                return template.getTemplateName();
            }
        } );
        templatePropertiesMap.put( "Parent Name", new TemplateValue()
        {
            @Override
            public String getTemplateProperty( final Template template )
            {
                return template.getParentTemplateName();
            }
        } );
        templatePropertiesMap.put( "Lxc Arch", new TemplateValue()
        {
            @Override
            public String getTemplateProperty( final Template template )
            {
                return template.getLxcArch();
            }
        } );
        templatePropertiesMap.put( "Utsname", new TemplateValue()
        {
            @Override
            public String getTemplateProperty( final Template template )
            {
                return template.getLxcUtsname();
            }
        } );
        templatePropertiesMap.put( "Config Path", new TemplateValue()
        {
            @Override
            public String getTemplateProperty( final Template template )
            {
                return template.getSubutaiConfigPath();
            }
        } );
        templatePropertiesMap.put( "Used on resource hosts", new TemplateValue()
        {
            @Override
            public String getTemplateProperty( final Template template )
            {
                StringBuilder buf = new StringBuilder();
                for ( final String rh : template.getFaisUsingThisTemplate() )
                {
                    buf.append( rh ).append( "; " );
                }
                return buf.toString();
            }
        } );

        // Containers
        container = createTreeContent( registryManager.getAllTemplates() );
        changedFilesBeanContainer.addAll( new ArrayList<GitChangedFile>() );
        templatesBeanContainer.addAll( registryManager.getAllTemplates() );

        // Tree View

        templateTree = new Tree( "Templates" );
        templateTree.setContainerDataSource( container );
        templateTree.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );
        templateTree.setItemCaptionPropertyId( CAPTION );
        templateTree.setItemIconPropertyId( ICON );
        templateTree.setImmediate( true );
        for ( final Object id : templateTree.getItemIds() )
        {
            templateTree.expandItem( id );
        }
    }


    private void setupView()
    {
        // Build templates layout
        ItemClickEvent.ItemClickListener changedFileRowClickListener = new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( final ItemClickEvent event )
            {
                BeanItem<GitChangedFile> file = changedFilesBeanContainer.getItem( event.getItemId() );

                if ( file == null )
                {
                    return;
                }

                showFileDiff( file );
            }
        };
        Table changedFilesTable = new Table( "Changed Files." );
        changedFilesTable.setWidth( "40%" );
        changedFilesTable.setImmediate( true );
        changedFilesTable.setContainerDataSource( changedFilesBeanContainer );
        changedFilesTable.setColumnHeaders( "File Path", "Status" );
        changedFilesTable.addItemClickListener( changedFileRowClickListener );

        final Table templateAInfoTable = constructTemplateTableInfo();
        final Table templateBInfoTable = constructTemplateTableInfo();

        VerticalLayout firstTemplateComponents =
                buildTemplateComponentLayout( "firstTemplateComponentsId", templateA, templateAInfoTable );
        VerticalLayout secondTemplateComponents =
                buildTemplateComponentLayout( "secondTemplateComponentsId", templateB, templateBInfoTable );

        Button swapTemplates = new Button( "Swap Templates" );
        swapTemplates.setId( "swapTemplatesId" );
        swapTemplates.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Object firstSelected = templateA.getValue();
                templateA.select( templateB.getValue() );
                templateB.select( firstSelected );
            }
        } );

        HorizontalLayout generalInfoLayout = new HorizontalLayout();
        generalInfoLayout.setSpacing( true );
        generalInfoLayout.setSizeFull();
        generalInfoLayout.setId( "generalInfoHorizontalLayoutId" );
        generalInfoLayout.addComponent( secondTemplateComponents );
        generalInfoLayout.setExpandRatio( secondTemplateComponents, 2.0f );
        generalInfoLayout.addComponent( swapTemplates );
        generalInfoLayout.setExpandRatio( swapTemplates, 1.0f );
        generalInfoLayout.setComponentAlignment( swapTemplates, Alignment.MIDDLE_CENTER );
        generalInfoLayout.addComponent( firstTemplateComponents );
        generalInfoLayout.setExpandRatio( firstTemplateComponents, 2.0f );
        generalInfoLayout.setId( "generalInfoHorizontalLayoutId" );

        VerticalLayout baseLayout = new VerticalLayout();
        baseLayout.setSpacing( true );
        baseLayout.setId( "baseLayoutId" );
        baseLayout.setSizeFull();
        baseLayout.addComponent( generalInfoLayout );
        baseLayout.addComponent( changedFilesTable );

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );
        horizontalSplit.setFirstComponent( templateTree );
        horizontalSplit.setSecondComponent( baseLayout );
        setCompositionRoot( horizontalSplit );

        //Listeners
        Property.ValueChangeListener templateComboBoxListener = new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                BeanItem<Template> item = templatesBeanContainer.getItem( event.getProperty().getValue() );
                Template template = item.getBean();
                if ( event.getProperty().getValue().equals( templateA.getValue() ) )
                {
                    showSelectedTemplateInfo( template, templateAInfoTable );
                }
                else
                {
                    showSelectedTemplateInfo( template, templateBInfoTable );
                }
            }
        };
        Property.ValueChangeListener treeClickListener = new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                treeItemClickEventTrigger( event );
            }
        };
        AbstractSelect.ItemDescriptionGenerator treeItemDescriptionGenerator =
                new AbstractSelect.ItemDescriptionGenerator()
                {
                    @Override
                    public String generateDescription( Component source, Object itemId, Object propertyId )
                    {
                        String description = "";
                        Item item = templateTree.getItem( itemId );
                        if ( item != null )
                        {
                            Template template = ( Template ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                            if ( template != null )
                            {
                                description = getTemplateDescription( template );
                            }
                        }
                        return description;
                    }
                };


        //setting listeners
        templateTree.setItemDescriptionGenerator( treeItemDescriptionGenerator );
        templateTree.addValueChangeListener( treeClickListener );
        templateA.addValueChangeListener( templateComboBoxListener );
        templateB.addValueChangeListener( templateComboBoxListener );
    }


    private VerticalLayout buildTemplateComponentLayout( String componentId, Component... layoutComponents )
    {
        VerticalLayout firstTemplateComponents = new VerticalLayout();
        firstTemplateComponents.setSpacing( true );
        firstTemplateComponents.addComponents( layoutComponents );
        firstTemplateComponents.setId( componentId );
        return firstTemplateComponents;
    }


    private Table constructTemplateTableInfo()
    {
        // Templates basic info tables
        final Table templateInfoTable = new Table( "Template Info" );
        templateInfoTable.setWidth( "80%" );
        templateInfoTable.setImmediate( true );
        templateInfoTable.addContainerProperty( TEMPLATE_PROPERTY, String.class, null );
        templateInfoTable.addContainerProperty( TEMPLATE_VALUE, String.class, null );

        //adding rows
        for ( String key : templatePropertiesMap.keySet() )
        {
            templateInfoTable.addItem( new Object[] { key, "" }, key );
        }
        return templateInfoTable;
    }


    private void treeItemClickEventTrigger( final Property.ValueChangeEvent event )
    {
        Item item = templateTree.getItem( event.getProperty().getValue() );

        if ( item != null )
        {
            final Template template = ( Template ) item.getItemProperty( VALUE_PROPERTY ).getValue();
            if ( template != null )
            {
                if ( template.getParentTemplateName() != null )
                {
                    templateA.select( template.getParentTemplateName() );
                }
                else
                {
                    templateA.select( template.getTemplateName() );
                }
                templateB.select( template.getTemplateName() );
            }
            else
            {
                Notification.show( "No template" );
            }
        }
    }


    private void showSelectedTemplateInfo( Template template, Table templateInfoTable )
    {
        for ( String key : templatePropertiesMap.keySet() )
        {
            Property item = templateInfoTable.getItem( key ).getItemProperty( TEMPLATE_VALUE );
            item.setValue( templatePropertiesMap.get( key ).getTemplateProperty( template ) );
        }

        changedFilesBeanContainer.removeAllItems();
        try
        {
            BeanItem<Template> aBeanItem = templatesBeanContainer.getItem( templateA.getValue() );
            BeanItem<Template> bBeanItem = templatesBeanContainer.getItem( templateB.getValue() );

            if ( aBeanItem != null && bBeanItem != null )
            {
                Template aTemplate = aBeanItem.getBean();
                Template bTemplate = bBeanItem.getBean();
                List<GitChangedFile> changedFiles = registryManager.getChangedFiles( aTemplate, bTemplate );
                changedFilesBeanContainer.addAll( changedFiles );
            }
        }
        catch ( RegistryException e )
        {
            LOGGER.error( "Error retrieving changed file list", e );
        }
    }


    /**
     * Construct hierarchical templates tree
     *
     * @param templates - templates list
     *
     * @return - {@code HierarchicalContainer} object with relevant hierarchy
     */
    public HierarchicalContainer createTreeContent( List<Template> templates )
    {

        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty( CAPTION, String.class, "" );
        container.addContainerProperty( VALUE_PROPERTY, Template.class, null );
        container.addContainerProperty( ICON, Resource.class, new ThemeResource( PHYSICAL_IMG ) );

        new Object()
        {

            @SuppressWarnings( "unchecked" )
            public void put( List<Template> data, HierarchicalContainer container )
            {
                for ( Template template : data )
                {
                    String orgId = template.getPk().toString();
                    if ( !container.containsId( orgId ) )
                    {
                        container.addItem( orgId );
                        container.getItem( orgId ).getItemProperty( CAPTION ).setValue( template.getTemplateName() );
                        container.getItem( orgId ).getItemProperty( VALUE_PROPERTY ).setValue( template );
                        if ( template.getChildren() == null || template.getChildren().isEmpty() )
                        {
                            container.setChildrenAllowed( orgId, false );
                        }
                        else
                        {
                            container.setChildrenAllowed( orgId, true );
                        }
                        if ( template.getParentTemplateName() == null || "".equals( template.getParentTemplateName() ) )
                        {
                            container.setParent( orgId, null );
                        }
                        else
                        {
                            if ( !container.containsId( template.getPk().toString() ) )
                            {
                                put( template.getChildren(), container );
                            }
                            container.setParent( orgId, TemplateRegistryComponent.this.registryManager
                                    .getParentTemplate( template.getTemplateName() ).getPk().toString() );
                        }
                    }
                }
            }
        }.put( templates, container );

        return container;
    }


    private String getTemplateDescription( Template template )
    {
        if ( template != null )
        {
            return "Name: " + template.getTemplateName() + "<br>" + "Parent: " + template.getParentTemplateName()
                    + "<br>" + "Arch: " + template.getLxcArch() + "<br>" + "Utsname: " + template.getLxcUtsname()
                    + "<br>" + "Cfg Path: " + template.getSubutaiConfigPath();
        }
        return "";
    }


    /**
     * Represent to user file diff between two templates
     *
     * @param file - changed file as {@code GitChangedFile} object wrapped in {@code BeanItem<>}
     */
    private void showFileDiff( BeanItem<GitChangedFile> file )
    {
        BeanItem<Template> bBeanItem = templatesBeanContainer.getItem( templateB.getValue() );
        BeanItem<Template> aBeanItem = templatesBeanContainer.getItem( templateA.getValue() );

        if ( aBeanItem == null && bBeanItem == null )
        {
            return;
        }
        Template aTemplate;
        Template bTemplate;

        if ( aBeanItem != null )
        {
            aTemplate = aBeanItem.getBean();
        }
        else
        {
            aTemplate = bBeanItem.getBean();
        }
        if ( bBeanItem != null )
        {
            bTemplate = bBeanItem.getBean();
        }
        else
        {
            bTemplate = aBeanItem.getBean();
        }

        GitChangedFile gitFile = file.getBean();
        String fileDiff = registryManager
                .getFileDiff( aTemplate.getTemplateName(), bTemplate.getTemplateName(), gitFile );
        FileDiffModalView modalView =
                new FileDiffModalView( gitFile.getGitFilePath(), new HorizontalLayout(), fileDiff );

        getUI().addWindow( modalView );
    }
}