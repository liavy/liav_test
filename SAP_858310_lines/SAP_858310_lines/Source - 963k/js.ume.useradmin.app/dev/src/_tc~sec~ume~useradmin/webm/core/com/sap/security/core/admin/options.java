package com.sap.security.core.admin;

import java.util.Enumeration;
import java.util.ArrayList;
import java.lang.Comparable;
import java.util.Collections;
import java.util.Iterator;

public abstract class Options implements java.io.Serializable {
	// helper class to sort for name
	class Pair implements Comparable, java.io.Serializable {
		private String name;
		private String id;

		Pair(String name, String id) {
			this.name = name;
			this.id = id;
		}

		public int compareTo(Object o) {
			return name.compareTo(((Pair) o).name);
		}

		public String getName() {
			return name;
		}

		public String getId() {
			return id;
		}
	}

	private ArrayList sortedList = null;

	public String getHtmlOptions(String selectedId) {
		if ( this.sortedList == null ) {
			synchronized (this) {
                // create list which is sorted for name
                sortedList = new ArrayList();
                Enumeration ids = getIds();
                while ( ids.hasMoreElements() ) {
                    String id = (String) ids.nextElement();
                    sortedList.add(new Pair(getName(id), id));
                }
                Collections.sort(sortedList);
			}
		}

		// build sorted html options
		Iterator iter = sortedList.iterator();
		StringBuffer result = new StringBuffer("");
		Pair pair = null;
		while ( iter.hasNext() ) {
			pair = (Pair) iter.next();
			appendHtmlOption(result, pair.getId(), pair.getName(), pair.getId().equals(selectedId));
		}
		return result.toString();
	} // getHtmlOptions

	public abstract Enumeration getIds();

	public abstract String getName(String id);

    public abstract boolean exists(String id);

	protected void appendHtmlOption(StringBuffer result,
                                    String id,
                                    String name,
                                    boolean select) {
		result.append("<option value=\"");
		result.append(id);
		if ( select ) {
			result.append("\" selected >");
		} else {
			result.append("\" >");
		}
			
		result.append(name).append("</option>");
	} // appendHtmlOption
}
