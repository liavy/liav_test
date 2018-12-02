package com.sap.archtech.daservice.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.entities.Element;
import com.tssap.dtr.client.lib.protocol.entities.PropertyElement;
import com.tssap.dtr.client.lib.protocol.entities.ResourceElement;
import com.tssap.dtr.client.lib.protocol.requests.dav.Depth;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindOption;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindRequest;
import com.tssap.dtr.client.lib.protocol.requests.dav.ProppatchRequest;

public class XmlDasLegalHold extends XmlDasMaster {

	private final static String PREFIX = "SAP:";
	private final static String NAMESPACE = "http://www.sap.com/ILM/";
	private final static String LEGALHOLD = "legal_hold";
	private final static String CASE = "case";

	private String uri;
	private String ilm_case;
	private String range;
	private String type;

	public XmlDasLegalHold(XmlDasLegalHoldRequest legalHoldRequest) {
		this.sac = legalHoldRequest.getSac();
		this.uri = legalHoldRequest.getUri();
		this.ilm_case = legalHoldRequest.getIlm_case();
		this.range = legalHoldRequest.getRange();
		this.type = legalHoldRequest.getType();
	}

	public XmlDasLegalHoldResponse execute() {

		// Variables
		boolean propFindErrorOccurred = false;
		boolean propPatchErrorOccurred = false;
		String propFindError = "";
		String propPatchError = "";
		IConnection conn = null;
		PropfindRequest propFindRequest = null;
		IResponse propFindResponse = null;
		ProppatchRequest propPatchRequest = null;
		IResponse propPatchResponse = null;

		// Create LEGALHOLD Response Object
		XmlDasLegalHoldResponse legalHoldResponse = new XmlDasLegalHoldResponse();

		try {

			// Create Initial Case List
			ArrayList<String> caseList = new ArrayList<String>();

			// Create PROPFIND Request
			if (type.equalsIgnoreCase("COL"))
				propFindRequest = new PropfindRequest(uri.substring(1) + "/");
			else
				propFindRequest = new PropfindRequest(uri.substring(1));

			// Add Name Space, Depth And Property Names
			propFindRequest.addNamespace(PREFIX, NAMESPACE);
			propFindRequest.setDepth(Depth.DEPTH_0);
			propFindRequest.setOption(PropfindOption.PROPERTIES);
			propFindRequest.addPropertyGet(PREFIX + LEGALHOLD, NAMESPACE);

			// Acquire DTR Connection
			conn = WebDavTemplateProvider.acquireWebDAVConnection(sac.store_id);

			// Send PROPFIND Request
			propFindResponse = propFindRequest.perform(conn);

			// Check If Response Is Multi-Status
			if (propFindResponse.isMultiStatus()) {

				// Write Multi-Status Response
				Iterator<ResourceElement> iter1 = propFindRequest
						.getResources();
				while (iter1.hasNext()) {
					ResourceElement resourceElement = iter1.next();
					Iterator<PropertyElement> iter2 = resourceElement
							.getProperties();
					while (iter2.hasNext()) {
						PropertyElement propertyElement = iter2.next();
						if (propertyElement.getStatusCode() == HttpServletResponse.SC_OK) {
							String[] values = propertyElement.getChildValues();
							if (values == null)
								throw new IOException(
										"No legal hold cases available despite the fact that the WebDAV server declares the opposite");
							String value = "";
							if (range.equalsIgnoreCase("Add")) {
								boolean caseAlreadyExists = false;
								for (int i = 0; i < values.length; i++) {
									value = values[i];
									caseList.add(value);
									if (value.equals(ilm_case))
										caseAlreadyExists = true;
								}
								if (!caseAlreadyExists)
									caseList.add(ilm_case);
							} else if (range.equalsIgnoreCase("Remove")) {
								for (int i = 0; i < values.length; i++) {
									value = values[i];
									if (!value.equals(ilm_case))
										caseList.add(value);
								}
							} else if (range.equalsIgnoreCase("Get")) {
								StringBuffer sb = new StringBuffer();
								for (int i = 0; i < values.length; i++) {
									value = values[i];
									sb.append(value);
									sb.append("\r\n");
								}
								legalHoldResponse.setEntityBody(sb.toString());
							}
						} else if (propertyElement.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
							if (range.equalsIgnoreCase("Add")) {
								caseList.add(ilm_case);
							}
						} else {
							propFindErrorOccurred = true;
							propFindError = "Property "
									+ propertyElement.getName()
									+ " of resource "
									+ resourceElement.getPath() + " returned: "
									+ propertyElement.getStatusCode() + " "
									+ propertyElement.getStatusDescription();
						}
					}
				}
			} else {
				propFindErrorOccurred = true;
				propFindError = propFindResponse.getStatus() + " "
						+ propFindResponse.getStatusDescription();
			}

			// Create PROPPATCH Request
			if (!propFindErrorOccurred) {
				if (range.equalsIgnoreCase("Add")) {
					if (type.equalsIgnoreCase("COL"))
						propPatchRequest = new ProppatchRequest(uri
								.substring(1)
								+ "/");
					else
						propPatchRequest = new ProppatchRequest(uri
								.substring(1));

					// Add Name Space
					propPatchRequest.addNamespace(PREFIX, NAMESPACE);

					// Add Legal Hold Cases
					Element parent = new Element(PREFIX + LEGALHOLD, NAMESPACE);
					Element child = null;
					for (Iterator<String> iter = caseList.iterator(); iter
							.hasNext();) {
						child = new Element(PREFIX + CASE, NAMESPACE, iter
								.next());
						parent.addChild(child);
					}
					propPatchRequest.addPropertySet(parent);

					// Send PROPPATCH Request
					propPatchResponse = propPatchRequest.perform(conn);

					// Check If Response Is Multi-Status
					if (propPatchResponse.isMultiStatus()) {

						// Write Multi-Status Response
						Iterator<ResourceElement> iter1 = propPatchRequest
								.getResources();
						while (iter1.hasNext()) {
							ResourceElement resourceElement = iter1.next();
							Iterator<PropertyElement> iter2 = resourceElement
									.getProperties();
							while (iter2.hasNext()) {
								PropertyElement propertyElement = iter2.next();
								if (propertyElement.getStatusCode() != HttpServletResponse.SC_OK) {
									propPatchErrorOccurred = true;
									propPatchError = "Property "
											+ propertyElement.getName()
											+ " of resource "
											+ resourceElement.getPath()
											+ " returned: "
											+ propertyElement.getStatusCode()
											+ " "
											+ propertyElement
													.getStatusDescription();
								}
							}
						}
					} else {
						propPatchErrorOccurred = true;
						propPatchError = propPatchResponse.getStatus() + " "
								+ propPatchResponse.getStatusDescription();
					}
				} else if ((range.equalsIgnoreCase("Remove"))) {

					// Check If Legal Hold Element Needs To Be Removed
					if (caseList.isEmpty()) {
						if (type.equalsIgnoreCase("COL"))
							propPatchRequest = new ProppatchRequest(uri
									.substring(1)
									+ "/");
						else
							propPatchRequest = new ProppatchRequest(uri
									.substring(1));

						// Add Name Space
						propPatchRequest.addNamespace(PREFIX, NAMESPACE);

						// Remove Legal Hold Property
						propPatchRequest.addPropertyRemove(PREFIX + LEGALHOLD,
								NAMESPACE);

						// Send PROPPATCH Request
						propPatchResponse = propPatchRequest.perform(conn);

						// Check If Response Is Multi-Status
						if (propPatchResponse.isMultiStatus()) {

							// Write Multi-Status Response
							Iterator<ResourceElement> iter1 = propPatchRequest
									.getResources();
							while (iter1.hasNext()) {
								ResourceElement resourceElement = iter1.next();
								Iterator<PropertyElement> iter2 = resourceElement
										.getProperties();
								while (iter2.hasNext()) {
									PropertyElement propertyElement = iter2
											.next();
									if (propertyElement.getStatusCode() != HttpServletResponse.SC_OK) {
										propPatchErrorOccurred = true;
										propPatchError = "Property "
												+ propertyElement.getName()
												+ " of resource "
												+ resourceElement.getPath()
												+ " returned: "
												+ propertyElement
														.getStatusCode()
												+ " "
												+ propertyElement
														.getStatusDescription();
									}
								}
							}
						} else {
							propPatchErrorOccurred = true;
							propPatchError = propPatchResponse.getStatus()
									+ " "
									+ propPatchResponse.getStatusDescription();
						}
					}

					// Overwrite Legal Hold Element
					if (!propPatchErrorOccurred && !caseList.isEmpty()) {
						if (type.equalsIgnoreCase("COL"))
							propPatchRequest = new ProppatchRequest(uri
									.substring(1)
									+ "/");
						else
							propPatchRequest = new ProppatchRequest(uri
									.substring(1));

						// Add Name Space
						propPatchRequest.addNamespace(PREFIX, NAMESPACE);

						// Add Legal Hold Cases
						Element parent = new Element(PREFIX + LEGALHOLD,
								NAMESPACE);
						Element child = null;
						for (Iterator<String> iter = caseList.iterator(); iter
								.hasNext();) {
							child = new Element(PREFIX + CASE, NAMESPACE, iter
									.next());
							parent.addChild(child);
						}
						propPatchRequest.addPropertySet(parent);

						// Send PROPPATCH Request
						propPatchResponse = propPatchRequest.perform(conn);

						// Check If Response Is Multi-Status
						if (propPatchResponse.isMultiStatus()) {

							// Write Multi-Status Response
							Iterator<ResourceElement> iter1 = propPatchRequest
									.getResources();
							while (iter1.hasNext()) {
								ResourceElement resourceElement = iter1.next();
								Iterator<PropertyElement> iter2 = resourceElement
										.getProperties();
								while (iter2.hasNext()) {
									PropertyElement propertyElement = iter2
											.next();
									if (propertyElement.getStatusCode() != HttpServletResponse.SC_OK) {
										propPatchErrorOccurred = true;
										propPatchError = "Property "
												+ propertyElement.getName()
												+ " of resource "
												+ resourceElement.getPath()
												+ " returned: "
												+ propertyElement
														.getStatusCode()
												+ " "
												+ propertyElement
														.getStatusDescription();
									}
								}
							}
						} else {
							propPatchErrorOccurred = true;
							propPatchError = propPatchResponse.getStatus()
									+ " "
									+ propPatchResponse.getStatusDescription();
						}
					}
				}
			}

			// Analyze PropFind And PropPatch Responses
			if (propFindErrorOccurred) {
				if (propFindError.startsWith(String
						.valueOf(HttpServletResponse.SC_NOT_FOUND))) {
					legalHoldResponse.setStatusCode(XmlDasMaster.SC_NOT_FOUND);
					legalHoldResponse
							.setReasonPhrase(XmlDasMaster.RP_NOT_FOUND);
				} else {
					legalHoldResponse
							.setStatusCode(XmlDasMaster.SC_CANNOT_READ_WEBDAV_PROPERTY);
					legalHoldResponse
							.setReasonPhrase(XmlDasMaster.RP_CANNOT_READ_WEBDAV_PROPERTY);
				}
				legalHoldResponse.setEntityBody(propFindError);
			} else if (propPatchErrorOccurred) {
				legalHoldResponse
						.setStatusCode(XmlDasMaster.SC_CANNOT_UPDATE_WEBDAV_PROPERTY);
				legalHoldResponse
						.setReasonPhrase(XmlDasMaster.RP_CANNOT_UPDATE_WEBDAV_PROPERTY);
				legalHoldResponse.setEntityBody(propPatchError);
			} else {
				legalHoldResponse.setStatusCode(XmlDasMaster.SC_OK);
				legalHoldResponse.setReasonPhrase(XmlDasMaster.RP_OK);
			}
		} catch (IOException ioex) {
			if (legalHoldResponse.getStatusCode() == 0) {
				legalHoldResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				legalHoldResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				legalHoldResponse.setEntityBody(ioex.toString());
				legalHoldResponse.setException(ioex);
			}
			return legalHoldResponse;
		} catch (Exception ex) {
			if (legalHoldResponse.getStatusCode() == 0) {
				legalHoldResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				legalHoldResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				legalHoldResponse.setEntityBody(ex.toString());
				legalHoldResponse.setException(ex);
			}
			return legalHoldResponse;
		} finally {
			try {

				// Prepare Request Object For Reuse
				if (propFindRequest != null)
					propFindRequest.clear();
				if (propPatchRequest != null)
					propPatchRequest.clear();

				// Release Response Stream For Reuse By Other Requests
				if (propFindResponse != null)
					propFindResponse.releaseStream();
				if (propPatchResponse != null)
					propPatchResponse.releaseStream();
			} catch (IOException ioex) {

				// $JL-EXC$
				String s = "Nothing Else To Do";
				s.toLowerCase();
			} finally {

				// Release DTR Connection
				WebDavTemplateProvider.releaseWebDAVConnection(conn);
			}
		}

		// Return Response When Method Was Successful
		return legalHoldResponse;
	}
}
