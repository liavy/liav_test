<%@ page errorPage="aas_error.jsp" import="com.sap.archtech.daservice.admin.*" %>

<%
String archivepath = request.getParameter("archivepath");
AssignArchiveStores aas = new AssignArchiveStores(request);
aas.execute();
%>

<jsp:forward page="aas_list.jsp">
    <jsp:param name="archivepath" value="<%=archivepath%>"/>
</jsp:forward>
