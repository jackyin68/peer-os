/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb.window;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 */
public class ProgressWindow extends Window {

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private volatile boolean track = true;
    private final UUID trackID;

    public ProgressWindow(UUID trackID) {
        super("Operation progress");
        setModal(true);
        setClosable(false);
        setWidth(600, ProgressWindow.UNITS_PIXELS);

        this.trackID = trackID;

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(43);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                track = false;
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
        indicator.setVisible(false);

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent(indicator);
        bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        bottomContent.addComponent(ok);

        content.addComponent(bottomContent);
        content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

        addComponent(content);

        start();
    }

    @Override
    protected void close() {
        super.close();
        track = false;
    }

    private void start() {
        MongoUI.getExecutor().execute(new Runnable() {

            public void run() {
                showProgress();
                MongoUI.getExecutor().execute(new Runnable() {

                    public void run() {
                        while (track) {
                            ProductOperationView po = MongoUI.getMongoManager().getProductOperationView(trackID);
                            if (po != null) {
                                setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                                if (po.getState() != ProductOperationState.RUNNING) {
                                    hideProgress();
                                    break;
                                }
                            } else {
                                setOutput("Product operation not found. Check logs");
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                break;
                            }
                        }
                    }
                });
            }

        });

    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
    }

    private void setOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
