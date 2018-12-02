package com.sap.archtech.daservice.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sap.archtech.daservice.commands.MasterMethod;
import com.sap.archtech.daservice.exceptions.SAXParseExceptionExtended;
import com.sap.archtech.daservice.util.Base64;
import com.sap.archtech.daservice.util.WebDavTemplateProvider;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.IResponseStream;
import com.tssap.dtr.client.lib.protocol.entities.StreamEntity;
import com.tssap.dtr.client.lib.protocol.requests.http.PutRequest;

public class XmlDasPut extends XmlDasMaster {

	private final int READBUFFER = 65536;

	public static boolean FILESYSTEMSYNC;
	private InputStream is;
	private String uri;
	private String type;
	private String mode;
	private String level;
	private Schema schema;

	public XmlDasPut(XmlDasPutRequest putRequest) {
		this.sac = putRequest.getSac();
		this.is = putRequest.getIs();
		this.uri = putRequest.getUri();
		this.type = putRequest.getType();
		this.mode = putRequest.getMode();
		this.level = putRequest.getLevel();
		this.schema = putRequest.getSchema();
	}

	public XmlDasPutResponse execute() {

		// Create PUT Response Object
		XmlDasPutResponse putResponse = new XmlDasPutResponse();

		// STORE Mode
		if (this.mode.equalsIgnoreCase("STORE")) {

			// WebDAV-System
			if (sac.type.equalsIgnoreCase("W")) {
				IConnection conn = null;
				PutRequest request = null;
				BufferedInputStream bis = null;
				IResponse response = null;
				try {

					// Create Digest Input Stream
					MessageDigest md = MessageDigest.getInstance("MD5");
					bis = new BufferedInputStream(new DigestInputStream(is, md));

					// Set Content Type
					if (type.equalsIgnoreCase("BIN"))
						type = "application/octet-stream";
					else
						type = "text/xml";

					// Create PUT Request
					InputStreamWrapperReadOnly iswro = null;
					StreamEntity se = null;
					ParserStreamEntity pse = null;
					ValidatorStreamEntity vse = null;

					// NO
					if (level.equalsIgnoreCase("NO")) {
						iswro = new InputStreamWrapperReadOnly(bis);
						se = new StreamEntity(iswro, type);
						request = new PutRequest(uri.substring(1), se);
					}

					// PARSE
					else if (level.equalsIgnoreCase("PARSE")) {
						pse = new ParserStreamEntity(bis, type);
						request = new PutRequest(uri.substring(1), pse);
					}

					// VALIDATE
					else {
						vse = new ValidatorStreamEntity(bis, type, schema);
						request = new PutRequest(uri.substring(1), vse);
					}

					// Acquire DTR Connection
					conn = WebDavTemplateProvider
							.acquireWebDAVConnection(sac.store_id);

					// Send PUT Request
					response = conn.send(request);

					// Get Response Data
					putResponse.setStatusCode(response.getStatus());
					putResponse
							.setReasonPhrase(response.getStatusDescription());
					putResponse.setCheckSum(Base64.encode(md.digest()));

					// NO
					if (level.equalsIgnoreCase("NO")) {
						if (iswro != null)
							putResponse.setContentLength(iswro
									.getContentLength());
					}
					// PARSE
					else if (level.equalsIgnoreCase("PARSE")) {
						if (pse != null)
							putResponse.setContentLength(pse
									.getSentContentLength());
					}
					// VALIDATE
					else {
						if (vse != null)
							putResponse.setContentLength(vse
									.getSentContentLength());
					}

					// Get Response Body
					if (response.getContentLength() != 0) {
						IResponseStream irs = response.getContent();
						if (irs != null) {
							BufferedReader rbr = null;
							try {
								InputStream ris = irs.asStream();
								if (ris != null) {
									rbr = new BufferedReader(
											new InputStreamReader(ris));
									StringBuffer sb = new StringBuffer();
									String s = "";
									while ((s = rbr.readLine()) != null)
										sb.append(s);
									putResponse.setEntityBody(sb.toString());
								}
							} catch (Exception ex) {
								putResponse
										.setEntityBody("Error while reading WebDAV PUT Response Body: "
												+ ex.toString());
								putResponse.setException(ex);
							} finally {
								if (rbr != null)
									rbr.close();
							}
						}
					}
				} catch (SAXParseExceptionExtended spex) {
					if (putResponse.getStatusCode() == 0) {
						putResponse.setStatusCode(XmlDasMaster.SC_CHECK_FAILED);
						putResponse
								.setReasonPhrase(XmlDasMaster.RP_CHECK_FAILED);
						putResponse.setEntityBody(spex.toString());
						putResponse.setException(spex);
					}
					return putResponse;
				} catch (IOException ioex) {
					if (putResponse.getStatusCode() == 0) {
						putResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
						putResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
						putResponse.setEntityBody(ioex.toString());
						putResponse.setException(ioex);
					}
					return putResponse;
				} catch (Exception ex) {
					if (putResponse.getStatusCode() == 0) {
						putResponse
								.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
						putResponse
								.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
						putResponse.setEntityBody(ex.toString());
						putResponse.setException(ex);
					}
					return putResponse;
				} finally {
					try {

						// Close Input Stream
						if (bis != null)
							bis.close();

						// Prepare Request Object For Reuse
						if (request != null)
							request.clear();

						// Release The Response Stream
						if (response != null)
							response.releaseStream();
					} catch (IOException ioex) {

						// $JL-EXC$
						String s = "Nothing Else To Do";
						s = s + "";
					} finally {

						// Release DTR Connection
						WebDavTemplateProvider.releaseWebDAVConnection(conn);
					}
				}
			}

			// File-System
			else {
				try {

					// Create Digest Input Stream
					MessageDigest md = MessageDigest.getInstance("MD5");
					BufferedInputStream bis = new BufferedInputStream(
							new DigestInputStream(is, md));
					BufferedOutputStream bos = null;
					FileOutputStream fos = null;

					// Unix Operation System
					if (System.getProperty("file.separator").startsWith("/")) {
						if (sac.unix_root.contains("<DIR_GLOBAL>"))
							fos = new FileOutputStream(sac.unix_root.replace(
									"<DIR_GLOBAL>", MasterMethod.GLOBAL_DIR)
									+ uri);
						else
							fos = new FileOutputStream(sac.unix_root + uri);
					}

					// Windows Operating System
					else {
						if (sac.win_root.contains("<DIR_GLOBAL>"))
							fos = new FileOutputStream(sac.win_root.replace(
									"<DIR_GLOBAL>", MasterMethod.GLOBAL_DIR)
									+ uri.replace('/', '\\'));
						else
							fos = new FileOutputStream(sac.win_root
									+ uri.replace('/', '\\'));
					}

					bos = new BufferedOutputStream(fos);

					// NO
					if (level.equalsIgnoreCase("NO")) {
						try {

							byte[] buffer = new byte[READBUFFER];
							int read = 0;
							long contentLength = 0;
							while ((read = bis.read(buffer)) != -1) {
								bos.write(buffer, 0, read);
								contentLength += read;
							}
							putResponse.setContentLength(contentLength);
						} catch (IOException ioex) {
							throw ioex;
						} finally {
							if (bis != null)
								bis.close();
							if (bos != null) {
								bos.flush();
								if (XmlDasPut.FILESYSTEMSYNC)
									fos.getFD().sync();
								bos.close();
							}
						}
					}

					// PARSE
					else if (level.equalsIgnoreCase("PARSE")) {
						InputStreamWrapperReadWrite iswrw = new InputStreamWrapperReadWrite(
								bis, bos);
						try {
							XMLReader parser = XMLReaderFactory
									.createXMLReader();
							parser.setErrorHandler(new ParserErrorHandler());
							parser.parse(new InputSource(iswrw));
							putResponse.setContentLength(iswrw
									.getContentLength());
						} catch (IOException ioex) {
							throw ioex;
						} catch (SAXParseException spex) {
							throw spex;
						} finally {
							if (iswrw != null)
								iswrw.close();
							if (bos != null) {
								bos.flush();
								if (XmlDasPut.FILESYSTEMSYNC)
									fos.getFD().sync();
								bos.close();
							}
						}
					}

					// VALIDATE
					else {
						InputStreamWrapperReadWrite iswrw = new InputStreamWrapperReadWrite(
								bis, bos);
						try {
							Validator validator = schema.newValidator();
							validator.setErrorHandler(new ParserErrorHandler());
							validator.validate(new SAXSource(new InputSource(
									iswrw)));
							putResponse.setContentLength(iswrw
									.getContentLength());
						} catch (IOException ioex) {
							throw ioex;
						} catch (SAXParseException spex) {
							throw spex;
						} finally {
							if (iswrw != null)
								iswrw.close();
							if (bos != null) {
								bos.flush();
								if (XmlDasPut.FILESYSTEMSYNC)
									fos.getFD().sync();
								bos.close();
							}
						}
					}

					// Set Response Data
					putResponse.setStatusCode(XmlDasMaster.SC_CREATED);
					putResponse.setReasonPhrase(XmlDasMaster.RP_CREATED);
					putResponse.setCheckSum(Base64.encode(md.digest()));
				} catch (SAXParseException spex) {
					putResponse.setStatusCode(XmlDasMaster.SC_CHECK_FAILED);
					putResponse.setReasonPhrase(XmlDasMaster.RP_CHECK_FAILED);
					putResponse.setEntityBody(spex.toString());
					putResponse.setException(spex);
					return putResponse;
				} catch (IOException ioex) {
					putResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
					putResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
					putResponse.setEntityBody(ioex.toString());
					putResponse.setException(ioex);
					return putResponse;
				} catch (Exception ex) {
					putResponse
							.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
					putResponse
							.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
					putResponse.setEntityBody(ex.toString());
					putResponse.setException(ex);
					return putResponse;
				}
			}
		}

		// NOSTORE Mode
		else {
			try {

				// Create Digest Input Stream
				MessageDigest md = MessageDigest.getInstance("MD5");
				BufferedInputStream bis = new BufferedInputStream(
						new DigestInputStream(is, md));

				// Create Input Stream Wrapper
				InputStreamWrapperReadOnly iswro = new InputStreamWrapperReadOnly(
						bis);

				// NO
				if (level.equalsIgnoreCase("NO")) {
					byte[] buffer = new byte[READBUFFER];
					while (iswro.read(buffer) != -1)
						;
					iswro.close();
				}

				// PARSE
				else if (level.equalsIgnoreCase("PARSE")) {
					XMLReader parser = XMLReaderFactory.createXMLReader();
					parser.setErrorHandler(new ParserErrorHandler());
					parser.parse(new InputSource(iswro));
					// iswro input stream closed by parser
				}

				// VALIDATE
				else {
					Validator validator = schema.newValidator();
					validator.setErrorHandler(new ParserErrorHandler());
					validator.validate(new SAXSource(new InputSource(iswro)));

					// iswro input stream closed by parser
				}

				// Set Response Data
				putResponse.setStatusCode(XmlDasMaster.SC_CREATED);
				putResponse.setReasonPhrase(XmlDasMaster.RP_CREATED);
				putResponse.setContentLength(iswro.getContentLength());
				putResponse.setCheckSum(Base64.encode(md.digest()));
			} catch (SAXParseException spex) {
				putResponse.setStatusCode(XmlDasMaster.SC_CHECK_FAILED);
				putResponse.setReasonPhrase(XmlDasMaster.RP_CHECK_FAILED);
				putResponse.setEntityBody(spex.toString());
				putResponse.setException(spex);
				return putResponse;
			} catch (IOException ioex) {
				putResponse.setStatusCode(XmlDasMaster.SC_IO_ERROR);
				putResponse.setReasonPhrase(XmlDasMaster.RP_IO_ERROR);
				putResponse.setEntityBody(ioex.toString());
				putResponse.setException(ioex);
				return putResponse;
			} catch (Exception ex) {
				putResponse
						.setStatusCode(XmlDasMaster.SC_INTERNAL_SERVER_ERROR);
				putResponse
						.setReasonPhrase(XmlDasMaster.RP_INTERNAL_SERVER_ERROR);
				putResponse.setEntityBody(ex.toString());
				putResponse.setException(ex);
				return putResponse;
			}
		}

		// Return Response When Method Was Successful
		return putResponse;
	}
}
