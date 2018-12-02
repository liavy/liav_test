package com.sap.archtech.daservice.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.entities.PropertyElement;
import com.tssap.dtr.client.lib.protocol.entities.ResourceElement;
import com.tssap.dtr.client.lib.protocol.requests.dav.Depth;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindOption;
import com.tssap.dtr.client.lib.protocol.requests.dav.PropfindRequest;

public class XmlDasPropFind extends XmlDasMaster {

	private final static String PREFIX = "SAP:";
	private final static String NAMESPACE = "http://www.sap.com/ILM/";

	private String uri;
	private String range;
	private String type;

	public XmlDasPropFind(XmlDasPropFindRequest propFindRequest) {
		this.sac = propFindRequest.getSac();
		this.uri = propFindRequest.getUri();
		this.range = propFindRequest.getRange();
		this.type = propFindRequest.getType();
	}

	public XmlDasPropFindResponse execute() {

		// Variables
		boolean resourceNotFound = false;
		IConnection conn = null;
		PropfindRequest request = null;
		IResponse response = null;

		// Create PROPFIND Response Object
		XmlDasPropFindResponse propFindResponse = new XmlDasPropFindResponse();

		try {

			// Create PROPFIND Request
			if (type.equalsIgnoreCase("COL"))
				request = new PropfindRequest(uri.substring(1) + "/");
			else
				request = new PropfindRequest(uri.substring(1));

			// Add Name Space, Depth And Property Names
			request.addNamespace(PREFIX, NAMESPACE);
			request.setDepth(Depth.DEPTH_0);
			if (this.range.equalsIgnoreCase("*"))
				request.setOption(PropfindOption.ALL_PROPERTIES);
			else {
				request.setOption(PropfindOption.PROPERTIES);
				request.addPropertyGet(PREFIX + this.range);
			}

			// Acquire DTR Connection
			conn = WebDavTemplateProvider.acquireWebDAVConnection(sac.store_id);

			// Send PROPFIND Request
			response = request.perform(conn);

			// Check If Response Is Multi-Status
			if (response.isMultiStatus()) {

				// Write Multi-Status Response
				Iterator<ResourceElement> iter1 = request.getResources();
				ArrayList<String> al = new ArrayList<String>();
				StringBuffer sb = new StringBuffer();
				while (iter1.hasNext()) {
					ResourceElement resourceElement = iter1.next();
					Iterator<PropertyElement> iter2 = resourceElement
							.getProperties();
					while (iter2.hasNext()) {
						PropertyElement propertyElement = iter2.next();
						if (propertyElement.getStatusCode() == HttpServletResponse.SC_OK) {
							if (propertyElement.getNamespaceURI()
									.equalsIgnoreCase(NAMESPACE)
									&& !propertyElement.getName()
											.equalsIgnoreCase("legal_hold")) {
								sb.append(propertyElement.getName());
								sb.append("=");
								sb.append(propertyElement.getValue());
								if (iter2.hasNext())
									sb.append("#");
								else
									sb.append("\r\n");
							}
						} else {
							if (propertyElement.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
								resourceNotFound = true;
								break;
							} else
								al.add(propertyElement.getName());
						}
					}
				}

				// Set Response Data
				if (al.size() == 0) {
					if (resourceNotFound == true) {
						propFindResponse
								.setStatusCode(XmlDasMaster.SC_NOT_FOUND);
						propFindResponse
								.setReasonPhrase(XmlDasMaster.RP_NOT_FOUND);
						propFindResponse.setEntityBody("");
					} else {
						propFindResponse.setStatusCode(XmlDasMaster.SC_OK);
						propFindResponse.setReasonPhrase(XmlDasMaster.RP_OK);
						propFindResponse.setEntityBody(sb.toString());
					}
				} else {
					propFindResponse
							.setStatusCode(XmlDasMaster.SC_CANNOT_READ_WEBDAV_PROPERTY);
					propFindResponse
							.setReasonPhrase(XmlDasMaster.RP_CANNOT_READ_WEBDAV_PROPERTY);
					StringBuffer errorProperties = new StringBuffer();
					for (int i = 0; i < al.size(); i++) {
						errorProperties.append((String) al.get(i));
						if (i != (al.size() - 1))
							errorProperties.append(", ");
					}
					propFindResponse.setEntityBody(errorProperties.toString());
				}
			} else {
				propFindResponse.setStatusCode(response.getStatus());
				propFindResponse.setReasonPhrase(response
						.getStatusDescription());
				if (response.getContentLength() != 0) {
					BufferedReader br = null;
					try {
						br = new BufferedReader(new InputStreamReader(response
								.getStream(), "UTF-8"));
						String entityBodyLine = "";
						String entityBody = "";
						while ((entityBodyLine = br.readLine()) != null)
							entityBody = entityBody + entityBodyLine + "\r\n";
						br.close();
						propFindResponse.setEntityBody(entityBody);
					} catch (Exception ex) {
						propFindResponse
								.setEntityBody("Error while reading WebDAV PROPFIND Response Body: "
										+ ex.toString());
						propFindResponse.setException(ex);
					} finally {
						if (br != null)
							br.close();
					}
				}
			}
		} catch (IOException ioex) {
			if (propFindResponse.getStatusCode() == 0) {
				propFindResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				propFindResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				propFindResponse.setEntityBody(ioex.toString());
				propFindResponse.setException(ioex);
			}
			return propFindResponse;
		} catch (Exception ex) {
			if (propFindResponse.getStatusCode() == 0) {
				propFindResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				propFindResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				propFindResponse.setEntityBody(ex.toString());
				propFindResponse.setException(ex);
			}
			return propFindResponse;
		} finally {
			try {

				// Prepare Request Object For Reuse
				if (request != null)
					request.clear();

				// Release Response Stream For Reuse By Other Requests
				if (response != null)
					response.releaseStream();
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
		return propFindResponse;
	}
}
