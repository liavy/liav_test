package com.sap.archtech.daservice.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.exceptions.XMLDASVirusScanException;
import com.sap.archtech.daservice.util.Base64;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.sap.security.core.server.vsi.api.Instance;
import com.sap.security.core.server.vsi.api.VSIFilterInputStream;
import com.sap.security.core.server.vsi.api.exception.VSIServiceException;
import com.sap.security.core.server.vsi.api.exception.VirusInfectionException;
import com.sap.security.core.server.vsi.api.exception.VirusScanException;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.requests.http.GetRequest;

public class XmlDasGet extends XmlDasMaster {

	private final int READBUFFER = 65536;

	private OutputStream os;
	private String uri;
	private long offset;
	private int length;
	private String checksum;
	private String mode;
	private String level;
	private Schema schema;

	public XmlDasGet(XmlDasGetRequest getRequest) {
		this.sac = getRequest.getSac();
		this.os = getRequest.getOutputStream();
		this.uri = getRequest.getUri();
		this.offset = getRequest.getOffset();
		this.length = getRequest.getLength();
		this.checksum = getRequest.getChecksum();
		this.mode = getRequest.getMode();
		this.level = getRequest.getLevel();
		this.schema = getRequest.getSchema();
	}

	public XmlDasGetResponse execute() {

		// Create GET Response Object
		XmlDasGetResponse getResponse = new XmlDasGetResponse();
		VSIFilterInputStream vis_is = null;
		InputStream is = null;
		MessageDigest md = null;
		Instance vsInstance = null;

		// Get Virus Scan Instance
		try {
			if (MasterMethod.vsiService != null)
				vsInstance = MasterMethod.vsiService
						.getInstance("archiving_DAS");
		} catch (Exception ex) {
			// $JL-EXC$
			vsInstance = null;
		}

		// WebDAV-System
		if (sac.type.equalsIgnoreCase("W")) {
			IConnection conn = null;
			GetRequest request = null;
			IResponse response = null;
			try {

				// Create GET Request
				request = new GetRequest(uri.substring(1));

				// Check If Range Request
				if (this.length != 0)
					request.setHeader("Range", "bytes="
							+ new Long(offset).toString() + "-"
							+ new Long(offset + length - 1).toString());

				// Acquire DTR Connection
				conn = WebDavTemplateProvider
						.acquireWebDAVConnection(sac.store_id);

				// Send GET Request
				response = conn.send(request);

				// Set Response Data
				getResponse.setStatusCode(response.getStatus());
				getResponse.setReasonPhrase(response.getStatusDescription());

				// Do Virus Check If Requested
				if (vsInstance != null) {
					try {

						// Create VSI Filter Input Stream
						vis_is = MasterMethod.vsiService
								.createVSIStream(response.getStream());

						// Perform Virus Scan
						if (!vsInstance.scanStream(vis_is)) {
							throw new XMLDASVirusScanException(
									"Virus Scan of archived stream from resource "
											+ this.uri + " failed");
						}
					} catch (VirusScanException vsex) {

						// VirusScanException Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan of archived stream from resource "
										+ this.uri
										+ " failed: VirusScanException occurred: "
										+ vsex.getLocalizedMessage());
					} catch (VirusInfectionException viex) {

						// VirusInfectionException Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan of archived stream from resource "
										+ this.uri
										+ " failed: VirusInfectionException occurred: "
										+ viex.getLocalizedMessage());
					} catch (VSIServiceException vsisex) {

						// VSIServiceException Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan of archived stream from resource "
										+ this.uri
										+ " failed: VSIServiceException occurred: "
										+ vsisex.getLocalizedMessage());
					} catch (Exception ex) {

						// Exception Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan of archived stream from resource "
										+ this.uri
										+ " failed: Exception occurred: "
										+ ex.getLocalizedMessage());
					} finally {
						if (MasterMethod.vsiService != null)
							MasterMethod.vsiService.releaseInstance(vsInstance);
					}
				}

				if (vsInstance != null) {

					// No Range Request
					if (this.length == 0) {

						// Check Sum
						if (this.checksum != null) {
							md = MessageDigest.getInstance("MD5");
							is = new DigestInputStream(vis_is, md);
						}

						// No Check Sum
						else {
							is = vis_is;
						}
					}
					// Range Request
					else {

						// Check Sum
						if (this.checksum != null) {
							md = MessageDigest.getInstance("MD5");
							is = new DigestInputStream(new InflaterInputStream(
									vis_is, new Inflater(true)), md);
						}

						// No Check Sum
						else {
							is = new InflaterInputStream(vis_is, new Inflater(
									true));
						}
					}
				} else {

					// No Range Request
					if (this.length == 0) {

						// Check Sum
						if (this.checksum != null) {
							md = MessageDigest.getInstance("MD5");
							is = new DigestInputStream(response.getStream(), md);
						}

						// No Check Sum
						else {
							is = response.getStream();
						}
					}
					// Range Request
					else {

						// Check Sum
						if (this.checksum != null) {
							md = MessageDigest.getInstance("MD5");
							is = new DigestInputStream(new InflaterInputStream(
									response.getStream(), new Inflater(true)),
									md);
						}

						// No Check Sum
						else {
							is = new InflaterInputStream(response.getStream(),
									new Inflater(true));
						}
					}
				}

				// Read Input Stream And Write It Directly Into Output Stream

				// NO
				if (level.equalsIgnoreCase("NO")) {

					// DELIVER
					if (mode.equalsIgnoreCase("DELIVER")) {
						try {
							byte[] buffer = new byte[READBUFFER];
							int read = 0;
							long contentLength = 0;
							while ((read = is.read(buffer)) != -1) {
								os.write(buffer, 0, read);
								contentLength += read;
							}
							getResponse.setContentLength(contentLength);
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
					}

					// NODELIVER
					else {
						try {
							byte[] buffer = new byte[READBUFFER];
							int read = 0;
							long contentLength = 0;
							while ((read = is.read(buffer)) != -1) {
								contentLength += read;
							}
							getResponse.setContentLength(contentLength);
							is.close();
							response.releaseStream();
						} catch (IOException ioex) {
							is.close();
							response.releaseStream();
							throw ioex;
						}
					}
				}

				// PARSE
				else if (level.equalsIgnoreCase("PARSE")) {

					// DELIVER
					if (mode.equalsIgnoreCase("DELIVER")) {
						InputStreamWrapperReadWrite iswrw = null;
						try {
							iswrw = new InputStreamWrapperReadWrite(is, os);
							XMLReader parser = XMLReaderFactory
									.createXMLReader();
							parser.setErrorHandler(new ParserErrorHandler());
							parser.parse(new InputSource(iswrw));
							getResponse.setContentLength(iswrw
									.getContentLength());
							iswrw.close();
							response.releaseStream();
						} catch (IOException ioex) {
							iswrw.close();
							response.releaseStream();
							throw ioex;
						} catch (SAXParseException spex) {
							iswrw.close();
							response.releaseStream();
							throw spex;
						} finally {
							if (os != null)
								os.flush();
						}
					}

					// NODELIVER
					else {
						InputStreamWrapperReadOnly iswro = null;
						try {
							iswro = new InputStreamWrapperReadOnly(is);
							XMLReader parser = XMLReaderFactory
									.createXMLReader();
							parser.setErrorHandler(new ParserErrorHandler());
							parser.parse(new InputSource(iswro));
							getResponse.setContentLength(iswro
									.getContentLength());
							iswro.close();
							response.releaseStream();
						} catch (IOException ioex) {
							iswro.close();
							response.releaseStream();
							throw ioex;
						} catch (SAXParseException spex) {
							iswro.close();
							response.releaseStream();
							throw spex;
						}
					}
				}

				// VALIDATE
				else {

					// DELIVER
					if (mode.equalsIgnoreCase("DELIVER")) {
						InputStreamWrapperReadWrite iswrw = null;
						try {
							iswrw = new InputStreamWrapperReadWrite(is, os);
							Validator validator = schema.newValidator();
							validator.setErrorHandler(new ParserErrorHandler());
							validator.validate(new SAXSource(new InputSource(
									iswrw)));
							getResponse.setContentLength(iswrw
									.getContentLength());
							iswrw.close();
							response.releaseStream();
						} catch (IOException ioex) {
							iswrw.close();
							response.releaseStream();
							throw ioex;
						} catch (SAXParseException spex) {
							iswrw.close();
							response.releaseStream();
							throw spex;
						} finally {
							os.flush();
						}
					}

					// NODELIVER
					else {
						InputStreamWrapperReadOnly iswro = null;
						try {
							iswro = new InputStreamWrapperReadOnly(is);
							Validator validator = schema.newValidator();
							validator.setErrorHandler(new ParserErrorHandler());
							validator.validate(new SAXSource(new InputSource(
									iswro)));
							getResponse.setContentLength(iswro
									.getContentLength());
							iswro.close();
							response.releaseStream();
						} catch (IOException ioex) {
							iswro.close();
							response.releaseStream();
							throw ioex;
						} catch (SAXParseException spex) {
							iswro.close();
							response.releaseStream();
							throw spex;
						}
					}
				}

				// Validate And Set Check Sum
				if (checksum != null) {
					String retrievedCheckSum = Base64.encode(md.digest());
					getResponse.setChecksum(retrievedCheckSum);
					if (retrievedCheckSum.equals(this.checksum))
						getResponse.setChecksumIdentical(true);
					else
						getResponse.setChecksumIdentical(false);
				}
			} catch (SAXParseException spex) {
				getResponse.setStatusCode(XmlDasMaster.SC_CHECK_FAILED);
				getResponse.setReasonPhrase(XmlDasMaster.RP_CHECK_FAILED);
				getResponse.setEntityBody(spex.toString());
				getResponse.setException(spex);
				return getResponse;
			} catch (XMLDASVirusScanException xdvsex) {
				getResponse.setStatusCode(XmlDasMaster.SC_VIRUS_SCAN_ERROR);
				getResponse.setReasonPhrase(XmlDasMaster.RP_VIRUS_SCAN_ERROR);
				getResponse.setEntityBody(xdvsex.toString());
				getResponse.setException(xdvsex);
				return getResponse;
			} catch (IOException ioex) {
				getResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				getResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				getResponse.setEntityBody(ioex.toString());
				getResponse.setException(ioex);
				return getResponse;
			} catch (Exception ex) {
				getResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				getResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				getResponse.setEntityBody(ex.toString());
				getResponse.setException(ex);
				return getResponse;
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

				// Do Virus Check If Requested
				if (vsInstance != null) {
					try {

						// Perform Virus Scan
						if (!vsInstance.scanFile(physicalPath)) {
							throw new XMLDASVirusScanException(
									"Virus Scan for archived file "
											+ physicalPath + " failed");
						}
					} catch (VirusScanException vsex) {

						// VirusScanException Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan for archived file "
										+ physicalPath
										+ " failed: VirusScanException occurred: "
										+ vsex.getLocalizedMessage());
					} catch (VirusInfectionException viex) {

						// VirusInfectionException Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan for archived file "
										+ physicalPath
										+ " failed: VirusInfectionException occurred: "
										+ viex.getLocalizedMessage());
					} catch (VSIServiceException vsisex) {

						// VSIServiceException Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan for archived file "
										+ physicalPath
										+ " failed: VSIServiceException occurred: "
										+ vsisex.getLocalizedMessage());
					} catch (Exception ex) {

						// Exception Occurred
						throw new XMLDASVirusScanException(
								"Virus Scan for archived file " + physicalPath
										+ " failed: Exception occurred: "
										+ ex.getLocalizedMessage());
					} finally {
						if (MasterMethod.vsiService != null)
							MasterMethod.vsiService.releaseInstance(vsInstance);
					}
				}

				// No Range Request
				if (this.length == 0) {

					// Set Response Data
					getResponse.setStatusCode(XmlDasMaster.SC_OK);
					getResponse.setReasonPhrase(XmlDasMaster.RP_OK);

					// Check Sum
					if (this.checksum != null) {
						md = MessageDigest.getInstance("MD5");
						File file = new File(physicalPath);
						is = new BufferedInputStream(new DigestInputStream(
								new FileInputStream(file), md));
					}

					// No Check Sum
					else {
						File file = new File(physicalPath);
						is = new BufferedInputStream(new FileInputStream(file));
					}
				}

				// Range Request
				else {

					// Set Response Data
					getResponse.setStatusCode(XmlDasMaster.SC_PARTIAL_CONTENT);
					getResponse
							.setReasonPhrase(XmlDasMaster.RP_PARTIAL_CONTENT);

					// Check Sum
					if (this.checksum != null) {
						md = MessageDigest.getInstance("MD5");
						RandomAccessFile raf = new RandomAccessFile(
								physicalPath, "r");
						raf.seek(offset);
						is = new BufferedInputStream(new DigestInputStream(
								new InflaterInputStream(new FileInputStream(raf
										.getFD()), new Inflater(true)), md));
					}

					// No Check Sum
					else {
						RandomAccessFile raf = new RandomAccessFile(
								physicalPath, "r");
						raf.seek(offset);
						is = new BufferedInputStream(new InflaterInputStream(
								new FileInputStream(raf.getFD()), new Inflater(
										true)));
					}
				}

				// Read Input Stream And Write It Directly Into Output Stream

				// NO
				if (level.equalsIgnoreCase("NO")) {

					// DELIVER
					if (mode.equalsIgnoreCase("DELIVER")) {
						try {
							byte[] buffer = new byte[READBUFFER];
							int read = 0;
							long contentLength = 0;
							while ((read = is.read(buffer)) != -1) {
								os.write(buffer, 0, read);
								contentLength += read;
							}
							getResponse.setContentLength(contentLength);
						} catch (IOException ioex) {
							throw ioex;
						} finally {
							if (is != null)
								is.close();
							if (os != null)
								os.flush();
						}
					}

					// NODELIVER
					else {
						try {
							byte[] buffer = new byte[READBUFFER];
							int read = 0;
							long contentLength = 0;
							while ((read = is.read(buffer)) != -1) {
								contentLength += read;
							}
							getResponse.setContentLength(contentLength);
						} catch (IOException ioex) {
							throw ioex;
						} finally {
							if (is != null)
								is.close();
						}
					}
				}

				// PARSE
				else if (level.equalsIgnoreCase("PARSE")) {

					// DELIVER
					if (mode.equalsIgnoreCase("DELIVER")) {
						InputStreamWrapperReadWrite iswrw = new InputStreamWrapperReadWrite(
								is, os);
						try {
							XMLReader parser = XMLReaderFactory
									.createXMLReader();
							parser.setErrorHandler(new ParserErrorHandler());
							parser.parse(new InputSource(iswrw));
							getResponse.setContentLength(iswrw
									.getContentLength());
						} catch (IOException ioex) {
							throw ioex;
						} catch (SAXParseException spex) {
							throw spex;
						} finally {
							iswrw.close();
							if (os != null)
								os.flush();
						}
					}

					// NODELIVER
					else {
						InputStreamWrapperReadOnly iswro = null;
						try {
							iswro = new InputStreamWrapperReadOnly(is);
							XMLReader parser = XMLReaderFactory
									.createXMLReader();
							parser.setErrorHandler(new ParserErrorHandler());
							parser.parse(new InputSource(iswro));
							getResponse.setContentLength(iswro
									.getContentLength());
						} catch (IOException ioex) {
							throw ioex;
						} catch (SAXParseException spex) {
							throw spex;
						} finally {
							iswro.close();
						}
					}
				}

				// VALIDATE
				else {

					// DELIVER
					if (mode.equalsIgnoreCase("DELIVER")) {
						InputStreamWrapperReadWrite iswrw = new InputStreamWrapperReadWrite(
								is, os);
						try {
							Validator validator = schema.newValidator();
							validator.setErrorHandler(new ParserErrorHandler());
							validator.validate(new SAXSource(new InputSource(
									iswrw)));
							getResponse.setContentLength(iswrw
									.getContentLength());
						} catch (IOException ioex) {
							throw ioex;
						} catch (SAXParseException spex) {
							throw spex;
						} finally {
							iswrw.close();
							if (os != null)
								os.flush();
						}
					}

					// NODELIVER
					else {
						InputStreamWrapperReadOnly iswro = null;
						try {
							iswro = new InputStreamWrapperReadOnly(is);
							Validator validator = schema.newValidator();
							validator.setErrorHandler(new ParserErrorHandler());
							validator.validate(new SAXSource(new InputSource(
									iswro)));
							getResponse.setContentLength(iswro
									.getContentLength());
						} catch (IOException ioex) {
							throw ioex;
						} catch (SAXParseException spex) {
							throw spex;
						} finally {
							iswro.close();
						}
					}
				}

				// Validate And Set Check Sum
				if (checksum != null) {
					String retrievedCheckSum = Base64.encode(md.digest());
					getResponse.setChecksum(retrievedCheckSum);
					if (retrievedCheckSum.equals(this.checksum))
						getResponse.setChecksumIdentical(true);
					else
						getResponse.setChecksumIdentical(false);
				}
			} catch (SAXParseException spex) {
				getResponse.setStatusCode(XmlDasMaster.SC_CHECK_FAILED);
				getResponse.setReasonPhrase(XmlDasMaster.RP_CHECK_FAILED);
				getResponse.setEntityBody(spex.toString());
				getResponse.setException(spex);
				return getResponse;
			} catch (XMLDASVirusScanException xdvsex) {
				getResponse.setStatusCode(XmlDasMaster.SC_VIRUS_SCAN_ERROR);
				getResponse.setReasonPhrase(XmlDasMaster.RP_VIRUS_SCAN_ERROR);
				getResponse.setEntityBody(xdvsex.toString());
				getResponse.setException(xdvsex);
				return getResponse;
			} catch (IOException ioex) {
				getResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				getResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				getResponse.setEntityBody(ioex.toString());
				getResponse.setException(ioex);
				return getResponse;
			} catch (Exception ex) {
				getResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				getResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				getResponse.setEntityBody(ex.toString());
				getResponse.setException(ex);
				return getResponse;
			}
		}

		// Return Response When Method Was Successful
		return getResponse;
	}
}
