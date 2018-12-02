<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>

<%-- start jsp page attribute setting --%>
<% if ( util.isServlet23() ) proxy.setResponseContentType("text/html; charset=utf-8"); %>
<%@ page session="true"%>
<%@ page import="com.sap.security.api.IUser" %>
<%@ page import="com.sapmarkets.tpd.master.TradingPartnerInterface"%>
<%@ page import="com.sap.security.core.role.IScopeDefinition"%>
<%@ page import="com.sap.security.core.role.IServiceRepository"%>
<%@ page import="com.sap.security.core.role.imp.xml.XMLServiceRepository"%>

<%LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute("languages");%>
<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute("countries");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<%-- end of page attribute setting --%>

<% TradingPartnerInterface company = (TradingPartnerInterface) proxy.getSessionAttribute(SelfRegLogic.slctcom);
   String companyName  = company.getDisplayName();
   UserAdminCustomization uac = new UserAdminCustomization();
   boolean orgReq = uac.isOrgUnitRequired(company);
   
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
   boolean toDisable = false;
   IUser self = null;
%>

<% if (!inPortal) { %>
<HTML>
<HEAD>
  <TITLE><%=userAdminLocale.get("SELF_REGISTRATION")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
</HEAD>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="putFocus(0,0);">
<% } %>

<script language="JavaScript">
function checkRequired() {
	var countryField = document.getElementById("<%=user.countryId%>");
	var frm = document.getElementById("applyCompanyUser");
	if ( countryField.value == "" ) {
		alert("<%=userAdminLocale.get("COUNTRY_INFO_IS_REQUIRED")%>");
		return false;
	} else {
		todoWhat = document.createElement("input");
		todoWhat.setAttribute("name", "<%=SelfRegLogic.applyCompanyUserAction%>");
		todoWhat.setAttribute("type", "hidden");
		todoWhat.setAttribute("value", "");
		frm.appendChild(todoWhat);				
		frm.submit();
		return true;
	}
}
</script>

<center>
<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
<tr>

<!-- Start Middle 780pxl Content space -->
	<td width="780" height="100%" valign="top" class="TB_CNT_BG">

<!-- Start Content -->
<table cellpadding="0" cellspacing="0" border="0" width="780" height="100%">
    <tr>
    <!-- Start Fuction Icons Shadow -->
    <td width="100%" valign="top" class="SIDE_N_BG">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
              <td background="<%=webpath%>layout/topbkgd.gif" width="100%"><img src="<%=webpath%>layout/sp.gif" height="4" border="0" alt=""></td>
          </tr>
        </table>
    </td>
    <!-- End Fuction Icons Shadow -->
    </tr>

	<tr>
	<!-- Start Transactional Content -->
	<td width="100%" valign="top" align="center">
		<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr class="SIDE_N_BG">
			<td class="TBLO_XSB">
			<span tabindex="0">&nbsp;&nbsp;<%=userAdminLocale.get("APPLY_COMPANYUSER_HEADER")%>&nbsp;<UM:encode><%=companyName%></UM:encode>&nbsp;...</span>
			</td>
			</tr>
			<tr class="SIDE_N_BG">
			<td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" border="0" alt=""></td>
			</tr>
			<tr class="SIDE_N_BG" align="center">
			<td class="TBLO_XXS_L">
			<span tabindex="0"><%=userAdminLocale.get("APPLY_COMPANYUSER_DESCRIPTION")%></span><br></td>
			</tr>
			<tr class="SIDE_N_BG">
			<td><img src="<%=webpath%>layout/sp.gif" width="1" height="12" border="0" alt=""></td>
			</tr>
		</table>

        <% if ( error.isError() ) { %>
		<table cellpadding="0" cellspacing="0" border="0">
			<tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr><td class="TX_ERROR_XSB">
    		    <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
    		    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
    		</td></tr>
		</table>
        <% } %>

    <form id="applyCompanyUser"
          name="applyCompanyUser"
          method="post"
          action="<%=selfRegAlias%>">
    <%@include file="/include/userprofile_contactinfo_change.jsp"%>

    <table cellpadding="0" cellspacing="0" border="0">
			<tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
		</table>

		<%@include file="/include/guestuserprofile_additionalinfo_change.jsp"%>

    <%-- following section could be customized by customers --%>
    <%@include file="/include/userprofile_customizedattri_change.jsp"%>
    <%-- end of the customized section--%>

		<table cellpadding="0" cellspacing="0" border="0">
			<tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
		</table>
		<!-- Start Page Action Buttons -->
		<table width="98%" border="0" cellpadding="0" cellspacing="0">
			<tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
			<% String submit = " "+userAdminLocale.get("SUBMIT")+" ";
        	   String reset = " "+userAdminLocale.get("RESET")+" ";
        	   String cancel = " "+userAdminLocale.get("CANCEL")+" "; %>
			<input <% if (orgReq) { %>type="button"<% } else { %>type="submit"<% } %>
			       class="BTN_LB"
			       name="<%=SelfRegLogic.applyCompanyUserAction%>"
			       tabindex="0"
			       value="<%=submit%>"
			       <% if (orgReq) { %>onClick="checkRequired()"<% } %>>&nbsp;
			<input class="BTN_LN"
			       type="submit"
			       tabindex="0"
			       name="<%=SelfRegLogic.resetApplyCompanyUserAction%>"
			       value="<%=reset%>">&nbsp;
			<input class="BTN_LN"
			       type="submit"
			       tabindex="0"
			       name="<%=SelfRegLogic.cancelApplyCompanyUserAction%>"
			       value="<%=cancel%>">
			</td></tr>
		</table></form>
		<!-- End Page Action Buttons -->

		<table cellpadding="0" cellspacing="0" border="0">
			<tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
		</table>

<%@ include file="contextspecific_includes_bottom.txt" %>



