package io.subutai.core.pluginmanager.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.pluginmanager.ui.PluginManagerComponent;
import io.subutai.core.pluginmanager.ui.PluginManagerPortalModule;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.pluginmanager.api.PluginManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class PluginManagerPortalModuleTest
{
    private PluginManagerPortalModule portalModule;

    @Mock
    PluginManager pluginManager;
    @Mock
    Tracker tracker;

    @Before
    public void setUp() throws Exception
    {
        portalModule = new PluginManagerPortalModule( pluginManager, tracker );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( portalModule.MODULE_NAME, portalModule.getId() );
        assertEquals( portalModule.MODULE_NAME, portalModule.getName() );
        assertNotNull( portalModule.getImage() );
        assertTrue( portalModule.isCorePlugin() );
        portalModule.init();
        portalModule.destroy();
    }


    @Test
    public void testCreateComponent() throws Exception
    {
        assertTrue( portalModule.createComponent() instanceof PluginManagerComponent );
    }
}