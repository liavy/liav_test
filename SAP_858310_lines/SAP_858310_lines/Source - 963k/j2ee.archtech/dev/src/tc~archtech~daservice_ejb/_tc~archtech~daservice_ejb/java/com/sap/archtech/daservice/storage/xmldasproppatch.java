package com.sap.archtech.daservice.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.entities.PropertyElement;
import com.tssap.dtr.client.lib.protocol.entities.ResourceElement;
import com.tssap.dtr.client.lib.protocol.requests.dav.ProppatchRequest;

public class XmlDasPropPatch extends XmlDasMaster {

	private final static String PREFIX = "SAP:";
	private final static String NAMESPACE = "http://www.sap.com/ILM/";

	private String uri;
	private String action;
	private String properties;
	private String type;

	public XmlDasPropPatch(XmlDasPropPatchRequest propPatchRequest) {
		this.sac = propPatchRequest.getSac();
		this.uri = propPatchRequest.getUri();
		this.action = propPatchRequest.getAction();
		this.properties = propPatchRequest.getProperties();
		this.type = propPatchRequest.getType();
	}

	public XmlDasPropPatchResponse execute() {

		// Create PROPPATCH Response Object
		XmlDasPropPatchResponse propPatchResponse = new XmlDasPropPatchResponse();

		IConnection conn = null;
		ProppatchRequest request = null;
		IResponse response = null;
		try {

			// Create PROPPATCH Request
			if (type.equalsIgnoreCase("COL"))
				request = new ProppatchRequest(uri.substring(1) + "/");
			else
				request = new ProppatchRequest(uri.substring(1));

			// Add Name Space And Properties
			request.addNamespace(PREFIX, NAMESPACE);
			StringTokenizer st = new StringTokenizer(this.properties, "#");
			while (st.hasMoreTokens()) {
				String token = (String) st.nextToken();
				if (action.equalsIgnoreCase("Set")) {
					request.addPropertySet(PREFIX
							+ token.substring(0, token.lastIndexOf("="))
									.toLowerCase(), token.substring(
							token.lastIndexOf("=") + 1, token.length()).trim());
				} else if (action.equalsIgnoreCase("Remove")) {
					request.addPropertyRemove(PREFIX
							+ token.substring(0, token.lastIndexOf("="))
									.toLowerCase());
				}
			}

			// Acquire DTR Connection
			conn = WebDavTemplateProvider.acquireWebDAVConnection(sac.store_id);

			// Send PROPPATCH Request
			response = request.perform(conn);

			// Check If Response Is Multi-Status
			if (response.isMultiStatus()) {

				// Write Multi-Status Response
				Iterator<ResourceElement> iter1 = request.getResources();
				ArrayList<String> al = new ArrayList<String>();
				while (iter1.hasNext()) {
					ResourceElement resourceElement = iter1.next();
					Iterator<PropertyElement> iter2 = resourceElement
							.getProperties();
					while (iter2.hasNext()) {
						PropertyElement propertyElement = iter2.next();
						if (propertyElement.getStatusCode() != HttpServletResponse.SC_OK)
							al
									.add(propertyElement.getName()
											+ " "
											+ propertyElement.getStatusCode()
											+ " "
											+ propertyElement
													.getStatusDescription()
											+ " "
											+ propertyElement
													.getErrorCondition());
					}
				}

				// Set Response Data
				if (al.size() == 0) {
					propPatchResponse.setStatusCode(XmlDasMaster.SC_OK);
					propPatchResponse.setReasonPhrase(XmlDasMaster.RP_OK);
				} else {
					propPatchResponse
							.setStatusCode(XmlDasMaster.SC_CANNOT_UPDATE_WEBDAV_PROPERTY);
					propPatchResponse
							.setReasonPhrase(XmlDasMaster.RP_CANNOT_UPDATE_WEBDAV_PROPERTY);
					StringBuffer errorProperties = new StringBuffer();
					for (int i = 0; i < al.size(); i++) {
						errorProperties.append((String) al.get(i));
						if (i != (al.size() - 1))
							errorProperties.append(", ");
					}
					propPatchResponse.setEntityBody(errorProperties.toString());
				}
			} else {
				propPatchResponse.setStatusCode(response.getStatus());
				propPatchResponse.setReasonPhrase(response
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
						propPatchResponse.setEntityBody(entityBody);
					} catch (Exception ex) {
						propPatchResponse
								.setEntityBody("Error while reading WebDAV PROPPATCH Response Body: "
										+ ex.toString());
						propPatchResponse.setException(ex);
					} finally {
						if (br != null)
							br.close();
					}
				}
			}
		} catch (IOException ioex) {
			if (propPatchResponse.getStatusCode() == 0) {
				propPatchResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				propPatchResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				propPatchResponse.setEntityBody(ioex.toString());
				propPatchResponse.setException(ioex);
			}
			return propPatchResponse;
		} catch (Exception ex) {
			if (propPatchResponse.getStatusCode() == 0) {
				propPatchResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				propPatchResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				propPatchResponse.setEntityBody(ex.toString());
				propPatchResponse.setException(ex);
			}
			return propPatchResponse;
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
		return propPatchResponse;
	}
}
