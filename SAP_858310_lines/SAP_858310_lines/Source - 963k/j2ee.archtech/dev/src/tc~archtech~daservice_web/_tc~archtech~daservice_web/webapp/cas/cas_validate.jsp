<%@ page contentType="text/html" errorPage="cas_validate_error.jsp"
	import="com.sap.archtech.daservice.admin.*,com.sap.archtech.daservice.ejb.*,java.util.*,javax.naming.*"%>

<%
String storeid = request.getParameter("storeid");
String archivestore = request.getParameter("archivestore").trim();
String storagesystem = request.getParameter("storagesystem");
String type = request.getParameter("type");
String winroot = request.getParameter("winroot").trim();
String unixroot = request.getParameter("unixroot").trim();
String destination = request.getParameter("destination").trim();
String proxyhost = request.getParameter("proxyhost").trim();
String proxyport = request.getParameter("proxyport").trim();
String isdefault = request.getParameter("isdefaultcheckbox");
String entertype = request.getParameter("entertype");

String archivestoreError = "";
String storagesystemError = "";
String typeError = "";
String winrootError = "";
String unixrootError = "";
String destinationError = "";
String isdefaultError = "";

// Validate IsDefault Checkbox
if ((isdefault != null) && (isdefault.equalsIgnoreCase("Y")))
	isdefault = "Y";
else
	isdefault = "N";

// Check if archive store already exists
if (entertype.equalsIgnoreCase("I"))
{

  try
  {
   
    // Get store id
    if ((archivestore == null) || (archivestore.length() == 0))
    {
      archivestoreError = " Archive Store missing";
    }
    else if(archivestore.equalsIgnoreCase("None"))
    {
	  archivestoreError = " '" + archivestore + "' is not a valid name for an Archive Store";
    }
    else
    { 
    	
      // Get Archive Store Id 
	  Context ctx = new InitialContext();
	  ArchStoreConfigLocalHome beanLocalHome =
	      (ArchStoreConfigLocalHome) ctx.lookup("java:comp/env/ArchStoreConfigBean");
	  Collection col = beanLocalHome.findByArchiveStore(archivestore.toUpperCase());
	  if (!col.isEmpty())
	  {
        archivestoreError = " Archive Store already exists";
      }
    }
  }
  catch (Exception ex)
  {
    throw ex;
  }
}
%>

<%
if (archivestoreError.length() != 0)
{
%>

<jsp:forward page="cas_enter.jsp">
	<jsp:param name="archivestoreerror" value="<%=archivestoreError%>" />
	<jsp:param name="archivestore" value="<%=archivestore%>" />
	<jsp:param name="storagesystem" value="<%=storagesystem%>" />
	<jsp:param name="type" value="<%=type%>" />
	<jsp:param name="winroot" value="<%=winroot%>" />
	<jsp:param name="unixroot" value="<%=unixroot%>" />
	<jsp:param name="destination" value="<%=destination%>" />
	<jsp:param name="proxyhost" value="<%=proxyhost%>" />
	<jsp:param name="proxyport" value="<%=proxyport%>" />
	<jsp:param name="isdefault" value="<%=isdefault%>" />
</jsp:forward>

<%
}
%>

<%
// Check if necessary input data is missing
if (archivestore.length() < 1)
{
  archivestoreError = "Archive Store missing!";
}

if (!(type.equalsIgnoreCase("W") || type.equalsIgnoreCase("F")))
{
  typeError = "Type missing!";
}

// Check if winroot syntax is correct 
if (winroot.endsWith("\\"))
{
	if (type.toUpperCase().startsWith("W"))
		winrootError = "WebDAV root must not end with a backslash";
	else
		winrootError = "Windows root must not end with a backslash";
} 
   
if (winroot.endsWith("/"))
{
	if (type.toUpperCase().startsWith("W"))
		winrootError = "WebDAV root must not end with a slash";
	else
		winrootError = "Windows root must not end with a slash";
}

//Check if unixroot syntax is correct     
if (unixroot.endsWith("/"))
{
		unixrootError = "Unix root must not end with a slash";
}    

//Check if winroot or unixroot are selected
if (winroot.length() == 0 && unixroot.length() == 0 && type.equalsIgnoreCase("F"))	
{
	winrootError = "Windows root missing!";
	unixrootError = "Unix root missing!";
}

// Check if destination is selected
if (destination.length() == 0 && type.equalsIgnoreCase("W"))	
{
        destinationError = "Destination missing!";	
}

// Clear unnecessary fields
if (type.equalsIgnoreCase("W"))
{
	winroot = "";
	unixroot = "";
}
else
{
	destination = "";
	proxyhost = "";
	proxyport = "0";
}

if (archivestoreError.length() != 0 ||
      storagesystemError.length() != 0 ||
      typeError.length() != 0 ||
      winrootError.length() != 0 ||
      unixrootError.length() != 0 ||
      destinationError.length() != 0 ||
      isdefaultError.length() != 0)
      {		
%>

<jsp:forward page="cas_enter.jsp">
	<jsp:param name="archivestoreerror" value="<%=archivestoreError%>" />
	<jsp:param name="storagesystemerror" value="<%=storagesystemError%>" />
	<jsp:param name="typeerror" value="<%=typeError%>" />
	<jsp:param name="winrooterror" value="<%=winrootError%>" />
	<jsp:param name="unixrooterror" value="<%=unixrootError%>" />
	<jsp:param name="destinationerror" value="<%=destinationError%>" />
	<jsp:param name="isdefaulterror" value="<%=isdefaultError%>" />
	<jsp:param name="archivestore" value="<%=archivestore%>" />
	<jsp:param name="storagesystem" value="<%=storagesystem%>" />
	<jsp:param name="type" value="<%=type%>" />
	<jsp:param name="winroot" value="<%=winroot%>" />
	<jsp:param name="unixroot" value="<%=unixroot%>" />
	<jsp:param name="destination" value="<%=destination%>" />
	<jsp:param name="proxyhost" value="<%=proxyhost%>" />
	<jsp:param name="proxyport" value="<%=proxyport%>" />
	<jsp:param name="isdefault" value="<%=isdefault%>" />
</jsp:forward>

<%
}
%>

<%
if (storeid.startsWith("X"))
{
%>

<jsp:forward page="cas_store.jsp">
	<jsp:param name="winroot" value="<%=winroot%>" />
	<jsp:param name="unixroot" value="<%=unixroot%>" />
	<jsp:param name="destination" value="<%=destination%>" />
	<jsp:param name="proxyhost" value="<%=proxyhost%>" />
	<jsp:param name="proxyport" value="<%=proxyport%>" />
	<jsp:param name="isdefault" value="<%=isdefault%>" />
</jsp:forward>

<%
}
else
{
%>

<jsp:forward page="cas_update.jsp">
	<jsp:param name="winroot" value="<%=winroot%>" />
	<jsp:param name="unixroot" value="<%=unixroot%>" />
	<jsp:param name="destination" value="<%=destination%>" />
	<jsp:param name="proxyhost" value="<%=proxyhost%>" />
	<jsp:param name="proxyport" value="<%=proxyport%>" />
	<jsp:param name="isdefault" value="<%=isdefault%>" />
</jsp:forward>

<%
}
%>
