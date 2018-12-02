<%-- to include this page requires:--%>
<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.core.*"%>
<%@page import="com.sap.security.api.UMFactory"%>
<%@page import="com.sap.security.api.IUserAccount"%>
<%@page import="com.sap.security.api.IUserAccountFactory"%>

<%@ include file="proxy.txt" %>

<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>
<%LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute(LanguagesBean.beanId);%>
<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute(CountriesBean.beanId);%>
<%-- end page attribute setting--%>

<% IUser owner = proxy.getActiveUser();
   IUserAccount[] accounts = owner.getUserAccounts();
   String logonId = "";
   if ( accounts.length > 0 ) {
       logonId = accounts[0].getLogonUid();
   }
   
   IUser subjugatedUser = user.getUser();
   accounts = subjugatedUser.getUserAccounts();
   IUserAccount account = null;
   if ( accounts.length > 0 ) {
       account = accounts[0];
   }   
   
   boolean toDisable = false;
   if ( UserAdminFactory.isUserReadOnly(subjugatedUser.getUniqueID()) ) {
       toDisable = true;
   }
    
   boolean notSelf = (subjugatedUser.getUniqueID().equals(owner.getUniqueID())==true)?false:true;
   boolean toModify = notSelf;
   if ( null != proxy.getRequestAttribute(UserAdminLogic.modifyUserAction) ) {
       toModify = true;
   }

   UserAdminCustomization uacustomization = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   boolean orgReq = uacustomization.isOrgUnitRequired(proxy, subjugatedUser);
   
   String modifyAction = UserAdminLogic.changeUserProfileAction;
   if ( toModify ) modifyAction = UserAdminLogic.modifyUserAction;
   boolean showCancel = false;
   Boolean temp = (Boolean) proxy.getRequestAttribute("cancelbutton");
   if ( null != temp ) {
       if (Boolean.TRUE.equals(temp) ) {
           showCancel = true;
       }
   }
   Boolean fromSnycList = (Boolean) proxy.getRequestAttribute("fromSyncList");

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
    <% if ( notSelf ) { %>
      <%=userAdminLocale.get("VIEW_OTHERUSER_HEADER")%>
    <% } else { %>
      <%=userAdminLocale.get("VIEW_USERPROFILE_HEADER")%>
    <% } %>
	  </td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
  <span tabindex="0"><%=userAdminLocale.get("VIEW_USERPROFILE_DESCRIPTION")%></span>
  </td></tr>
</table>

<% if ( info.isInfo() ) { %>
<!-- Start Info Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
<tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_success.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
</td></tr>
</table>
<!-- End Info Msg -->
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%@include file="/include/userprofile_basicinfo_view.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<% if ( null != account ) { %>
<%@include file="/include/useraccount_view.jsp"%>
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>
<%@include file="/include/userprofile_contactinfo_view.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>
<%if (orgReq) { %>
<%@include file="/include/userprofile_additionalinfo_view.jsp"%>
<% } else { %>
<%@include file="/include/userprofile_additionalinfo_view.jsp"%>
<% } %>

<%-- following section could be customized by customers --%>
<%@include file="/include/userprofile_customizedattri_view.jsp"%>
<%-- end of the customized section--%>

<!-- end data table -->
<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Page Action Buttons -->
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <input type="hidden" name="<%=user.uidId%>" value="<%=user.getUid()%>">
    <% String copytonew = " "+userAdminLocale.get("COPY_TO_NEW")+" ";
       String modify = " "+userAdminLocale.get("MODIFY")+" ";
       String cancel = " "+userAdminLocale.get("CANCEL")+" "; %>
	<% if (!toDisable) { %>
    <input type="submit"
	       name="<%=modifyAction%>"
	       tabindex="0"
	       value="<%=modify%>"
	       class="BTN_LB">&nbsp;
    <% } %>
  <% if ( toModify ) { %>
	<input type="submit"
	       name="<%=UserAdminLogic.createUserFromReferenceAction%>"
	       tabindex="0"
	       value="<%=copytonew%>"
	       class="BTN_LN">&nbsp;
  <% } %>
	<%-- start of Biller Direct Impl. --%> 	
	<% if ( toModify && null != util.checkEmpty(com.sap.security.api.UMFactory.getProperties().get(UserAdminCustomization.UM_ADMIN_DISPLAY_REDIRECT)) ) { %>       
	<input type="submit"
	       name="<%=modifyAction%>"
	       tabindex="0"
	       value=" <%=userAdminLocale.get("VIEW_ADDITIONAL")%> "
	       class="BTN_LN">&nbsp;
	<% } %>
	<%-- end of Biller Direct Impl. --%>    
  <% if ( toModify && showCancel ) { %>
	<% if ( null == fromSnycList ) { %>
    <input type="hidden" name="cancelbutton" value="true">
    <input type="submit"
	       name="<%=UserAdminLogic.cancelUserProfileViewAction%>"
	       tabindex="0"
	       value="<%=cancel%>"
	       class="BTN_LN">
	<% } else { %>
	<input type="button"
	       name="<%=UserAdminLogic.cancelUserProfileViewAction%>"
	       tabindex="0"
	       value="<%=cancel%>"
	       class="BTN_LN"
           onClick="javascript:gobacktoSync();">
	<% } %>
  <% } %>
  </td></tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

