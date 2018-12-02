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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/*
 * Provides a few convenience methods for sending, retrieving and evaluating loader messages.
 */
public class MessageHelper {
    
    private static final Logger logger = Logger.getLogger(MessageHelper.class.getName());

    static final int SLEEP_TIME_IN_MILLIS = 5000;
    
    static Context context = null;
    static QueueConnectionFactory queueConnFactory = null;
    static Queue statusQueue = null;
    static Queue loadQueue = null;

    static {
        try {
            context = new InitialContext();
            queueConnFactory = (QueueConnectionFactory) context
                    .lookup("java:comp/env/jms/LoaderQueueConnectionFactory");
            statusQueue = (Queue) context
                    .lookup("java:comp/env/jms/LoaderStatusQueue");
            loadQueue = (Queue) context.lookup("java:comp/env/jms/LoaderQueue");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    static String getCommaSeperatedList(String[] list) {
        if (list == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.length; i++)
        {
            if (i != 0) {
                result.append(",");
            }
            result.append(list[i]);
        }
        return result.toString();
    }

    public static List<StatusMessage> getStatusMessages() throws NamingException, JMSException {
        QueueConnection connection = null;
        QueueSession session = null;
        QueueBrowser browser = null;
        try {
            connection = queueConnFactory.createQueueConnection();
            connection.start();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            browser = session.createBrowser(statusQueue);

            final List<StatusMessage> result = new ArrayList<StatusMessage>();
            final Enumeration enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                final StatusMessage message = (StatusMessage)((ObjectMessage) enumeration.nextElement()).getObject();
                result.add(message);
            }
            return result;
        } finally {
            if (browser != null) browser.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }

    public static void clearStatusMessages() throws NamingException, JMSException {
        QueueConnection connection = null;
        QueueSession session = null;
        QueueReceiver receiver = null;
        try {
            connection = queueConnFactory.createQueueConnection();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();

            receiver = session.createReceiver(statusQueue);
            do {
            } while (receiver.receiveNoWait() != null);
        } finally {
            if (receiver != null) receiver.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }

    public static void sendMessage(LoadMessage message, Queue queue) throws JMSException {
        logger.fine("Sending message to queue " + queue.getQueueName() + ":\n" + message.toString());
        QueueConnection connection = null;
        QueueSession session = null;
        QueueSender sender = null;
        try {
            connection = queueConnFactory.createQueueConnection();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            sender = session.createSender(queue);
            connection.start();
            final ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setObject(message);
            sender.send(objectMessage);
        } finally {
            if (sender != null)
                sender.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
        }
    }

    public static void sendStatusMessage(StatusMessage message) throws JMSException {
        logger.info(message.getValueStr());
        sendMessage(message, statusQueue);
    }

    public static void sendLoadMessage(ActionMessage message) throws JMSException {
        sendMessage(message, loadQueue);
    }

    public static boolean isStatusDeletingTable() throws NamingException, JMSException {
        int deleteStarted = 0;
        int deleteFinished = 0;
        List<StatusMessage> statusMessages = getStatusMessages();
        for (StatusMessage statusMessage : statusMessages) {
            if (statusMessage.getStatus().equals(StatusMessage.STATUS_DELETING)) {
                deleteStarted++;
            } else if (statusMessage.getStatus().equals(StatusMessage.STATUS_DELETING_FINISHED)) {
                deleteFinished++;
            }
        }
        return deleteStarted != deleteFinished;
    }

    /*
     * @return true if table successfully deleted, false if not yet deleted
     */
    public static boolean isStatusTableDeleted(String table) throws NamingException, JMSException {
        List<StatusMessage> statusMessages = getStatusMessages();
        for (StatusMessage statusMessage : statusMessages) {
            if (statusMessage.getStatus().equals(StatusMessage.STATUS_DELETING_FINISHED) && statusMessage.getTables()[0].equals(table)) {
                return true;
            }
        }
        return false;
    }

    /*
     * @return true if table successfully deleted, false if something went wrong
     */
    public static boolean waitForTableDeleted(String table) throws NamingException, JMSException {
        while (!isStatusTableDeleted(table)) {
            try {
                Thread.sleep(SLEEP_TIME_IN_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (MessageHelper.isInterrupted()) {
                return false;
            }
        }
        return true;
    }

    public static StatusMessage isStatus(String status) throws NamingException, JMSException {
        List<StatusMessage> statusMessages = getStatusMessages();
        for (StatusMessage statusMessage : statusMessages) {
            if (statusMessage.getStatus().equals(status)) {
                return statusMessage;
            }
        }
        return null;
    }

    public static boolean isStatusStopping() throws NamingException, JMSException {
        return isStatus(StatusMessage.STATUS_STOPPING) != null;
    }
 
    public static boolean isStatusFailed() throws NamingException, JMSException {
        return isStatus(StatusMessage.STATUS_FAILED) != null;
    }
 
    /*
     * true if there is still a thread running, i.e.
     * false if not started,
     * true if stopping but not stopped yet
     */
    public static boolean isStatusRunning() throws NamingException, JMSException {
        return getStatusMessages().size() != 0 && isStatus(StatusMessage.STATUS_ENDED) == null;
    }

    /*
     * precondition: we know that we have started the thread
     * true if the thread is still running, i.e.
     * true if stopping but not stopped yet
     */
    public static boolean isStatusRunning(ActionMessage message) throws NamingException, JMSException {
        String tables = MessageHelper.getCommaSeperatedList(message.getTables());
        List<StatusMessage> statusMessages = getStatusMessages();
        for (StatusMessage statusMessage : statusMessages) {
            String status = statusMessage.getStatus();
            String domain = statusMessage.getDomain();
            String tables2 = MessageHelper.getCommaSeperatedList(statusMessage.getTables());
            if ((status.equals(StatusMessage.STATUS_STOPPED)
                            || status.equals(StatusMessage.STATUS_FAILED)
                            || status.equals(StatusMessage.STATUS_SUCCESSFULLY_FINISHED))
                            && domain != null && domain.equals(message.getDomain())
                            && tables2 != null && tables2.equals(tables)
                            && statusMessage.getThread() == message.getThread()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFinishedSuccessfully() throws NamingException, JMSException {
        StatusMessage sm = isStatus(StatusMessage.STATUS_ENDED);
        if (sm == null) {
            return false;
        }
        List<StatusMessage> statusMessages = getStatusMessages();
        for (StatusMessage statusMessage : statusMessages) {
            String status = statusMessage.getStatus();
            if (status.equals(StatusMessage.STATUS_FAILED)
                    || status.equals(StatusMessage.STATUS_STOPPING)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isInterrupted() throws NamingException, JMSException {
        return isStatusStopping() || isStatusFailed();
    }

    public static class InterruptChecker {
        long lastTimeChecked = 0;

        public boolean isInterrupted() throws NamingException, JMSException {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTimeChecked < SLEEP_TIME_IN_MILLIS) {
                return false;
            }
            lastTimeChecked = currentTime;
            return MessageHelper.isInterrupted();
        }
    }
}
