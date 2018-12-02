package com.sap.archtech.daservice.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.GeneralConflictException;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.HTTPException;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.http.DeleteRequest;

public class XmlDasDelete extends XmlDasMaster {

	private String uri;
	private String range;
	private HashSet<Long> notDeletedColIds;
	private HashSet<Long> notDeletedResIds;
	private HashMap<Long, HashSet<Long>> notDeletedColStoreIds;
	private Connection connection;

	public XmlDasDelete(XmlDasDeleteRequest deleteRequest) {
		this.sac = deleteRequest.getSac();
		this.uri = deleteRequest.getUri();
		this.range = deleteRequest.getRange();
		this.notDeletedColIds = deleteRequest.getNotDeletedColIds();
		this.notDeletedResIds = deleteRequest.getNotDeletedResIds();
		this.notDeletedColStoreIds = deleteRequest.getNotDeletedColStoreIds();
		this.connection = deleteRequest.getConnection();
	}

	public XmlDasDeleteResponse execute() {
		// Create DELETE Response Object
		XmlDasDeleteResponse deleteResponse = new XmlDasDeleteResponse();

		// Adapt URI If A Collection Should Be Deleted
		if (this.range.equalsIgnoreCase("COL"))
			this.uri += "/";

		// WebDAV-System
		if (sac.type.equalsIgnoreCase("W")) {
			IConnection conn = null;
			DeleteRequest request = null;
			IResponse response = null;
			int socketReadTimeout = 60000;
			try {

				// Create DELETE Request
				request = new DeleteRequest(uri.substring(1));

				// Acquire DTR Connection
				try {
					conn = WebDavTemplateProvider
							.acquireWebDAVConnection(sac.store_id);
				} catch (Exception ex) {
					throw new GeneralConflictException(ex.toString());
				}

				// Increase SocketReadTimeout About Factor Ten
				socketReadTimeout = conn.getSocketReadTimeout();
				conn.setSocketReadTimeout(socketReadTimeout * 10);

				// Send DELETE Request
				response = conn.send(request);

				// Set Response Data
				deleteResponse.setStatusCode(response.getStatus());
				deleteResponse.setReasonPhrase(response.getStatusDescription());

				// Collection With All Internal Members Has Been Successfully
				// Deleted
				if (deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
						|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED
						|| deleteResponse.getStatusCode() == HttpServletResponse.SC_NO_CONTENT) {
					if (response.getContentLength() > 0) {
						BufferedReader br = null;
						try {
							br = new BufferedReader(new InputStreamReader(
									response.getStream(), "UTF-8"));
							String entityBodyLine = "";
							String entityBody = "";
							while ((entityBodyLine = br.readLine()) != null)
								entityBody = entityBody + entityBodyLine
										+ "\r\n";
							br.close();
							deleteResponse.setEntityBody(entityBody);
						} catch (Exception ex) {
							deleteResponse
									.setEntityBody("Error while reading WebDAV DELETE Response Body: "
											+ ex.toString());
							deleteResponse.setException(ex);
						} finally {
							if (br != null) {
								try {
									br.close();
								} catch (IOException ioex) {

									// $JL-EXC$
									String s = ioex.toString();
									s.toLowerCase();
								}
							}
						}
					}
				}

				// Parts Of The Collection Have Not Been Deleted
				else if (deleteResponse.getStatusCode() == DasResponse.SC_MULTI_STATUS) {
					try {

						// Delete Collection
						if (this.range.equalsIgnoreCase("COL")) {

							// Only DELETE From Collection Is Required To Read
							// Multi-Status
							if ((notDeletedColIds != null)
									&& (notDeletedResIds != null)
									&& (notDeletedColStoreIds != null)
									&& (connection != null)) {

								// Create SAX Parser
								SAXParserFactory factory = SAXParserFactory
										.newInstance();
								SAXParser parser = factory.newSAXParser();
								MultiStatusHandler multiStatusHandler = new MultiStatusHandler(
										sac, uri.substring(0, uri
												.length() - 1),
										notDeletedColIds, notDeletedResIds,
										notDeletedColStoreIds, connection);
								parser.parse(response.getStream(),
										multiStatusHandler);
							}
						}

						// Delete Resource
						else {

							// Create SAX Parser
							SAXParserFactory factory = SAXParserFactory
									.newInstance();
							SAXParser parser = factory.newSAXParser();
							HashSet<String> resList = new HashSet<String>();
							MultiStatusHandlerResource multiStatusHandler = new MultiStatusHandlerResource(
									sac, uri
											.substring(0, uri.length()),
									resList);
							parser.parse(response.getStream(),
									multiStatusHandler);
							if (resList.isEmpty()) {
								deleteResponse
										.setEntityBody(HttpServletResponse.SC_INTERNAL_SERVER_ERROR
												+ " URI "
												+ this.uri
												+ " not found in Multi-Status delete response");
							} else if (resList.size() == 1) {
								deleteResponse.setEntityBody((String) resList
										.iterator().next());
							} else {
								deleteResponse
										.setEntityBody(HttpServletResponse.SC_INTERNAL_SERVER_ERROR
												+ " URI "
												+ this.uri
												+ " "
												+ resList.size()
												+ " times found in Multi-Status delete response");
							}
						}
					} catch (SAXException sex) {
						deleteResponse.setEntityBody(sex.toString());
						deleteResponse.setException(sex);
					} catch (IOException ioex) {
						deleteResponse.setEntityBody(ioex.toString());
						deleteResponse.setException(ioex);
					} catch (ParserConfigurationException pcex) {
						deleteResponse.setEntityBody(pcex.toString());
						deleteResponse.setException(pcex);
					} catch (Exception ex) {
						deleteResponse.setEntityBody(ex.toString());
						deleteResponse.setException(ex);
					}
				}

				// Collection With All Internal Members Was Not Deleted
				else {
					if (response.getContentLength() > 0) {
						BufferedReader br = null;
						try {
							br = new BufferedReader(new InputStreamReader(
									response.getStream(), "UTF-8"));
							String entityBodyLine = "";
							String entityBody = "";
							while ((entityBodyLine = br.readLine()) != null)
								entityBody = entityBody + entityBodyLine
										+ "\r\n";
							br.close();
							deleteResponse.setEntityBody(entityBody);
						} catch (Exception ex) {
							deleteResponse
									.setEntityBody("Error while reading WebDAV DELETE Response Body: "
											+ ex.toString());
							deleteResponse.setException(ex);
						} finally {
							if (br != null) {
								try {
									br.close();
								} catch (IOException ioex) {

									// $JL-EXC$
									String s = ioex.toString();
									s.toLowerCase();
								}
							}
						}
					}
				}
			} catch (GeneralConflictException gcex) {
				if (deleteResponse.getStatusCode() == 0) {

					deleteResponse
							.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
					deleteResponse
							.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
					deleteResponse.setEntityBody(gcex.toString());
					deleteResponse.setException(gcex);
				}
				return deleteResponse;
			} catch (HTTPException httpex) {
				if (deleteResponse.getStatusCode() == 0) {
					deleteResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
					deleteResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
					deleteResponse.setEntityBody(httpex.toString());
					deleteResponse.setException(httpex);
				}
				return deleteResponse;
			} catch (IOException ioex) {
				if (deleteResponse.getStatusCode() == 0) {
					deleteResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
					deleteResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
					deleteResponse.setEntityBody(ioex.toString());
					deleteResponse.setException(ioex);
				}
				return deleteResponse;
			} finally {
				try {

					// Prepare Request Object For Reuse
					if (request != null)
						request.clear();

					// Release The Response Stream
					if (response != null)
						response.releaseStream();
				} catch (IOException ioex) {

					// $JL-EXC$
					String s = ioex.toString();
					s.toLowerCase();
				} finally {

					// Decrease SocketReadTimeout To Initially Value
					conn.setSocketReadTimeout(socketReadTimeout);

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
				File file = new File(physicalPath);

				// Delete Directory With All Internal Members
				if (this.range.equalsIgnoreCase("COL")) {
					if (!traverse(file))
						throw new IOException(
								"Error while deleting collection "
										+ physicalPath);
				}

				// Delete Single File
				else {
					if (!file.delete())
						throw new IOException("Error while deleting resource "
								+ physicalPath);
				}

				// Set Response Data
				deleteResponse.setStatusCode(XmlDasMaster.SC_OK);
				deleteResponse.setReasonPhrase(XmlDasMaster.RP_OK);
			} catch (IOException ioex) {
				deleteResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				deleteResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				deleteResponse.setEntityBody(ioex.toString());
				deleteResponse.setException(ioex);
				return deleteResponse;
			} catch (Exception ex) {
				deleteResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				deleteResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				deleteResponse.setEntityBody(ex.toString());
				deleteResponse.setException(ex);
				return deleteResponse;
			}
		}

		// Return Response When Method Was Successful
		return deleteResponse;
	}

	public static boolean traverse(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = traverse(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}
