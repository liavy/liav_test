package com.sap.dictionary.database.dbs;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.xml.sax.InputSource;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class ArchiveEntry implements DbsConstants {
	private static final Location loc = 
		Location.getLocation(ArchiveEntry.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	InputStream inputStream = null;
	String name = null;
	String inputString = null;
	boolean streamIsLocal = false;
	boolean saveStreamToString = false;

	public ArchiveEntry(String name, InputStream inputStream, 
			boolean streamIsLocal, boolean saveStreamToString) {
		this.name = name;
		this.inputStream = inputStream;
		this.saveStreamToString = saveStreamToString;
		if (!streamIsLocal)
			saveStreamToString();
	}

	void saveStreamToString() {
		BufferedReader br = new BufferedReader(new 
											InputStreamReader(inputStream));
		String s;
		StringBuffer buffer = new StringBuffer();
		try {
			while ((s = br.readLine()) != null) {
				buffer.append(s);
			}
		} catch (IOException ex) {
			throw new JddRuntimeException(ex,SDA_ENTRYREAD_ERR,cat,Severity.ERROR,loc);
		}
		inputString = buffer.toString();
	}

	public String getName() {
		return name;
	}
	public InputStream getInputStream() {
		if (inputString != null) 
			return new ByteArrayInputStream(inputString.getBytes());
		if (saveStreamToString) {
			saveStreamToString();
			return new ByteArrayInputStream(inputString.getBytes());
		}
		return inputStream;
	}
	public InputSource getInputSource() {
		if (inputString != null)
			return new InputSource(new StringReader(inputString));
		if (saveStreamToString) {
			saveStreamToString();
			return new InputSource(new StringReader(inputString));
		}
		return new InputSource(inputStream);
	}
	public Reader getReader() {
		if (inputString != null) 
			return new StringReader(inputString);
		if (saveStreamToString) {
			saveStreamToString();
			return new StringReader(inputString);
		}
		return null;
	}
	public String getString() {
		if (inputString != null) 
			return inputString;
		if (saveStreamToString) {
			saveStreamToString();
			return inputString;
		}
		return inputString;
	}
	public int getLength() {
		if (inputString != null) 
			return inputString.length();
		if (saveStreamToString) {
			saveStreamToString();
			return inputString.length();
		}
		return 0;
	}

}
