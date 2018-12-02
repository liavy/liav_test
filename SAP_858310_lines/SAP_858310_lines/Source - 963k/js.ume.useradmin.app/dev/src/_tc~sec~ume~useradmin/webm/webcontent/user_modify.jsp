<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.*"%>

<%@ include file="proxy.txt" %>
<%LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute(LanguagesBean.beanId);%>
<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute(CountriesBean.beanId);%>

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

<% java.util.Locale locale = proxy.getLocale();
   if ( null == locale ) locale = java.util.Locale.getDefault();
   boolean hasAccount = false;
   IUserAccount account = userAccount.getUserAccount();
   if ( null != account ) {
       hasAccount = true;
   }
   IUser self = user.getUser();

   UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   boolean orgReq = uac.isOrgUnitRequired(proxy, self);
   
   boolean orgCreate = false;
   boolean toDisable = false;
   if ( null != proxy.getSessionAttribute("orgCreate")) {
       orgCreate = ((Boolean)proxy.getSessionAttribute("orgCreate")).booleanValue();
   }
   
   boolean allowCert = uac.isCertLogonAllowed();

   String parent = UserAdminLogic.userModifyPage;
   String action = UserAdminLogic.modifyUserAction;
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");

   boolean showCancel = false;
   Boolean temp = (Boolean) proxy.getRequestAttribute("cancelbutton");
   if ( null != temp ) {
       if (Boolean.TRUE.equals(temp) ) {
           showCancel = true;
       }
   }
%>

<%-- start html--%>
<%if (!inPortal) {%> <html>
<head>
<TITLE><%=userAdminLocale.get("USER_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
<script language="JavaScript" src="js/roleList.js"></script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<script language="JavaScript">
	
    function doCompanySelect() {
      var frm = document.getElementById("modifyuser");
      inputTag1 = document.createElement("input");
      inputTag1.setAttribute("name", "<%=UserAdminLogic.selectCompanyAction%>");
      inputTag1.setAttribute("type", "hidden");
      inputTag1.setAttribute("value", "");
      frm.appendChild(inputTag1);
      inputTag2 = document.createElement("input");
      inputTag2.setAttribute("name", "<%=UserAdminLogic.preRequest%>");
      inputTag2.setAttribute("type", "hidden");
      inputTag2.setAttribute("value", "<%=UserAdminLogic.modifyUserAction%>");
      frm.appendChild( inputTag2 );
      frm.submit();
    }

	function doOrgUnitSearch() {
	  var frm = document.getElementById("modifyuser");
	  inputTag = document.createElement("input");
	  inputTag.setAttribute("name", "<%=UserAdminLogic.searchOrgUnitAction%>");
	  inputTag.setAttribute("type", "hidden");
	  inputTag.setAttribute("value", "");
	  frm.appendChild( inputTag );
	  inputTag2 = document.createElement("input");
	  inputTag2.setAttribute("name", "<%=UserAdminLogic.preRequest%>");
	  inputTag2.setAttribute("type", "hidden");
	  inputTag2.setAttribute("value", "<%=UserAdminLogic.modifyUserAction%>");
	  frm.appendChild( inputTag2 );
	  frm.submit();
	}
	
    function setSlctRoles() {
        /*
        var frm = document.forms[0];
        setSlct(frm.elements['<%=UserAdminLogic.assignedRoles%>']);
        setSlct(frm.elements['<%=UserAdminLogic.availableRoles%>']);
        */
    }

    function setSlct(slct) {
        for(var i=0; i<slct.options.length; i++) {
            if ( slct.options[i].value != "") {
              slct.options[i].selected = true;
            } else {
              slct.options[i].selected = false;
            }
        }
    }
</script>

<%@ include file="contextspecific_includes_top.txt" %>

<!-- Start Section Header -->
<a name="main"></a>

<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("MODIFY_USER_HEADER")%></span>
    </td></tr>
</table>

<!-- End Section Header -->

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

<% if ( info.isInfo() ) { %>
<!-- Start Info Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
<tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_warning.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
</td></tr>
</table>
<!-- End Info Msg -->
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L"><%=userAdminLocale.get("MODIFY_USER_DESCRIPTION")%></td></tr>
</table>

<form id="modifyuser"
      name="modifyuser"
      method="post"
      action="<%=userAdminAlias%>"
      onSubmit="javascript:setSlctRoles();">
<%@include file="/include/userprofile_basicinfo_change_withrole.jsp"%>

<% if ( hasAccount ) { %>
	<table cellpadding="0" cellspacing="0" border="0">
	  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	</table>
	<%@include file="/include/useraccount_change.jsp"%>
	
	<% if (allowCert) { %>
	<table cellpadding="0" cellspacing="0" border="0">
	  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	</table>
	<%@include file="/include/usercertificate_update.jsp"%>
	<% } %>
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>
<%@include file="/include/userprofile_contactinfo_change.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>
<% if ( orgCreate) { %>
<%@include file="/include/userprofile_addtionalinfo_create_withorgunit.jsp"%>
<% } else { %>
    <%if (orgReq) { %>
    <%@include file="/include/userprofile_additionalinfo_change.jsp"%>
    <% } else { %>
    <%@include file="/include/userprofile_additionalinfo_change.jsp"%>
    <% } %>
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
	<input type="submit"
	       name="<%=UserAdminLogic.performUserModifyAction%>"
	       tabindex="0"
	       value="<%=save%>"
	       class="BTN_LB" >&nbsp;
	<%-- start of Biller Direct Impl. --%> 	
	<% if ( null != util.checkEmpty(com.sap.security.api.UMFactory.getProperties().get(UserAdminCustomization.UM_ADMIN_MODIFY_REDIRECT)) ) { %>       
	<input type="submit"
	       name="<%=UserAdminLogic.performUserModifyAction%>"
	       tabindex="0"
	       value=" <%=userAdminLocale.get("SAVE_AND_EDIT")%> "
	       class="BTN_LN">&nbsp;
	<% } %>
	<%-- end of Biller Direct Impl. --%>  	       
    <% if ( showCancel) { %>
    <input type="hidden" name="cancelbutton" value="true">
    <% } %>
	<input type="submit"
	       name="<%=UserAdminLogic.cancelUserModifyAction%>"
	       tabindex="0"
	       value="<%=cancel%>"
	       class="BTN_LN">
  </td></tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>


