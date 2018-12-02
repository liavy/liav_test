<%@ taglib uri="UM" prefix="UM" %>

<%@ include file="proxy.txt" %>

<%LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute(LanguagesBean.beanId);%>
<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute(CountriesBean.beanId);%>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>
<jsp:useBean id="companySearchResult"
             class="com.sap.security.core.admin.CompanySearchResultBean"
             scope="request"/>

<% java.util.Locale locale = proxy.getLocale();
   if ( null == locale ) locale = java.util.Locale.getDefault();
   String action = (String) proxy.getSessionAttribute("action");
   if ( null == action ) {
       action = UserAdminLogic.createNewUserAction;
   }

   boolean toDisable = false;
   
   boolean orgReq = ((Boolean)proxy.getRequestAttribute(UserAdminLogic.isOrgUnitRequired)).booleanValue();
   
   String parent = (String) proxy.getSessionAttribute(UserAdminLogic.parent);
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");

   IUser self = null;

   boolean showCancel = false;
   Boolean temp = (Boolean) proxy.getRequestAttribute("cancelbutton");
   if ( null != temp ) {
       if (Boolean.TRUE.equals(temp) ) {
           showCancel = true;
       }
   }
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

<script language="JavaScript">
    function doCompanySelect() {
      frm = document.getElementById("createuser");
      inputTag1 = document.createElement("input");
      inputTag1.setAttribute("name", "<%=UserAdminLogic.selectCompanyAction%>");
      inputTag1.setAttribute("type", "hidden");
      inputTag1.setAttribute("value", "");
      frm.appendChild( inputTag1 );
      inputTag2 = document.createElement("input");
      inputTag2.setAttribute("name", "<%=UserAdminLogic.preRequest%>");
      inputTag2.setAttribute("type", "hidden");
      inputTag2.setAttribute("value", "<%=util.filteringSpecialChar(action)%>");
      frm.appendChild( inputTag2 );
      frm.submit();
    }
</script>

<%@ include file="contextspecific_includes_top.txt" %>

<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("CREATE_NEW_USER_HEADER")%></span>
      </td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<% if ( error.isError() ) { %>
<!-- Start Error Msg-->
<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_ERROR_XSB">
    <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Error Msg -->
<% } %>

<form id="createuser"
      name="createuser"
      method="post"
      action="<%=userAdminAlias%>">
<%@include file="/include/userprofile_basicinfo_create.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%@include file="/include/useraccount_create.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%@include file="/include/userprofile_contactinfo_change.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%if (orgReq) { %>
	<%@include file="/include/userprofile_addtionalinfo_create_withorgunit.jsp"%>
<% } else { %>
	<%@include file="/include/userprofile_additionalinfo_change.jsp"%>
<% } %>

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
    <% String add = " "+userAdminLocale.get("CREATE")+" ";
       String reset = " "+userAdminLocale.get("CLEAR")+" ";
       String cancel = " "+userAdminLocale.get("CANCEL")+" "; %>
	<input type="submit"
	       name="<%=UserAdminLogic.performUserCreateAction%>"
	       tabindex="0"
	       value="<%=add%>"
	       class="BTN_LB">&nbsp;
	<%-- start of Biller Direct Impl. --%> 	       
	<% if ( null != util.checkEmpty(com.sap.security.api.UMFactory.getProperties().get(UserAdminCustomization.UM_ADMIN_CREATE_REDIRECT)) ) { %>
	<input type="submit"
	       name="<%=UserAdminLogic.performUserCreateAction%>"
	       tabindex="0"
	       value=" <%=userAdminLocale.get("CREATE_AND_EDIT")%> "
	       class="BTN_LN">&nbsp;
	<% } %>
	<%-- end of Biller Direct Impl. --%>  	       
	<input type="submit"
	       name="<%=UserAdminLogic.performUserCreateResetAction%>"
	       tabindex="0"
	       value="<%=reset%>"
	       class="BTN_LN">&nbsp;
    <% if ( showCancel) { %>
    <input type="hidden" name="cancelbutton" value="true">
    <% } %>
	<% if (action.equals(UserAdminLogic.createUserFromReferenceAction)) { %>
	<input type="submit"
	       name="<%=UserAdminLogic.cancelCreateUserFromReferenceAction%>"
	       tabindex="0"
	       value="<%=cancel%>"
	       class="BTN_LN">
	<% } %>
  </td></tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

