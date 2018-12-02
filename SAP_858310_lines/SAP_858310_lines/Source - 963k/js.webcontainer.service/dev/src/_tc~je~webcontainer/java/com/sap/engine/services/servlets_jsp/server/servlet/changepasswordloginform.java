/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.servlet;

import com.sap.tc.logging.Location;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;

public class ChangePasswordLoginForm extends HttpServlet {
  private static final String ERROR = "error";
  private static final String IMG = "img";
  private static final String IMG_PATH = "com/sap/engine/services/servlets_jsp/server/servlet/images/";
  private static final String SERVLET_PATH = "ChangePassword?img=";
  private static final byte[] PASS_TEXT = "Password expired. Provide a new one.".getBytes();
  private static final byte[] ERROR_TEXT = "Login error -- please try again.".getBytes();
  transient private static Location currentLocation = null;
  transient private ApplicationContext applicationContext = null;

  private static final byte[] pageBeg = ("<html>\n" +
	  "<head>\n" +
		"<title>Expired Password</title>\n" +
		"<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=iso-8859-1\">\n" +
		"<script language=javascript type=\"text/javascript\">\n" +
		"	function validPass (passForm) { \n" +
//		"		if (passForm.j_sap_password.value == \"\") {\n" +
//		"			alert (\"Please, enter a password\") \n" +
//		"			passForm.j_sap_password.focus() \n" +
//		"			return false" +
//		"		}\n" +
		"		if (passForm.j_sap_password.value != passForm.j_sap_again.value) {\n" +
		"			alert (\"The passwords you entered do not match\")\n" +
		"			passForm.j_sap_password.focus()\n" +
		"			passForm.j_sap_again.select()\n" +
		"			return false" +
		"		}\n" +
		" 	return true\n" +
		"	}\n" +
		"</script>\n" +
		"<style>\n" +
		"	td {font-family : Arial, Helvetica, sans-serif;\n" +
		"		font-size : 12px;\n" +
		"	}\n" +
		"</style>\n" +
    "</head>  \n" +
    "<BODY BGCOLOR=\"#FFFFFF\" leftmargin=\"0\" topmargin=\"0\" rightmargin=\"0\" bottommargin=\"0\" marginheight=\"0\" marginwidth=\"0\">\n" +
    "<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=0> \n" +
    "  <TR>\n" +
    "    <TD width=\"84\"><IMG SRC=\"" + SERVLET_PATH + "top_01.gif\" WIDTH=84 HEIGHT=48></TD>\n" +
    "    <TD COLSPAN=6 width=\"100%\" align=\"center\" height=\"48\"><font face=\"Arial, Verdana, Helvetica\" size=\"4\" color=\"#808080\"><b>"+ ServiceContext.getServiceContext().getServerName() + " Password Management"+"</b></TD>\n" +
    "  </TR> \n" +
    "  <TR>\n" +
    "    <TD COLSPAN=2 width=\"100%\" background=\"" + SERVLET_PATH + "top_05.gif\"><IMG SRC=\"" + SERVLET_PATH + "top_05.gif\" WIDTH=2 HEIGHT=42></TD>\n" +
    "    <TD align=\"right\" width=\"90\" background=\"" + SERVLET_PATH + "top_05.gif\"><IMG SRC=\"" + SERVLET_PATH + "top_05.gif\" WIDTH=90 HEIGHT=42></TD>\n" +
    "    <TD COLSPAN=4 width=\"197\" align=\"right\" valign=\"top\" background=\"" + SERVLET_PATH + "top_05.gif\"><font face=\"Arial, Verdana, Helvetica\" size=\"3\" color=\"#FFFFFF\"><nobr><b>" + new String(ServiceContext.getServiceContext().getServerVersion()) + "</b>&nbsp;</TD>\n" +
    "  </TR>\n" +
    "  <TR>\n" +
    "    <TD><IMG SRC=\"" + SERVLET_PATH + "spacer.gif\" WIDTH=84 HEIGHT=1></TD>\n" +
    "    <TD width=\"100%\"><IMG SRC=\"" + SERVLET_PATH + "spacer.gif\" WIDTH=176 HEIGHT=1></TD>\n" +
    "    <TD><IMG SRC=\"" + SERVLET_PATH + "spacer.gif\" WIDTH=203 HEIGHT=1></TD>\n" +
    "    <TD><IMG SRC=\"" + SERVLET_PATH + "spacer.gif\" WIDTH=90 HEIGHT=1></TD>\n" +
    "    <TD><IMG SRC=\"" + SERVLET_PATH + "spacer.gif\" WIDTH=88 HEIGHT=1></TD>\n" +
    "    <TD><IMG SRC=\"" + SERVLET_PATH + "spacer.gif\" WIDTH=90 HEIGHT=1></TD>\n" +
    "    <TD><IMG SRC=\"" + SERVLET_PATH + "spacer.gif\" WIDTH=19 HEIGHT=1></TD>\n" +
    "  </TR>\n" +
    "</TABLE>\n" +
    "<br><br><br>\n" +
    "<form method=\"POST\" onSubmit=\"return validPass(this)\" action=\"").getBytes() ;

  private static final byte[] pageEnd1 = ("\"> \n" +
    "<table cellspacing=\"2\" cellpadding=\"2\" border=\"0\" align=\"center\">\n" +
    "		<tr> <td colspan=\"2\"><b>").getBytes();

   private static final byte[] pageEnd2 = ("</b><br><br></td></tr>\n" +
    "		<tr> <td>Password:</td><td><INPUT TYPE=\"password\" NAME=\"j_sap_current_password\" size=\"15\"></td></tr>\n" +
    "		<tr> <td>New password:</td><td><INPUT TYPE=\"password\" NAME=\"j_sap_password\" size=\"15\"></td></tr>\n" +
    "		<tr> <td>Retype password:</td><td><INPUT TYPE=\"password\" NAME=\"j_sap_again\" size=\"15\"></td></tr>\n" +
    "		<tr> <td align=\"right\"><br><input type=\"submit\" value=\"Submit\"></td> \n" +
    "        <td><br><input type=\"reset\" value=\"Clear\"></td></tr>\n" +
    "</table></form></body></html>\n").getBytes();

  public void init(ServletConfig config) throws ServletException {
   super.init(config);
   currentLocation = Location.getLocation(ChangePasswordLoginForm.class);
   applicationContext = ((ServletContextImpl) config.getServletContext()).getApplicationContext();
  }

  public void service(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse) throws ServletException, IOException {
    OutputStream out = httpservletresponse.getOutputStream();
    String img = httpservletrequest.getParameter(IMG);
    if (img != null) {
      String path = ParseUtils.canonicalize(IMG_PATH + img.replace(File.separatorChar, ParseUtils.separatorChar));
      if (!path.startsWith(IMG_PATH)) {
        //TODO:Polly ok
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000345",
          "Invalid path [{0}] found. Path should be in package [{1}] for web application [{2}].",
          new Object[]{path, IMG_PATH, applicationContext.getAliasName()}, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
        httpservletresponse.sendError(ResponseCodes.code_not_found, Responses.mess34 + " " + img);//here we do not need user action
        return;
      }
      httpservletresponse.setContentType("image/gif");
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
      if (is != null) {
        byte[] buff = new byte[1024];
        int c;
        while((c = is.read(buff)) != -1) {
          out.write(buff, 0, c);
        }
      } else {
        //TODO:Polly ok
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000346",
          "Cannot find image in the specified path [{0}] for [{1}] web application.",
          new Object[]{path, applicationContext.getAliasName()}, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
        httpservletresponse.sendError(ResponseCodes.code_not_found, Responses.mess35 + " " + img);//here we do not need user action
      }
      return;
    } else {
      httpservletresponse.setContentType("text/html");
    }
    boolean isrror = new Boolean(httpservletrequest.getParameter(ERROR)).booleanValue();
    String sap_j_security_check = "sap_j_security_check";
    sap_j_security_check = httpservletresponse.encodeURL(sap_j_security_check);
    out.write(pageBeg);
    out.write(sap_j_security_check.getBytes());
    out.write(pageEnd1);
    if (isrror) {
      out.write(ERROR_TEXT);
    } else {
      out.write(PASS_TEXT);
    }
    out.write(pageEnd2);
  }
}
