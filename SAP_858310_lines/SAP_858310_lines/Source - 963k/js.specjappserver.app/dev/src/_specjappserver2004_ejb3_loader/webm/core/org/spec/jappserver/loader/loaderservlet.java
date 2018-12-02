package org.spec.jappserver.loader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoaderServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    private static final String TXRATE_PARAM_NAME = "txRate";
    private static final String PARALLELISM_PARAM_NAME = "Parallelism";
    private static final String FLAT_FILE_PARAM_NAME = "FlatFile";
    private static final String FLAT_FILE_PARAM_NAME2 = "Generate flat files (only) using ";
    private static final String FLAT_FILE_DELIMITER_PARAM_NAME = "delimiter";
    private static final String FLAT_FILE_PARAM_NAME3 = " into ";
    private static final String FLAT_FILE_DIR_PARAM_NAME = "directory";

    private static final String BUTTON_PARAM_NAME = "button";
    private static final String BUTTON_START = "Start";
    private static final String BUTTON_STOP = "Stop";
    private static final String BUTTON_REFRESH = "Refresh";
    private static final String BUTTON_RESET = "Reset";
    
    private static final String ONE = "1";
    private static final String DEFAULT_DELIMITER = "|";
    private static final String DEFAULT_DIR = "/tmp";
    
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = 60*MILLIS_PER_SECOND;
    private static final long MILLIS_PER_HOUR = 60*MILLIS_PER_MINUTE;
    
    private HttpServletRequest request;
    private String errorMsg;

    @Override
    public void init() {
    }

    private String getDurationStr (long durationInMillis) {
        long durationInHours = durationInMillis/MILLIS_PER_HOUR;
        durationInMillis -= durationInHours*MILLIS_PER_HOUR;
        long durationInMin = durationInMillis/MILLIS_PER_MINUTE;
        durationInMillis -= durationInMin*MILLIS_PER_MINUTE;
        long durationInSec = durationInMillis/MILLIS_PER_SECOND;
        return (durationInHours > 0 ? durationInHours + "h " : "") +
                (durationInHours > 0 || durationInMin > 0 ? durationInMin + "m " : "") +
                durationInSec + "s";
    }

    private void addErrMsg(String msg) {
        if (errorMsg == null) {
            errorMsg = "";
        } else {
            errorMsg += "\n";
        }
        errorMsg += msg;
    }
    
    private static int step(int scale) {
        // As per osgjava-6525
        // step = 10 ^ (INT (LOG(IR)));
        // DBIR = (CEILING( IR /  step)) * step;
        final double log10 = Math.log(10.0);
        final double step = Math.floor(Math.pow(10.0, Math.floor(Math.log(scale) / log10)));
        final int dbIR = (int) Math.ceil(scale / step) * (int) step;
        return dbIR;
    }

    private String getParameterStr(String param) {
        String str = request.getParameter(param);
        request.getSession().setAttribute(param, str);
        return str;
    }
    
    private int getParameter(String param) {
        String str = getParameterStr(param);
        int result = 1;
        try {
            result = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            addErrMsg("Invalid " + param + ": " + str);
        }
        if (result <= 0) {
            addErrMsg(param + " must be 1 or bigger.");
        }
        return result;
    }

    private String getDirParameter() {
        final String checked = request.getParameter(FLAT_FILE_PARAM_NAME);
        request.getSession().setAttribute(FLAT_FILE_PARAM_NAME, checked);
        final String flatFilesDir = request.getParameter(FLAT_FILE_DIR_PARAM_NAME);
        request.getSession().setAttribute(FLAT_FILE_DIR_PARAM_NAME, flatFilesDir);
        final boolean isChecked = checked != null;
        if (isChecked) {
            if (flatFilesDir == null || flatFilesDir.length() == 0) {
                addErrMsg("Cannot generate flat files since no directory was defined.");
            } else if (!(new File(flatFilesDir)).isDirectory()) {
                addErrMsg("Cannot generate flat files since the directory "
                        + flatFilesDir + " does not exist.");
            } else if ((new File(flatFilesDir)).list().length != 0) {
                addErrMsg("Cannot generate flat files since the directory " + flatFilesDir + " is not empty.");
            }
        }
        return isChecked ? flatFilesDir : null;
    }

    private void setDefaultSessionAttributes()
    {
        HttpSession session = request.getSession();
        if (session.getAttribute(TXRATE_PARAM_NAME) == null) {
            session.setAttribute(TXRATE_PARAM_NAME, ONE);
        }
        if (session.getAttribute(PARALLELISM_PARAM_NAME) == null) {
            session.setAttribute(PARALLELISM_PARAM_NAME, ONE);
        }
        if (session.getAttribute(FLAT_FILE_DELIMITER_PARAM_NAME) == null) {
            session.setAttribute(FLAT_FILE_DELIMITER_PARAM_NAME, DEFAULT_DELIMITER);
        }
        if (session.getAttribute(FLAT_FILE_DIR_PARAM_NAME) == null) {
            session.setAttribute(FLAT_FILE_DIR_PARAM_NAME, DEFAULT_DIR);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            this.request = request;
            errorMsg = null;
            setDefaultSessionAttributes();
            
            String button = request.getParameter(BUTTON_PARAM_NAME);
            if (button != null) {
                if (button.equals(BUTTON_START)) {
                    if (MessageHelper.isStatusRunning()) {
                        addErrMsg("Cannot start task since it is running. Please stop it first.");
                    } else {
                        MessageHelper.clearStatusMessages();
                        final int txRate = getParameter(TXRATE_PARAM_NAME);
                        final int parallelism = getParameter(PARALLELISM_PARAM_NAME);
                        final String delimiter = getParameterStr(FLAT_FILE_DELIMITER_PARAM_NAME);
                        final String flatFilesDirectory = getDirParameter();
                        if (errorMsg == null) {
                            final int dbIR = step(txRate);
                            StatusMessage statusMessage = new StatusMessage("Starting "
                                    + (flatFilesDirectory == null ? "loading of database"
                                            : "generating flat files using delimiter '" + delimiter
                                                    + "' into directory " + flatFilesDirectory)
                                    + " with supplied txRate = " + txRate + ", stepped database IR = " + dbIR
                                    + ", max. parallelism = " + parallelism, StatusMessage.STATUS_STARTING);
                            MessageHelper.sendStatusMessage(statusMessage);
                            ActionMessage message = new ActionMessage();
                            message.setDbIR(dbIR);
                            message.setParallelism(parallelism);
                            message.setFlatFilesDirectory(flatFilesDirectory);
                            message.setFlatFileDelimiter(delimiter);
                            message.setRootSeed(new Random().nextLong());
                            MessageHelper.sendLoadMessage(message);
                        }
                    }
                } else if (button.equals(BUTTON_STOP)) {
                    if (MessageHelper.isStatusRunning()) {
                        StatusMessage statusMessage = new StatusMessage("Stopping task ... please wait, ",
                                StatusMessage.STATUS_STOPPING);
                        MessageHelper.sendStatusMessage(statusMessage);
                    } else {
                        addErrMsg("Cannot stop task since it is not running.");
                    }
                } else if (button.equals(BUTTON_RESET)) {
                    MessageHelper.clearStatusMessages();
                }
            }

            // refresh button, just show status page
            writePage(response.getWriter());
        } catch (Throwable t) {
            t.printStackTrace(response.getWriter());
        }
    }
    
    private void writeTextField(PrintWriter responseWriter, String name) {
        responseWriter.println(name);
        String value = (String)request.getSession().getAttribute(name);
        responseWriter.println("<input type=\"text\" name=\"" + name + "\" value=\"" + value + "\" size=6/>");
    }
    
    private void writeButton(PrintWriter responseWriter, String button) {
        responseWriter.println("<input type=\"submit\" name=\"" + BUTTON_PARAM_NAME + "\" value=\"" + button + "\"/>");
    }
    
    private void writePage(PrintWriter responseWriter) {
        responseWriter.println("<html>");
        responseWriter.println("<head>");
        responseWriter.println("<title>SPECjAppServer Loader</title>");
        responseWriter.println("</head>");
        responseWriter.println("<body>");
        responseWriter.println("<h2>SPECjAppServer Loader</h2>");
        responseWriter.println("Please do <b>not</b> try to start a task <b>from another client</b> at the same time.<br>");
        responseWriter.println("After loading the database please <b>restart the SPECjAppServer application or refresh cached data</b> from database (e.g. counters used in table generators) since the data is loaded directly using JDBC - SPECjAppServer uses JPA.<br>");
        responseWriter.println("<br>");
        responseWriter.println("If <b>flat files</b> are generated in a cluster running on more than one server then<br>");
        responseWriter.println("1) If parallelism > 1 a flat file for a table might be split into different flat files on different servers.<br>");
        responseWriter.println("2) It cannot be checked properly if the flat file directories are empty i.e. there might be undiscovered old files to which the data is appended.<br>");
        try {
            final List<StatusMessage> statusMessages = MessageHelper.getStatusMessages();
            responseWriter.println("<h3>Status</h3>");
            responseWriter.println("<pre>");
            if (statusMessages.size() > 0) {
                for (StatusMessage message : statusMessages) {
                    responseWriter.println(message.getValueStr());
                }
                responseWriter.println();

                StatusMessage started = MessageHelper.isStatus(StatusMessage.STATUS_STARTING);
                if (MessageHelper.isStatusRunning()) {
                    if (started == null) {
                        responseWriter.println("Task not finished yet ...");
                    } else {
                        responseWriter.println("Task not finished yet ... (" +
                                getDurationStr(System.currentTimeMillis() - started.getTimestamp()) +
                                ").");
                    }
                } else {
                    if (MessageHelper.isStatusStopping()) {
                        responseWriter.println("Task has been stopped.");
                    } else {
                        StatusMessage ended = MessageHelper.isStatus(StatusMessage.STATUS_ENDED);
                        String durationStr = ".";
                        if (started != null && ended != null) {
                            durationStr = " (" + getDurationStr(ended.getTimestamp() - started.getTimestamp())+ ").";
                        }
                        if (MessageHelper.isFinishedSuccessfully()) {
                            responseWriter.println("Task finished successfully" + durationStr);
                        } else {
                            StatusMessage sm = MessageHelper.isStatus(StatusMessage.STATUS_FAILED);
                            sm.getThrowable().printStackTrace(responseWriter);
                            responseWriter.println();
                            responseWriter.println("Task finished unsuccessfully" + durationStr);
                        }
                    }
                }
            } else {
                responseWriter.println("Nothing started yet.");
            }
            
            responseWriter.println("</pre>");

            if (errorMsg != null) {
                responseWriter.println("<h3>Error</h3>");
                responseWriter.println("<pre>");
                responseWriter.println(errorMsg);
                responseWriter.println("</pre>");
            }
            
            responseWriter.println("<form name=\"loader\" action=\"load\" method=\"GET\">");
            if (MessageHelper.isStatusRunning()) {
                writeButton(responseWriter, BUTTON_REFRESH);
                if (MessageHelper.isStatusStopping()) {
                    StatusMessage message = MessageHelper.isStatus(StatusMessage.STATUS_STOPPING);
                    long elapsedMillis = System.currentTimeMillis() - message.getTimestamp();
                    if (elapsedMillis > 3*MessageHelper.SLEEP_TIME_IN_MILLIS)
                    {
                        responseWriter.println("<pre>");
                        responseWriter.println("Task was tried to be stopped before " + (elapsedMillis/1000) + " seconds.");
                        responseWriter.println("");
                        if (MessageHelper.isStatusDeletingTable()) {
                            responseWriter.println("Most probably the task cannot be stopped since a big table is deleted on the database which cannot be interrupted.");
                            responseWriter.println("Please wait until deleting of this table has finished.");
                            responseWriter.println("");
                        }
                        responseWriter.println("Sometimes we cannot recover from errors during the task. Please have a look into your server log, too.");
                        responseWriter.println("A typical error is that the datasource in LoaderMDB couldn't be injected.");
                        responseWriter.println("After you have checked manually that there is no task thread running you can try to reset and then restart the task.");
                        responseWriter.println("</pre>");
                        writeButton(responseWriter, BUTTON_RESET);
                    }
                } else {
                    writeButton(responseWriter, BUTTON_STOP);
                }
            } else {
                responseWriter.println("<br>");
                writeTextField(responseWriter, TXRATE_PARAM_NAME);
                writeTextField(responseWriter, PARALLELISM_PARAM_NAME);
                writeButton(responseWriter, BUTTON_START);
                responseWriter.println("<br>");
                boolean isChecked = request.getSession().getAttribute(FLAT_FILE_PARAM_NAME) != null;
                responseWriter.println("<input type=\"checkbox\" name=\"" + FLAT_FILE_PARAM_NAME + "\" value=\""
                        + FLAT_FILE_PARAM_NAME + "\"" + (isChecked ? "checked" : "") + "/>" + FLAT_FILE_PARAM_NAME2);
                writeTextField(responseWriter, FLAT_FILE_DELIMITER_PARAM_NAME);
                responseWriter.println(FLAT_FILE_PARAM_NAME3);
                writeTextField(responseWriter, FLAT_FILE_DIR_PARAM_NAME);
            }
            responseWriter.println("</form>");
        } catch (Exception e) {
            responseWriter.println("<h4>Error</h4>");
            responseWriter.println("<pre>");
            e.printStackTrace(responseWriter);
            responseWriter.println("</pre>");
        }
        responseWriter.println("</body>");
        responseWriter.println("</html>");
    }
}
