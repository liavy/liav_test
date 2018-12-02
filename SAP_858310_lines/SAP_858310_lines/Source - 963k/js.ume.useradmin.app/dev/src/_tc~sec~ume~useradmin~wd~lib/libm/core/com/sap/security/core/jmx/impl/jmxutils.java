package com.sap.security.core.jmx.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.IMessage;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.UMException;
import com.sap.security.api.UMRuntimeException;
import com.sap.security.core.jmx.IJmxMapEntry;
import com.sap.security.core.jmx.IJmxMessage;

/**
 * Stateless utility methods that can be used on both JMX server and client side
 * to transform usual Java data types like <code>java.util.Map</code> to special
 * types usable for JMX communication and vice versa.

 * @author Heiko Ettelbrueck (d034567)
 */

public class JmxUtils {

    /**
     * Convert a <code>java.util.Map</code> collection to a JMX transportable
     * array of {@link IJmxEntity} objects.
     * 
     * @param map The <code>Map</code> to convert.<br/>
     *            Please note that {@link IJmxEntity} currently only supports
     *            <code>String</code> keys and values in the input <code>Map</code>.
     * @return Array of {@link IJmxEntity} objects containing the Map data.
     * @throws OpenDataException if the input <code>Map</code> contains entry
     *         keys or values that are no <code>String</code> objects.
     */
    public static IJmxMapEntry[] convertMapToJmxMapEntries(Map map)
    throws OpenDataException {
    	IJmxMapEntry[] mapEntries = new IJmxMapEntry[map.size()];
    	
    	Iterator entries = map.entrySet().iterator();
    	Map.Entry currentMapEntry = null;
        int currentMapEntriesIndex = 0;
    
    	while(entries.hasNext()) {
    		currentMapEntry = (Map.Entry) entries.next();

            Object key = currentMapEntry.getKey();
            String keyString = null;
    		if(key instanceof String) {
                keyString = (String) key;
            }
            else {
                String msg = "This method can only handle String objects " +
                    "as Map keys, but the following key is not a String: " + key;
    			throw new OpenDataException(msg);
    		}

            Object value = currentMapEntry.getValue();
            String valueString = null;
            if(value instanceof String) {
                valueString = (String) value;
            }
            else {
                String msg = "This method can only handle String objects " +
                    "as Map values, but the following value is not a String: " + value;
                throw new OpenDataException(msg);
            }

            mapEntries[currentMapEntriesIndex++] = new JmxMapEntry(keyString, valueString);
        }
    	
    	return mapEntries;
    }

    /**
     * Convert an array of {@link IJmxEntity} objects (back) to a <code>java.util.Map</code>.
     * 
     * @param mapEntries The array of <code>IJmxEntity</code> objects.
     * @return The <code>Map</code> containing the data of the <code>IJmxEntity</code> objects.
     */
    public static Map<String, String> convertJmxMapEntriesToMap(IJmxMapEntry[] mapEntries) {
    	Map<String, String> map = new HashMap<String, String>(mapEntries.length);
    	
    	for(int i = 0; i < mapEntries.length; i++) {
    		map.put(mapEntries[i].getKey(), mapEntries[i].getValue());
    	}
    	
    	return map;
    }

    
    /**
     * Convert an array of {@link IJmxMapEntry} objects (back) to a <code>java.util.Properties</code>.
     * 
     * @param props The array of <code>IJmxMapEntry</code> objects.
     * @return The <code>Properties</code> object containing the data of the <code>IJmxMapEntry</code> objects.
     */
    public static Properties convertJmxMapEntriesToProperties(CompositeData[] props) 
    		throws OpenDataException {
		Properties newProps = new Properties();
		if (props != null && props.length > 0 ) {
			for (int i = 0; i < props.length; i++) {
				IJmxMapEntry mapEntry = new JmxMapEntry(props[i]);
				newProps.setProperty(mapEntry.getKey(), mapEntry.getValue());
			}
		}
		return newProps;
	}
    
    /**
     * Converts a comma separated string into a List
     * @param listString One String containing all elements separated by the seperator
     * @param delimiter Character seperating all elements in the string
     * @return List The List element contains all elements separated by the delimiter in the listString  
     */
    public static List<String> convertStringToList(String listString, String delimiter) {
        List<String> list = new ArrayList<String>();
        if (listString != null && listString.length() > 0 
                && delimiter != null && delimiter.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(listString, delimiter, false);
            while (tokenizer.hasMoreTokens()) {
                list.add((String) tokenizer.nextToken());
            }
        }
        return list;
    }
    
    private static IJmxMessage convertIMessageToIJmxMessage(IMessage message,
			Locale locale) throws OpenDataException {
		JmxMessage jmxMessage = null;
		if (message != null) {
			jmxMessage = new JmxMessage(message.getType(), message
					.getLifeTime(), message.getCategory(), message
					.getTimeStamp(), message.getMessage(), message
					.getLocalizedMessage(locale), message.getGuid());
		}
		return jmxMessage;
	}
    
    private static List<IJmxMessage> convertIMessageIteratorToIJmxMessageList(Iterator<IMessage> messageIt,
			Locale locale) throws OpenDataException {
    	List<IJmxMessage> jmxMessageList = new ArrayList<IJmxMessage>();
		if (messageIt != null) {
			while (messageIt.hasNext()){
				jmxMessageList.add(convertIMessageToIJmxMessage(
						messageIt.next(), locale));
			}
		}
		return jmxMessageList;
	}
    
    public static List<IJmxMessage> convertISearchResultMessagesToIJmxMessageList(ISearchResult sr,
			Locale locale, List<IJmxMessage> additionalMessages) throws OpenDataException {
		List<IJmxMessage> jmxMessageList = null;
		if (sr != null) {
			jmxMessageList = convertIMessageIteratorToIJmxMessageList(sr.getMessages(false), locale);
		}
		if (additionalMessages != null){
			for (Iterator<IJmxMessage> it = additionalMessages.iterator(); it.hasNext();){
				jmxMessageList.add(it.next());
			}
		}
		return jmxMessageList;
	}    

    public static List<IJmxMessage> convertIPrincipalMessagesToIJmxMessageList(IPrincipal p,
			Locale locale) throws OpenDataException {
    	List<IJmxMessage> jmxMessageList = null;
		if (p != null) {
			jmxMessageList = convertIMessageIteratorToIJmxMessageList(p.getMessages(false), locale);
		}
		return jmxMessageList;
	}

	public static String convertExceptionMessageToString(Throwable e,
			Locale locale) {
		if (e != null) {
			Iterator it = null;
			if (e instanceof UMException) {
				it = ((UMException) e).getMessages(false);
			}
			if (e instanceof UMRuntimeException) {
				it = ((UMRuntimeException) e).getMessages(false);
			}
			if (it != null) {
				StringBuffer buff = new StringBuffer();
				while (it.hasNext()) {
					IMessage element = (IMessage) it.next();
					String message = element.getLocalizedMessage(locale);
					buff.append(message);
					if (it.hasNext()) {
						buff.append(JmxSearchHelper.SEPARATOR);
					}
				}
				return buff.toString();
			}
		}
		return null;
	}
    
}
