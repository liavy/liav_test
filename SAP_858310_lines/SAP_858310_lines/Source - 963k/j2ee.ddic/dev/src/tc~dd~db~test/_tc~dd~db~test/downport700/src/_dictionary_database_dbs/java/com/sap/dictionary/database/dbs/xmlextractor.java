package com.sap.dictionary.database.dbs;

/**
 * Überschrift:   Dictionary Types: Simple Types and Structures
 * Beschreibung:
 * Copyright:     Copyright (c) 2002
 * Organisation:
 * @author Kerstin Hoeft
 * @version 1.0
 */

import java.util.*;
import java.io.*;
// JAXP packages
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.net.*;

public class XmlExtractor extends DefaultHandler {
	private XMLReader xmlReader = null;
	private XmlMap currentVectorElement = null;
	private LinkedList stackOfOpenVectorElements = null;
	private HashMap currentSubelementsNames = null;
	private LinkedList stackOfSubelementsNames = null;
	private Event lastEvent = null;
	private Object currentValue = "";

	public XmlExtractor() {
		createXmlExtractor(false);
	}

	public XmlExtractor(boolean withValidation) {
		createXmlExtractor(withValidation);
	}

	private void createXmlExtractor(boolean withValidation) {
		// Create a JAXP SAXParserFactory and configure it
		// SAXParserFactory spf = SAXParserFactory.newInstance();
		//Set validation on/off
		// spf.setValidating(withValidation);
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
	    this.getClass().getClassLoader());
		try {
			// Create a JAXP SAXParser
			
			xmlReader = 
								SAXParserFactory.newInstance().newSAXParser().getXMLReader();

			// XMLReader parser = new SAXParser();
			//SAXParser saxParser = spf.newSAXParser();

			// Get the encapsulated SAX XMLReader
			// System.out.println("\n" + saxParser + " createXMLExtractor");
			// xmlReader = saxParser.getXMLReader();
		} catch (Exception ex) {
			throw new JddRuntimeException(ExType.XML_ERROR,"",ex);
		}

		// Set the ContentHandler of the XMLReader
		xmlReader.setContentHandler(this);

		// Set an ErrorHandler before parsing
		xmlReader.setErrorHandler(new XMLErrorHandler(System.err));
		
		Thread.currentThread().setContextClassLoader(cl);
	}

	/*  private void createXmlExtractor(boolean withValidation) {
	  // Create a JAXP SAXParserFactory and configure it
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    //Set validation on/off
	    spf.setValidating(withValidation);

	    try {
	      // Create a JAXP SAXParser
	      SAXParser saxParser = spf.newSAXParser();

	      // Get the encapsulated SAX XMLReader
	      xmlReader = saxParser.getXMLReader();
	      }
	      catch (Exception ex) {
	        System.err.println(ex);
	        System.exit(1);
	      }

	      // Set the ContentHandler of the XMLReader
	      xmlReader.setContentHandler(this);

	      // Set an ErrorHandler before parsing
	      xmlReader.setErrorHandler(new XMLErrorHandler(System.err));
	  }*/
	public XmlMap map(URL url) {
		try {
			// Tell the XMLReader to parse the XML document
			//xmlReader.parse(file.toURL().toString());
			xmlReader.parse(url.toString());
		} catch (SAXException se) {//$JL-EXC$ 
			throw new JddRuntimeException(ExType.XML_ERROR,"",se);	
		} catch (IOException ioe) {//$JL-EXC$
			throw new JddRuntimeException(ExType.XML_ERROR,"",ioe);
		} catch (Exception ex) {//$JL-EXC$
			throw new JddRuntimeException(ExType.XML_ERROR,"",ex);
		}	
		return currentVectorElement;
	}

	public XmlMap map(File file) {
		try {
			// Tell the XMLReader to parse the XML document
			xmlReader.parse(file.toURL().toString());
		} catch (SAXException se) {//$JL-EXC$
			throw new JddRuntimeException(ExType.XML_ERROR,"",se);	
		} catch (IOException ioe) {//$JL-EXC$
			throw new JddRuntimeException(ExType.XML_ERROR,"",ioe);
		} catch (Exception ex) {//$JL-EXC$
			throw new JddRuntimeException(ExType.XML_ERROR,"",ex);
		}	
		return currentVectorElement;
	}

	public XmlMap map(InputSource source)  {
		try {
			// Tell the XMLReader to parse the XML document
			//xmlReader.parse(file.toURL().toString());
			xmlReader.parse(source);
		} catch (SAXException se) {
			throw new JddRuntimeException(ExType.XML_ERROR,"",se);	
		} catch (IOException ioe) {
			throw new JddRuntimeException(ExType.XML_ERROR,"",ioe);
		} catch (Exception ex) {
			throw new JddRuntimeException(ExType.XML_ERROR,"",ex);
		}	
		return currentVectorElement;
	}

	// Parser calls this once at the beginning of a document
	public void startDocument() throws SAXException {
		stackOfOpenVectorElements = new LinkedList();
		stackOfSubelementsNames = new LinkedList();
		lastEvent = Event.ELEMENT_IN_PROCESS;
	}

	// Parser calls this for each element in a document
	public void startElement(
		String namespaceURI,
		String localName,
		String rawName,
		Attributes atts)
		throws SAXException {
			
		if (lastEvent == Event.ELEMENT_IN_PROCESS)
			openNewVectorElement();
		lastEvent = Event.ELEMENT_IN_PROCESS;
		for (int i = 0; i < atts.getLength(); i++) {
			if (i == 0) {
				openNewVectorElement();
				lastEvent = Event.END_ATTRIBUTES;
			}
			currentVectorElement.put(
				makeUniqueName(atts.getQName(i)),
				atts.getValue(i));	
		}
		currentValue = "";
	}

	public void characters(char[] ch, int start, int length) {
		String s = new String(ch, start, length);
		if (!currentValue.equals("")) {
			currentValue = currentValue + s;
		} else
			currentValue = currentValue + s;
	}

	// Parser calls this for each element in a document
	public void endElement(
		String namespaceURI,
		String localName,
		String rawName)
		throws SAXException {

		if (lastEvent == Event.END_ELEMENT
			|| lastEvent == Event.END_ATTRIBUTES) {
			if (lastEvent == Event.END_ATTRIBUTES) {
				currentVectorElement.put(
					makeUniqueName(rawName),
					currentValue);
			}
			currentValue = currentVectorElement;
			closeCurrentVectorElement();
		}
		currentVectorElement.put(makeUniqueName(rawName), currentValue);
		currentValue = "";
		lastEvent = Event.END_ELEMENT;
	}

	// Parser calls this once after parsing a document
	public void endDocument() throws SAXException {
	}

	public void openNewVectorElement() {
		if (currentVectorElement != null) {
			stackOfOpenVectorElements.addFirst(currentVectorElement);
		}
		currentVectorElement = new XmlMap();
		if (currentSubelementsNames != null) {
			stackOfSubelementsNames.addFirst(currentSubelementsNames);
		}
		currentSubelementsNames = new HashMap();
	}

	public void closeCurrentVectorElement() {
		currentVectorElement = (XmlMap) stackOfOpenVectorElements.removeFirst();
		currentSubelementsNames =
			(HashMap) stackOfSubelementsNames.removeFirst();
	}

	public String makeUniqueName(String basicName) {
		int counter = 0;
		String uniqueName = " ";

		Integer counterObject =
			(Integer) currentSubelementsNames.get(basicName);
		if (counterObject == null)
			counter = 0;
		else
			counter = counterObject.intValue();
		if (counter == 0)
			uniqueName = basicName;
		else
			uniqueName = basicName + counter;
		counter++;
		currentSubelementsNames.put(basicName, new Integer(counter));
		return uniqueName;
	}

	// Error handler to report errors and warnings
	private static class XMLErrorHandler implements ErrorHandler {
		// Error handler output goes here */
		private PrintStream out;

		XMLErrorHandler(PrintStream out) {
			this.out = out;
		}

		// Returns a string describing parse exception detail
		private String getParseExceptionInfo(SAXParseException spe) {
			String systemId = spe.getSystemId();
			if (systemId == null) {
				systemId = "null";
			}
			String info =
				"URI="
					+ systemId
					+ " Line="
					+ spe.getLineNumber()
					+ ": "
					+ spe.getMessage();
			return info;
		}

		// The following methods are standard SAX ErrorHandler methods.
		// See SAX documentation for more info.
		public void warning(SAXParseException spe) throws SAXException {
			out.println("Warning: " + getParseExceptionInfo(spe));
		}

		public void error(SAXParseException spe) throws SAXException {
			String message = "Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}

		public void fatalError(SAXParseException spe) throws SAXException {
			String message = "Fatal Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}
	}

	private static class Event {
		private String name = " ";

		private Event(String name) {
			this.name = name;
		}

		public static final Event ELEMENT_IN_PROCESS =
			new Event("ELEMENT_IN_PROCESS");
		public static final Event END_ATTRIBUTES = new Event("END_ATTRIBUTES");
		public static final Event END_ELEMENT = new Event("END_ELEMENT");

		public String toString() {
			return name;
		}
	}

}
