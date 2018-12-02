<%@ page contentType="text/html" errorPage="aas_error.jsp" import="com.sap.security.api.*,com.sap.archtech.daservice.mbeans.*" %>

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
    <title>Assign Archive Stores</title>
  </head>
  
  <body bgcolor="white">
  
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
    
    IUser user = UMFactory.getAuthenticator().getLoggedInUser();
    boolean hasPermission = user.hasPermission(new XmldasAdminPermission("write", "*"));
    if (hasPermission == true)
    {
    
    %>
    
    <p><font face="Arial" size="2">
    Enter the archive path you want to assign/unassign an archive store to:
    <p>
  
    <form action="aas_list.jsp" method="post">
      <table border="1" width="100%">
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Archive Path</b>
          </td>
          <td width="80%" bgcolor="#CCFFFF"><input type="text" name="archivepath" size="50" value="/"></td>
        </tr>
      </table>
      <p>
        <input type="submit" value="Continue">
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


