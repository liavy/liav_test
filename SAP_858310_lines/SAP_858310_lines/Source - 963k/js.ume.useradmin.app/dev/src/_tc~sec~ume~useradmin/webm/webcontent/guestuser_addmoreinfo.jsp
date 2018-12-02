<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>
<%com.sap.security.core.admin.CountriesBean countries = (com.sap.security.core.admin.CountriesBean) proxy.getSessionAttribute("countries");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>

<% IUser self = user.getUser();
   boolean orgReq = false;
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
%>

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


<form method="post" action="<%=userAdminAlias%>">
<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("COMPANY_USER_INFO")%></span>
    </td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
    <span tabindex="0"><%=userAdminLocale.get("GUEST_USER_INSTRUCTION")%></span>
  </td></tr>
</table>

<% if ( error.isError() ) { %>
<!-- Start Error Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_ERROR_XSB">
    <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Error Msg -->
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%@include file="/include/userprofile_contactinfo_change.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%@include file="/include/guestuserprofile_additionalinfo_change.jsp"%>

<%-- following section could be customized by customers --%>
<%@include file="/include/userprofile_customizedattri_change.jsp"%>
<%-- end of the customized section--%>

<!-- end data table -->
<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Page Action Buttons -->
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <% String submit = " "+userAdminLocale.get("SUBMIT")+ " ";
       String cancel = " "+userAdminLocale.get("CANCEL")+" "; %>
    <input type="submit"
           name="<%=UserAdminLogic.performGuestUserAddAction%>"
           tabindex="0"
           value="<%=submit%>"
           class="BTN_LB">&nbsp;
    <input type="submit"
           name="<%=UserAdminLogic.performGuestUserCancelAction%>"
           tabindex="0"
           value="<%=cancel%>"
           class="BTN_LN">
  </td></tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

