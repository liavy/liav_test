
package com.sap.sl.util.cvers.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentElementXMLException;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

public class XMLParser extends DefaultHandler {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(XMLParser.class.getName());

	private DefaultHandler adapter = null;
		
	public XMLParser () throws ComponentElementXMLException {

		super ();	
	}
	
	public void parse (String xmlString) throws ComponentElementXMLException {
		
		log.entering("parse");
		log.debug ("parse = "+xmlString);
		ByteArrayInputStream bais = new ByteArrayInputStream (xmlString.getBytes());

		SAXParserFactory parserfactory=SAXParserFactory.newInstance();
		parserfactory.setValidating(false);  

		try {
			SAXParser parser=parserfactory.newSAXParser();
			parserfactory.setValidating(false);  
			parser.parse (bais, this);
			return;
		} catch (SAXException sax) {
			String msg = "XML analysis error (SAXException): "+xmlString;
			log.error (msg, sax);
			throw new ComponentElementXMLException (msg+" "+sax.getMessage());
		} catch (IOException e) {
			String msg = "XML analysis error (IOException): "+xmlString;
			log.error (msg, e);
			throw new ComponentElementXMLException (msg+" "+e.getMessage());
		} catch (ParserConfigurationException e) {
			String msg = "XML analysis error (ParserConfigurationException): "+xmlString;
			log.error (msg, e);
			throw new ComponentElementXMLException (msg+" "+e.getMessage());
		} finally {
			log.exiting("parse");
		}
	}
	
	public void startDocument () {
	}
	
 	private String getAttribValue (Attributes attrib, String name) throws SAXException {
 		if (attrib == null) {
 			String msg = "attrib = null";
			log.error (msg);
 			throw new SAXException (msg);
 		}
 		String value = attrib.getValue(name);
 		if (value == null) {
			String msg = "attribute '"+name+"' not found";
			log.warning (msg);
			return null;
 		}
		String val = value.trim();
		log.debug ("XMLParser, getAttribValue "+name+" = "+val);
 		return val;
 	}
	
	/**
	 * added this special handling to convert 'null' strings to null objects
	 * see CSN 460800 2007
	 */
	private String checkNullAttribValue (Attributes attrib, String name) throws SAXException {
		String result = getAttribValue(attrib, name);
		if (result == null) {
			return result;
		}
		if (result.equals("")) {
			return null;
		}
		if (result.equals("null")) {
			return null;
		}
		return result;
	}

	/*
			"<RTSDATA VERSION=\"1\">" + 
			"<COMMAND=\"updateSC\" " + 
			"NAME=\""+scdata.getName()+"\" " + 
			"VENDOR=\""+scdata.getVendor()+"\" " + 
			"TYPE=\"SC\" " + 
			"SUBSYSTEM=\""+scdata.getSubsystem()+"\" " + 
			"LOCATION=\""+scdata.getLocation()+"\" " + 
			"COUNTER=\""+scdata.getCounter()+"\" " + 
			"SCVENDOR=\""+scdata.getSCVendor()+"\" " + 
			"SCNAME=\""+scdata.getSCName()+"\" " + 
			"RELEASE=\""+scdata.getRelease()+"\" " + 
			"SERVICELEVEL=\""+scdata.getServiceLevel()+"\" " + 
			"PATCHLEVEL=\""+scdata.getServiceLevel()+"\" " + 
			"DELTAVERSION=\""+scdata.getDeltaVersion()+"\" " + 
			"UPDATEVERSION=\""+scdata.getUpdateVersion()+"\" " + 
			"APPLYTIME=\""+scdata.getApplyTime()+"\" " + 
			"SCTYPEID=\""+scdata.getSCElementTypeID()+"\" " + 
			"SPTYPEID=\""+scdata.getSPElementTypeID()+"\" " + 
			"SPNAME=\""+scdata.getSPName()+"\" " + 
			"SPVERSION=\""+scdata.getSPVersion()+"\" " + 
			"</COMMAND>" +   
			"</RTSDATA>";

	 */
 	
 	String version = null;
 	String command = null;
	ComponentElementIF compElem = null;
 	
 	public String getCommand () {
 		return command;
 	}
 	
 	public ComponentElementIF getComponentElement () {
 		return compElem;
 	}
 	
	public void analyseUpdateSC (String ns, String localname, String qname, Attributes attributes) throws SAXException {

		log.debug ("XMLParser, startElement ns="+ns+" ln="+localname+" qn="+qname);
		
		String scname = getAttribValue (attributes,"NAME");
		String scvendor = getAttribValue (attributes, "VENDOR");
		String name = getAttribValue (attributes,"SCNAME");
		String vendor = getAttribValue (attributes, "SCVENDOR");
		String location = getAttribValue (attributes, "LOCATION");
		String type = getAttribValue (attributes, "TYPE");
		String counter = getAttribValue (attributes, "COUNTER");
		String provider = checkNullAttribValue (attributes, "PROVIDER");
		String release = checkNullAttribValue (attributes, "RELEASE");
		String servicelevel = checkNullAttribValue (attributes, "SERVICELEVEL");
		String patchlevel = checkNullAttribValue (attributes, "PATCHLEVEL");
		String updateversion = checkNullAttribValue (attributes, "UPDATEVERSION");
		String sctypeID = checkNullAttribValue (attributes, "SCTYPEID");
		String sptypeID = checkNullAttribValue (attributes, "SPTYPEID");
		String spname = checkNullAttribValue (attributes, "SPNAME");
		String spversion = checkNullAttribValue (attributes, "SPVERSION");
		String applytime = checkNullAttribValue (attributes, "APPLYTIME");
		
		compElem = 
			ComponentFactoryIF.getInstance().createComponentElement(
				vendor, name, type, ComponentElementIF.DEFAULTSUBSYSTEM,
				location, counter, scvendor, 
				scname,
				release, servicelevel, patchlevel, ComponentElementIF.FULLVERSION,
				updateversion, 
				applytime,
				sctypeID, sptypeID, spname, 
				spversion,
				"NWDI", 	// servertype
				null, 		// perforce server
				null, 		// changelist number
				null);		// rootdir
		if (compElem == null) {
			log.error ("analyseUpdateSC: compElem == null, " + 
			 "1="+scname+ ", " +
			"2="+scvendor+ ", " +
			"3="+name+ ", " +
			"4="+vendor+ ", " +
			"5="+location+ ", " + 
			"6="+type+ ", " + 
			"7="+counter+ ", " + 
			"8="+provider+ ", " + 
			"9="+release+ ", " + 
			"10="+servicelevel+ ", " + 
			"11="+patchlevel+ ", " + 
			"12="+updateversion+ ", " + 
			"13="+sctypeID+ ", " + 
			"14="+sptypeID+ ", " + 
			"15="+spname+ ", " + 
			"16="+spversion+ ", " + 
			"17="+applytime);
		} else {
			log.debug ("analyseUpdateSC: compElem = "+compElem.toString());
		}
	}
	
	private Vector configscs = new Vector ();
	
	public RTSComponentData[] getComponentData () {
		
		RTSComponentData[] result = new RTSComponentData [configscs.size()];
		for (int i=0; i < configscs.size(); i++) {
			result [i] = (RTSComponentData) configscs.elementAt(i);
		}
		return result;
	}

	public void analyseConfigSC (String ns, String localname, String qname, Attributes attributes) throws SAXException {

		log.debug ("XMLParser, startElement ns="+ns+" ln="+localname+" qn="+qname);
		
		String scname = getAttribValue (attributes, "NAME");
		String scvendor = getAttribValue (attributes, "VENDOR");
		String developed = getAttribValue (attributes,"DEVELOPED");
		String cmsname = getAttribValue (attributes, "CMSNAME");
		String mode = getAttribValue (attributes, "MODE");
		String cmsurl = getAttribValue (attributes, "CMSURL");
		String trackname = getAttribValue (attributes, "TRACKNAME");
		String systype = getAttribValue (attributes, "SYSTYPE");
		String location = getAttribValue (attributes, "LOCATION");
		String release = checkNullAttribValue (attributes, "RELEASE");
		String provider = checkNullAttribValue (attributes, "PROVIDER");
		String servicelevel = checkNullAttribValue (attributes, "SERVICELEVEL");
		String patchlevel = checkNullAttribValue (attributes, "PATCHLEVEL");
		boolean devsc = false;
		if ((developed != null) && (developed.equals("YES"))) {
			devsc = true;
		}
		RTSComponentData data = new RTSComponentData (mode, scname, scvendor, devsc, cmsname, cmsurl, trackname, location, systype, provider, release, servicelevel, patchlevel);
		if (data == null) {
			log.error("analyseConfigSC data == null");
			return;
		}
		log.debug ("analyseConfigSC, data = "+data.toString());
		configscs.add(data);
	}
	
	public void startElement(String ns, String localname, String qname, Attributes attributes) throws SAXException {

		log.debug ("XMLParser, startElement ns="+ns+" ln="+localname+" qn="+qname);
		
		qname = qname.toUpperCase();
		
		if (qname.equals("RTSDATA")) {
			version = getAttribValue (attributes,"VERSION");
			command = getAttribValue (attributes,"COMMAND");
			return;
		}

		if (qname.equals("UPDATESC")) {
			analyseUpdateSC (ns, localname, qname, attributes);
			return;
		}

		if (qname.equals("CONFIGSCS")) {
			configscs = new Vector ();
			return;
		}

		if (qname.equals("CONFIGSC")) {
			analyseConfigSC (ns, localname, qname, attributes);
			return;
		}

		String msg = "startElement, Unknown XML tag ns='"+ns+"' localname='"+localname+"' qname='"+qname+"'";
		log.error (msg);
		throw new SAXException (msg);
	}
	
	String tag = "";
	
	public void endElement(String s1, String tagname, String s2)
		throws SAXException
	{

		log.debug ("XMLParser, endElement "+tagname+" s1="+s1+" s2="+s2);

		if (s2.equals("RTSDATA")) {
			return;
		}
		
		if (s2.equals("UPDATESC")) {
			return;
		}
		
		if (s2.equals("CONFIGSC")) {
			return;
		}
		
		if (s2.equals("CONFIGSCS")) {
			return;
		}
		
		String msg = "endElement, Unknown XML end tag '"+s2+"'";
		log.error (msg);
		throw new SAXException (msg);
	}

	public void endDocument () {
		log.debug ("endDocument");
	}
	
}
