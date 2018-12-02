package com.sap.archtech.daservice.beanfacade;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>CommandResponse</code> is responsible for temporarily buffering
 * the data resulting from the execution of those XMLDAS commands exposed in the
 * Session Bean Facade. Although it does not deal with HTTP in any way, it is
 * derived from the <code>HttpServletResponse</code> because this is required
 * by the XMLDAS commands.
 */
class CommandResponse implements HttpServletResponse {
	private final CommandResponseSerializer serializer;
	private final Properties headerProperties;
	private int status;
	private String characterEncoding;
	private String contentType;
	private Locale locale;
	private boolean isCommitted;

	CommandResponse() {
		serializer = new CommandResponseSerializer();
		headerProperties = new Properties();
		status = 200;
		characterEncoding = "UTF-8";
		contentType = "text/xml";
		locale = Locale.getDefault();
		isCommitted = false;
	}

	// Servlet API methods required by XMLDAS commands
	public ServletOutputStream getOutputStream() {
		return serializer;
	}

	public void setHeader(String s, String s1) {
		headerProperties.setProperty(s, s1);
	}

	public void setStatus(int i) {
		status = i;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	// Custom methods
	int getStatus() {
		return status;
	}

	String getHeaderProperty(String property) {
		if (headerProperties.containsKey(property)) {
			return headerProperties.getProperty(property);
		}
		return "";
	}

	// Servlet API methods with dummy implementations
	public void addCookie(Cookie cookie) {
		throw new UnsupportedOperationException("Cookies are not supported");
	}

	public boolean containsHeader(String s) {
		return headerProperties.containsKey(s);
	}

	public String encodeURL(String s) {
		throw new UnsupportedOperationException("URL encoding is not supported");
	}

	public String encodeRedirectURL(String s) {
		throw new UnsupportedOperationException("URL encoding is not supported");
	}

	public String encodeUrl(String s) {
		throw new UnsupportedOperationException("URL encoding is not supported");
	}

	public String encodeRedirectUrl(String s) {
		throw new UnsupportedOperationException("URL encoding is not supported");
	}

	public void sendError(int i, String s) throws IOException {
		throw new UnsupportedOperationException(
				"Method \"sendError(int, String)\" is not supported");
	}

	public void sendError(int i) throws IOException {
		throw new UnsupportedOperationException(
				"Method \"sendError(int)\" is not supported");
	}

	public void sendRedirect(String s) throws IOException {
		throw new UnsupportedOperationException("Redirection is not supported");
	}

	public void setDateHeader(String s, long l) {
		throw new UnsupportedOperationException(
				"Date headers are not supported");
	}

	public void addDateHeader(String s, long l) {
		throw new UnsupportedOperationException(
				"Date headers are not supported");
	}

	public void addHeader(String s, String s1) {
		throw new UnsupportedOperationException(
				"Adding headers is not supported");
	}

	public void setIntHeader(String s, int i) {
		throw new UnsupportedOperationException(
				"Integer headers are not supported");
	}

	public void addIntHeader(String s, int i) {
		throw new UnsupportedOperationException(
				"Integer headers are not supported");
	}

	public void setStatus(int i, String s) {
		throw new UnsupportedOperationException(
				"Method \"setStatus(int, String)\" is not supported");
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public String getContentType() {
		return contentType;
	}

	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(serializer);
	}

	public void setCharacterEncoding(String s) {
		characterEncoding = s;
	}

	public void setContentLength(int i) {
		throw new UnsupportedOperationException(
				"Method \"setContentLength(int)\" is not supported");
	}

	public void setContentType(String s) {
		contentType = s;
	}

	public void setBufferSize(int i) {
		// no buffering
	}

	public int getBufferSize() {
		return 0;
	}

	public void flushBuffer() {
		// see javadoc: calling this methods commits the response
		isCommitted = true;
	}

	public void resetBuffer() {
		// see javadoc: throws exception if committed
		if (isCommitted) {
			throw new IllegalStateException(
					"CommandResponse has already been committed");
		}
	}

	public void reset() {
		// see javadoc: clears headers and status code, throws exception if
		// committed
		if (isCommitted) {
			throw new IllegalStateException(
					"CommandResponse has already been committed");
		}
		status = 0;
		headerProperties.clear();
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}
}
