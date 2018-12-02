package com.sap.archtech.daservice.storage;

import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

class MultiStatusHandlerResource extends DefaultHandler {

	private static final Location loc = Location
			.getLocation("com.sap.archtech.daservice");
	private static final Category cat = Category.getCategory(
			Category.APPS_COMMON_ARCHIVING, "XML_DAS");

	private boolean insideResponseElement = false;
	private boolean insideHrefElement = false;
	private boolean insideStatusElement = false;
	private String hrefElement = null;
	private String statusElement = null;
	private int status = 0;
	private Sapxmla_Config sac = null;
	private String uri;
	private HashSet<String> resList;
	private boolean hrefReached;

	public MultiStatusHandlerResource(Sapxmla_Config sac, String uri,
			HashSet<String> resList) {
		this.sac = sac;
		this.uri = uri;
		this.resList = resList;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {
		qName = qName.toLowerCase();

		if (qName.endsWith("response"))
			insideResponseElement = true;

		else if (insideResponseElement) {
			if (qName.endsWith("href")) {
				hrefElement = null;
				insideHrefElement = true;
			}
			if (qName.endsWith("status")) {
				statusElement = null;
				insideStatusElement = true;
			}
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		qName = qName.toLowerCase();

		if (qName.endsWith("response"))
			insideResponseElement = false;

		else if (insideResponseElement) {
			if (qName.endsWith("href")) {
				insideHrefElement = false;

				try {
					if (hrefElement.endsWith("/"))
						hrefElement = hrefElement.substring(0, hrefElement
								.length() - 1);
					if (hrefElement.endsWith(this.uri))
						hrefReached = true;
				} catch (Exception ex) {
					cat.infoT(loc, "URL " + hrefElement + " on archive store "
							+ sac.archive_store + " not deleted! Exception: "
							+ ex.toString());
					throw new SAXException(ex);
				}
			}
			if (qName.endsWith("status")) {
				insideStatusElement = false;

				try {
					if (hrefReached) {

						// Assuming That String Belongs To "HTTP/X.X YYY ZZZ"
						if (statusElement == null)
							throw new Exception(
									"Status element in Multi-Status response is empty!");
						statusElement = statusElement.substring(statusElement
								.indexOf(" ") + 1, statusElement.length());
						status = Integer.parseInt(statusElement.trim()
								.substring(0, 3));

						// Evaluate Status Code
						this.resList
								.add(new String(status + " " + hrefElement));
						hrefReached = false;
					}
				} catch (NumberFormatException nfex) {
					cat.infoT(loc, "URL " + hrefElement + " on archive store "
							+ sac.archive_store
							+ " not deleted! NumberFormatException: "
							+ nfex.toString());
					throw new SAXException(nfex);
				} catch (Exception ex) {
					cat.infoT(loc, "URL " + hrefElement + " on archive store "
							+ sac.archive_store + " not deleted! Exception: "
							+ ex.toString());
					throw new SAXException(ex);
				}
			}
		}
	}

	public void characters(char[] ch, int start, int length) {
		if (insideResponseElement) {

			// Get URL
			if (insideHrefElement) {
				if (hrefElement == null)
					hrefElement = new String(ch, start, length).toLowerCase();
				else
					hrefElement += new String(ch, start, length).toLowerCase();
			}

			// Get Status Code
			if (insideStatusElement) {

				// Filter Out Response Code From Status Element
				if (statusElement == null)
					statusElement = new String(ch, start, length);
				else
					statusElement += new String(ch, start, length);
			}
		}
	}
}
