/*
 * Created on 21.02.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.components.impl;

import com.sap.sl.util.components.api.SCLevelIF;
import com.sap.sl.util.components.api.SCRequirementIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**  
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SCVersion extends SoftwareComponent implements SCVersionIF {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(SCVersion.class.getName());

	private String location = null;
	private String counter = null;
	private String provider = null;
	private SCLevelIF level = null;
	private SCRequirementIF[] required = null;
	private SCVersionIF history = null;
	private SCLevelIF[] coverages = null;
	private SCLevelIF[] compatibles = null;
				
	protected SCVersion (String _vendor, String _name, 
	String _provider,
	String _location,
	String _counter,
	SCLevelIF _level,
	SCVersionIF _history,
	SCRequirementIF[] _required,
	SCLevelIF[] _coverages,
	SCLevelIF[] _compatibles) {
		
		super (_vendor, _name);
		location = _location;
		counter = _counter;
		provider = _provider;
		history = _history;
		required = _required;
		coverages = _coverages;
		compatibles = _compatibles;
		level = _level;
	}

	public String getLocation () {
		return location;	
	}
	
	public String getCounter () {
		return counter;
	}

	public void setCounter (String newcounter) {
		counter = newcounter;
	}
	
	public void setLocation (String newlocation) {
		location = newlocation;
	}
	
	public String getProvider () {
		return provider;
	}
	
	public void setProvider (String newprovider) {
		provider = newprovider;
	}
	
	public SCLevelIF getComponentLevel () {
		return level;
	}
	
	public SCRequirementIF[] getRequired () {
		return required;
	}
	
	public SCLevelIF[] getCoverages () {
		return coverages;
	}
	
	public SCLevelIF[] getCompatibles () {
		return compatibles;
	}
	
	public SCVersionIF getNextElement () {
		return history;
	}
	
	public boolean hasMoreElements () {
		return history != null;
	}
	
	public void setSourcePointer (SCVersionIF history) {
		this.history = history;
	}
	
	private String getVersionXML (SCVersionIF version) {
		
		if (version == null) {
			log.debug ("SCVersion/getVersionXML version==null");
			return " ";
		}
		StringBuffer buffer = new StringBuffer ();
		log.debug ("getVersionXML "+version.getName()+" vendor='" + version.getVendor()+"' location='" + version.getLocation() + "' " +
					"counter='" + version.getCounter()+"' " + 
					"provider='" + version.getProvider() + "'");
		
		buffer.append ("<scversion name=\"" + version.getName()+"\" vendor=\"" + version.getVendor()+"\" location=\"" + version.getLocation() + "\" " +
			"counter=\"" + version.getCounter()+"\" " + 
			"provider=\"" + version.getProvider() + "\" ");
		
		SCLevelIF level = version.getComponentLevel();
		
		if (level == null) {
			log.error ("SCVersion/getVersionXML level==null");
			buffer.append(">");
		} else {
			buffer.append(level.getXML () + ">");
		}

		buffer.append("<screquirements>");
		SCRequirementIF[] required = version.getRequired();
		if (required != null) {
			for (int i=0; i < required.length; i++) {
				buffer.append (required [i].getXML ());
			}
		}
		buffer.append("</screquirements>");

		buffer.append("<sccoverages>");
		SCLevelIF[] coverages = version.getCoverages();
		if (coverages != null) {
			for (int i=0; i < coverages.length; i++) {
				buffer.append ("<sccoverage ");
				buffer.append (coverages [i].getXML ());
				buffer.append (" />");
			}
		}
		buffer.append("</sccoverages>");

		buffer.append("<sccompatibles>");
		SCLevelIF[] compatibles = version.getCompatibles();
		if (compatibles != null) {
			for (int i=0; i < compatibles.length; i++) {
				buffer.append ("<sccompatible ");
				buffer.append (compatibles [i].getXML ());
				buffer.append (" />");
			}
		}
		buffer.append("</sccompatibles>");

		buffer.append ("</scversion>");

		return buffer.toString();
	}
	
	public String getXML () {
		
		StringBuffer buffer = new StringBuffer ();
		buffer.append ("<scversions>");

		buffer.append(this.getVersionXML(this));

		SCVersionIF current = this.history;
		while (current != null) {
			buffer.append (this.getVersionXML(current));
			current = current.getNextElement();
		}
		buffer.append ("</scversions>");

		return buffer.toString();
	}

	public String toString () {
		
		String result = "SCVersion: N="+name+" V="+vendor+" L="+location+" C="+counter+" P="+provider+" "+
				level.toString();
		if (required == null) {
			result = result + " <noReq>";
		} else {
			result = result + " req=" + required.length;
		};
		if (history == null) {
			result = result + " <noHist>";
		} else {
			result = result + " <history>";
		};
		if (coverages == null) {
			result = result + " <noCov>";
		} else {
			result = result + " cov=" + coverages.length;
		};
		if (compatibles == null) {
			result = result + " <noComp>";
		} else {
			result = result + " comp=" + compatibles.length;
		};
		return result;
	}

}
