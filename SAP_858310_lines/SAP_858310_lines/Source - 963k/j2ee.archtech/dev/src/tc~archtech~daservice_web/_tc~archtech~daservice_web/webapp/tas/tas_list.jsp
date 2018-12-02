<%@ page contentType="text/html" errorPage="tas_error.jsp"
	import="com.sap.security.api.*,com.sap.archtech.daservice.mbeans.*,com.sap.archtech.daservice.commands.*,com.sap.archtech.daservice.data.*,com.sap.archtech.daservice.util.*,com.sap.security.core.server.destinations.api.*,com.sap.archtech.daservice.ejb.*,com.tssap.dtr.client.lib.protocol.*,com.tssap.dtr.client.lib.protocol.session.*,com.tssap.dtr.client.lib.protocol.requests.http.*,com.tssap.dtr.client.lib.protocol.session.*,java.io.*,java.util.*,javax.naming.*"%>

<%
  String isUserValid = (String) session.getAttribute("isUserValid");
  if (!(isUserValid != null && isUserValid.equals("x")))
  {
%>

<jsp:forward page="../index.jsp" />

<%
  }
%>


<html>

<head>
<title>Test all archive stores</title>
</head>

<body>

<table border="1" width="100%" cellpadding="5" cellspacing="5">
	<tr>
		<td width="20%"><img border="0"
			src="/DataArchivingService/sap_corporate.gif" width="61" height="33">
		</td>
		<td width="80%" bgcolor="#FFFFFF">
		<h1><b><font face="Arial" color="#003366" size="5">Test
		Archive Stores</font></b></h1>
		</td>
	</tr>
</table>
    
    <%
    
    IUser user = UMFactory.getAuthenticator().getLoggedInUser();
    boolean hasPermission = user.hasPermission(new XmldasAdminPermission("write", "*"));
    if (hasPermission == true)
    {
    
    %>

<p><font face="Arial" size="2"> The following archive stores
have been tested:
<p>
<table border="1" cellpadding="2" cellspacing="0" rules=all>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Archive Store</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Storage System</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Default</th>		
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Store Type</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Windows Root</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Unix Root</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Destination</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Proxy Host</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Proxy Port</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">ILM</th>
	<th bgcolor="#003366" align="left" valign="top"><font face="Arial"
		color="#FFFFFF" size="2">Status</th>
		
	<%
      try
      {

        // Get all BC_XMLA_CONFIG entries
	SortedMap sm = new TreeMap();
	Context ctx = new InitialContext();
	ArchStoreConfigLocalHome beanLocalHome = (ArchStoreConfigLocalHome) ctx.lookup("java:comp/env/ArchStoreConfigBean");
	Collection col = beanLocalHome.findAll();
	Iterator iter = col.iterator();
	while(iter.hasNext())
	{
          ArchStoreConfigLocal ascl = (ArchStoreConfigLocal) iter.next();
          sm.put(ascl.getArchivestore(), ascl.getSapxmla_Config());
	}
	
	for (Iterator i = sm.entrySet().iterator(); i.hasNext();)
	{
	  Sapxmla_Config sac = (Sapxmla_Config)((Map.Entry)i.next()).getValue();
          String archivestore = sac.archive_store;
          String storagesystem = sac.storage_system;
          String storetype = sac.type;
          if (storetype.equalsIgnoreCase("W"))
            storetype = "WebDAV System";
          else
            storetype = "File System";
          String winroot = sac.win_root;
          if (winroot == null)
            winroot = "";
          String winroot_normalized = winroot;
          winroot_normalized = winroot_normalized.replace("<", "&lt;");
          winroot_normalized = winroot_normalized.replace(">", "&gt;");
          String unixroot = sac.unix_root;  
          if (unixroot == null)
            unixroot = "";
          String unixroot_normalized = unixroot;
          unixroot_normalized = unixroot_normalized.replace("<", "&lt;");
          unixroot_normalized = unixroot_normalized.replace(">", "&gt;");            
          String destination = sac.destination;
          if (destination == null)
            destination = "";
          String proxyhost = sac.proxy_host;
          if (proxyhost == null)
            proxyhost = "";
          int proxyportint = sac.proxy_port;
          if (proxyportint < 0 || proxyportint > 65535)
            proxyportint = 0;
          String proxyport = null;
          if (proxyportint != 0)
            proxyport = Integer.toString(proxyportint);
          else
            proxyport = "";
          short ilmconformanceshort = sac.ilm_conformance;
          String ilmconformance = Short.toString(ilmconformanceshort);
          String isdefault = sac.is_default;
          if ((isdefault == null) || (isdefault.length() == 0))
          {
        	isdefault = "N";
          }
          else
          {
        	  isdefault = isdefault.toUpperCase();
        	  if (!isdefault.equalsIgnoreCase("Y"))
        	     isdefault = "N";
          }          

          // Check If Archive Store Is Running 
          boolean storeRuns = false;
          if (storetype.startsWith("WebDAV System"))
          {
	    int optionsStatusCode = 0;
	    String optionsReasonPhrase = "";
	    boolean optionsSupportsDAV = false;
	    int headStatusCode = 0;
	    String headReasonPhrase = "";
	    try
	    {

	      // At This Time The New Archive Store Is Not Accessable From HTTP Connection Pool				
	      com.tssap.dtr.client.lib.protocol.Connection conn = null;
				
	      // After Destination Service Usage
	      if (destination.trim().length() != 0)
	      {				
	      DestinationService destService = (DestinationService) new InitialContext()
	      .lookup(DestinationService.JNDI_KEY);
	      if (destService == null)
		  throw new NamingException(
		  "Destination Service is not available");
	      HTTPDestination httpDest = (HTTPDestination) destService
	      .getDestination("HTTP", destination);
	      IConnectionTemplate connTemplate = httpDest
	      .getHTTPConnectionTemplate();
	      conn = new com.tssap.dtr.client.lib.protocol.Connection(connTemplate);
	      }
				
	      // Before Destination Service Usage
	      else
	      {	
	      conn = new com.tssap.dtr.client.lib.protocol.Connection(winroot);
	      conn.setSessionContext(new SessionContext(
		  WebDavTemplateProvider.WEBDAVCLIENTUSR,
		  WebDavTemplateProvider.WEBDAVCLIENTPWD,
		  WebDavTemplateProvider.WEBDAVCLIENTAUTHSCHEME));
	      }

	      if (proxyhost == null || proxyhost.length() == 0)
	      {
	        conn.setUseProxy(false);
	      }
	      else
	      {
	        conn.setProxyHost(proxyhost);
		    conn.setProxyPort(proxyportint);
		    conn.setUseProxy(true);
	      }
	      conn.setSocketReadTimeout(WebDavTemplateProvider.WEBDAVREADTIMEOUT);
	      conn.setSocketConnectTimeout(WebDavTemplateProvider.WEBDAVCONNECTTIMEOUT);
	      conn.setSocketExpirationTimeout(WebDavTemplateProvider.WEBDAVEXPIRATIONTIMEOUT);

	      // OPTIONS request
	      OptionsRequest optionsRequest = new OptionsRequest("");
	      IResponse optionsResponse = optionsRequest.perform(conn);
	      optionsSupportsDAV = optionsRequest.supportsDAV();
	      optionsStatusCode = optionsResponse.getStatus();
	      optionsReasonPhrase = optionsResponse.getStatusDescription();

	      // Get ILM Conformance Class From Archive Store
	      Header header = optionsResponse.getHeader("SAP-ILM-Conformance");
	      if (header != null)
	      {
	        String ilmValue = header.getValue();
		    if (ilmValue != null && ilmValue.length() > 0)
		    {
		      ilmconformance = ilmValue;
	        }
	        else
	        {
		      ilmconformance = "0";
		    }
	      }
	      else
	      {
	        ilmconformance = "0";
	      }

	      // HEAD Request
	      HeadRequest headRequest = new HeadRequest("");
	      IResponse headResponse = headRequest.perform(conn);
	      headStatusCode = headResponse.getStatus();
	      headReasonPhrase = headResponse.getStatusDescription();
	      conn.close();
              
              if ((optionsStatusCode == 200) && (optionsSupportsDAV == true) && (headStatusCode == 200))
                storeRuns = true;
        }
        catch (Exception ex)
        {
          storeRuns = false;
        }
          }
          else 
          {          
            String uri = "";
        
            if (System.getProperty("file.separator").startsWith("/")) {
	      if (unixroot.contains("<DIR_GLOBAL>"))
	        uri = unixroot.replace("<DIR_GLOBAL>", MasterMethod.GLOBAL_DIR);
	      else
	        uri = unixroot;
	    }
            else
            {
	      if (winroot.contains("<DIR_GLOBAL>"))
	        uri = winroot.replace("<DIR_GLOBAL>", MasterMethod.GLOBAL_DIR);
	      else
	        uri = winroot;
            }
            
            try
            {
              File rootpath = new File(uri);
              storeRuns = rootpath.isDirectory();
            }
            catch (Exception ex)
            {
              storeRuns = false;
            }
          }
       %>

	<tr bgcolor="#CCFFFF">
		<td><font face="Arial" size="2"><%=archivestore%></td>
		<td><font face="Arial" size="2"><%=storagesystem%></td>

		<%
		if (isdefault.equalsIgnoreCase("Y"))
		{
        %>

		<td align="center"><font face="Arial" size="2"><input type="checkbox" checked="checked" disabled></td>		
		
		<%
		}
		else
		{	
        %>
				
		<td align="center"><font face="Arial" size="2"><input type="checkbox" disabled></td>
		
		<%
		}
        %>
		
		<td><font face="Arial" size="2"><%=storetype%></td>
		<td><font face="Arial" size="2"><%=winroot_normalized%></td>
		<td><font face="Arial" size="2"><%=unixroot_normalized%></td>
		<td><font face="Arial" size="2"><%=destination%></td>
		<td><font face="Arial" size="2"><%=proxyhost%></td>
		<td><font face="Arial" size="2"><%=proxyport%></td>
		<td align="center"><font face="Arial" size="2"><%=ilmconformance%></td>

		<% 
          if (storeRuns == true)
          {
        // Store Runs
        %>

		<td valign="middle" align="center"><img border="0"
			src="green.gif"></td>

		<%
          }
          else
          {
        // Store Does Not Run
        %>

		<td valign="middle" align="center"><img border="0" src="red.gif"></td>

		<%
          }
        %>
		
	</tr>

	<% 
        } // End for() loop
                
      } // End try 
      catch (Exception ex)
      {
      
        // Forward Excception
        throw ex;  
      }
      %>

</table>
<p><a href="/DataArchivingService/index.jsp">Home</a> &nbsp; <a
	href="tas_list.jsp">Refresh</a>
	
    <%

    }
    else
    {

    %>
    
    <p>
    You have no permission for this action!
    <p>
    <a href="/DataArchivingService/index.jsp">Home</a>
    
    <%
    
    }
    
    %>	
	
</body>

</html>


