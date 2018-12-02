package com.sap.security.core.admin;

import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.ResourceBean;


public class ResourceBundleOptions extends Options implements Serializable{
	public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/ResourceBundleOptions.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
	private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);    	
	private transient ResourceBean rbean;
	private String[][] entries;
	private Locale _locale;
	private String _baseName;

    public ResourceBundleOptions(String baseName, Locale locale) {
    	_locale=locale;
    	_baseName=baseName;
        this.rbean = new ResourceBean(locale, baseName, this.getClass().getClassLoader());
    }

	private ResourceBean getrbean()
	{
		if (rbean == null)
		{
		  rbean = new ResourceBean(_locale, _baseName, this.getClass().getClassLoader());
		}
		return rbean;
	}
		
    public Enumeration getIds() {
        return getrbean().getIds();
    }

    public String getKeyByValue(String value) {
        String key = null;
        Enumeration keys = getIds();
        while (keys.hasMoreElements()) {
            String id = (String) keys.nextElement();
            String name = (String) getName(id);
            if ( name.equals(value) ) {
                key = id;
                break;
            }
        }
        return key;
    }

    public String getName(String id) {
        try {
            if ( null == util.checkEmpty(id) ) return "";
            return getrbean().get(id);
        } catch ( MissingResourceException ex ) {
			if ( trace.beDebug() )
			trace.debugT("getName", "no value found for input key", new String[]{id});
             return "";
        }
    }

    public boolean exists(String id) {
        try {
            getrbean().get(id);
            return true;
        } catch ( MissingResourceException ex ) {
			if ( trace.beDebug() )
			trace.debugT("exists", "no value found for input key", new String[]{id});        	
			return false;
        }
    }

	public String getHtmlOptions(String selectedId) {
		if ( null == this.entries ) {
			synchronized (this)	{
                String[] ids = toArray();
                Collator collator = Collator.getInstance(getrbean().getLocale());
                int size = ids.length;
				int sortedSize = size;
                CollationKey[] keys = new CollationKey[size];
				String id = null;				
                for (int i=0, j=0; i<size; i++) {
					id = ids[i];
					if ( "DEFAULT".equalsIgnoreCase(id) ) {
						sortedSize = sortedSize - 1;
						continue;
					}
                    keys[j++] = collator.getCollationKey(getName(id));
                }
				this.entries = new String[sortedSize][2];
                this.entries = sort(keys, sortedSize);
			}
		}

		// build sorted html options
		StringBuffer result = new StringBuffer("");
		for (int i=0; i<this.entries.length; i++) {
			appendHtmlOption(result,
                             entries[i][0],
                             entries[i][1],
                             entries[i][0].equals(selectedId));
		}
		return result.toString();
	} // getHtmlOptions(String)

    private String[] toArray() {
        Enumeration ids = getIds();
        java.util.Vector result = new java.util.Vector();
        while ( ids.hasMoreElements() ) {
            String id = (String) ids.nextElement();
            result.add(id);
        }
        return (String[])result.toArray(new String[result.size()]);
    } // toArray

    private String[][] sort(CollationKey[] keys, int size) {
        String[][] sortedEntries = new String[size][2];
        for (int i=0; i<size; i++) {
            for (int j=i; j<size; j++) {
                if( keys[i].compareTo(keys[j])>0 ) {
                    // swap keys[i] and keys[j]
                    CollationKey temp = keys[i];
                    keys[i] = keys[j];
                    keys[j] = temp;
                }
            }
			sortedEntries[i][0] = getKeyByValue(keys[i].getSourceString());
			sortedEntries[i][1] = keys[i].getSourceString();
        }
        return sortedEntries;
    } // sort
}

