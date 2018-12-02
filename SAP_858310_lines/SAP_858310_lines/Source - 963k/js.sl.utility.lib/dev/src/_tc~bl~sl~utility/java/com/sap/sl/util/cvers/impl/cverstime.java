package com.sap.sl.util.cvers.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *  The central CVERS instance to handle the allowed processes
 *  (read and write CVERS)
 *
 *@author     md
 *@created    20. Juni 2003
 *@version    1.0
 */

public class CVersTime {

	public static final String TS_MILLI_FORMAT="yyyyMMddHHmmssSSSS";
	public static final String TS_SEC_FORMAT="yyyyMMddHHmmss";

	/**
	 * Method getTimeStamp(String)
	 * calculates a String value which represents the actual time in seconds
	 * @param format - Please don't use TS_ constants
	 */
	public static String getTimeStamp(String format) {
		Calendar calendar=new GregorianCalendar();
		SimpleDateFormat formatter = new SimpleDateFormat (format);

		return formatter.format(calendar.getTime());
	}

}
