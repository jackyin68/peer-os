package org.safehaus.kiskis.mgmt.shared.protocol.api;

import java.util.List;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:40 PM
 */
public interface CommandManagerInterface {

    void executeCommand(Command command);

    void addListener(CommandListener listener);

    void removeListener(CommandListener listener);

    public List<Request> getCommands(UUID taskUuid);

    public Integer getResponseCount(UUID taskuuid);

    public Response getResponse(UUID taskuuid, Integer requestSequenceNumber);

    public List<ParseResult> parseTask(Task task, boolean isResponseDone);

    public void saveResponse(Response response);

    public String saveTask(Task task);

    public List<Task> getTasks();

    public Task getTask(UUID uuid);

    public boolean truncateTables();

    public boolean saveCassandraClusterData(CassandraClusterInfo cluster);

    public List<CassandraClusterInfo> getCassandraClusterData();

    public List<HadoopClusterInfo> getHadoopClusterData();

    public boolean saveHadoopClusterData(HadoopClusterInfo cluster);
}
