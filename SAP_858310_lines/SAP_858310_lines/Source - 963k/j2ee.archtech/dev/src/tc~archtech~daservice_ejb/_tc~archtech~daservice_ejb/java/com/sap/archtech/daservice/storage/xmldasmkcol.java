package com.sap.archtech.daservice.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.dav.MkColRequest;

public class XmlDasMkcol extends XmlDasMaster {

	private String uri;

	public XmlDasMkcol(XmlDasMkcolRequest mkcolRequest) {
		this.sac = mkcolRequest.getSac();
		this.uri = mkcolRequest.getUri();
	}

	public XmlDasMkcolResponse execute() {

		// Create MKCOL Response Object
		XmlDasMkcolResponse mkcolResponse = new XmlDasMkcolResponse();

		// WebDAV-System
		if (sac.type.equalsIgnoreCase("W")) {
			IConnection conn = null;
			MkColRequest request = null;
			IResponse response = null;
			try {

				// Create MKCOL Request
				request = new MkColRequest(uri.substring(1));

				// Acquire DTR Connection
				conn = WebDavTemplateProvider
						.acquireWebDAVConnection(sac.store_id);

				// Send MKCOL Request
				response = conn.send(request);

				// Set Response Data
				mkcolResponse.setStatusCode(response.getStatus());
				mkcolResponse.setReasonPhrase(response.getStatusDescription());
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
						mkcolResponse.setEntityBody(entityBody);
					} catch (Exception ex) {
						mkcolResponse
								.setEntityBody("Error while reading WebDAV MKCOL Response Body: "
										+ ex.toString());
						mkcolResponse.setException(ex);
					} finally {
						if (br != null)
							br.close();
					}
				}
			} catch (IOException ioex) {
				if (mkcolResponse.getStatusCode() == 0) {
					mkcolResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
					mkcolResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
					mkcolResponse.setEntityBody(ioex.toString());
					mkcolResponse.setException(ioex);
				}
				return mkcolResponse;
			} catch (Exception ex) {
				if (mkcolResponse.getStatusCode() == 0) {
					mkcolResponse
							.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
					mkcolResponse
							.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
					mkcolResponse.setEntityBody(ex.toString());
					mkcolResponse.setException(ex);
				}
				return mkcolResponse;
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
		}

		// File-System
		else {
			try {
				String physicalPath = "";

				// Unix Operation System
				if (System.getProperty("file.separator").startsWith("/")) {
					if (sac.unix_root.contains("<DIR_GLOBAL>"))
						physicalPath = sac.unix_root.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR)
								+ uri;
					else
						physicalPath = sac.unix_root + uri;
				}

				// Windows Operating System
				else {
					if (sac.win_root.contains("<DIR_GLOBAL>"))
						physicalPath = sac.win_root.replace("<DIR_GLOBAL>",
								MasterMethod.GLOBAL_DIR)
								+ uri.replace('/', '\\');
					else
						physicalPath = sac.win_root + uri.replace('/', '\\');
				}

				// Create File Object
				File directory = new File(physicalPath);

				// Create Directory
				if (!directory.mkdir())
					throw new IOException("Error while creating collection "
							+ physicalPath);

				// Set Response Data
				mkcolResponse.setStatusCode(XmlDasMaster.SC_CREATED);
				mkcolResponse.setReasonPhrase(XmlDasMaster.RP_CREATED);
			} catch (IOException ioex) {
				mkcolResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				mkcolResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				mkcolResponse.setEntityBody(ioex.toString());
				mkcolResponse.setException(ioex);
				return mkcolResponse;
			} catch (Exception ex) {
				mkcolResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				mkcolResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				mkcolResponse.setEntityBody(ex.toString());
				mkcolResponse.setException(ex);
				return mkcolResponse;
			}
		}

		// Return Response When Method Was Successful
		return mkcolResponse;
	}
}
