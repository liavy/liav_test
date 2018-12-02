package com.sap.archtech.daservice.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

class MultiStatusHandler extends DefaultHandler {

	private final static String SEL_COL_TAB = "SELECT COLID, STOREID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TAB2 = "SELECT COLID FROM BC_XMLA_COL WHERE PARENTCOLID = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE RESNAME = ? AND COLID = ?";
	private final static String SEL_RES_TAB2 = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ?";
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
	private HashSet<Long> notDeletedColIds;
	private HashSet<Long> notDeletedResIds;
	private HashMap<Long, HashSet<Long>> notDeletedColStoreIds;
	private Connection connection;

	public MultiStatusHandler(Sapxmla_Config sac, String uri,
			HashSet<Long> notDeletedColIds, HashSet<Long> notDeletedResIds,
			HashMap<Long, HashSet<Long>> notDeletedColStoreIds,
			Connection connection) {
		this.sac = sac;
		this.uri = uri;
		this.notDeletedColIds = notDeletedColIds;
		this.notDeletedResIds = notDeletedResIds;
		this.notDeletedColStoreIds = notDeletedColStoreIds;
		this.connection = connection;
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
			if (qName.endsWith("href"))
				insideHrefElement = false;
			if (qName.endsWith("status")) {
				insideStatusElement = false;

				try {

					// Assuming That String Belongs To "HTTP/X.X YYY ZZZ"
					if (statusElement == null)
						throw new Exception(
								"Status element in Multi-Status response is empty!");
					statusElement = statusElement.substring(statusElement
							.indexOf(" ") + 1, statusElement.length());
					status = Integer.parseInt(statusElement.trim().substring(0,
							3));

					// Write Log Entry
					cat.infoT(loc, "DELETE: Multi-Status for URL "
							+ hrefElement + " on archive store "
							+ sac.archive_store + " was " + status);

					// Evaluate Status Code
					if (status >= HttpServletResponse.SC_BAD_REQUEST) {

						// Normalize URL
						if (hrefElement == null)
							throw new Exception(
									"Href element in Multi-Status response is empty!");
						if (hrefElement.endsWith("/"))
							hrefElement = hrefElement.substring(hrefElement
									.indexOf(this.uri),
									hrefElement.length() - 1);
						else
							hrefElement = hrefElement.substring(hrefElement
									.indexOf(this.uri), hrefElement.length());

						// Check If Collection or Resource
						String calcResp = determineCollectionOrResource(hrefElement);
						if (calcResp != null) {
							long lid = 0;

							// URL Points To A Collection
							if (calcResp.startsWith("COL")) {
								lid = Long.parseLong(calcResp.substring(3));
								notDeletedColIds.add(new Long(lid));
								HashSet<Long> hs = (HashSet<Long>) notDeletedColStoreIds
										.get(new Long(sac.store_id));
								if (hs != null)
									hs.add(new Long(lid));

								// Add All Internal Members
								if (status == XmlDasMaster.SC_LOCKED)
									traverseInternalMembers(lid);
							}

							// URL Points To A Resource
							else {
								lid = Long.parseLong(calcResp.substring(3,
										calcResp.indexOf("#")));
								notDeletedResIds.add(new Long(lid));
								lid = Long.parseLong(calcResp.substring(
										calcResp.indexOf("#") + 1, calcResp
												.length()));
								notDeletedColIds.add(new Long(lid));
								HashSet<Long> hs = (HashSet<Long>) notDeletedColStoreIds
										.get(new Long(sac.store_id));
								if (hs != null)
									hs.add(new Long(lid));
							}
						} else {
							throw new Exception("URL " + hrefElement
									+ " on archive store " + sac.archive_store
									+ " not found in XML DAS Meta Data");
						}
					} else if (!((status == HttpServletResponse.SC_OK)
							|| (status == HttpServletResponse.SC_ACCEPTED) || (status == HttpServletResponse.SC_NO_CONTENT))) {
						throw new Exception("Status " + status + " for URL "
								+ hrefElement + " on archive store "
								+ sac.archive_store + " is not allowed");
					}
				} catch (SQLException sqlex) {
					cat
							.infoT(loc, "URL " + hrefElement
									+ " on archive store " + sac.archive_store
									+ " not deleted! SQLException: "
									+ sqlex.toString());
					throw new SAXException(sqlex);
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

	private String determineCollectionOrResource(String uri)
			throws SQLException {
		long colId = 0;
		long resId = 0;
		String resName = "";
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		ResultSet result = null;
		try {
			pst1 = connection.prepareStatement(SEL_COL_TAB);
			pst1.setString(1, uri);
			result = pst1.executeQuery();
			int hits = 0;
			while (result.next()) {
				colId = result.getLong("COLID");
				hits++;
			}
			result.close();
			pst1.close();
			if (hits == 1) {
				return "COL" + colId;
			} else {
				pst2 = connection.prepareStatement(SEL_COL_TAB);
				pst2.setString(1, uri.substring(0, uri.lastIndexOf("/")));
				result = pst2.executeQuery();
				hits = 0;
				while (result.next()) {
					colId = result.getLong("COLID");
					hits++;
				}
				result.close();
				pst2.close();
				if (hits == 1) {
					resName = uri.substring(uri.lastIndexOf("/") + 1);
					pst3 = connection.prepareStatement(SEL_RES_TAB);
					pst3.setString(1, resName);
					pst3.setLong(2, colId);
					result = pst3.executeQuery();
					hits = 0;
					while (result.next()) {
						resId = result.getLong("RESID");
						hits++;
					}
					result.close();
					pst3.close();
					if (hits == 1) {
						return "RES" + resId + "#" + colId;
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
			if (result != null)
				result.close();
			if (pst1 != null)
				pst1.close();
			if (pst2 != null)
				pst2.close();
			if (pst3 != null)
				pst3.close();
		}
	}

	private void traverseInternalMembers(long colid) throws SQLException {

		// Local Variables
		ResultSet result = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		ArrayList<Long> internColList = new ArrayList<Long>();
		try {

			// Get All Collection IDs
			pst1 = this.connection.prepareStatement(SEL_COL_TAB2);
			pst1.setLong(1, colid);
			result = pst1.executeQuery();
			long cid = 0;
			while (result.next()) {
				cid = result.getLong("COLID");
				notDeletedColIds.add(new Long(cid));
				HashSet<Long> hs = (HashSet<Long>) notDeletedColStoreIds
						.get(new Long(sac.store_id));
				if (hs != null)
					hs.add(new Long(cid));
				internColList.add(new Long(cid));
			}
			result.close();
			pst1.close();

			// Get All Resource IDs
			pst2 = this.connection.prepareStatement(SEL_RES_TAB2);
			pst2.setLong(1, colid);
			result = pst2.executeQuery();
			long rid = 0;
			while (result.next()) {
				rid = result.getLong("RESID");
				notDeletedResIds.add(new Long(rid));
			}
			result.close();
			pst2.close();

			// Check If More Internal Collections Exists
			if (internColList.size() == 0) {
				return;
			}

			// More Internal Collections Exists
			else {
				for (int i = 0; i < internColList.size(); i++)
					this.traverseInternalMembers(((Long) internColList.get(i))
							.longValue());
			}
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
			if (result != null)
				result.close();
			if (pst1 != null)
				pst1.close();
			if (pst2 != null)
				pst2.close();
		}
	}
}
