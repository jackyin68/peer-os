package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.agent;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 10/8/13
 * Time: 8:22 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IAgentSend {
    public Request send(Response response);
}
