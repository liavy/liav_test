<%@ page contentType="text/html" errorPage="aas_error.jsp" import="com.sap.security.api.*,com.sap.archtech.daservice.mbeans.*,com.sap.archtech.daservice.admin.*,com.sap.archtech.daservice.ejb.*,java.sql.*,java.text.*,java.util.*,javax.naming.*,javax.sql.*" %>

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
    <title>List Archive Paths</title>
  </head>
  
  <body>
  
    <table border="1" width="100%" cellpadding="5" cellspacing="5">     
      <tr>
        <td width="20%"><img border="0" src="/DataArchivingService/sap_corporate.gif" width="61" height="33">
        </td>
        <td width="80%" bgcolor="#FFFFFF">
          <h1><b><font face="Arial" color="#003366" size="5">List Archive Paths</font></b></h1>
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
    The following home paths have been created:
    <p>
    
    <table border="1" cellpadding="5" cellspacing="1" rules=all>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Home Path</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Collection Type</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Creation Date</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Creation User</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Archive Store</th>
      <th bgcolor="#003366" align="center" valign="top"><font face="Arial" color="#FFFFFF" size="2">Action</th>
      
      <%  
      Connection connection = null;
      PreparedStatement pst1 = null;
      ResultSet result = null;
      
      // Get Timestamp Formatter
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      
      try
      {

        // Get DB connection
        AdminConnectionProvider acp = new AdminConnectionProvider();
        connection = acp.getConnection();
     
	    // Get all archive stores
		HashMap hm = new HashMap();
		ArrayList al = new ArrayList();
		Context ctx = new InitialContext();
		ArchStoreConfigLocalHome beanLocalHome =
		    (ArchStoreConfigLocalHome) ctx.lookup("java:comp/env/ArchStoreConfigBean");
		Collection col = beanLocalHome.findAll();
		Iterator iter = col.iterator();
		while(iter.hasNext())
		{
		  ArchStoreConfigLocal ascl = (ArchStoreConfigLocal) iter.next();
		  String archStore = ascl.getArchivestore();
		  al.add(archStore);
		  hm.put((Long)ascl.getPrimaryKey(), archStore);
		} 
     
        // Get all home collections
        pst1 = connection.prepareStatement("SELECT * FROM BC_XMLA_COL I WHERE COLTYPE = 'H' ORDER BY URI");
        result = pst1.executeQuery();
        while(result.next())
        {
          long colid = result.getLong("COLID");
          String uri = result.getString("URI") + "/";
          Timestamp creationtime = result.getTimestamp("CREATIONTIME");
          String creationuser = result.getString("CREATIONUSER");
          long storeid = result.getLong("STOREID");
          String selectedArchiveStore = (String) hm.get(new Long(storeid));
          if (selectedArchiveStore == null)
                selectedArchiveStore = "None";
		  String coltype = result.getString("COLTYPE");
		  if (coltype.equalsIgnoreCase("H"))
		    coltype = "Home";
		  else
			coltype = "Application";                                                
      %>     

      <tr bgcolor="#CCFFFF">     
        <td>
          <font face="Arial" size="2"><%=uri%>
        </td>
        <td>
          <font face="Arial" size="2"><%=coltype%>
        </td>
        <td>
          <font face="Arial" size="2"><%=sdf.format(new java.util.Date(creationtime.getTime() + (creationtime.getNanos() / 1000000)))%>
        </td>
        <td>
          <font face="Arial" size="2"><%=creationuser%>
        </td>
        <td>
          <font face="Arial" size="2"><%=selectedArchiveStore%>        
        </td>
        <td>
          <form action="aas_list_all_app.jsp" method="post">
            <input type="hidden" name="uri" value="<%=uri%>">
            <input type="submit" value="Expand">
          </form>
        </td>        
      </tr>
      
      <%
        } // End while() loop
        
        // Close result set and prepared statement
        result.close();
        pst1.close();
        
        // Commit database
        connection.commit();
        
      } // End try
      catch (SQLException sqlex)
      {
      
        // Rollback database
        connection.rollback();
        
        // Forward SQL exception
        throw sqlex;
      }
      finally
      {
        try
        {
   
          // Close DB Objects
          if (result != null)
            result.close();
          if (pst1 != null)
            pst1.close();
          if (connection != null)
            connection.close();
        }
        catch (SQLException sqlex)
        {
          throw new SQLException("Error while closing database objects: " + sqlex);
        }
      }
      %>
      
    </table>
    <p>
       
    <a href="/DataArchivingService/index.jsp">Home</a>
    
    &nbsp;   
       
    <a href="aas_list_all.jsp">Refresh</a>
    
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


