<%@ page errorPage="shp_error.jsp" import="com.sap.archtech.daservice.admin.*,java.net.*" %>

<%
String homepath = request.getParameter("homepath");
String action = request.getParameter("action");
String user = (String) session.getAttribute("SessionUser");
String context = request.getParameter("context");
String archivestore = request.getParameter("archivestore");
if (archivestore.equalsIgnoreCase("None"))
  archivestore = null; 
String authorization = (String) session.getAttribute("AuthRequHead");
if (authorization == null)
  authorization = "";  
URL url = new URL(request.getScheme() + "://localhost:" + request.getServerPort() + AdminMaster.DASPATH);
SynchronizeHomePath shp = new SynchronizeHomePath(url, authorization, homepath, action, user, context, archivestore);
String responsecode = shp.execute();
%>

<jsp:forward page="shp_result.jsp">
  <jsp:param name="responsecode" value="<%=responsecode%>" />
</jsp:forward>
 

