<%@ include file="proxy.txt" %>
<%-- to include this page requires:--%>
<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.*"%>
<%LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute("languages");%>
<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute("countries");%>

<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>
<jsp:useBean id="companySearchResult"
             class="com.sap.security.core.admin.CompanySearchResultBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>
<%-- end page attribute setting--%>

<% IUser self = user.getUser();
   boolean toDisable = false;
   IUserAccount account = userAccount.getUserAccount();
   
   UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   boolean orgReq = uac.isOrgUnitRequired(proxy, self);
   
   Boolean toApplyCom = null;
   if ( null != proxy.getRequestAttribute("toApplyCom") ) {
       toApplyCom = (Boolean) proxy.getRequestAttribute("toApplyCom");
   }
   String changeAction = UserAdminLogic.performUserProfileChangeAction;
   String cancelAction = UserAdminLogic.cancelUserProfileChangeAction;
   if ( null != toApplyCom ) {
       changeAction = UserAdminLogic.performGuestUserAddAction;
       cancelAction = UserAdminLogic.performGuestUserCancelAction;
   }
   
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

<script language="JavaScript">	 
    function doCompanySelect() {
      var frm = document.getElementById("changeUserOwnProfile");
      actionTag = document.createElement("input");
      actionTag.setAttribute("name", "<%=UserAdminLogic.selectCompanyAction%>");
      actionTag.setAttribute("type", "hidden");
      actionTag.setAttribute("value", "");
      frm.appendChild( actionTag );
      preReqTag = document.createElement("input");
      preReqTag.setAttribute("name","<%=UserAdminLogic.preRequest%>");
      preReqTag.setAttribute("type","hidden");
      preReqTag.setAttribute("value","<%=UserAdminLogic.changeUserProfileAction%>");
      frm.appendChild( preReqTag );
      frm.submit();
    }
    
	function checkRequired() {
		var frm = document.getElementById("changeUserOwnProfile");
		var countryField = document.getElementById("<%=user.countryId%>");		
		if ( countryField.value == "" ) {
			alert("<%=userAdminLocale.get("COUNTRY_INFO_IS_REQUIRED")%>");
			return false;
		} else {
			todoWhat = document.createElement("input");
			todoWhat.setAttribute("name", "<%=changeAction%>");
			todoWhat.setAttribute("type", "hidden");
			todoWhat.setAttribute("value", "");
			frm.appendChild(todoWhat);				
			frm.submit();
			return true;
		}
	}   
	
	function doOrgUnitSearch() {
	  var frm = document.getElementById("changeUserOwnProfile");
	  inputTag = document.createElement("input");
	  inputTag.setAttribute("name", "<%=UserAdminLogic.searchOrgUnitAction%>");
	  inputTag.setAttribute("type", "hidden");
	  inputTag.setAttribute("value", "");
	  frm.appendChild( inputTag );
	  inputTag2 = document.createElement("input");
	  inputTag2.setAttribute("name", "<%=UserAdminLogic.preRequest%>");
	  inputTag2.setAttribute("type", "hidden");
	  inputTag2.setAttribute("value", "<%=changeAction%>");
	  frm.appendChild( inputTag2 );
	  frm.submit();
	}	 
</script>
<%@ include file="contextspecific_includes_top.txt" %>

<form id="changeUserOwnProfile" name="changeUserOwnProfile" method="post" action="<%=userAdminAlias%>">

<!-- Start Section Header -->
<% if ( null == toApplyCom ) { %>
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("MODIFY_USERPROFILE_HEADER")%></span></td></tr>
</table>
<!-- End Section Header -->
<% } else { %>
<!-- Start Section Header -->
<a name="main"></a><table class="SEC_TB_BDR" cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
  <tr><td>
    <table cellpadding="2" cellspacing="1" border="0" width="100%">
    <tr class="SEC_TB_TD">
      <td class="SEC_TB_TD" width="95%">&nbsp;
      <span tabindex="0"><%=userAdminLocale.get("COMPANY_USER_INFO")%></span>
      </td>
    </tr>
    </table>
  </td></tr>
</table>
<!-- End Section Header -->
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<% if ( null == toApplyCom ) { %>
<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
  <span tabindex="0"><%=userAdminLocale.get("MODIFY_USERPROFILE_DESCRIPTION")%></span>
  </td></tr>
</table>
<% } else { %>
<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
  <span tabindex="0"><%=userAdminLocale.get("GUEST_USER_INSTRUCTION")%></span>
  </td></tr>
</table>
<% } %>

<% if ( info.isInfo() ) { %>
<!-- Start Confirm Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_warning.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Confirm Msg -->
<% } %>

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

<%@include file="/include/userprofile_basicinfo_change.jsp"%>


<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%@include file="/include/userprofile_contactinfo_change.jsp"%>


<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<% if ( null == toApplyCom ) { %>
	<%if (orgReq) { %>
		<%@include file="/include/userprofile_additionalinfo_change.jsp"%>
	<% } else { %>
		<%@include file="/include/userprofile_additionalinfo_change.jsp"%>
	<% } %>
<% } else { %>
	<%@include file="/include/guestuserprofile_additionalinfo_change.jsp"%>
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
    <% String save = " "+userAdminLocale.get("SAVE_CHANGES")+" ";
       String cancel = " "+userAdminLocale.get("CANCEL")+" "; %>
	<input <% if (orgReq) { %>type="button"<% } else { %>type="submit"<% } %>
	       name="<%=changeAction%>"
	       tabindex="0"
	       value="<%=save%>"
	       class="BTN_LB"
	       <% if (orgReq) { %>onClick="checkRequired()"<% } %>>&nbsp;
	<input type="submit"
	       name="<%=cancelAction%>"
	       tabindex="0"
	       value="<%=cancel%>"
	       class="BTN_LN">
  </td></tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

