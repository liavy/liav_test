<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.IUser"%>

<%@ include file="proxy.txt" %>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

<%-- start html--%>
<%if (!inPortal) {%>
<html>
<head>
<TITLE><%=userAdminLocale.get("USER_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("SEARCH_RESULT_HEADER")%></span>
    </td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<% if ( info.isInfo() ) { %>
<!-- Start Confirm Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_success.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Confirm Msg -->
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<form method="post" action="<%=userAdminAlias%>">
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <input type="submit"
           name="<%=UserAdminLogic.searchUsersAction%>"
           tabindex="0"
           value="&nbsp;<%=userAdminLocale.get("SEARCH_AGAIN")%>&nbsp;"
           class="BTN_LB">&nbsp;
  </td></tr>
</table>
</form>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

