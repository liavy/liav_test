
package com.sap.sl.util.components.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.sl.util.components.api.ComponentElementXMLException;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.components.api.SCLevelIF;
import com.sap.sl.util.components.api.SCRequirementIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

public class XMLParser extends DefaultHandler {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(XMLParser.class.getName());

	private DefaultHandler adapter = null;
	private SCVersionIF scversion = null;
	private String xmlString = null;
	
	public XMLParser () throws ComponentElementXMLException {

		super ();	
	}
	
	public SCVersionIF parse (String xmlString) throws ComponentElementXMLException {
		
		this.xmlString = xmlString;
		ByteArrayInputStream bais = new ByteArrayInputStream (xmlString.getBytes());

		SAXParserFactory parserfactory=SAXParserFactory.newInstance();
		parserfactory.setValidating(false);  

		try {
			SAXParser parser=parserfactory.newSAXParser();
			parserfactory.setValidating(false);  
			parser.parse (bais, this);
			return scversion;
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
		}
	}
	
	public void startDocument () {
	}
	
	String location = null;
	String counter = null;
	String provider = null;
	String scname = null;
	String scvendor = null;
	
	Vector vrequired = null;
	Vector vcoverages = null;
	Vector vcompatibles = null;
	
	SCRequirementIF[] required = null;
	SCLevelIF[] coverages = null;
	SCLevelIF[] compatibles = null;
	
	SCLevelIF level = null;
	Vector history = null;
	
	SCRequirementIF req = null;
	SCLevelIF cover = null;
	SCLevelIF compat = null;
	
/*
 *	<scversions>
 *  <scversion name="SC1" vendor="sap.com" location="MAIN_Basic_C" 
 *             counter="20000202" provider="MAIN"  
 *             release="0" servicelevel="2" patchlevel="2">
 *      <screquirements>
 *      	<screquirement name="SC2" vendor="sap.com" location="20050102" release="6.40" />
 *      </screquirements>
 *      <sccoverages>
 *           <sccoverage release="0" servicelevel="0" patchlevel="2" />
 *      </sccoverages>
 *      <sccompatibles>
 *           <sccompatible release="0" servicelevel="0" patchlevel="0" />
 *      </sccompatibles>
 *   </scversion>
 * </scversions>
 */
 
 	private String getAttribValue (Attributes attrib, String name) throws SAXException {
 		if (attrib == null) {
 			String msg = "attrib = null";
			log.error (msg);
 			throw new SAXException (msg);
 		}
 		String value = attrib.getValue(name);
 		if (value == null) {
			String msg = "attribute '"+name+"' not found";
			log.error (msg);
			throw new SAXException (msg);
 		}
		String val = value.trim();
		log.debug ("XMLParser, getAttribValue "+name+" = "+val);
 		return val;
 	}

	public void startElement(String ns, String localname, String qname, Attributes attributes) throws SAXException {

		log.debug ("XMLParser, startElement ns="+ns+" ln="+localname+" qn="+qname);
		
		// there is a difference between running this standalone as java program or within the engine
		// within the engine: localname = "" but qname is filled !
		
		if (localname.equals("")) {
			localname = qname;
		}
		if (localname.equals("scversions")) {
			history = new Vector ();
			return;
		}
		if (localname.equals("screquirements")) {
			vrequired = new Vector ();
			return;
		}
		if (localname.equals("sccoverages")) {
			vcoverages = new Vector ();
			return;
		}
		if (localname.equals("sccompatibles")) {
			vcompatibles = new Vector ();
			return;
		}
		if (localname.equals("scversion")) {
			scname = getAttribValue (attributes,"name");
			scvendor = getAttribValue (attributes, "vendor");
			location = getAttribValue (attributes, "location");
			counter = getAttribValue (attributes, "counter");
			provider = getAttribValue (attributes, "provider");
			String release = getAttribValue (attributes, "release");
			String servicelevel = getAttribValue (attributes, "servicelevel");
			String patchlevel = getAttribValue (attributes, "patchlevel");
			level = ComponentFactoryIF.getInstance().createSCLevel(release, servicelevel, patchlevel);
			return;
		}

		//   	<screquirement name="SC2" vendor="sap.com" location="20050102" release="6.40" />

		if (localname.equals("screquirement")) {
			String scname = attributes.getValue("name").trim();
			String scvendor = attributes.getValue("vendor").trim();
			String provider = attributes.getValue("provider").trim();
			String release = attributes.getValue("release").trim();
			String servicelevel = attributes.getValue("servicelevel").trim();
			String patchlevel = attributes.getValue("patchlevel").trim();
			SCLevelIF level = ComponentFactoryIF.getInstance().createSCLevel(release, servicelevel, patchlevel);
			SCRequirementIF req = ComponentFactoryIF.getInstance().createSCRequirement(ComponentFactoryIF.getInstance().createSoftwareComponent(scvendor, scname), provider, level);
			log.debug ("XMLParser, req: "+req.toString());
			vrequired.add (req);
			return;
		}

		if (localname.equals("sccoverage")) {
			String release = attributes.getValue("release").trim();
			String servicelevel = attributes.getValue("servicelevel").trim();
			String patchlevel = attributes.getValue("patchlevel").trim();
			SCLevelIF level = ComponentFactoryIF.getInstance().createSCLevel(release, servicelevel, patchlevel);
			log.debug ("XMLParser, coverage: "+level.toString());
			vcoverages.add (level);
			return;
		}

		if (localname.equals("sccompatible")) {
			String release = attributes.getValue("release").trim();
			String servicelevel = attributes.getValue("servicelevel").trim();
			String patchlevel = attributes.getValue("patchlevel").trim();
			SCLevelIF level = ComponentFactoryIF.getInstance().createSCLevel(release, servicelevel, patchlevel);
			log.debug ("XMLParser, compat: "+level.toString());
			vcompatibles.add (level);
			return;
		}

		String msg = "Unknown XML tag '"+localname+"'";
		log.error (msg);
		log.error ("XML-String = "+xmlString);
		throw new SAXException (msg);
	}
	
	String tag = "";
	
	public void endElement(String s1, String tagname, String s2)
		throws SAXException
	{

		log.debug ("XMLParser, endElement '"+tagname+"' s1='"+s1+"' s2='"+s2+"'");
		
		if (tagname.equals("")) {
			tagname = s2;
		}
		if (tagname.equals("scversions")) {

			if (history.size() == 0) {
				scversion = null;
				log.debug ("XMLParser, no scversion defined, return null");
				return;
			}

			scversion = (SCVersionIF) history.elementAt(0);
			scversion.setSourcePointer(null);

			SCVersionIF current = scversion;
			for (int i=1; i < history.size(); i++) {
				SCVersionIF nextptr = (SCVersionIF) history.elementAt(i);
				nextptr.setSourcePointer(null);
				current.setSourcePointer(nextptr);
				current = nextptr;
			}
			return;
		}
		
		/*      <screquirements>
		*      	<screquirement name="SC2" vendor="sap.com" location="20050102" release="6.40" />
		*      </screquirements>
		*      <coverages>
		*           <coverage release="0" servicelevel="0" patchlevel="2" />
		*      </coverages>
		*      <compatibles>
		*           <compatible release="0" servicelevel="0" patchlevel="0" />
		*      </compatibles>
		*/
		
		if (tagname.equals("screquirements")) {
			required = new SCRequirementIF [vrequired.size()];
			for (int i=0; i < vrequired.size(); i++) {
				required [i] = (SCRequirementIF) vrequired.elementAt(i);
			}
			return;
		}
		if (tagname.equals("sccoverages")) {
			coverages = new SCLevelIF [vcoverages.size()];
			for (int i=0; i < vcoverages.size(); i++) {
				coverages [i] = (SCLevelIF) vcoverages.elementAt(i);
			}
			return;
		}
		if (tagname.equals("sccompatibles")) {
			compatibles = new SCLevelIF [vcompatibles.size()];
			for (int i=0; i < vcompatibles.size(); i++) {
				compatibles [i] = (SCLevelIF) vcompatibles.elementAt(i);
			}
			return;
		}

		if (tagname.equals("scversion")) {
			ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
			SCVersionIF scv = factory.createSCVersion (scvendor, scname, provider, location, counter, level, null, required, coverages, compatibles);
			history.add(scv);
			return;
		};

		if (tagname.equals("sccoverage")) {
			return;
		}

		if (tagname.equals("screquirement")) {
			return;
		}

		if (tagname.equals("sccompatible")) {
			return;
		}

		String msg = "Unknown XML end tag '"+tagname+"'";
		log.error (msg);
		throw new SAXException (msg);
	}

	public void endDocument () {
		log.debug ("endDocument");
	}
	
}
