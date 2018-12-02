package com.sap.security.core.admin;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

public class TimeZonesBean extends Options {
	public static final String beanId = "timezones";

	private Hashtable timeZonesIds;
	private Vector sortedIds;
	
	// private static Hashtable instances = new Hashtable();
	
	public static synchronized TimeZonesBean getInstance(Locale locale) {
		/* @todo
		String localeStr = locale.toString();
		if ( !instances.containsKey(localeStr) ) {
			instances.put(localeStr, new TimeZonesBean(locale));
		}
		return (TimeZonesBean)instances.get(localeStr);
		*/	
		return new TimeZonesBean(locale);	
	}
	
	private TimeZonesBean(Locale locale) {	
		Locale _locale = (locale==null)?Locale.getDefault():locale;
			
		String[] ids = TimeZone.getAvailableIDs();
		timeZonesIds = new Hashtable(ids.length);
		sortedIds = new Vector(ids.length);

		final String format = "GMT{0}{1,number,00}:{2,number,00} ({3}) {4}";
		Object[] arguments = new Object[5];
		String id = null;
		for ( int i = 0; i < ids.length; i++ ) {
			// get offset in minutes sec from UTC
			id = ids[i];
			int offset = TimeZone.getTimeZone(id).getRawOffset() / (60*1000);
			int hours = Math.abs(offset / 60);
			int mins = Math.abs(offset % 60);

			arguments[0] = offset < 0 ? "-" : "+";
			arguments[1] = new Integer(hours);
			arguments[2] = new Integer(mins);
			arguments[3] = id;
			arguments[4] = TimeZone.getTimeZone(id).getDisplayName(_locale);
			timeZonesIds.put(ids[i], MessageFormat.format(format, arguments));
			sortedIds.add(ids[i]);
		}		
	} // TimeZonesBean
	
	public Enumeration getIds() {
		return sortedIds.elements();
	} // getIds
	
	public String getName(String id) {
		if ( (null == id) || (util.empty.equals(id)) ) return util.empty;
		return (String) timeZonesIds.get(id);
	}
	
	public boolean exists(String id) {
		return timeZonesIds.contains(id);
	} // exists
}

