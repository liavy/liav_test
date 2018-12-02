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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.JMSException;
import javax.naming.NamingException;

/*
 * Super class of the domain loader classes.
 */
public abstract class Load  {

    static final int FIRST_THREAD_NUMBER = 1;
    static final int LOAD_IN_ONE_SECTION = 1;
    
    String dbKey;
    
    final ActionMessage message;

    final int scale;
    final int parallelism;

    final String flatFilesDirectory;
    final String delimiter;
    final boolean isFlatFile;
    
    final SeedGenerator seedGen;
    final RandNum r;

    final int numCustomers;
    final int numAssemblies;
    final int numItems;
    final int numOrders;
    
    Set<ActionMessage> runningThreads = new HashSet<ActionMessage>();

    Load(ActionMessage message) {
        this.message = message;
        scale = message.getDbIR();
        parallelism = message.getParallelism();

        flatFilesDirectory = message.getFlatFilesDirectory();
        delimiter = message.getFlatFileDelimiter();
        isFlatFile = flatFilesDirectory != null;

        seedGen = new SeedGenerator(message.getRootSeed());
        r = new RandNum();

        numCustomers = Helper.getNumCustomers(scale);
        numAssemblies = Helper.getNumAssemblies(scale);
        numItems = numAssemblies;
        numOrders = Helper.getNumOrders(scale);
    }

    public int getStartedThreads() {
        return runningThreads.size();
    }
    
    Connection getConnection() throws SQLException, NamingException
    {
        return getConnection(dbKey);
    }
    
    Connection getConnection(String dbKey) throws SQLException, NamingException
    {
        if (isFlatFile) {
            return new FlatFileConnection(flatFilesDirectory, delimiter);
        } 
        return DatabaseHelper.getConnection(dbKey);
    }
    
    void cleanTable(String table) throws NamingException, JMSException, SQLException
    {
        if (MessageHelper.isInterrupted()) return;
        StatusMessage statusMessage = new StatusMessage(message, "Deleting table " + table + ", ",
                StatusMessage.STATUS_DELETING);
        statusMessage.setTables(new String[] {table});
        MessageHelper.sendStatusMessage(statusMessage);
        DatabaseHelper.clean(getConnection(), table);
        statusMessage = new StatusMessage(message, "Deleting table " + table + " finished, ",
                StatusMessage.STATUS_DELETING_FINISHED);
        statusMessage.setTables(new String[] {table});
        MessageHelper.sendStatusMessage(statusMessage);
    }
    
    void loadSequence(String id, int nextSeq) throws SQLException, NamingException, JMSException {
        if (MessageHelper.isInterrupted()) return;
        DatabaseHelper.loadSequence(getConnection(), id, nextSeq);
    }

    void addActionMessages(SortedSet<ActionMessage> actionMessages, String table, int num) throws CloneNotSupportedException {
        addActionMessages(actionMessages, new String[] {table}, num);
    }
    
    void addActionMessages(SortedSet<ActionMessage> actionMessages, String[] tables, int num) throws CloneNotSupportedException {
        message.setTables(tables);
        final int threads = Helper.roundedNumThreads(num, parallelism);
        final int numPerThread = num/threads;
        message.setNumPerThread(numPerThread);
        for (int thread = 0; thread < threads; thread++) {
            message.setThread(thread);
            message.setStart(thread*numPerThread + FIRST_THREAD_NUMBER);
            actionMessages.add(message.clone());
        }
    }

    abstract void addActionMessages(TreeSet<ActionMessage> actionMessages) throws CloneNotSupportedException;
    abstract void loadSequences() throws SQLException, NamingException, JMSException;

    abstract boolean loadTableSection(final String[] tables, final int start, final int num)  throws SQLException, NamingException, JMSException;
    
    void loadTableSection() throws JMSException, NamingException, SQLException {
        String[] tables = message.getTables();
        int start = message.getStart();

        if (!isFlatFile) {
            // delete tables if first section of tables is going to be loaded
            boolean isFirstThread = start == FIRST_THREAD_NUMBER;
            for (String table : tables) {
                if (isFirstThread) {
                        cleanTable(table);
                } else {
                    // or wait till table has been deleted
                    MessageHelper.waitForTableDeleted(table);
                }
            }
        }

        // load table section
        StatusMessage statusMessage = new StatusMessage(message, "Generating ", StatusMessage.STATUS_STARTED);
        MessageHelper.sendStatusMessage(statusMessage);
        boolean isSuccessfullyFinished = false;
        if (!MessageHelper.isInterrupted()) {
            isSuccessfullyFinished = loadTableSection(tables, start, message.getNumPerThread());
        }
        statusMessage.setStatus(isSuccessfullyFinished ? StatusMessage.STATUS_SUCCESSFULLY_FINISHED : StatusMessage.STATUS_STOPPED);
        MessageHelper.sendStatusMessage(statusMessage);
    }

    public void loadAll(TreeSet<ActionMessage> actionMessages) throws NamingException, JMSException {
        boolean isInterrupted = false;
        while (actionMessages.size() > 0 || getStartedThreads() > 0) {
            isInterrupted |= MessageHelper.isInterrupted();

            if (!isInterrupted) {
                // start threads for loading table sections (if not interrupted e.g. client stopped it or exception occured)
                while (getStartedThreads() < parallelism && actionMessages.size() > 0 ) {
                    ActionMessage am = actionMessages.first();
                    actionMessages.remove(am);
                    runningThreads.add(am);
                    MessageHelper.sendLoadMessage(am);
                }
            }
            
            // check for threads who have been ended
            boolean isAThreadEnded = false;
            Iterator<ActionMessage> iter = runningThreads.iterator();
            while (iter.hasNext()) {
                ActionMessage am = iter.next();
                if (!MessageHelper.isStatusRunning(am)) {
                    isAThreadEnded = true;
                    iter.remove();
                }
            }

            // if interrupted wait till all threads have been stopped
            if (isInterrupted && getStartedThreads() == 0) {
                return;
            }
            
            if (!isAThreadEnded) {
                try {
                    Thread.sleep(MessageHelper.SLEEP_TIME_IN_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
