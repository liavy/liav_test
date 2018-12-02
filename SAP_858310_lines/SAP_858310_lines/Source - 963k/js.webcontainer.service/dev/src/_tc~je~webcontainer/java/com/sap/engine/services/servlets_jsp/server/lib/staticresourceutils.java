/**
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;

import com.sap.engine.services.httpserver.interfaces.client.RequestLine;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.interfaces.properties.MimeMappings;
import com.sap.engine.services.httpserver.interfaces.properties.HttpProperties;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.protocol.HeaderValues;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.ServletNotFoundException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacade;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacade;

/**
 * Contains utility methods to serve dispatched static resources.
 *
 * @version 1.0 2004-10-14
 */
public class StaticResourceUtils {

	public static void dispatchToResource(ServletRequest servletrequest, ServletResponse servletresponse,
											boolean isForward, String jspFile, ApplicationContext context, boolean aliasChanged) throws IOException, ServletException {
		File f = findFile(servletrequest, isForward, jspFile, context, aliasChanged);
		if (f == null) {
			//ok - must return empty body, not to throw exception .. according to the spec
			return;
		}
    if( isForward ) {
      addForwardStaticCacheHeaders(FilterUtils.unWrapRequest(servletrequest), FilterUtils.unWrapResponse(servletresponse), jspFile);
      setContentTypeOfStaticResource(context, servletresponse, f);
    }
		Object jspWriter = null;
		String includeInOut = (String)servletrequest.getAttribute("com.sap.engine.internal.jsp.includeInOut");
		if (includeInOut != null && includeInOut.equals("true")) {
			jspWriter = servletrequest.getAttribute("com.sap.engine.internal.jsp.out");
		}
		if (jspWriter != null && jspWriter instanceof Writer) {
			writeFile(f, (Writer)jspWriter, servletrequest, servletresponse);
		} else {
			try {
        if (servletresponse instanceof javax.servlet.ServletResponseWrapper) {
          writeFile(f,  servletresponse.getOutputStream(), servletrequest, servletresponse);
        } else {
          writeFile(f,  FilterUtils.unWrapResponse(servletresponse).getOutStream(), servletrequest, servletresponse);
        }
      } catch (IllegalStateException e) {
        writeFile(f, servletresponse.getWriter(), servletrequest, servletresponse);
			}
		}
	}


	private static void writeFile(File f, Writer out, ServletRequest servletrequest, ServletResponse servletresponse) throws IOException {
		int read = 0;
		int[] range;
		char[] buf = new char[4096];

		range = prepareWriteFile(f, servletrequest, servletresponse);
		if (range != null) {
			int start = range[0];
			int finish = range[1];
			long length = finish - start + 1;
			byte[] srcBuf = new byte[buf.length];
			RandomAccessFile r = new RandomAccessFile(f, "r");
			try {
				r.seek(start);
				while (length > 0 && (read = r.read(srcBuf, 0, (length > srcBuf.length) ? srcBuf.length : (int) length)) != -1) {
					for (int i = 0; i < read; i++) {
						buf[i] = (char) (srcBuf[i] & 0xFF);
					}
					out.write(buf, 0, read);
					length = length - read;
				}
			} finally {
				r.close();
			}
		} else {
      FileInputStream fis = new FileInputStream(f);
			try {
        InputStreamReader in = new InputStreamReader(fis);
				while ((read = in.read(buf)) != -1) {
					out.write(buf, 0, read);
				}
			} finally {
				fis.close();
			}
		}
	}


	private static void writeFile(File f, OutputStream out, ServletRequest servletrequest, ServletResponse servletresponse) throws IOException {
		int read = 0;
		int[] range;
		byte[] srcBuf = new byte[4096];

		range = prepareWriteFile(f, servletrequest, servletresponse);
		if (range != null) {
			int start = range[0];
			int finish = range[1];
			long length = finish - start + 1;
			RandomAccessFile r = new RandomAccessFile(f, "r");
			try {
				r.seek(start);
				while (length > 0 && (read = r.read(srcBuf, 0, (length > srcBuf.length) ? srcBuf.length : (int) length)) != -1) {
					out.write(srcBuf, 0, read);
					length = length - read;
				}
			} finally {
				r.close();
			}
		} else {
			FileInputStream in = new FileInputStream(f);
			try {
				while ((read = in.read(srcBuf)) != -1) {
					out.write(srcBuf, 0, read);
				}
			} finally {
				in.close();
			}
		}
	}


	private static int[] prepareWriteFile(File f, ServletRequest servletrequest, ServletResponse servletresponse) {
		String s = FilterUtils.unWrapRequest(servletrequest).getHeader(HeaderNames.request_header_range);

		if (s != null) {
			s = s.toUpperCase();
			if (s.indexOf("BYTES") > -1) {
				int start = -1;
				int finish = -1;
				long length;
				String s1;
				String s2;
				int flen = (int) f.length();

				s = s.substring(s.indexOf("=") + 1).trim();
				int i = s.indexOf("-");
				s1 = s.substring(0, i);
				s2 = s.substring(i + 1);
				if (i == 0) {
					finish = flen - 1;
					start = finish - (new Integer(s2)).intValue();
				} else if (i == (s.length() - 1)) {
					start = (new Integer(s1)).intValue();
					finish = flen - 1;
				} else {
					start = (new Integer(s1)).intValue();
					finish = (new Integer(s2)).intValue();
				}

				if (finish > flen) {
					finish = flen - 1;
				}

				if (start <= finish) {
					HttpServletResponseFacade response = FilterUtils.unWrapResponse(servletresponse);
					response.setStatus(ResponseCodes.code_partial_content);
					response.setHeader(HeaderNames.entity_header_content_range, "bytes " + start + "-" + finish + "/" + flen);

					if (start > 0 || finish < flen - 1) {
						length = finish - start + 1;
						response.setIntHeader(HeaderNames.entity_header_content_length, (int)length);
						return new int[] {start, finish};
					}
				}
			}
		}
		return null;
	}

	private static File findFile(ServletRequest servletrequest, boolean isForward, String jspFile, ApplicationContext context, boolean aliasChanged)
										throws ServletException, ServletNotFoundException, IOException {
		String realPath = FilterUtils.unWrapRequest(servletrequest).getRealPathLocal(jspFile);
		File f = new File(realPath);
		if (f.exists() && f.getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar).equals(realPath)) {
      if (aliasChanged) {
        return f;
      }
      String rootDir = context.getServletContext().getRealPath("").replace(File.separatorChar, ParseUtils.separatorChar);
			if (!realPath.startsWith(rootDir)) {
				if (isForward) {
					throw new WebServletException(WebServletException.FILE_OUTSIDE_ROOT_DIR);
				}
				return null;
			}
			String relativeToRootPath = realPath.substring(rootDir.length()).toLowerCase();
			if (relativeToRootPath.startsWith("web-inf/")
					|| relativeToRootPath.startsWith("/web-inf/")
					|| relativeToRootPath.equals("web-inf")
					|| relativeToRootPath.startsWith("meta-inf/")
					|| relativeToRootPath.startsWith("/meta-inf/")
					|| relativeToRootPath.equals("meta-inf")) {
        if (LogContext.getLocationRequestInfoServer().beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000247",
						"Including a file from [META-INF] or [WEB-INF] directories in include directive. The file is: [{0}].", 
						new Object[]{relativeToRootPath}, null, null);
				}
			}
			if (f.length() == 0) {
				return null;
			}
			return f;
		}
		if (isForward) {
			throw new ServletNotFoundException(ServletNotFoundException.Requested_resource_not_found, new Object[] {jspFile});
		} else {
			return null;
		}
	}

	/**
	 * Adds the same cache headers as if the resource is requested directly i.e. without forward, via Http service only.
	 * This behavior is switched only when attribute persists in request with name "com.sap.engine.servlets_jsp.forward.static.set-cache-headers" and value new Boolean(true).
	 * The name is constant in Constants.FORWARD_TO_STATIC_PARAMETER.
 	 * This is feature request from the portal. See internal CSN  2786767.
	 * Contact Persons: D024318, D000409, D019632, D020760, D039185, I022111
	 * @param request
	 * @param response
	 * @param jspFile
	 */
  private static void addForwardStaticCacheHeaders(HttpServletRequestFacade request, HttpServletResponseFacade response,  String jspFile) {

    if( response.getHeader(HeaderNames.entity_header_cache_control) == null &&
      response.getHeader(HeaderNames.entity_header_pragma) == null &&
      response.getHeader(HeaderNames.entity_header_expires) == null &&
      ( request.getAttribute(Constants.FORWARD_TO_STATIC_PARAMETER) != null &&
      ((Boolean)request.getAttribute(Constants.FORWARD_TO_STATIC_PARAMETER)).booleanValue() )
      ){
      if (LogContext.getLocationRequestInfoServer().beInfo()) {
        LogContext.getLocationRequestInfoServer().infoT("Adding headers for forwarding to static resource: [" + jspFile + "]");
      }
      RequestLine requestLine = request.getHttpParameters().getRequest().getRequestLine();
      FilterUtils.addCacheHeaders(response, requestLine.getHttpMajorVersion(), requestLine.getHttpMinorVersion());
      request.removeAttribute(Constants.FORWARD_TO_STATIC_PARAMETER);
    }
  }

  private static void setContentTypeOfStaticResource(ApplicationContext applicationContext, ServletResponse response, File staticResource) {
    HttpProvider httpProvider = ServiceContext.getServiceContext().getHttpProvider();
    HttpProperties httpProperties = httpProvider == null ? null : httpProvider.getHttpProperties();

    // Find the content type
    String contentType = getMimeType(
        staticResource     == null ? null : staticResource.getName(),
        applicationContext == null ? null : applicationContext.getServletContext(),
        httpProperties     == null ? null : httpProperties.getMimeMappings()
    );

    // Ensure valid Content-Type header
    if (contentType == null || contentType.trim().length() == 0) {
      contentType = HeaderValues.content_unknown;
    }

    // Set the content type header
    response.setContentType(contentType);
  }

  public static String getMimeType(String fileName, ServletContext servletContext, MimeMappings httpMimeMappings) {
    if (fileName == null) {
      return HeaderValues.content_unknown;
    }

    String mimeType = servletContext == null ? null : servletContext.getMimeType(fileName);
    if (mimeType == null) {
      mimeType = fileName;
      int i = mimeType.lastIndexOf('.');
      if (i >= 0) {
        mimeType = mimeType.substring(i);
      }
      mimeType = mimeType.toLowerCase();
      mimeType = httpMimeMappings == null ? null : httpMimeMappings.getMimeType(mimeType);
    }

    if (mimeType != null) {
      return mimeType.replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar);
    } else {
      return HeaderValues.content_unknown;
    }
  }

}
