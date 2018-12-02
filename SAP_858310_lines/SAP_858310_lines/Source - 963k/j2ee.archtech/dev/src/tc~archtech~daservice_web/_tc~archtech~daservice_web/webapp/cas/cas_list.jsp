<%@ page contentType="text/html" import="com.sap.security.api.*,com.sap.archtech.daservice.mbeans.*,com.sap.archtech.daservice.admin.*,com.sap.archtech.daservice.data.*,com.sap.archtech.daservice.ejb.*,java.util.*,javax.naming.*" %>

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
    <title>List all archive stores</title>
  </head>
  
  <body>
  
    <table border="1" width="100%" cellpadding="5" cellspacing="5">     
      <tr>
        <td width="20%"><img border="0" src="/DataArchivingService/sap_corporate.gif" width="61" height="33">
        </td>
        <td width="80%" bgcolor="#FFFFFF">
          <h1><b><font face="Arial" color="#003366" size="5">Define Archive Stores</font></b></h1>
        </td>
      </tr>   
    </table>
    
    <%
    
    IUser user = UMFactory.getAuthenticator().getLoggedInUser();
    boolean hasPermission = user.hasPermission(new XmldasAdminPermission("write", "*"));
    if (hasPermission == true)
    {
    
    %>
    
    <p><font face="Arial" size="2">
    The following archive stores have been defined:
    <p>
    
    <table border="1" cellpadding="5" cellspacing="1" rules=all>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Archive Store</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Storage System</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Default</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Store Type</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Windows Root</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Unix Root</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Destination</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Proxy Host</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Proxy Port</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">ILM</th>
      <th bgcolor="#003366" align="center" valign="top" colspan="2"><font face="Arial" color="#FFFFFF" size="2">Action</th>
      
      <%
      try
      {
                  
		// Get all BC_XMLA_CONFIG entries
		SortedMap tm = new TreeMap();
		Context ctx = new InitialContext();
		ArchStoreConfigLocalHome beanLocalHome =
			(ArchStoreConfigLocalHome) ctx.lookup("java:comp/env/ArchStoreConfigBean");
		Collection col = beanLocalHome.findAll();
		Iterator iter = col.iterator();
		while(iter.hasNext())
		{
		  ArchStoreConfigLocal ascl = (ArchStoreConfigLocal) iter.next(); 
		  String archivestore = ascl.getArchivestore();
		  Sapxmla_Config sac = ascl.getSapxmla_Config();
		  tm.put(archivestore, sac);
		} 					
		for (Iterator i = tm.entrySet().iterator(); i.hasNext();)
		{
		  Sapxmla_Config sac = (Sapxmla_Config) ((Map.Entry) i.next()).getValue();		  
		  long storeid = sac.store_id;
		  String archivestore = sac.archive_store;
		  String storagesystem = sac.storage_system;
		  String type = sac.type;
		  if (type.equalsIgnoreCase("W"))
		    type = "WebDAV System";
          else
            type = "File System";
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
          String proxyport = Integer.toString(sac.proxy_port);
          if (proxyport == null || proxyport.startsWith("0"))
            proxyport = "";
          String ilm_conformance = Short.toString(sac.ilm_conformance);
          if (ilm_conformance == null)
            ilm_conformance = "0";
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
      %>
       
      <tr bgcolor="#CCFFFF">     
        <td>
          <font face="Arial" size="2"><%=archivestore%>
        </td>
        <td>
          <font face="Arial" size="2"><%=storagesystem%>
        </td>
        
		<%
		if (isdefault.equalsIgnoreCase("Y"))
		{
        %>

		<td align="center"><font face="Arial" size="2"><input type="checkbox" name="isdefault" value="<%=isdefault%>" checked="checked" disabled></td>		
		
		<%
		}
		else
		{	
        %>
				
		<td align="center"><font face="Arial" size="2"><input type="checkbox" name="isdefault" value="<%=isdefault%>" disabled></td>
		
		<%
		}
        %>        
        
        <td>
          <font face="Arial" size="2"><%=type%>
        </td>
        <td>
          <font face="Arial" size="2"><%=winroot_normalized%>
        </td>
        <td>
          <font face="Arial" size="2"><%=unixroot_normalized%>
        </td>
        <td>
          <font face="Arial" size="2"><%=destination%>
        </td>
        <td>
          <font face="Arial" size="2"><%=proxyhost%>
        </td>
        <td>
          <font face="Arial" size="2"><%=proxyport%>
        </td>
        <td align="center">
          <font face="Arial" size="2"><%=ilm_conformance%>
        </td>
        <td>
          <form action="cas_enter.jsp" method="post">
            <input type="hidden" name="entertype" value="U">
            <input type="hidden" name="storeid" value="<%=storeid%>">
            <input type="hidden" name="archivestore" value="<%=archivestore%>">
            <input type="hidden" name="storagesystem" value="<%=storagesystem%>">
            <input type="hidden" name="type" value="<%=type%>">
            <input type="hidden" name="winroot" value="<%=winroot%>">
            <input type="hidden" name="unixroot" value="<%=unixroot%>">
            <input type="hidden" name="destination" value="<%=destination%>">
            <input type="hidden" name="proxyhost" value="<%=proxyhost%>">
            <input type="hidden" name="proxyport" value="<%=proxyport%>">
            <input type="hidden" name="proxyport" value="<%=ilm_conformance%>">
            <input type="hidden" name="isdefault" value="<%=isdefault%>">
            <input type="submit" value="Edit">
          </form>
        </td>
        <td>
          <form action="cas_delete.jsp" method="post">
            <input type="hidden" name="entertype" value="D">
            <input type="hidden" name="storeid" value="<%=storeid%>">
            <input type="hidden" name="archivestore" value="<%=archivestore%>">
            <input type="submit" value="Delete">
          </form>
        </td>
      </tr>
      
      <%
        } // End for() loop
        
        
      } // End try
      catch (Exception ex)
      {
        throw ex;
      }
      %>
      
    </table>
    <p>
   
     <table>
       <tr>
         <td>
           <form action="cas_enter.jsp" method="post">
             <font face="Arial" size="2">
               <input type="hidden" name="entertype" value="I">
               <input type="submit" value=" New ">
             </font>  
           </form>
         </td>
         <td valign="top">
           <font face="Arial" size="2">&nbsp;Define new archive store</font>
         </td>
       </tr>
     </table>
       
    <a href="/DataArchivingService/index.jsp">Home</a>
    
    &nbsp;   
       
    <a href="cas_list.jsp">Refresh</a>
    
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


