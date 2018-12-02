<%@ page contentType="text/html" import="com.sap.security.core.server.destinations.api.*,javax.naming.*,java.util.*" %>

<html>

  <head>
    <title>Define Archive Stores</title>
  </head>
  
  <body bgcolor="white">
  
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
    // Get Local Destinations
    ArrayList destinations = new ArrayList();
 	try {
		InitialContext initCtx = new InitialContext();
		DestinationService destService = (DestinationService) initCtx
				.lookup(DestinationService.JNDI_KEY);
		Iterator iter = destService.getDestinationNames("HTTP").iterator();
		HTTPDestination dest = null;
		while (iter.hasNext())
		{
			destinations.add((String) iter.next());
		}
		initCtx.close();
	} catch (Exception ex) {
		throw new Exception(ex);
	}
    
    // Get parameters
    String entertype = request.getParameter("entertype");
    if (entertype == null)
      entertype = "X";
      
    String storeid = request.getParameter("storeid");
    if (storeid == null)
      storeid = "X";

    String archivestore = request.getParameter("archivestore");
    if (archivestore == null)
      archivestore = "";

    String storagesystem = request.getParameter("storagesystem");
    if (storagesystem == null)
      storagesystem = "";

    String type = request.getParameter("type");
    if (type == null)
      type = "X";
    else if (type.toUpperCase().startsWith("W"))
      type = "W";
    else if (type.toUpperCase().startsWith("F"))
      type = "F";
    else
      type = "X";
      
    String winroot = request.getParameter("winroot");
    if (winroot == null)
      winroot = "";

    String unixroot = request.getParameter("unixroot");
    if (unixroot == null)
      unixroot = "";
      
    String destination = request.getParameter("destination");
    if (destination == null)
      destination = "";
      
    String proxyhost = request.getParameter("proxyhost");
    if (proxyhost == null)
      proxyhost = "";
      
    String proxyport = request.getParameter("proxyport");
    if (proxyport == null || proxyport.startsWith("0"))
      proxyport = "";
    
    String isdefault = request.getParameter("isdefault");
    if ((isdefault == null) || (isdefault.length() == 0))
    {	
  	  isdefault = "N";
    }
    else
    {
  	  if (isdefault.equalsIgnoreCase("Y"))
 	     isdefault = "Y";
 	  else 
 		  isdefault = "N";	 
    }
  	  
    if (storeid.startsWith("X"))
    { 
    
    %>
            
    <p><font face="Arial" size="2">
      Enter the required information to create a new archive store:
    <p>
       
    <%
    }
    else
    {
    %>
    
    <p><font face="Arial" size="2">
      Enter the required information to update an existing archive store:
    <p>
    
    <%
    }
    %>
         
    <form action="cas_validate.jsp" method="post">
    
      <table border="1" width="100%">
      
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Archive Store:</b>
          </td>
          
          <%
          if (entertype.equalsIgnoreCase("U"))
          {
          %>
          
          <td bgcolor="#CCFFFF">
            <input type="text" name="archivestore" value="<%=archivestore%>" size="50" readonly="readonly">
          </td>
          
          <%
          }
          else
          {
          %>
          
          <td bgcolor="#CCFFFF">
            <input type="text" name="archivestore" value="<%=archivestore%>" size="50">
          </td>
          
          <%
          }
          String archivestoreError = request.getParameter("archivestoreerror");
          if (archivestoreError != null && archivestoreError.length() > 0)
          {
          %>
          
          <td bgcolor="#FF9900">
            <font face="Arial" size="2"><%=archivestoreError%>
          </td>
          
          <%
          }
          %>
          
        </tr>
        
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Storage System:</b>
          </td>
          
          <td bgcolor="#CCFFFF">
            <input type="text" name="storagesystem" value="<%=storagesystem%>" size="50">
          </td>
          
          <%
          String storagesystemError = request.getParameter("storagesystemerror");
          if (storagesystemError != null && storagesystemError.length() > 0)
          {
          %>
          
          <td bgcolor="#FF9900">
            <font face="Arial" size="2"><%=storagesystemError%>
          </td>
          
          <%
          }
          %>
          
        </tr>

        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Set as Default:</b>
          </td>
          
          <td bgcolor="#CCFFFF">

          <%
            if (isdefault.equalsIgnoreCase("Y"))
            {
          %>
          
              <input type="checkbox" name="isdefaultcheckbox" value="Y" checked="checked" ><font face="Arial" size="2">
              
          <%
            }
            else 
            {
          %>
           
              <input type="checkbox" name="isdefaultcheckbox" value="Y" ><font face="Arial" size="2">
              
          <%
            }
          %>
           
          </td>
          
          <%
          String isDefaultError = request.getParameter("isdefaulterror");
          if (isDefaultError != null && isDefaultError.length() > 0)
          {
          %>
          
          <td bgcolor="#FF9900">
            <font face="Arial" size="2"><%=isDefaultError%>
          </td>
          
          <%
          }
          %>
          
        </tr>
        
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Store Type:</b>
          </td>
          
          <td bgcolor="#CCFFFF">

          <%
          if (entertype.equalsIgnoreCase("U"))
          {
            if (type.equalsIgnoreCase("W"))
            {
          %>
          
              <input type="radio" checked readonly="readonly"><font face="Arial" size="2"> WebDAV System <br>
              <input type="radio" readonly="readonly"><font face="Arial" size="2"> File System
              
          <%
            }
            else 
            {
          %>
           
              <input type="radio" readonly="readonly"><font face="Arial" size="2"> WebDAV System <br>
              <input type="radio" checked readonly="readonly"><font face="Arial" size="2"> File System
              
          <%
            }
          }
          else
          {
            if (type.equalsIgnoreCase("W"))
            {
          %>
          
              <input type="radio" name="type" value="W" checked><font face="Arial" size="2"> WebDAV System <br>
              <input type="radio" name="type" value="F"><font face="Arial" size="2"> File System
              
          <%
            }
            else if (type.equalsIgnoreCase("F"))
            {
          %>
           
              <input type="radio" name="type" value="W" ><font face="Arial" size="2"> WebDAV System <br>
              <input type="radio" name="type" value="F" checked><font face="Arial" size="2"> File System
           
          <%
            }
            else
            {
          %>
           
              <input type="radio" name="type" value="W" ><font face="Arial" size="2"> WebDAV System <br>
              <input type="radio" name="type" value="F" ><font face="Arial" size="2"> File System
              
          <%
            }
          }
          %>
           
          </td>
          
          <%
          String typeError = request.getParameter("typeerror");
          if (typeError != null && typeError.length() > 0)
          {
          %>
          
          <td bgcolor="#FF9900">
            <font face="Arial" size="2"><%=typeError%>
          </td>
          
          <%
          }
          %>
          
        </tr>
        
        <tr>
          <td width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Windows Root:</b>
          </td>
          
          <td bgcolor="#CCFFFF">
            <input type="text" name="winroot" value="<%=winroot%>" size="50">
          </td>
          
          <%
          String winrootError = request.getParameter("winrooterror");
          if (winrootError != null && winrootError.length() > 0)
          {
          %>
          
          <td bgcolor="#FF9900">
            <font face="Arial" size="2"><%=winrootError%>
          </td>
          
          <%
          }
          %>

        </tr>
        
        <tr>
          <td  width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Unix Root:</b>
          </td>
          <td bgcolor="#CCFFFF">
            <input type="text" name="unixroot" value="<%=unixroot%>" size="50">
          </td>
          
          <%
          String unixrootError = request.getParameter("unixrooterror");
          if (unixrootError != null && unixrootError.length() > 0)
          {
          %>
          
          <td bgcolor="#FF9900">
            <font face="Arial" size="2"><%=unixrootError%>
          </td>
          
          <%
          }
          %>
          
        </tr>
        
        <tr>
          <td  width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Destination:</b>
          </td>
          <td bgcolor="#CCFFFF">
            <select size="1" name="destination">
              <option></option>
              
              <%                            
              Iterator i = destinations.iterator();
              while (i.hasNext())
              {
				String destname = (String) i.next();
				if (destname.equalsIgnoreCase(destination))
				{	
              %>
              
                <option selected value="<%=destname%>"><%=destname%></option>
              
              <%
				}
                else
                {	
              %>
                	
                <option value="<%=destname%>"><%=destname%></option>

              <%
                }
              }
              %> 
              
            </select>
          </td>
          
          <%
          String destinationError = request.getParameter("destinationerror");
          if (destinationError != null && destinationError.length() > 0)
          {
          %>
          
          <td bgcolor="#FF9900">
            <font face="Arial" size="2"><%=destinationError%>
          </td>
          
          <%
          }
          %>
          
        </tr>
        
        <tr>
          <td  width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Proxy Host:</b>
          </td>
          <td bgcolor="#CCFFFF">
            <input type="text" name="proxyhost" value="<%=proxyhost%>" size="50">
          </td>
        </tr>
        
        <tr>
          <td  width="20%" bgcolor="#003366" align="left" valign="center"><font face="Arial" color="#FFFFFF" size="2">
            <b>Proxy Port:</b>
          </td>
          <td bgcolor="#CCFFFF">
            <input type="text" name="proxyport" value="<%=proxyport%>" size="5">
          </td>
        </tr>
                                
      </table>
      
      <br>
      
      <table>
        <tr>
          <td>
          
          <%
          if (storeid.startsWith("X"))
          {
          %>
           
            <input type="submit" value="Insert Archive Store">
            <input type="hidden" name="entertype" value="I">
             
          <%
          }
          else
          {
          %>
           
            <input type="submit" value="Update Archive Store">
            <input type="hidden" name="entertype" value="U">
             
          <%
          }
          %>
           
            <input type="hidden" name="storeid" value="<%=storeid%>">
            <input type="hidden" name="archivestore" value="<%=archivestore%>">
            <input type="hidden" name="storagesystem" value="<%=storagesystem%>">
            <input type="hidden" name="type" value="<%=type%>">
            <input type="hidden" name="winroot" value="<%=winroot%>">
            <input type="hidden" name="unixroot" value="<%=unixroot%>">
            <input type="hidden" name="destination" value="<%=destination%>">
            <input type="hidden" name="proxyhost" value="<%=proxyhost%>">
            <input type="hidden" name="proxyport" value="<%=proxyport%>">
            <input type="hidden" name="isdefault" value="<%=isdefault%>">
            
       </form>
       
        </td>
        <td>
          <form action="cas_list.jsp" method="post">
            <input type="submit" value="Cancel">
    </form>
    
        </td>
        
      </tr>
      
    </table>
    
    <a href="/DataArchivingService/index.jsp">
      Home
    </a>
    
  </body>
  
</html>
