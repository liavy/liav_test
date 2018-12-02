package com.sap.security.core.jmx.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import javax.management.openmbean.OpenDataException;

import com.sap.i18n.timezone.SAPTimeZone;
import com.sap.security.core.jmx.IJmxMapEntry;

public class TimeZonesBean implements Serializable{

	private static final long serialVersionUID = 4751154173925224804L;

	public static final String beanId = "timezones";

	private Map<String, String> timeZonesIds;
	private String[] sortedIds;
	
	private class TimeZonesBeanEnumeration implements Enumeration
	{
		int index = 0;	
		
		public boolean hasMoreElements() {
			return index < sortedIds.length;
		}

		public Object nextElement() {
			if (this.hasMoreElements())
			{
				index++;
				return sortedIds[index-1];
			}
			throw new NoSuchElementException();
		}		
	}
	
	public static TimeZonesBean getInstance(Locale locale) {
		if (locale == null) locale = Locale.getDefault();
		return new TimeZonesBean(locale);
	}
	
	private TimeZonesBean(Locale locale) {
		/*
		 * no caching of timezones, because of I18N & AS ABAP circumstances
		 *  I18N Service not started / I18N Destination not available
		 *  New creation of ABAP timezone via runtime without AS Java Server restart
		 */
		String[] availabletimeZoneIDs = SAPTimeZone.getAvailableIDs();
		
		timeZonesIds = new HashMap<String, String>(availabletimeZoneIDs.length);
		sortedIds = new String[availabletimeZoneIDs.length];
		
		String[] sortedMessages = new String[availabletimeZoneIDs.length];
		Map<String, String> idsByMessage = new HashMap<String, String>();

		final String format = "GMT{0}{1,number,00}:{2,number,00} ({3}) {4}";
		Object[] arguments = new Object[5];
		String id = null;
		for ( int i = 0; i < availabletimeZoneIDs.length; i++ ) {
			id = availabletimeZoneIDs[i];
			TimeZone tz = SAPTimeZone.getTimeZone(id);

			// get offset in minutes sec from UTC
			int offset = tz.getRawOffset() / (60*1000);
			int hours = Math.abs(offset / 60);
			int mins = Math.abs(offset % 60);

			arguments[0] = offset < 0 ? "-" : "+";
			arguments[1] = new Integer(hours);
			arguments[2] = new Integer(mins);
			arguments[3] = id;
			arguments[4] = tz.getDisplayName(locale);
			String message = MessageFormat.format(format, arguments);
			timeZonesIds.put(availabletimeZoneIDs[i], message);
			idsByMessage.put(message, availabletimeZoneIDs[i]);
			sortedMessages[i] = message;
		}
		java.util.Arrays.sort(sortedMessages);
		for ( int i = 0; i < availabletimeZoneIDs.length; i++ )
		{
			sortedIds[i] = (String)idsByMessage.get(sortedMessages[i]);
		}
	} // TimeZonesBean
	
	public Enumeration getIds() {
		return new TimeZonesBeanEnumeration();
	} // getIds
	
	public String getName(String id) {
		if ( (null == id) || ("".equals(id)) ) return "";
		return (String)timeZonesIds.get(id);
	}
	
	public boolean exists(String id) {
		return timeZonesIds.containsKey(id);
	} // exists
	
	public JmxMap getMap() throws OpenDataException {
		Enumeration ids = getIds();
		IJmxMapEntry[] entries = new IJmxMapEntry[sortedIds.length];
		int counter = 0;
		while ( ids.hasMoreElements() ) {
			String id = (String) ids.nextElement();
			entries[counter] = (new JmxMapEntry(id, getName(id)));
			counter++;
		}
		JmxMap result = new JmxMap();
		result.setEntries(entries);
		return result;
	} // getMap
}