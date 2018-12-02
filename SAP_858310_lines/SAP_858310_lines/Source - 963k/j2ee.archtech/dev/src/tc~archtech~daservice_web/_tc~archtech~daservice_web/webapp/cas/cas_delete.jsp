<%@ page errorPage="cas_delete_error.jsp" import="com.sap.archtech.daservice.admin.*,java.net.*" %>

<%

String authorization = (String) session.getAttribute("AuthRequHead");
if (authorization == null)
	authorization = "";  

// Delete archive store
String archivestore = request.getParameter("archivestore");
ConfigureArchiveStores cas = new ConfigureArchiveStores(request);
cas.delete();

%>

<jsp:forward page="cas_list.jsp"/>
