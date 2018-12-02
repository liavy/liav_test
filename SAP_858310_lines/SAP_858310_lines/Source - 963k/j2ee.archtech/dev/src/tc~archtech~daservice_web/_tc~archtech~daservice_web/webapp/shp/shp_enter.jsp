<%@ page contentType="text/html" errorPage="shp_error.jsp" import="com.sap.security.api.*,com.sap.archtech.daservice.mbeans.*,com.sap.archtech.daservice.admin.*,com.sap.archtech.daservice.ejb.*,java.util.*,javax.naming.*" %>

<%
  String isUserValid = (String) session.getAttribute("isUserValid");
  if (!(isUserValid != null && isUserValid.equals("x")))
  {
%>

  <jsp:forward page="../index.jsp" />	
  
<%
  }     
  Set storeList = new TreeSet();
  try
  {

	// Get all archive stores
	Context ctx = new InitialContext();
	ArchStoreConfigLocalHome beanLocalHome =
		(ArchStoreConfigLocalHome) ctx.lookup("java:comp/env/ArchStoreConfigBean");
	Collection col = beanLocalHome.findAll();
	Iterator iter = col.iterator();
	while(iter.hasNext())
	{
	  ArchStoreConfigLocal ascl = (ArchStoreConfigLocal) iter.next();
		storeList.add(ascl.getArchivestore());
	}
  }
  catch (Exception ex)
  {
  
    // Forward Exception 
    throw ex;
  }
%>
   
<html>

  <head>
    <title>Synchronize Home Path</title>
  </head>
  
  <body bgcolor="white">
  
    <table border="1" width="100%" cellpadding="5" cellspacing="5">
      <tr>
        <td width="20%"><img border="0" src="/DataArchivingService/sap_corporate.gif" width="61" height="33">
        </td>
        <td width="80%" bgcolor="#FFFFFF">
          <h1><b><font face="Arial" color="#003366" size="5">Synchronize Home Path</font></b></h1>
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
    To create a new home collection or delete an existing one, enter the necessary information:
    <p>
  
    <form action="shp_execute.jsp" method="post">
      <table border="1" width="100%">
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Home Path</b>
          </td>
          <td width="80%" bgcolor="#CCFFFF"><input type="text" name="homepath" size="50" value="/"></td>
        </tr>
        <tr>
          <td width="20%"bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Action</b>
          </td>
          <td width="80%" bgcolor="#CCFFFF">
            <input type="radio" name="action" value="I" checked><font face="Arial" size="2">
              Insert New Home Collection
            <br>  
            <input type="radio" name="action" value="D"><font face="Arial" size="2">
              Delete Existing Home Collection
          </td>
        </tr>
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Context</b>
          </td>
          <td width="80%" bgcolor="#CCFFFF">
            <input type="text" name="context" size="50">
          </td>
        </tr>
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Archive Store</b>
          </td>
          <td width="80%" bgcolor="#CCFFFF">
            <select size="1" name="archivestore">
              <option>None</option>
              
              <%
                            
              Iterator i = storeList.iterator();
              while (i.hasNext())
              {
				String archiveStore = (String) i.next();
              %>
              
                <option value="<%=archiveStore%>"><%=archiveStore%></option>
              
              <%
              }
              %> 

            </select>
          </td>
        </tr>
      </table>
      <p>
        <input type="submit" value="Execute">
      </p>
    </form>
    <p>
   
    <a href="/DataArchivingService/index.jsp">
      Home
    </a>
    
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
