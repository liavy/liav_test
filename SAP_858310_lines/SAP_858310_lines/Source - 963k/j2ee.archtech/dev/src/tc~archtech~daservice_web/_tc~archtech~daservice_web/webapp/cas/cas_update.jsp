<%@ page errorPage="cas_update_error.jsp" import="com.sap.archtech.daservice.admin.*,java.net.*" %>

<%

String authorization = (String) session.getAttribute("AuthRequHead");
if (authorization == null)
	authorization = "";  

// Update archive store
String archivestore = request.getParameter("archivestore");
ConfigureArchiveStores cas = new ConfigureArchiveStores(request);
cas.update();

%>

<jsp:forward page="cas_list.jsp"/>



