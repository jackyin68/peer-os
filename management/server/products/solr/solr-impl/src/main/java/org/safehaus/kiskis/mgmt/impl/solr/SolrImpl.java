package org.safehaus.kiskis.mgmt.impl.solr;

import java.util.ArrayList;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.solr.Solr;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

public class SolrImpl implements Solr {

    private static final Logger LOG = Logger.getLogger(SolrImpl.class.getName());

    public static final String MODULE_NAME = "Solr";
    private TaskRunner taskRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY, "Installing Solr");

        if (po == null) {
            return null;
        }

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                try {
                    po.addLog(String.format("Creating %d lxc containers", config.getNumberOfNodes()));
                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(config.getNumberOfNodes());
                    config.setNodes(new HashSet<Agent>());

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        config.getNodes().addAll(entry.getValue());
                    }

                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {

                        po.addLog("Cluster info saved to DB\nInstalling Solr");

                        //install
                        Task installTask = taskRunner.executeTask(Tasks.getInstallTask(config.getNodes()));

                        if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLogDone("Installation succeeded");
                        } else {
                            po.addLogFailed("Installation failed");
                        }

                    } else {
                        //destroy all lxcs also
                        Set<String> lxcHostnames = new HashSet<String>();
                        for (Agent lxcAgent : config.getNodes()) {
                            lxcHostnames.add(lxcAgent.getHostname());
                        }
                        try {
                            lxcManager.destroyLxcs(lxcHostnames);
                        } catch (LxcDestroyException ex) {
                            po.addLogFailed("Could not save new cluster configuration to DB! Please see logs. Use LXC module to cleanup\nInstallation aborted");
                        }
                        po.addLogFailed("Could not save new cluster configuration to DB! Please see logs\nInstallation aborted");
                    }
                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }

            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Destroying cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                po.addLog("Destroying lxc containers");

                Set<String> lxcHostnames = new HashSet<String>();
                for (Agent lxcAgent : config.getNodes()) {
                    lxcHostnames.add(lxcAgent.getHostname());
                }
                try {
                    lxcManager.destroyLxcs(lxcHostnames);
                    po.addLog("Lxc containers successfully destroyed");
                } catch (LxcDestroyException ex) {
                    po.addLog(String.format("%s, skipping...", ex.getMessage()));
                }

                if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                    po.addLogDone("Cluster info deleted from DB\nDone");
                } else {
                    po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                }

            }
        });

        return po.getId();
    }

    public UUID startNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Starting node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }

                Task startNodeTask = Tasks.getStartTask(node);
                final Task checkNodeTask = Tasks.getStatusTask(node);

                taskRunner.executeTask(startNodeTask, new TaskCallback() {

                    @Override
                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                        if (task.getData() == TaskType.START && task.isCompleted()) {
                            //run status check task
                            return checkNodeTask;

                        } else if (task.getData() == TaskType.STATUS) {
                            if (Util.isFinalResponse(response)) {
                                if (stdOut.contains("is running")) {
                                    task.setData(NodeState.RUNNING);
                                } else if (stdOut.contains("is not running")) {
                                    task.setData(NodeState.STOPPED);
                                }

                                synchronized (task) {
                                    task.notifyAll();
                                }
                            }

                        }

                        return null;
                    }
                });

                synchronized (checkNodeTask) {
                    try {
                        checkNodeTask.wait((checkNodeTask.getAvgTimeout() + startNodeTask.getAvgTimeout()) * 1000 + 1000);
                    } catch (InterruptedException ex) {
                    }
                }

                if (NodeState.RUNNING.equals(checkNodeTask.getData())) {
                    po.addLogDone(String.format("Node on %s started", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to start node %s. %s",
                            lxcHostName,
                            startNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID stopNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Stopping node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }

                Task stopNodeTask = Tasks.getStopTask(node);
                final Task checkNodeTask = Tasks.getStatusTask(node);

                taskRunner.executeTask(stopNodeTask, new TaskCallback() {

                    @Override
                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                        if (task.getData() == TaskType.STOP && task.isCompleted()) {
                            //run status check task
                            return checkNodeTask;

                        } else if (task.getData() == TaskType.STATUS) {
                            if (Util.isFinalResponse(response)) {
                                if (stdOut.contains("is running")) {
                                    task.setData(NodeState.RUNNING);
                                } else if (stdOut.contains("is not running")) {
                                    task.setData(NodeState.STOPPED);
                                }

                                synchronized (task) {
                                    task.notifyAll();
                                }
                            }

                        }

                        return null;
                    }
                });

                synchronized (checkNodeTask) {
                    try {
                        checkNodeTask.wait((checkNodeTask.getAvgTimeout() + stopNodeTask.getAvgTimeout()) * 1000 + 1000);
                    } catch (InterruptedException ex) {
                    }
                }

                if (NodeState.STOPPED.equals(checkNodeTask.getData())) {
                    po.addLogDone(String.format("Node on %s stopped", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to stop node %s. %s",
                            lxcHostName,
                            stopNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID checkNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Checking node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }

                final Task checkNodeTask = taskRunner.executeTask(Tasks.getStatusTask(node));

                NodeState nodeState = NodeState.UNKNOWN;
                if (checkNodeTask.isCompleted()) {
                    Result result = checkNodeTask.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut().contains("is running")) {
                        nodeState = NodeState.RUNNING;
                    } else if (result.getStdOut().contains("is not running")) {
                        nodeState = NodeState.STOPPED;
                    }
                }

                if (NodeState.UNKNOWN.equals(nodeState)) {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            lxcHostName,
                            checkNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                } else {
                    po.addLogDone(String.format("Node %s is %s",
                            lxcHostName,
                            nodeState
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Destroying %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostName);
                if (agent == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }

                if (config.getNodes().size() == 1) {
                    po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }

                config.getNodes().remove(agent);
                //destroy lxc
                po.addLog("Destroying lxc container...");
                Agent physicalAgent = agentManager.getAgentByHostname(agent.getParentHostName());
                if (physicalAgent == null) {
                    po.addLog(
                            String.format("Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                                    agent.getHostname()));
                } else {
                    if (!lxcManager.destroyLxcOnHost(physicalAgent, agent.getHostname())) {
                        po.addLog("Could not destroy lxc container. Use LXC module to cleanup, skipping...");
                    }
                }
                //update db
                po.addLog("Updating db...");
                if (!dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLogFailed(String.format("Error while updating cluster info [%s] in DB. Check logs\nFailed",
                            config.getClusterName()));
                } else {
                    po.addLogDone("Done");
                }
            }
        });

        return po.getId();
    }

    public UUID addNode(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Adding node to %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                try {

                    po.addLog("Creating lxc container");

                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(1);

                    Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

                    //install
                    po.addLog("Installing Solr");

                    Task installTask = taskRunner.executeTask(Tasks.getInstallTask(Util.wrapAgentToSet(lxcAgent)));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLogDone("Installation succeeded");

                        config.getNodes().add(lxcAgent);

                        if (dbManager.saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                            po.addLogDone("Cluster info updated in DB\nDone");
                        } else {
                            po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
                        }
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s",
                                installTask.getResults().entrySet().iterator().next().getValue().getStdErr()));
                    }

                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }
            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {
        try {

            return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getClusters", ex);
        }

        return new ArrayList<Config>();
    }

}
