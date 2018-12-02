/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

import com.sap.engine.services.servlets_jsp.jspparser_api.ComponentDecorator;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.OutputActionTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

/**
 * 
 * This class add to the generated from the parser output custom data 
 * that concerns only the Engine and is specific for the Engine - imports, extends, implements, main method ...
 *  
 * @author Todor Mollov
 * 
 * @version 7.0
 *
  */
public class GenerateJavaHelper implements ComponentDecorator
{
	private static final String defaultStartComment ="/* Automatically generated.\r\n" +
															" * Copyright (c) 2000-2006 by SAP AG, Walldorf.,\r\n" +
															" * http://www.sap.com\r\n" +
															" * All rights reserved.\r\n" +
															" *\r\n" +
															" * This software is the confidential and proprietary information\r\n" +
															" * of SAP AG, Walldorf. You shall not disclose such Confidential\r\n" +
															" * Information and shall use it only in accordance with the terms\r\n" +
															" * of the license agreement you entered into with SAP.\r\n" +
															" * Do not edit.\r\n" +
															" */\r\n";
	private static final String imports=															
															"\r\nimport java.io.*;\r\n"+
															"import java.util.*;\r\n"+
															"import java.beans.*;\r\n"+
															"import javax.servlet.*;\r\n"+
															"import javax.servlet.http.*;\r\n"+
															"import javax.servlet.jsp.*;\r\n"+
															"import javax.servlet.jsp.tagext.*;\r\n"+
															"import com.sap.engine.services.servlets_jsp.lib.jspruntime.*;\r\n";
															//"import com.sap.engine.services.servlets_jsp.server.LogContext;\r\n"; 
	/**
	 * The implicit imports according JSP 2.1
	 */
	private static final String importsShort=															
		"\r\nimport javax.servlet.*;\r\n"+
		"import javax.servlet.http.*;\r\n"+
		"import javax.servlet.jsp.*;\r\n";

	private static final String defaultExtends ="com.sap.engine.services.servlets_jsp.lib.jspruntime.JspBase";														 
	
	public void decorate(JspPageInterface parser) throws JspParseException
	{
		parser.getCommentCode().append(defaultStartComment);
		
		if (parser.getWebContainerParameters().isExtendedJspImports()) {
			parser.getImportDirective().append(imports);		  
		}else{
			parser.getImportDirective().append(importsShort);		  
		}
		if (parser.getExtendsDirective().length() == 0) {
			
			parser.getExtendsDirective().append(defaultExtends);
		}
		//generate functions code
		if (parser.getFunctionsCode().length() > 0) {
			parser.getClassBodyCode().append("static {\r\n");
			parser.getClassBodyCode().append(parser.getFunctionsCode());
			parser.getClassBodyCode().append("}\r\n");
		}
		parser.getClassBodyCode().append(addMainMethod(parser));

	}
	
	private StringBuffer addMainMethod(JspPageInterface parser) {
	  StringBuffer out = new StringBuffer();
    // generate method
    out.append("\tpublic void _jspService(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException\r\n");
    out.append("\t{\r\n");
    out.append("\t\t//isXml=" + parser.isXml() + "\r\n");
    out.append("\t\tJspFactory  _jspFactory     = JspFactory.getDefaultFactory();\r\n");
    out.append("\t\tPageContext pageContext = _jspFactory.getPageContext(this,request,response," + (parser.getErrorPage() == null ? "null" : "\"" + parser.getErrorPage().replace('\\', '/') + "\"")
        + "," + parser.getCreateSession() + "," + parser.getBufferSize() + "," + parser.getAutoFlush() + ");\r\n");
    out.append("\t\tServletConfig config = pageContext.getServletConfig();\r\n");
    out.append("\t\tServletContext application = pageContext.getServletContext();\r\n");
    out.append("\t\tcom.sap.engine.services.servlets_jsp.lib.jspruntime.PageContextImpl _jspx_pageContext = (com.sap.engine.services.servlets_jsp.lib.jspruntime.PageContextImpl) pageContext;\r\n");
    if (StringUtils.greaterThan(parser.getParserParameters().getJspConfigurationProperties().getSpecificationVersion(), 2.4)) {
      out.append("\t\tcom.sap.engine.services.servlets_jsp.server.application.InjectionWrapper _jspx_resourceInjector = ((com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl)_jspx_pageContext.getServletContext()).getApplicationContext().getInjectionWrapper();").append("\n");
    }
    if (parser.getCreateSession()) {
      out.append("\t\tHttpSession session = pageContext.getSession();\r\n");
    }

    out.append("\t\tif (!response.isCommitted()) {\r\n");

    String contentType = parser.getContentType();
    if (contentType == null) {
      if (parser.isXml()) {
        contentType = "text/xml";
      } else {
        contentType = "text/html";
      }
    }
    if (contentType.indexOf("charset") < 0) {
      if (parser.isXml()) {
        contentType += ";charset=UTF-8";
      } else if (parser.getPageEncoding() != null && !parser.isDefaultEncodingUsed()) {
        contentType += ";charset=" + parser.getPageEncoding();
      }
    }
    parser.setContentType(contentType);
    out.append("\t\tresponse.setContentType(\"" + contentType + "\");\r\n");
    out.append("\t\t}\r\n");
    out.append("\t\tJspWriter   out     = pageContext.getOut();\r\n");
    out.append("\t\tjava.lang.Object page = this;\r\n");
    if (parser.isErrorPage()) {
      // JSP.1.4.3 Using JSPs as Error Pages
      // The variable is set to the value of the javax.servlet.error.exception request attribute
      // value if present, otherwise to the value of the javax.servlet.jsp.jspException request attribute value
      out.append("\t\tThrowable exception = (Throwable) request.getAttribute(\"javax.servlet.error.exception\");\r\n");
      out.append("\t\tif( exception == null ){\r\n");
      out.append("\t\t\texception = (Throwable) request.getAttribute(\"javax.servlet.jsp.jspException\");\r\n");
      out.append("\t\t}\r\n");
    }
    out.append("\t\ttry {\r\n");
    StringBuffer xmlProlog = OutputActionTag.getXMLDeclaration(parser);
    if (xmlProlog != null) {
      out.append("\t\tout.print(\"").append(xmlProlog).append("\");\r\n");
    }

    out.append(parser.getScriptletsCode());

    out.append("\t\t} catch (ThreadDeath tde) { throw tde;\r\n");
    out.append("\t\t} catch (OutOfMemoryError ome) { throw ome;\r\n");
    out.append("\t\t} catch (Throwable t) {\r\n");
    out.append("\t\t\tif (!(t instanceof SkipPageException)) {\r\n");
    out.append("\t\t\t\ttry {\r\n");
    out.append("\t\t\t\t\tout.clear();\r\n");
    out.append("\t\t\t\t} catch(java.io.IOException _jspioex) {}\r\n");
    out.append("\t\t\t\t_jspx_pageContext.handlePageException(t);\r\n");
    if (parser.getErrorPage() != null) {
      parser.setErrorPage(null);
    }
    out.append("\t\t\t}\r\n");
    out.append("\t\t}finally{\r\n");
    out.append("\t\t\t_jspFactory.releasePageContext(pageContext);\r\n");
    out.append("\t\t}\r\n");
    out.append("\t}\r\n");
    return out;
  }
	
}
