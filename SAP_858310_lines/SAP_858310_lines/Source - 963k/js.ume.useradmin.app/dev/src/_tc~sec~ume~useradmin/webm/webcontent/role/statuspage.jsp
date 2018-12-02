<%@ page import="com.sap.security.core.admin.role.*" %>
<%@ include file="/proxy.txt" %>

<%
String message = (String)HelperClass.getAttr(proxy, "refresh_msg");
%>

<%if (!inPortal) {%> 
<html>
	<head>
		<title>Main Menu</title>
		<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
	</head>
	<body>
<%}%>
		<table border="1" align="center">
   			<tr><td><h3>Refreshing role XML files</h3></td></tr>
   			<tr><td class="TX_CFM_XSB">
				<UM:encode><%=message%></UM:encode>
   			</td></tr>
		</table>
	</body>
</html>
