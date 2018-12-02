<%@ page errorPage="cas_store_error.jsp" import="com.sap.archtech.daservice.admin.*,java.net.*" %>

<%

String authorization = (String) session.getAttribute("AuthRequHead");
if (authorization == null)
	authorization = "";  

// Insert new archive store
String archivestore = request.getParameter("archivestore");
ConfigureArchiveStores cas = new ConfigureArchiveStores(request);
cas.insert();

%>

<jsp:forward page="cas_list.jsp"/>






