<%@ page import = "com.sap.security.core.admin.UserAdminLogic" %>
<%@ include file="proxy.txt" %>
<%if (!inPortal) {%>
<html>
<head>
<meta http-equiv="refresh"
      content="0;url=<%=userAdminAlias%>?<%=UserAdminLogic.gotoDefaultPageAction%>=">
</head>
<body>
</body>
<%}%>
</HTML>
