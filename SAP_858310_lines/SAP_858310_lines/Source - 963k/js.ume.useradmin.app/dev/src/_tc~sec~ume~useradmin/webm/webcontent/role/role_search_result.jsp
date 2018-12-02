<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="com.sap.security.core.role.*" %>
<%@ page import="com.sap.security.core.role.imp.*" %>
<%@ page import="com.sap.security.core.*" %>
<%@ page import="com.sap.security.core.admin.role.*" %>
<%@ page import="com.sap.security.core.admin.group.*" %>
<%@ page import="com.sap.security.api.*" %>
<%@ page import="java.util.Iterator" %>
<%@ include file="/proxy.txt" %>

<jsp:useBean id="info" class="com.sap.security.core.util.InfoBean" scope='request'/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope="request"/>
<%com.sap.security.core.admin.role.RoleAdminMessagesBean roleAdminMessages = (com.sap.security.core.admin.role.RoleAdminMessagesBean) proxy.getSessionAttribute("roleAdminMessages");%>
<%com.sap.security.core.admin.role.RoleAdminLocaleBean roleAdminLocale = (com.sap.security.core.admin.role.RoleAdminLocaleBean) proxy.getSessionAttribute("roleAdminLocale");%>

<% 
int length = 0;
int colspan = 7;
int totalItems = 0;
int currentPage = 0;
int totalPages = 0;
Integer[] itemPerPageOptions = null;
int currentItemPerPage = 0;
IPrincipal[] assignedMembers = null;
String principal = (String)HelperClass.getAttr(proxy,"principal");
ListBean list = (ListBean) proxy.getRequestAttribute(ListBean.beanId);
if (null != list)
{
	java.util.Vector temp = list.getObjsOnCurrentPage();
	if ((null != temp) && (!temp.isEmpty()))
	{
		assignedMembers = HelperClass.getPrincipals(temp);
		length = temp.size();
		totalItems = list.getTotalItems();
		currentPage = list.getCurrentPage();
		totalPages = list.getTotalPages();
		itemPerPageOptions = list.getItemPerPageOptions();
		currentItemPerPage = list.getCurrentItemPerPage();
   	}
}

String   rolenamesReport = (String) HelperClass.getAttr(proxy,"rolenamesReport");
String   roleIDs = (String) HelperClass.getAttr(proxy,"roleIDs");
String   roleDescription = (String) HelperClass.getAttr(proxy,"roleDescription");
if ("MultiGroup".equals( roleDescription )) 
	roleDescription = roleAdminLocale.get("MULTI_USER_GROUP_ASSIGNMENT");
principal = principal.toLowerCase();
boolean useUserAsPrincipal = "user".equals( principal );

String searchFilter = (String)HelperClass.getAttr(proxy, "searchFilter");
String memberSF = (String)HelperClass.getAttr(proxy, "memberSF");
%>

<%if (!inPortal) {%> <html>
<head>
<title><%=roleAdminLocale.get("ROLE_MANAGEMENT")%></title>
<script language="JavaScript" src="<%=webpath%>js/util-role.js"></script>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
<script language="JavaScript" src="<%=webpath%>js/roleList.js"></SCRIPT>
<script language="JavaScript">
function setReq(value)
{
	document.frmRoleInfo.cmd.value = value;
	var msg = "";
	var selected = 0;
	var checkboxGrp = document.frmRoleInfo.selectedUsers;
	var valChecked;
	document.frmRoleInfo.uncheckedItems.value = msg;

	if(checkboxGrp == null)
	{
		return 0;	
	}
	if(checkboxGrp.length == null)
	{
		if (checkboxGrp.checked == true)
		{
			valChecked = 1;
			selected = 1;
		}
		else
		{
			valChecked = 0;
			msg = msg + checkboxGrp.value + ",";
		}

	}
	else
	{
		for (var i=0; i<checkboxGrp.length; i++)
		{
			if (checkboxGrp[i].checked == true)
			{
				valChecked = 1;
				selected = 1;
			}
			else
			{
				valChecked = 0;
				msg = msg + checkboxGrp[i].value + ",";
			}
		}
	}
	// you can put the content of msg into a hidden text field
	document.frmRoleInfo.uncheckedItems.value = msg;
	return selected;
}

function checkbox_select_all()
{
	var checkboxGrp = document.frmRoleInfo.selectedUsers;
	if(checkboxGrp.length == null)
	{
			checkboxGrp.checked = true;

	}
	else
	{
		for (var i=0; i<checkboxGrp.length; i++)
		{
			checkboxGrp[i].checked = true;
		}
	}
}

function checkbox_deselect_all()
{
	var checkboxGrp = document.frmRoleInfo.selectedUsers;
	if(checkboxGrp.length == null)
	{
			checkboxGrp.checked = false;

	}
	else
	{
		for (var i=0; i<checkboxGrp.length; i++)
		{
			checkboxGrp[i].checked = false;
		}
	}
}

function toggleSelection()
{
	if(document.frmRoleInfo.toggle.value == "")
	{

		checkbox_select_all();
		document.frmRoleInfo.toggle.value = "selected";
	}
	else
	{
		checkbox_deselect_all();
		document.frmRoleInfo.toggle.value = "";
	}
	return false;
}

function doSearch()
{
	if ((document.frmRoleInfo.cmd.value == null) || (document.frmRoleInfo.cmd.value == ""))
		document.frmRoleInfo.cmd.value = 'role-assignment-search-groups';
}
</script>
</head> <%}%>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="frmRoleInfo" onSubmit="doSearch()" method="post">
<%@ include file="/contextspecific_includes_top.txt" %>
	<!-- start Section Header -->
   	<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
		<tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=roleAdminLocale.get("SEARCH_GROUPS_HEAD")%></td></tr>
   	</table>
   	<!-- End Section Header -->
   	<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
	<!-- Start Section Description -->
	<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
		<tr><td width="100%" class="TBLO_XXS_L"><%=roleAdminLocale.get("SEARCH_GROUPS_ROLES_DESC")%></td></tr>
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

	<table cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
		<tr><td colspan="2"><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
		<!--  Group Search ..... -->
     	<tr>
			<td class="TBLO_XXS_R" nowrap><LABEL FOR="Search"><%=userAdminLocale.get("NEW_SEARCH")%></LABEL></td>
	   		<td class="TX_XXS" nowrap><input name="searchFilter" type="text" size="20" style="width: 2in" value="<UM:encode><%=searchFilter%></UM:encode>">&nbsp;
 				<input type="image" src="<%=webpath%>layout/search.gif" width="24" height="20" border="0" alt="<%=userAdminLocale.get("SEARCH")%>"
 				   	onClick="doSearch()">&nbsp;&nbsp;&nbsp;&nbsp;
 			</td>
		</tr>
		<tr><td colspan="2"><IMG height="15" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	</table>

	<table cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr><td class="TBLO_XS_L"><%=roleAdminLocale.get("FOUND_GROUPS")%>&nbsp;</td></tr>
 	</table>

	<!-- Start Result Table-->
	<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%">
	    <tr><td>
			<table cellpadding="1" cellspacing="1" border="0" width="100%">
			<% if ( length > 0 ) { %>
				<%@ include file="/role/role_pagenavigation.jsp"%>
			<% } %> 
				<tr>
					<td class="TBDATA_HEAD" nowrap>
 					<% if ( length > 0 ) {%>
 				   		<input type=image src="<%=webpath%>layout/mon_check.gif" width="13" height="13" border="0" alt="<%=roleAdminLocale.get("SELECTING")%>" onClick="return toggleSelection()"></td>
 					<% } %> 
			   		<td scope="col" class="TBDATA_HEAD" nowrap><%=roleAdminLocale.get("NAME")%></td>
			   		<td scope="col" class="TBDATA_HEAD" nowrap><%=roleAdminLocale.get("DESCRIPTION_TABLE_HEADER")%></td>
				</tr>
		   	<%  for (int j=0; j < length; j++)
				{
		        	String displayName = assignedMembers [j].getDisplayName();
			  		String uid = assignedMembers [j].getUniqueID();
			  		String description = HelperClass.getPrincipalInfo(assignedMembers [j]);
			  		if (description == null) description = "";
	        %>
				<tr class="<%= (j % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
			    	<td class="TBDATA_XXS_C" width="1%" nowrap><input type="checkbox" name=selectedUsers class="noborder" value="<%=util.URLEncoder(uid)%>"></td>
				    <td scope="row" class="TBDATA_XS_L"><UM:encode><%=displayName%></UM:encode></td>
				    <td class="TBDATA_XXS_L" nowrap><UM:encode><%=description%></UM:encode></td>
				</tr>
			<% } %>
		    <% if ( length > 0 ) { %>
				<%@ include file="/role/role_pagenavigation.jsp"%>
		    <% } %> 
			</table>
		</td></tr>
	</table>
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>

	<!-- Start Page Action Buttons -->
	<table width="98%" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td width="100%" align="left" class="TBLO_XXS_L" nowrap>
			<% if ( length > 0 ) {%>
				<input class="BTN_LN" type="submit" name="select" value="&nbsp;<%=roleAdminLocale.get("SELECT")%>&nbsp;"
					onClick="setReq('role-assignment-add-groups')">&nbsp;
			<% } %> 
				<input class="BTN_LN" type="submit" name="cancel" value="&nbsp;<%=roleAdminLocale.get("CANCEL")%>&nbsp;"
					onClick="setReq('role-abort')">&nbsp;
			</td>
		</tr>
   	</table>

	<input type="hidden" name="rolenamesReport" value="<UM:encode><%=rolenamesReport%></UM:encode>">
	<input type="hidden" name="roleDescription" value="<UM:encode><%=roleDescription%></UM:encode>">
	<input type="hidden" name="roleIDs" value="<UM:encode><%=roleIDs%></UM:encode>">
	<input type="hidden" name="cmd" value="">
	<input type="hidden" name="toggle" value="">
	<input type="hidden" name="uncheckedItems">
	<input type="hidden" name="principal" value="<UM:encode><%=principal%></UM:encode>">
	<input type="hidden" name="searching" value="true">
	<input type="hidden" name="memberSF" value="<UM:encode><%=memberSF%></UM:encode>">
	<input type="hidden" name="searchFilter">
</form>
<%@ include file="/contextspecific_includes_bottom.txt" %>
