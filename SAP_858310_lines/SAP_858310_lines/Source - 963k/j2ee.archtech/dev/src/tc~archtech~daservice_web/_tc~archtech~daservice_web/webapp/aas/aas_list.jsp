<%@ page contentType="text/html" errorPage="aas_error.jsp" import="com.sap.archtech.daservice.admin.*,com.sap.archtech.daservice.ejb.*,java.sql.*,java.text.*,java.util.*,javax.naming.*" %>

<%
  String isUserValid = (String) session.getAttribute("isUserValid");
  if (!(isUserValid != null && isUserValid.equals("x")))
  {
%>

  <jsp:forward page="../index.jsp" />	
  
<%
  }
  String archivepath = request.getParameter("archivepath");
  if (archivepath != null)
  {
     archivepath = archivepath.toLowerCase().trim();
     if (archivepath.length() < 3)
        archivepath = "/";
     else if (archivepath.endsWith("/"))
        archivepath = archivepath.substring(0, archivepath.length()-1);
  }
  else
  {
     archivepath = "/";  
  }
%>

<html>

  <head>
    <title>Assign Archive Stores</title>
  </head>
  
  <body>
  
    <table border="1" width="100%" cellpadding="5" cellspacing="5">     
      <tr>
        <td width="20%"><img border="0" src="/DataArchivingService/sap_corporate.gif" width="61" height="33">
        </td>
        <td width="80%" bgcolor="#FFFFFF">
          <h1><b><font face="Arial" color="#003366" size="5">Assign Archive Stores</font></b></h1>
        </td>
      </tr>   
    </table>
          
      <%  
      Connection connection = null;
      PreparedStatement pst1 = null;
      ResultSet result = null;
      int hits = 0;
      
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
		Set set = new TreeSet();
		Context ctx = new InitialContext();
		ArchStoreConfigLocalHome beanLocalHome =
			(ArchStoreConfigLocalHome) ctx.lookup("java:comp/env/ArchStoreConfigBean");
		Collection col = beanLocalHome.findAll();
		Iterator iter = col.iterator();
        while(iter.hasNext())
        {
		  ArchStoreConfigLocal ascl = (ArchStoreConfigLocal) iter.next();
		  String archStore = ascl.getArchivestore();
		  set.add(archStore);
		  hm.put((Long)ascl.getPrimaryKey(), archStore);
        } 
      
        // Get collection
        pst1 = connection.prepareStatement("SELECT * FROM BC_XMLA_COL WHERE URI = ?");
        pst1.setString(1, archivepath);
        result = pst1.executeQuery();
        long colid = 0;
        String uri = "";
        String coltype = "";
        Timestamp creationtime = null;
        String creationuser = "";
        long storeid = 0;
        long parentcolid = 0;
        String errorText = "";
        while(result.next())
        {
          colid = result.getLong("COLID");
          uri = result.getString("URI");
          coltype = result.getString("COLTYPE");
          if (coltype.equalsIgnoreCase("H"))
            coltype = "Home";
          else if (coltype.equalsIgnoreCase("A"))
            coltype = "Application";
          else
          {
        	errorText = "Assigning / unassigning a system collection is not allowed!";
        	continue;
          }	
          creationtime = result.getTimestamp("CREATIONTIME");
          creationuser = result.getString("CREATIONUSER");
          storeid = result.getLong("STOREID");
          parentcolid = result.getLong("PARENTCOLID");
          
          // Check if collection should be displayed for assignment
          AssignArchiveStores aas = new AssignArchiveStores(request);
          boolean hasAssignedDescendants = aas.hasAssignedDescendants(connection, colid);
	  boolean hasAssignedDescendantsContainingResources = aas.hasAssignedDescendantsContainingResources(connection, colid);
	  boolean hasAssignedAncestor = aas.hasAssignedAncestor(connection, parentcolid);		  
	  if ((hasAssignedAncestor) || (storeid == 0 && hasAssignedDescendants) || (storeid != 0 && hasAssignedDescendantsContainingResources))
	  {
      	errorText = "Assigning / unassigning archive path " + archivepath + " is not possible!";
	    continue;
	  }
          hits++;
        } // End while() loop
        
        // Close result set and prepared statement
        result.close();
        pst1.close();
        
        // Commit database
        connection.commit();	         
        
        // Check if the entered archive path is valid
        if (hits == 0)
        {
        	if (errorText.length() == 0)
        	{
        %>
        
            <p><font face="Arial" size="2">
	    No archive path <%=archivepath%> found!
	    <p>
	    
        <%
        	}
        	else
        	{
        %>
                
            <p><font face="Arial" size="2">
    	    <%=errorText%>
    	    <p>
    	    
        <%
        	}
        }    
        else
        {
        %>

    <p><font face="Arial" size="2">
    The assignment of the following collection to archive stores can be changed:
    <p>
    
    <table border="1" cellpadding="5" cellspacing="1" rules=all>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">URI</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Collection Type</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Creation Date</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Creation User</th>
      <th bgcolor="#003366" align="left" valign="top"><font face="Arial" color="#FFFFFF" size="2">Archive Store</th>
      <th bgcolor="#003366" align="center" valign="top"><font face="Arial" color="#FFFFFF" size="2">Action</th>

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
          <form action="aas_store.jsp" method="post">
            <p>
            <select size="1" name="archstore">
              
              <%
              String selectedArchiveStore = (String) hm.get(new Long(storeid));
              if (selectedArchiveStore == null)
              {
                selectedArchiveStore = "None";
              %>
            
              <option value="<%=selectedArchiveStore%>" selected><%=selectedArchiveStore%></option>
              
              <%
			    Iterator it = set.iterator();
			    while (it.hasNext())
                {
                  String notSelectedArchiveStore = (String) it.next();
                  if (!(notSelectedArchiveStore.equals(selectedArchiveStore)))
                  {
              %>
              
              <option value="<%=notSelectedArchiveStore%>"><%=notSelectedArchiveStore%></option>
              
              <%
                  }
                } // End while loop
              }
              else
              {
			  %>
            
			  <option value="<%=selectedArchiveStore%>" selected><%=selectedArchiveStore%></option>
              <option value="null">None</option>
              	
              <%              	
              }
              %>
                       
            </select></p>
        </td>
        <td>
            <input type="hidden" name="colid" value="<%=colid%>">
            
            
            
            
            <input type="hidden" name="archivepath" value="<%=archivepath%>">

            
            
            
            <input type="submit" value="Save">
          </form>
        </td>
      </tr>
      
      <%
        } // End else 
      } // End try
      catch (SQLException sqlex)
      {
      
        // Rollback database
        connection.rollback();
        
        // Forward SQL exception
        throw sqlex;
      }
      catch (Exception ex)
      {
      
	    // Rollback database
	    connection.rollback();
        
	    // Forward Exception
	    throw ex;
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
    
    <a href="aas_entry.jsp">New selection</a>
    
  </body>
  
</html>


