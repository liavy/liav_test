<%@ page errorPage="pst_error.jsp" import="com.sap.archtech.daservice.admin.*,java.net.*" %>

<%
String archivepath = request.getParameter("archivepath");
String authorization = (String) session.getAttribute("AuthRequHead");
if (authorization == null)
	authorization = "";  
URL url = new URL(request.getScheme() + "://localhost:" + request.getServerPort() + AdminMaster.DASPATH);
PackStatus pst = new PackStatus(url, authorization, archivepath);
String responsecode = pst.execute();
%>

<jsp:forward page="pst_result.jsp">
  <jsp:param name="responsecode" value="<%=responsecode%>" />
</jsp:forward>




