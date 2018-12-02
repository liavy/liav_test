/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/*
 * Tables are loaded asynchonously by using jms messages
 * which are processed by jms threads in parallel
 * on different cluster nodes on different servers.
 *
 * 1) This MDB is asynchonously initiated by a ActionMessage from the LoaderServlet.
 * 2) It controls loading the database table sections:
 * For each section of a table a ActionMessage is sent (in parallel)
 * which starts loading this table section in a separate thread.
 * When this thread has finished it sents a status message back to this MDB
 * which waits till all table section have been loaded.
 * 3) When all table section have been loaded a Status Message is sent back to LoaderServlet.
 */
@MessageDriven(mappedName = "jms/LoaderQueue",
        activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                             @ActivationConfigProperty(propertyName = "connectionFactoryName", propertyValue = "jms/LoaderQueueConnectionFactory")
})
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LoaderMDB implements MessageListener {

    private static final Logger logger = Logger.getLogger(LoaderMDB.class.getName());

    public void onMessage(Message message) {
        Load loader = null;
        try {
            ActionMessage loadMessage = (ActionMessage)((ObjectMessage)message).getObject();
            logger.fine("Received message in LoaderMDB:\n" + loadMessage.toString());
            String domain = loadMessage.getDomain();
            if (domain == null) {
                try {
                    TreeSet<ActionMessage> actionMessages = new TreeSet<ActionMessage>();
                    loader = new LoadCorp(loadMessage);
                    loader.addActionMessages(actionMessages);
                    loader.loadSequences();
                    loader = new LoadOrds(loadMessage);
                    loader.addActionMessages(actionMessages);
                    loader.loadSequences();
                    loader = new LoadMfg(loadMessage);
                    loader.addActionMessages(actionMessages);
                    loader.loadSequences();
                    loader = new LoadSupp(loadMessage);
                    loader.addActionMessages(actionMessages);
                    loader.loadSequences();
                    loader.loadAll(actionMessages);
                } catch (Throwable t) {
                    t.printStackTrace();

                    String errorMessage = "Thread controller has failed";
                    if (loader != null && loader.getStartedThreads() > 0) {
                        errorMessage += "\nDetails: The thread controller of the SPECjAppServer database loader has failed.";
                        errorMessage += "\nAt the failure time there were still " + loader.getStartedThreads() + " threads running.";
                        errorMessage += "\nPlease make sure that there are no other threads running before starting another task.";
                    }
                    StatusMessage statusMessage = new StatusMessage(errorMessage, StatusMessage.STATUS_FAILED);
                    statusMessage.setThrowable(t);
                    MessageHelper.sendStatusMessage(statusMessage);
                }
                
                // send message that task has ended and all threads finished,
                // this does not mean that it was successful or not - could be failed or stopped
                StatusMessage statusMessage = new StatusMessage("Task has ended", StatusMessage.STATUS_ENDED);
                MessageHelper.sendStatusMessage(statusMessage);
            } else {
                try {
                    // loading sections of tables, forward message to domain
                    if (domain.equals(LoadMessage.CORPORATE_DOMAIN)) {
                        loader = new LoadCorp(loadMessage);
                    } else if (domain.equals(LoadMessage.ORDER_DOMAIN)) {
                        loader = new LoadOrds(loadMessage);
                    } else if (domain.equals(LoadMessage.MANUFACTURER_DOMAIN)) {
                        loader = new LoadMfg(loadMessage);
                    } else if (domain.equals(LoadMessage.SUPPLIER_DOMAIN)) {
                        loader = new LoadSupp(loadMessage);
                    } else {
                        throw new IllegalArgumentException ("Domain " + domain + " is unknown.");
                    }
                    loader.loadTableSection();
                } catch (Throwable t) {
                    t.printStackTrace();

                    StatusMessage statusMessage = new StatusMessage(loadMessage, "Thread has failed while working on ", StatusMessage.STATUS_FAILED);
                    statusMessage.setThrowable(t);
                    MessageHelper.sendStatusMessage(statusMessage);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
