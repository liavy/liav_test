package com.sap.archtech.daservice.util;

import java.util.StringTokenizer;

public class StringFilter {

	/**
	 * Filters 0x00 to 0x31 and 0x7F characters out and replaces double 0x20
	 * characters with a single 0x20 character
	 */
	public static String filterResponseHeaderField(String s) {
		char c;
		String t = "";
		String r = "";
		StringTokenizer st = null;
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if ((c > 0x1F) && (c < 0x7F))
				t += c;
			else
				t += " ";
		}
		if (t.indexOf("  ") != -1) {
			st = new StringTokenizer(t, "  ");
			while (st.hasMoreTokens())
				r += st.nextToken() + " ";
		} else
			r = t;
		return r;
	}
}