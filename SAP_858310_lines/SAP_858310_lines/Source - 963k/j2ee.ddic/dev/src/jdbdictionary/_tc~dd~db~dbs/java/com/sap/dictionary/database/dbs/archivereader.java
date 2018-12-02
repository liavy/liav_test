package com.sap.dictionary.database.dbs;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class ArchiveReader implements DbsConstants {
	private static final Location loc = 
		Location.getLocation(ArchiveReader.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	String[] suffixes = null;
	ZipFile file = null;
	ZipInputStream inputStream = null;
	Enumeration fileEntries = null;
	boolean entryStreamIsLocal = false;
	boolean entryToString = true;

	public ArchiveReader(String fileName, String suffix,
				boolean entryToString) {
		this(fileName,new String[] {suffix},entryToString);						
	}
	
	public ArchiveReader(String fileStreamName, String suffix) {
		this(fileStreamName,new String[] {suffix});
	}
	
	public ArchiveReader(ZipInputStream zipStream, String suffix) {
		this(zipStream,new String[] {suffix});
	}
		
	public ArchiveReader(String fileName, String[] suffixes,
				boolean entryToString) {
	 	entryStreamIsLocal = true;
		this.suffixes = suffixes;
		this.entryToString = entryToString;
		try {
			file = new ZipFile(fileName);
		} catch (IOException ex) {
			throw new JddRuntimeException(ex,SDA_FILEREAD_ERR,cat,Severity.ERROR,loc);
		}
		fileEntries = file.entries();		
	}

	public ArchiveReader(String fileStreamName, String[] suffixes) {
		entryStreamIsLocal = false;
		this.suffixes = suffixes;
		try {
			inputStream = new ZipInputStream(new FileInputStream(fileStreamName));
		} catch (FileNotFoundException ex) {
			throw new JddRuntimeException(ex,SDA_FILEREAD_ERR,cat,Severity.ERROR,loc);
		}
	}

	public ArchiveReader(ZipInputStream zipStream, String[] suffixes) {
		entryStreamIsLocal = false;
		this.suffixes = suffixes;
		inputStream = zipStream;
	}

	public ArchiveEntry getNextEntry()  {
		if (entryStreamIsLocal) {
			while (true) {
				if (fileEntries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) fileEntries.nextElement();
					String ename = entry.getName();
					if (suffixes != null) {
						boolean matchesSuffix = false;
						for (int i = 0; i < suffixes.length; i++) {
							if (ename.endsWith(suffixes[i])) {
								matchesSuffix = true;
								break;
							}
						}
						if (!matchesSuffix)
							continue;
					}
					cat.info(loc,NEXT_ARCHIVE_OBJECT,new Object[]{ename});
					InputStream in;
					try {
						in = file.getInputStream(entry);
					} catch (IOException ex) {
						throw new JddRuntimeException(ex,SDA_ENTRYREAD_ERR,cat,
							Severity.ERROR,loc);
					}
					return new ArchiveEntry(ename, in, true, entryToString);
				} else {
					try {
						file.close();
					} catch (IOException ex) {
						throw new JddRuntimeException(ex,SDA_ENTRYREAD_ERR,cat,
							Severity.ERROR,loc);
					}
					return null;
				}
			}
		} else {
			while (true) {
				ZipEntry entry;
				try {
					entry = inputStream.getNextEntry();
				} catch (IOException ex) {
					throw new JddRuntimeException(ex,SDA_ENTRYREAD_ERR,cat,
						Severity.ERROR,loc);
				}
				if (entry != null) {
					String ename = entry.getName();
					cat.info(loc,NEXT_ARCHIVE_OBJECT,new Object[]{ename});
					if (suffixes != null) {
						boolean matchesSuffix = false;
						for (int i = 0; i < suffixes.length; i++) {
							if (ename.endsWith(suffixes[i])) {
								matchesSuffix = true;
								break;
							}
						}
						if (!matchesSuffix)
							continue;
					}
					ArchiveEntry aentry = new ArchiveEntry(ename, inputStream, false, 
																																				true);
					try {
						inputStream.closeEntry();
					} catch (IOException ex) {
						throw new JddRuntimeException(ex,SDA_ENTRYREAD_ERR,cat,
							Severity.ERROR,loc);
					}
					return aentry;
				} else {
					try {
						inputStream.close();
					} catch (IOException ex) {
						throw new JddRuntimeException(ex,SDA_ENTRYREAD_ERR,
							cat,Severity.ERROR,loc);
					}
					return null;
				}
			}
		}
	}
}
