<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.IUser"%>
<%@ include file="proxy.txt" %>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>

<%if (!inPortal) {%>
<html>
<head>
<title><%=userAdminLocale.get("USER_MANAGEMENT")%></title>
<script language="JavaScript" src="js/basic.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<%@ include file="contextspecific_includes_top.txt" %>

<!-- Satrt Section Header -->

<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("USER_DEACTIVATION_HEADER")%></span>
      </td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
    <span tabindex="0"><%=userAdminLocale.get("USER_DEACTIVATION_DESCRIPTION")%></span>
  </td></tr>
</table>
<!-- End Section Description -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<form name="userdeactivate" method="post" action="<%=userAdminAlias%>">
<table class="TBLO_XXS_L" cellpadding="0" cellspacing="0" width="98%" border="0">
  <tr><td CLASS="TBLO_XXS_L">
    <table cellpadding="2" cellspacing="1" border="0">
    <tr>
      <td CLASS="TBLO_XXS_R" tabindex="0" nowrap>
            <%=userAdminLocale.get("DEACTIVATED_BY")%>:
      </td>
      <td CLASS="TBLO_XSB" tabindex="0" nowrap>
      <% StringBuffer displayname = new StringBuffer(performer.getDisplayName());
         if ( null!=performer.getFirstName() && null!=performer.getLastName() ) {
             displayname.replace(0, displayname.length(), performer.getFirstName());
             displayname.append(" ").append(performer.getLastName());
         }%>
      <UM:encode><%=displayname.toString()%></UM:encode>
      </td>
    </tr>
    <tr>
      <td CLASS="TBLO_XXS_R" tabindex="0" nowrap>
            <%=userAdminLocale.get("DEACTIVATION_DATE")%>:
      </td>
      <td CLASS="TBLO_XS" tabindex="0" nowrap>
        <% DateUtil du = new DateUtil(DateUtil.MEDIEM, proxy.getLocale());%> 
        <%= du.getTime(new java.util.Date()) %>
      </td>
    </tr>
    <tr>
      <td CLASS="TBLO_XXS_R" valign="top" nowrap><LABEL FOR="<%=user.messageToRequestor%>"><%=userAdminLocale.get("REASON")%>:</LABEL></td>
      <td nowrap>
        <textarea id="<%=user.messageToRequestor%>"
                  name="<%=user.messageToRequestor%>"
                  tabindex="0" 
                  value="<%=user.getMessageToRequestor()%>"
                  wrap="soft"
                  cols="25"
                  rows="5"
                  style="width: 2.5in"
                  CLASS="TX_XS"></textarea>
      </td>
      </tr>
    </table>
  </td></tr>
</table>
<!-- End Table TBLO_XS -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Page Action Buttons -->
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" class="TBLO_XS_L" nowrap>
    <input type="submit"
           name="<%=UserAdminLogic.performUserLockAction%>"
           tabindex="0" 
           value="&nbsp;<%=userAdminLocale.get("DEACTIVATE")%>&nbsp;"
           class="BTN_LB">&nbsp;
    <input class="BTN_LN"
           type="submit"
           tabindex="0" 
           name="<%=UserAdminLogic.cancelUserDeOrActivateAction%>"
           value="&nbsp;<%=userAdminLocale.get("CANCEL")%>&nbsp;">
  </td></tr>
</table>
</form>

<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>



