package com.sap.dictionary.database.dbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;
import java.text.*;
import com.sap.tc.logging.*;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbTraceFormatter extends TraceFormatter {
	
	private final static int INITIAL_CAPACITY = 512;
	private static final Format DATE_FORMATTER = 
	                      new SimpleDateFormat("yyyy-MM-dd",Locale.US);
	private static final Format TIME_FORMATTER = 
	                      new SimpleDateFormat("H:mm:ss",Locale.US);
	private static final String SUBMIT_INFO = getSubmitInfo();
	//private static final int POS1 = 31;
	//private static final int POS2 = 89;
	
	public String format(LogRecord rec) {
  	//System.out.println("getApplication " + rec.getApplication());	
  	//System.out.println("getArgObjs " + rec.getArgObjs());
  	//System.out.println("getArgs " + rec.getArgs());
  	//System.out.println("getDsrComponent " + rec.getDsrComponent());
  	//System.out.println("getDsrTransaction " + rec.getDsrTransaction());
  	//System.out.println("getDsrUser " + rec.getDsrUser());
  	//System.out.println("getGroup " + rec.getGroup());
  	//System.out.println("getGroupId " + rec.getGroupId());
  	//System.out.println("getGroups " + rec.getGroups());
  	//System.out.println("getId " + rec.getId());
  	//System.out.println("getIndent " + rec.getIndent());
  	//System.out.println("getLevel " + rec.getLevel());
  	//System.out.println("getLocation " + rec.getLocation());
  	//System.out.println("getMsgClear " + rec.getMsgClear());
  	//System.out.println("getMsgCode " + rec.getMsgCode());
  	//System.out.println("getMsgType " + rec.getMsgType());
  	//System.out.println("getRelatives " + rec.getRelatives());
  	//System.out.println("getResourceBundleName " + rec.getResourceBundleName());
  	//System.out.println("getSession " + rec.getSession());
  	//System.out.println("getSeverity " + rec.getSeverity());
  	//System.out.println("getSource " + rec.getSource());
  	//System.out.println("getSourceName " + rec.getSourceName().substring(28));
  	//System.out.println("getThread " + rec.getThread());
  	//System.out.println("getThreadName " + rec.getThreadName());
  	//System.out.println("getTime " + rec.getTime());
  	//System.out.println("getTransaction " + rec.getTransaction());
  	//System.out.println("getUser " + rec.getUser());

		StringBuffer res = new StringBuffer(INITIAL_CAPACITY);
		int sev = rec.getSeverity();
		//int pos = POS1;
		if (sev >= Severity.ERROR) {
			//pos = POS2;
			res.append(EOL);
			// res.append("E R R O R *******E R R O R********E R R O R*************");
			res.append("E R R O R *******");
			res.append(" (");
			res.append(rec.getLocationName().substring(32));
			res.append(" ");
			res.append(SUBMIT_INFO);
			res.append(")");
			res.append(EOL);
		}
		res.append(TIME_FORMATTER.format(rec.getTime()));
		res.append(" ");
		res.append(DATE_FORMATTER.format(rec.getTime()));
		res.append(" ");
		res.append(rec.getLocationName().substring(28,31));
		res.append("-");
		res.append(Severity.toString(sev));
		res.append(":");
		res.append("  ");
		try{
			res.append(
      		MessageFormat.format(rec.getMsgClear(),rec.getArgs().toArray()));
      		/*
      res.insert(pos,
      		MessageFormat.format(rec.getMsgClear(),rec.getArgs().toArray()));
      		*/
    }
    catch(IllegalArgumentException e){
    	res.append(rec.getMsgClear());
    }
    //if (sev >= Severity.ERROR) {
    //	res.append(" (");
		//	res.append(rec.getSourceName().substring(32));
		//	res.append(")");
		//}
    //res.append("]");
		//res.append(" ");
		//res.append(rec.getSourceName().substring(28));
		res.append(EOL);
		
    String result = TIME_FORMATTER.format(rec.getTime()) + " " + 
    					Severity.toString(rec.getSeverity()) + " " + 
    					rec.getLocationName().substring(28,Math.min(
    										58,rec.getLocationName().length())) + " " + 
    								 rec.getMsgClear() + "\n";  
  	//String result = super.format(log);
  	//System.out.println("result of trace format" + result);
  	return res.toString(); 	 
  }
	
	private static String getSubmitInfo() {
		InputStream stream = DbDeployConfig.class.getResourceAsStream("config/"
		    + "submit" + ".txt");
		if (stream == null)
			return "no submit info";
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String res = "no submit info";
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("*") || line.startsWith("//") || line.length() == 0)
					continue;
				res = line;
			}
			reader.close();
			stream.close();
		} catch (IOException e) {
			return "no submit info";
		}
		return res;
	}

}
