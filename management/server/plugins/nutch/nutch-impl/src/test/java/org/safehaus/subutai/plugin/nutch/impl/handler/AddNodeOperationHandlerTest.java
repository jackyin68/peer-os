package org.safehaus.subutai.plugin.nutch.impl.handler;

/* TODO Rewrite tests
import org.junit.Test;
import org.safehaus.subutai.api.nutch.Config;
import org.safehaus.subutai.impl.nutch.NutchImpl;
import org.safehaus.subutai.impl.nutch.handler.mock.LuceneImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class AddNodeOperationHandlerTest {


	@Test
	public void testWithoutCluster() {
		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(new LuceneImplMock(), "test-cluster",
				"lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getTrackerOperation().getLog().contains("not exist"));
		assertEquals(operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testWithExistingCluster() {
		NutchImpl impl = new LuceneImplMock().setClusterConfig(new Config());
		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(impl, "test-cluster", "lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getTrackerOperation().getLog().contains("not connected"));
		assertEquals(operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED);
	}

}
*/