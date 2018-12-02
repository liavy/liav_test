package com.sap.archtech.initservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.security.core.server.csi.util.StringUtils;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The class <code>InitServlet</code> represents the main entry point of the XML DAS Initialization Service.
 */
public class InitServlet extends HttpServlet
{
  private static final long serialVersionUID = 42L;
  
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector Initialization");
  private static final Location loc = Location.getLocation("com.sap.archtech.initservice");
  // stored for diagnosis
  private String[] loadedArchSets;
  private String deployedActions;
  private String mbeanInfo;
  
  public void init() throws ServletException
  {
    loc.entering();
    InputStream actionsStream = null;
    try
    {
      DatabaseAccessor dbAccessor = new DatabaseAccessor();
      String[] archSetNames = dbAccessor.getRegisteredArchivingSets();
      actionsStream = ActionsDescriptorBuilder.buildActionsDescriptor(archSetNames);
      ActionsDescriptorBuilder.deployActions(actionsStream);
      createDiagnosisData(archSetNames, actionsStream);
      DestinationInitializer destInit = new DestinationInitializer();
      destInit.initDstService();
      MBeanInitializer mbeanInit = new MBeanInitializer();
      mbeanInit.registerMBeans();
      mbeanInfo = mbeanInit.getMBeansInfo();
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Accessing the database failed:" + e.getMessage(), e);
      throw new ServletException(e);
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Loading the Archiving Sets failed:" + e.getMessage(), e);
      throw new ServletException(e);
    }
    catch(ActionsBuilderException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Building/deploying the \"actions.xml\" failed:" + e.getMessage(), e);
      throw new ServletException(e);
    }
    catch(RemoteException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Initialization of the HTTP Destination failed:" + e.getMessage(), e);
      throw new ServletException(e);
    }
    catch(IOException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Creating diagnosis data from the deployed \"actions.xml\" failed:" + e.getMessage(), e);
      throw new ServletException(e);
    }
    catch(DestinationException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Initialization of the HTTP Destination failed:" + e.getMessage(), e);
      throw new ServletException(e);
    }
    finally
    {
      if(actionsStream != null)
      {
        try
        {
          actionsStream.close();
        }
        catch(IOException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing the \"actions.xml\" stream failed:" + e.getMessage(), e);
        }
      }
      loc.exiting();
    }
  }
  
  public void destroy()
  {
    loc.entering();
    try
    {
      MBeanInitializer mbeanInit = new MBeanInitializer();
      mbeanInit.unregisterMBeans();
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.WARNING, loc, "Lookup of MBean Server failed:" + e.getMessage(), e);
    }
    finally
    {
      loc.exiting();
    }
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    cat.infoT(loc, new StringBuilder("Received HTTP GET request (Client: ")
        			.append(request.getRemoteHost())
        			.append(", User: ")
        			.append(request.getRemoteUser())
        			.append(")")
        			.toString());
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    writeHtmlHeader(out);
    out.println("<H2>Results of Initialization Phase</H2>");
    writeLoadedArchSets(out);
    writeActionsXml(out);
    writeMBeanInfo(out);
    writeHtmlFooter(out);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
  {
    cat.infoT(loc, new StringBuilder("Received HTTP POST request (Client: ")
		.append(request.getRemoteHost())
		.append(", User: ")
		.append(request.getRemoteUser())
		.append(")")
		.toString());
  }
  
  private void createDiagnosisData(String[] archSetNames, InputStream actionsStream) throws IOException
  {
    if(loadedArchSets == null)
    {
      loadedArchSets = new String[archSetNames.length];
    }
    System.arraycopy(archSetNames, 0, loadedArchSets, 0, archSetNames.length);
    // Reset the InputStream to allow for reading it again
    // Note, "actionsStream" is actually a "ByteArrayInputStream"; hence, "reset()" should set it to position "0"
    actionsStream.reset();
    BufferedReader reader = new BufferedReader(new InputStreamReader(actionsStream, "UTF-8"));
    String inputLine = null;
    StringBuilder allInput = new StringBuilder();
    try
    {
      while((inputLine = reader.readLine()) != null)
      {
        allInput.append(inputLine).append('\n');
      }
      deployedActions = StringUtils.escapeToHTML(allInput.toString());
    }
    finally
    {
      reader.close();
    }
  }
  
  private void writeHtmlHeader(PrintWriter out)
  {
    String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " + "Transitional//EN\">\n";
    out.println(
      docType
        + "<HTML>\n"
        + "<HEAD><TITLE>XMLDAS Initialization Service</TITLE></HEAD>\n"
        + "<BODY BGCOLOR=\"#FDF5E6\">\n"
        + "<H1>XMLDAS Initialization Service - Diagnosis</H1>\n"
        + "<PRE>");
  }
  
  private void writeLoadedArchSets(PrintWriter out)
  {
    out.println("<b>Found following Archiving Set registrations: </b>");
    out.println("<ul>");
    for(int i = 0, n = loadedArchSets.length; i < n; i++)
    {
      out.println("<li>" + loadedArchSets[i] + "</li>");
    }
    out.println("</ul>");
  }
  
  private void writeActionsXml(PrintWriter out)
  {
    out.println("<b>Deployed Actions Descriptor: </b>");
    out.println(deployedActions);
  }
  
  private void writeMBeanInfo(PrintWriter out)
  {
    out.println("<b>Info about MBean registrations: </b>");
    out.println(mbeanInfo);
  }
  
  private void writeHtmlFooter(PrintWriter out)
  {
    out.println("</pre><p><hr></p></BODY></HTML>");
  }
}
