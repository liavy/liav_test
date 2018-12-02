<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>
<%@ page isErrorPage="true" session="true"%>
<%@ page import="com.sap.security.api.UMFactory" %>
<jsp:useBean id="throwable"
             class="java.lang.Throwable"
             scope="request"/>

<%if (!inPortal) {%>
<html>
<HEAD>
  <TITLE><%=userAdminLocale.get("USE_NEW_UI")%></TITLE>
  <script language="JavaScript" src="js/basic.js"></script>
</head>


<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=userAdminLocale.get("USE_NEW_UI")%> <a href="/useradmin">/useradmin</a></td></tr>
</table>
<!-- End Section Header -->


<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
        <IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>
<%@ include file="contextspecific_includes_bottom.txt" %>
