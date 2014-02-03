package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

import java.util.List;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.Config;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.HBaseClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

public class HBaseTable extends Table {

//    private IndexedContainer container;
    private final ServiceManager manager;
//    private NodesWindow nodesWindow;
    HBaseCommandEnum cce;
    Button selectedStartButton;
    Button selectedStopButton;
    Item selectedItem;
    Config selectedConfig;

    public HBaseTable() {
        setSizeFull();
        this.manager = new ServiceManager();
        this.setCaption("HBase clusters");
        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getCassandraContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(HBaseClusterInfo.UUID_LABEL, String.class, "");
//        container.addContainerProperty(HBaseClusterInfo.DOMAINNAME_LABEL, String.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
//        container.addContainerProperty("Status", Button.class, "");
        container.addContainerProperty("Manage", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        List<Config> cdList = HBaseDAO.getClusterInfo();
        for (Config config : cdList) {
            addClusterDataToContainer(container, config);
        }
        return container;
    }

    private void addClusterDataToContainer(final Container container, final Config config) {
        final Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty(HBaseClusterInfo.UUID_LABEL).setValue(config.getUuid());
//        item.getItemProperty(HBaseClusterInfo.DOMAINNAME_LABEL).setValue(config.get);

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
//                getWindow().showNotification("Starting cassandra cluster: " + cci.getName());
//                cce = HBaseCommandEnum.START;
//                selectedItem = item;
//                manager.runCommand(cci.getNodes(), cce);
            }
        });

        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
//                getWindow().showNotification("Stopping cassandra cluster: " + cci.getName());
//                cce = HBaseCommandEnum.STOP;
//                selectedItem = item;
//                manager.runCommand(cci.getNodes(), cce);

            }
        });

//        Button statusButton = new Button("Status");
//        statusButton.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                cce = HBaseCommandEnum.STATUS;
//                manager.runCommand(cci.getNodes(), cce);
//            }
//        });
        Button manageButton = new Button("Manage");
        manageButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
//                HBaseClusterInfo info = HBaseDAO.getHBaseClusterInfoByUUID(cci.getUuid());
//                nodesWindow = new NodesWindow(info, manager);
//                getApplication().getMainWindow().addWindow(nodesWindow);

            }
        });

        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                selectedConfig = config;
                getWindow().showNotification("Destroying HBase cluster: " + config.getUuid());
                cce = HBaseCommandEnum.PURGE;
//                manager.runCommand(cci.getAllnodes(), cce);
            }
        });

        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
//        item.getItemProperty("Status").setValue(statusButton);
        item.getItemProperty("Manage").setValue(manageButton);
        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public void refreshDatasource() {
        this.setContainerDataSource(getCassandraContainer());
    }

//    public NodesWindow getNodesWindow() {
//        return nodesWindow;
//    }
    private void switchState(Boolean state) {
        Button start = (Button) selectedItem.getItemProperty("Start").getValue();
        start.setEnabled(state);
        Button stop = (Button) selectedItem.getItemProperty("Stop").getValue();
        stop.setEnabled(!state);
    }

    public void onResponse(Response response) {
        if (manager.getCurrentTask() != null && response.getTaskUuid() != null
                && manager.getCurrentTask().getUuid().compareTo(response.getTaskUuid()) == 0) {
            List<ParseResult> list = RequestUtil.parseTask(response.getTaskUuid(), true);
            Task task = RequestUtil.getTask(response.getTaskUuid());
            if (!list.isEmpty()) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    manager.moveToNextTask();
                    if (manager.getCurrentTask() != null) {
                        for (Command command : manager.getCurrentTask().getCommands()) {
                            manager.executeCommand(command);
                        }
                    } else {
//                        if (nodesWindow != null && nodesWindow.isVisible()) {
//                            nodesWindow.updateUI(task);
//                        }
                        manageUI(task.getTaskStatus());
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
//                    if (nodesWindow != null && nodesWindow.isVisible()) {
//                        nodesWindow.updateUI(task);
//                    }
                }
            }
        }
    }

    private void manageUI(TaskStatus ts) {
        if (cce != null) {
            switch (cce) {
                case START: {

                    switch (ts) {
                        case SUCCESS: {
                            getWindow().showNotification("Start success");
                            switchState(false);
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Start failed. Please use Terminal to check the problem");
                            break;
                        }
                    }
                    break;

                }
                case STOP: {

                    switch (ts) {
                        case SUCCESS: {
                            getWindow().showNotification("Stop success");
                            switchState(true);
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Stop failed. Please use Terminal to check the problem");
                            break;
                        }
                    }
                    break;
                }
                case PURGE: {
                    switch (ts) {
                        case SUCCESS: {
                            getWindow().showNotification("Purge success");
                            if (HBaseDAO
                                    .deleteHBaseClusterInfo(selectedConfig.getUuid())) {
//                    container.removeItem(itemId);
                                refreshDatasource();
                            }
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Purge failed. Please remove using Terminal");
                            break;
                        }
                    }
                    break;
                }
            }
        }
        cce = null;
    }
}
