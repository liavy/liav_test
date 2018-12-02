package com.sap.archtech.daservice.storage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.http.GetRequest;

public class XmlDasGetStream extends XmlDasMaster {

	private final int READBUFFER = 65536;

	private OutputStream os;
	private String uri;
	private long offset;
	private int length;

	public XmlDasGetStream(XmlDasGetStreamRequest getStreamRequest) {
		this.sac = getStreamRequest.getSac();
		this.os = getStreamRequest.getOutputStream();
		this.uri = getStreamRequest.getUri();
		this.offset = getStreamRequest.getOffset();
		this.length = getStreamRequest.getLength();
	}

	public XmlDasGetStreamResponse execute() {

		// Create GET Response Object
		XmlDasGetStreamResponse getStreamResponse = new XmlDasGetStreamResponse();
		InputStream is = null;

		// WebDAV-System
		if (sac.type.equalsIgnoreCase("W")) {
			IConnection conn = null;
			GetRequest request = null;
			IResponse response = null;
			try {

				// Create GET Request
				request = new GetRequest(uri.substring(1));

				// Set Range Request
				request.setHeader("Range", "bytes="
						+ new Long(offset).toString() + "-"
						+ new Long(offset + length - 1).toString());

				// Acquire DTR Connection
				conn = WebDavTemplateProvider
						.acquireWebDAVConnection(sac.store_id);

				// Send GET Request
				response = conn.send(request);

				// Set Response Data
				getStreamResponse.setStatusCode(response.getStatus());
				getStreamResponse.setReasonPhrase(response
						.getStatusDescription());

				// Get Input Stream
				is = response.getStream();

				// Read Input Stream And Write It Directly Into Output Stream
				try {
					byte[] buffer = new byte[READBUFFER];
					int read = 0;
					long contentLength = 0;
					while ((read = is.read(buffer)) != -1) {
						os.write(buffer, 0, read);
						contentLength += read;
					}
					getStreamResponse.setContentLength(contentLength);
					is.close();
					response.releaseStream();
				} catch (IOException ioex) {
					is.close();
					response.releaseStream();
					throw ioex;
				} finally {
					if (os != null)
						os.flush();
				}
			} catch (IOException ioex) {
				getStreamResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				getStreamResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				getStreamResponse.setEntityBody(ioex.toString());
				getStreamResponse.setException(ioex);
				return getStreamResponse;
			} catch (Exception ex) {
				getStreamResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				getStreamResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				getStreamResponse.setEntityBody(ex.toString());
				getStreamResponse.setException(ex);
				return getStreamResponse;
			} finally {

				// Prepare Request Object For Reuse
				if (request != null)
					request.clear();

				// Release DTR Connection
				WebDavTemplateProvider.releaseWebDAVConnection(conn);
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

				// Set Response Data
				getStreamResponse
						.setStatusCode(XmlDasMaster.SC_PARTIAL_CONTENT);
				getStreamResponse
						.setReasonPhrase(XmlDasMaster.RP_PARTIAL_CONTENT);

				// Get Input Stream
				RandomAccessFile raf = new RandomAccessFile(physicalPath, "r");
				raf.seek(offset);
				is = new BufferedInputStream(new FileInputStream(raf.getFD()));

				// Read Input Stream And Write It Directly Into Output Stream
				try {
					byte[] buffer = new byte[READBUFFER];
					int read = 0;
					int contentLength = 0;
					while ((read = is.read(buffer)) != -1) {
						if ((contentLength + read) <= length) {
							os.write(buffer, 0, read);
							contentLength += read;
						} else {
							read = length - contentLength;
							os.write(buffer, 0, read);
							contentLength += read;
							break;
						}
					}
					getStreamResponse.setContentLength(contentLength);
				} catch (IOException ioex) {
					throw ioex;
				} finally {
					if (is != null)
						is.close();
					if (os != null)
						os.flush();
				}
			} catch (IOException ioex) {
				getStreamResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				getStreamResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				getStreamResponse.setEntityBody(ioex.toString());
				getStreamResponse.setException(ioex);
				return getStreamResponse;
			} catch (Exception ex) {
				getStreamResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				getStreamResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				getStreamResponse.setEntityBody(ex.toString());
				getStreamResponse.setException(ex);
				return getStreamResponse;
			}
		}

		// Return Response When Method Was Successful
		return getStreamResponse;
	}
}
