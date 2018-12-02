<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="com.sap.security.core.role.*" %>
<%@ page import="com.sap.security.core.role.imp.*" %>
<%@ page import="com.sap.security.core.*" %>
<%@ page import="com.sap.security.core.admin.role.*" %>
<%@ page import="com.sap.security.api.*" %>
<%@ page import="java.util.Iterator" %>
<%@ include file="/proxy.txt" %>

<jsp:useBean id="info" class="com.sap.security.core.util.InfoBean" scope='request'/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope="request"/>
<%com.sap.security.core.admin.role.RoleAdminMessagesBean roleAdminMessages = (com.sap.security.core.admin.role.RoleAdminMessagesBean) proxy.getSessionAttribute("roleAdminMessages");%>
<%com.sap.security.core.admin.role.RoleAdminLocaleBean roleAdminLocale = (com.sap.security.core.admin.role.RoleAdminLocaleBean) proxy.getSessionAttribute("roleAdminLocale");%>

<%
Iterator roles = (Iterator)HelperClass.getAttr(proxy,"list_roles");
String searchFilter = (String)HelperClass.getAttr(proxy, "searchFilter");

//String myUrl= RoleAdminServlet.alias+"?cmd=role-management-main";
String passedUrl = "needed for company block - currently commented out"; //CompanySearchLogic.getSearchCompanyURL(proxy, myUrl, true);

IRole role = null;
String roleID = "";
String displayRolename = "";
String template = "";
//template = (String)HelperClass.getAttr(proxy,"template");
String ID = (String)HelperClass.getAttr(proxy,"ID");

String type = (String)HelperClass.getAttr(proxy,"type");
if("".equals(type)) type = "TRADING_PARTNER";
String redirectURL= (String)HelperClass.getAttr(proxy,"redirectURL");
String GROUP = "group";
String USER = "user";
String principal = (String)HelperClass.getAttr(proxy,"principal");

%>

<%if (!inPortal) {%> <html>
<head>
<title><%=roleAdminLocale.get("MAIN_MENU")%></title>
<script language="JavaScript" src="<%=webpath%>js/util-role.js"></script>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>

<script language="JavaScript">
function setCmd(value)
{
  document.frmRoleInfo.cmd.value = value;
}
function setNewSF()
{
  document.frmRoleInfo.newSearchFilter.value = 'true';
  document.frmRoleInfo.cmd.value = 'role-management-main';
}
function setNewSFPost()
{
  document.frmRoleInfo.newSearchFilter.value = 'true';
}
function expandME(el) {
	whichEl = eval(el);
	whichIm = event.srcElement;
	if (whichEl.style.display == "none") {
		whichEl.style.display = "block";
		whichIm.src = "<%=webpath%>layout/icon_open.gif";
	}
	else {
		whichEl.style.display = "none";
		whichIm.src = "<%=webpath%>layout/icon_close.gif";
	}
}
function newWindow() {
		window.open("05_2_1.htm", "sub", "WIDTH=200, HEIGHT=460, status=yes, resizable=yes scrollbars=yes")
}
function newWindow2() {
		window.open("12.htm", "sub", "WIDTH=400, HEIGHT=400, status=yes, resizable=yes scrollbars=yes")
}
</script>

</head> <%}%>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="frmRoleInfo" action="<%=RoleAdminServlet.alias%>" onSubmit="setNewSFPost()" method="post">
<%@ include file="/contextspecific_includes_top.txt" %>

<center>
<!-- Start Section Header -->
<a name="main"></a>		
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
	<tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=roleAdminLocale.get("MAIN_HEADER")%></td></tr>
</table>
<!-- End Section Header -->
<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
	<tr><td width="100%" class="TBLO_XXS_L"><%=roleAdminLocale.get("ASSIGN_MAIN")%></td></tr>
</table>
<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>

<!-- Start Info Msg-->
<% if ( info.isInfo() ) { %>
	<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
	<tr><td width="100%" class="TX_CFM_XSB">
	    <img src="<%=webpath%>layout/ico12_msg_success.gif" width="12" height="12" border="0" />&nbsp;<UM:encode><%=roleAdminMessages.print(info.getMessage())%></UM:encode>
	</td></tr>
	</table>
<% } %>
<!-- End Info Msg -->
<!-- Start Error Msg-->
<% if ( error.isError() ) { %>
	<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
	  <tr><td width="100%" class="TX_ERROR_XSB">
	    <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;<UM:encode><%=roleAdminMessages.print(error.getMessage())%></UM:encode>
	  </td></tr>
	</table>
<% } %>
<!-- End Error Msg -->

<TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
	
    <!--  Role Search ..... -->
  	<tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
  	<tr>
  		<td class="TBLO_XXS_R" nowrap><LABEL FOR="Search"><%=userAdminLocale.get("NEW_SEARCH")%></LABEL></td>
  		<td class="TX_XXS" nowrap><input name="searchFilter" type="text" size="20" style="width: 2in" value="<UM:encode><%=searchFilter%></UM:encode>">&nbsp;
			<input type="image" src="<%=webpath%>layout/search.gif" width="24" height="20" border="0" alt="<%=userAdminLocale.get("SEARCH")%>"
				   onClick="setNewSF()">&nbsp;&nbsp;&nbsp;&nbsp;
  		</td>
  	</tr>

	<tr><td colspan="2"><IMG height="15" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	<tr>
	   <td class="TBLO_XXS_R" valign="top" nowrap><LABEL FOR="CompanyRoles"><%=roleAdminLocale.get("AVAILABLE_ROLES")%></LABEL></td>
	   <td class="TBLO_XXS_L" nowrap>
  	   <table width="100%" border="0" cellspacing="0" cellpadding="0">
		<tr> <td class="TX_XS" valign="top" style="width:400px">
	<%
	if ((null == roles) || !(roles.hasNext())) { %>
			<select id="CompanyRoles" name="rolename" width="<%=RoleAdminServlet.MAX_ROLE_NAME_LENGTH%>" size="10" style="width:300px" disabled>
	<% }
	else { %>
			<select id="CompanyRoles" name="rolename" width="<%=RoleAdminServlet.MAX_ROLE_NAME_LENGTH%>" size="10" style="min-width:300px" multiple>
	<%
	   while (roles.hasNext())
	   {
		  role = (IRole) roles.next();
		  if(null != role)
		  {
			 roleID = (String) role.getUniqueID();
			 displayRolename = (String) role.getDisplayName();
		  }
	%>
				   <option value="<%=util.URLEncoder(roleID)%>"><UM:encode><%=displayRolename%></UM:encode></option>
	<% }} %>
			</select>
		  </td></tr>
		<tr><td colspan="2"><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
		<tr><td valign="top" class="TBLO_XXS_L" nowrap>
		<% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.ADD_ROLES) ) { %>
			<input type="image" src="<%=webpath%>layout/icon_newDL.gif" width="24" height="20" border="0" alt="<%=roleAdminLocale.get("ADD_ROLE")%>"
				onClick="setCmd('role-management-add')">&nbsp;&nbsp;&nbsp;&nbsp;
		<% }
		   if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MODIFY_ROLES) ) { %>
			<input type="image" src="<%=webpath%>layout/icon_viewDL.gif" width="24" height="20" border="0" alt="<%=roleAdminLocale.get("MODIFY")%>"
				onClick="return validateModifyRole(window.document.forms['frmRoleInfo'],'rolename','role-management-modify','<%=roleAdminLocale.get("ONLY_ONE_ROLE_NOTIFICATION")%>','<%=roleAdminLocale.get("CHOOSE_ROLE_NOTIFICATION")%>')">&nbsp;&nbsp;&nbsp;&nbsp;
		<% }
		   if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.ASSIGN_ROLES) ) { %>
			<input type="image" src="<%=webpath%>layout/assignRoles.gif" width="24" height="20" border="0" alt="<%=roleAdminLocale.get("ASSIGNMENT_USERS")%>"
				onClick="return confirmAssignRoles(window.document.forms['frmRoleInfo'],'role-assignment','rolename','<%=roleAdminLocale.get("ATLEAST_ONE_ROLE_NOTIFICATION")%>','<%=USER%>')">&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="image" src="<%=webpath%>layout/assignrole.gif" width="24" height="20" border="0" alt="<%=roleAdminLocale.get("ASSIGNMENT_GROUP")%>"
				onClick="return confirmAssignRoles(window.document.forms['frmRoleInfo'],'role-assignment','rolename','<%=roleAdminLocale.get("ATLEAST_ONE_ROLE_NOTIFICATION")%>','<%=GROUP%>')">&nbsp;&nbsp;&nbsp;&nbsp;
		<% }
		   if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.DELETE_ROLES) ) { %>
			<input type="image" src="<%=webpath%>layout/trash.gif" width="24" height="20" border="0" alt="<%=roleAdminLocale.get("DELETE")%>"
				onClick="return confirmDeletion(window.document.forms['frmRoleInfo'],'role-management-delete','rolename','<%=roleAdminLocale.get("ROLE_CONFIRM_DELETION")%>','<%=roleAdminLocale.get("ATLEAST_ONE_ROLE_NOTIFICATION")%>');">&nbsp;&nbsp;&nbsp;&nbsp;
		<% } %>
 			<input type="image" src="<%=webpath%>layout/exportgroup.gif" width="24" height="20" border="0" alt="<%=userAdminLocale.get("DOWNLOAD_USERS_NOW")%>"
 				onClick="return confirmAssignRoles(window.document.forms['frmRoleInfo'],'role-export','rolename','<%=roleAdminLocale.get("ATLEAST_ONE_ROLE_NOTIFICATION")%>')">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		</td></tr>
	</table>
  </td></tr>
</table>

<input type="hidden" name="ID" value="<UM:encode><%=ID%></UM:encode>">
<input type="hidden" name="type" value="<UM:encode><%=type%></UM:encode>">
<input type="hidden" name="cmd" value="">
<input type="hidden" name="newSearchFilter">
<input type="hidden" name="principal" value="<UM:encode><%=principal%></UM:encode>">
</form>
<%@ include file="/contextspecific_includes_bottom.txt" %>


