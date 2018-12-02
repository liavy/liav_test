package com.sap.sl.util.jarsl.impl;

import com.sap.sl.util.jarsl.api.ManifestEntryIF;

/**
 * @author d030435
 * The ManifestEntry object represents a single manifest entry and is a element of the manifest vector
 */

final class ManifestEntry implements ManifestEntryIF {
	private String name;          /* name of the entry */
	private String attribute;     /* name of the attribute */
	private String value;         /* value of the atrribute */
	private boolean archiveentry;
  private int hash=0;
	ManifestEntry(String attribute, String value, String name) {
		this(attribute,value,name,false);
	}
	ManifestEntry(String attribute, String value, String name, boolean archiveentry) {
		this.name=name;
		this.attribute=attribute;
		this.value=value;
		this.archiveentry=archiveentry;
    hash=("@1"+name+"@2"+attribute+"@3"+value).hashCode();
	}
	/* The value string in divided into lines with 70 characters at each case. Every line starts with blank */
	String getValueToWrite() {
		String writestring="";
		for (int i=0; i<value.length(); i=i+70) {
			if (i==0) {
				writestring=value.substring(i,i+70<value.length()?i+70:value.length());
			}
			else {
				writestring=writestring+"\n "+value.substring(i,i+70<value.length()?i+70:value.length());
			}
		}
		return writestring;
	}
	/* compares to ManifestEntry objects */
	public boolean equals(Object mf) {
		if (mf instanceof ManifestEntry && this.name.compareTo(((ManifestEntry)mf).name)==0 && this.attribute.compareTo(((ManifestEntry)mf).attribute)==0 && this.value.compareTo(((ManifestEntry)mf).value)==0) {
			return true;
		}
		else {
			return false;
		}
	}
	public int hashCode() {
		return hash;
	}
	// public
	public String getEntryName() {
		return name;		
	}
	public String getAttributeName() {
		return attribute;
	}
	public String getAttributeValue() {
		return value;
	}
  public boolean isArchiveEntry() {
    return archiveentry;
  }
  public void setEntryName(String in) {
    name=in;
    hash=("@1"+name+"@2"+attribute+"@3"+value).hashCode();
  }
  public void setAttributeName(String in) {
    attribute=in;
    hash=("@1"+name+"@2"+attribute+"@3"+value).hashCode();
  }
  public void setAttributeValue(String in) {
    value=in;
    hash=("@1"+name+"@2"+attribute+"@3"+value).hashCode();
  }
  public void setArchiveEntry(boolean in) {
    archiveentry=in;
  }
}