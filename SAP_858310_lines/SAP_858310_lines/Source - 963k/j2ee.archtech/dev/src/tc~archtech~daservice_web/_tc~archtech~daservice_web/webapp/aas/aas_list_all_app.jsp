<%@ page contentType="text/html" errorPage="aas_error.jsp" import="com.sap.archtech.daservice.admin.*,com.sap.archtech.daservice.ejb.*,java.sql.*,java.text.*,java.util.*,javax.naming.*,javax.sql.*" %>

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
    
    <p><font face="Arial" size="2">
    The following archive paths exist for the selected home path:
    <p>
    
    <table border="1" cellpadding="5" cellspacing="1" rules=all>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Archive Path</font></th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Collection Type</font></th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Creation Date</font></th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Creation User</font></th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Archive Store</font></th>
      <th bgcolor="#003366" align="center" valign="top"><font face="Arial" color="#FFFFFF" size="2">Action</font></th>            
      
      <%  
      Connection connection = null;
      PreparedStatement pst0 = null;
      PreparedStatement pst1 = null;
      PreparedStatement pst2 = null;
      ResultSet result0 = null;
      ResultSet result1 = null;
      ResultSet result2 = null;
      
      // Get Timestamp Formatter
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      
      // Get URI From Request
      String path = request.getParameter("uri").trim();
      String backpath = path.substring(0, path.length() - 1);
      backpath = backpath.substring(0, backpath.lastIndexOf('/') + 1);

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
     
        // Get collection id
        long parentColId = 0;
        String cType = "";
        pst0 = connection.prepareStatement("SELECT COLID, COLTYPE FROM BC_XMLA_COL WHERE URI = ?");
	    pst0.setString(1, path.substring(0, path.length()-1));
        result0 = pst0.executeQuery();
        while(result0.next())
        {
        	parentColId = result0.getLong("COLID");        	
    	    cType = result0.getString("COLTYPE");        	
        }    	    
        result0.close();
        pst0.close();
        
        // Check if collection is a home collection
        if (cType.equalsIgnoreCase("S"))
        {
        %>	
        	
        <jsp:forward page="aas_list_all.jsp" />
        	        	
        <%	
        }
        
        // Get all child collections
        pst1 = connection.prepareStatement("SELECT * FROM BC_XMLA_COL WHERE PARENTCOLID = ? ORDER BY URI");
	    pst1.setLong(1, parentColId);
        result1 = pst1.executeQuery();
        while(result1.next())
        {
          long colId = result1.getLong("COLID");
          String uri = result1.getString("URI") + "/";
          Timestamp creationtime = result1.getTimestamp("CREATIONTIME");
          String creationuser = result1.getString("CREATIONUSER");
          long storeid = result1.getLong("STOREID");
          String selectedArchiveStore = (String) hm.get(new Long(storeid));
          if (selectedArchiveStore == null)
                selectedArchiveStore = "None";
          String coltype = result1.getString("COLTYPE");
          if (coltype.equalsIgnoreCase("H"))
            coltype = "Home";
          else
            coltype = "Application";  
          
          // Check if collection has child collections
          pst2 = connection.prepareStatement("SELECT COLID FROM BC_XMLA_COL WHERE PARENTCOLID = ?");
  	      pst2.setLong(1, colId);
  	      pst2.setMaxRows(1);
  	      result2 = pst2.executeQuery();
          int hits = 0;
          while(result2.next())
            hits++;
          result2.close();
          pst2.close();
      %>     

      <tr bgcolor="#CCFFFF">     
        <td>
          <font face="Arial" size="2"><%=uri%></font>
        </td>
        <td>
          <font face="Arial" size="2"><%=coltype%></font>
        </td>
        <td>
          <font face="Arial" size="2"><%=sdf.format(new java.util.Date(creationtime.getTime() + (creationtime.getNanos() / 1000000)))%></font>
        </td>
        <td>
          <font face="Arial" size="2"><%=creationuser%></font>
        </td>
        <td>
          <font face="Arial" size="2"><%=selectedArchiveStore%></font>        
        </td>
        <td align="right" valign="middle">
              
         <table>         
         <tr>
         <%
         if (hits != 0)
         {
         %>
         
          <td align="center" valign="middle">
          <form action="aas_list_all_app.jsp" method="post">
            <input type="hidden" name="uri" value="<%=uri%>">
            <input type="submit" value="Expand">
          </form>
          </td>
          
         <% 
         }
         %>
            
          <td align="center" valign="middle">           
          <form action="aas_list_all_app.jsp" method="post">
            <input type="hidden" name="uri" value="<%=backpath%>">
            <input type="submit" value="Collapse">
          </form>
          </td>          
          </tr>
          </table>  
         </td>        
        
      </tr>
      
      <%
        } // End while() loop
        
        // Close result set and prepared statement
        result1.close();
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
          if (result0 != null)
            result0.close();
          if (result1 != null)
              result1.close();
          if (result2 != null)
              result2.close();
          if (pst0 != null)
              pst0.close();
          if (pst1 != null)
              pst1.close();
          if (pst2 != null)
              pst2.close();
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
       
    <a href="aas_list_all.jsp">List Archive Paths</a>
        
  </body>
  
</html>


