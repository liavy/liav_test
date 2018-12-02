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

/*
 * JMS message flow:
 * LoaderServlet -> controller thread of LoaderMDB
 *                  controller thread of LoaderMDB -> threads of LoaderMDB which are loading table sections

 * It describes the actions a (jms) thread has to execute.
 * And it delivers the data needed to execute those actions.
 */
public class ActionMessage extends LoadMessage implements Cloneable, Comparable<ActionMessage> {

    private static final long serialVersionUID = -4380774199352658345L;

    private int dbIR;
    private int parallelism;
    private long rootSeed;
    private int start;
    private int num;
    private String flatFilesDirectory;
    private String delimiter;

    public int getDbIR() {
        return dbIR;
    }
    public void setDbIR(int dbIR) {
        this.dbIR = dbIR;
    }
    public int getNumPerThread() {
        return num;
    }
    public void setNumPerThread(int num) {
        this.num = num;
    }
    public int getParallelism() {
        return parallelism;
    }
    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }
    public long getRootSeed() {
        return rootSeed;
    }
    public void setRootSeed(long rootSeed) {
        this.rootSeed = rootSeed;
    }
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public String getFlatFilesDirectory() {
        return flatFilesDirectory;
    }
    public void setFlatFilesDirectory(String flatFilesDirectory) {
        this.flatFilesDirectory = flatFilesDirectory;
    }
    public String getFlatFileDelimiter() {
        return delimiter;
    }
    public void setFlatFileDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String getValueStr() {
        StringBuffer str = new StringBuffer();
        str.append(super.getValueStr());
        str.append(", dbIR=");
        str.append(getDbIR());
        str.append(", numPerThread=");
        str.append(getNumPerThread());
        str.append(", parallelism=");
        str.append(getParallelism());
        str.append(", rootSeed=");
        str.append(getRootSeed());
        str.append(", start=");
        str.append(getStart());
        str.append(", flatFilesDirectory=");
        str.append(getFlatFilesDirectory());
        str.append(", flatFileDelimiter=");
        str.append(getFlatFileDelimiter());
        return str.toString();
    }

    @Override
    public String toString() {
        return "[ActionMessage " + getValueStr() + "]";
    }

    @Override
    public ActionMessage clone() throws CloneNotSupportedException {
        ActionMessage am = (ActionMessage)super.clone();
        return am;
    }

    public int compareTo(ActionMessage am) {
        if (getThread() < am.getThread()) {
            return -1;
        }
        
        if (getThread() > am.getThread()) {
            return 1;
        }
        
        return MessageHelper.getCommaSeperatedList(getTables()).compareTo(MessageHelper.getCommaSeperatedList(am.getTables()));
    }
    
    @Override
    public boolean equals(Object am) {
        return compareTo((ActionMessage) am) == 0;
    }
    
    @Override
    public int hashCode() {
        return MessageHelper.getCommaSeperatedList(getTables()).hashCode() + getThread();
    }
}
