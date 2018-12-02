<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="com.sap.security.api.*" %>
<%@ page import="com.sap.security.core.*" %>
<%@ page import="com.sap.security.core.imp.AbstractPrincipal" %>
<%@ page import="com.sap.security.core.role.*" %>
<%@ page import="com.sap.security.core.role.imp.*" %>
<%@ page import="com.sap.security.core.admin.role.*" %>
<%@ page import="com.sap.security.core.admin.group.*" %>
<%@ page import="java.util.*"%>
<%@ include file="/proxy.txt" %>

<%com.sap.security.core.admin.role.RoleAdminLocaleBean roleAdminLocale = (com.sap.security.core.admin.role.RoleAdminLocaleBean) proxy.getSessionAttribute("roleAdminLocale");%>

<% 
	int length = 0;
	int colspan = 7;
	int totalItems = 0;
	int currentPage = 0;
	int totalPages = 0;

	Integer[] itemPerPageOptions = null;
   	int currentItemPerPage = 0;
   	IPrincipal[] parentPrincipals = null;
   	ListBean list = (ListBean) proxy.getSessionAttribute("rolesGroupsList");
   	if (null != list)
   	{
   		java.util.Vector temp = list.getObjsOnCurrentPage();
		if ((null != temp) && (!temp.isEmpty()))
		{
			parentPrincipals = HelperClass.getPrincipals(temp);
			length = temp.size();
			totalItems = list.getTotalItems();
			currentPage = list.getCurrentPage();
			totalPages = list.getTotalPages();
			itemPerPageOptions = list.getItemPerPageOptions();
			currentItemPerPage = list.getCurrentItemPerPage();
   		}
   	}
    String pageKey = UserAdminLogic.listPage;
    String pageName = UserAdminLogic.userSearchResultPage;

	IUser user = (IUser) HelperClass.getAttr( proxy, "user" );
	String userUniqueName = null;
	String userDisplayName = null;
	if (user != null)
	{
		userUniqueName = user.getUniqueName();
		userDisplayName = user.getDisplayName();
	}
	String principalType = (String) HelperClass.getAttr( proxy, "principalType" );
	// default principal type is role
	boolean useRoleAsPrincipal = ("group".equals(principalType))?false:true;
	Set directParentIDs = (Set) proxy.getSessionAttribute( "directParentIDs" );

	String assigned_principal_head = roleAdminLocale.get("ASSIGNED_ROLES_HEAD");
	String assigned_principal_desc = roleAdminLocale.get("ASSIGNED_ROLES_DESC");
	String assigned_principal_table = roleAdminLocale.get("ASSIGNED_ROLES_TABLE"); 
	if ( !useRoleAsPrincipal )
	{
		assigned_principal_head = roleAdminLocale.get("ASSIGNED_GROUPS_HEAD");
		assigned_principal_desc = roleAdminLocale.get("ASSIGNED_GROUPS_DESC");
		assigned_principal_table = roleAdminLocale.get("ASSIGNED_GROUPS_TABLE"); 
	}
%>

<%if (!inPortal) {%> <html>
<head>
	<title><%=userAdminLocale.get("USER_MANAGEMENT")%></title>
	<script language="JavaScript" src="<%=webpath%>js/util-role.js"></script>
	<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
	<SCRIPT Language="JavaScript" src="<%=webpath%>js/roleList.js"></SCRIPT>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<form name="user_viewrolesgroups" action="<%=userAdminAlias%>" method="post">
<%@ include file="/contextspecific_includes_top.txt" %>
   <!-- start Section Header -->
   <table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
	<tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=assigned_principal_head%></td></tr>
   </table>
   <!-- End Section Header -->
   <table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
<!-- Start Section Description -->
	
	<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
	    <tr><td width="100%" class="TBLO_XXS_L"><%=assigned_principal_desc%></td></tr>
	</table>
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>

	<table cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
		<tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
     	<tr>
			<td class="TBLO_XXS_R" width="25%"><%=userAdminLocale.get("USER_ID")+":"%></td>
			<td class="TX_XXS"><UM:encode><%=userUniqueName%></UM:encode></td>
		</tr>
		<tr>
			<td class="TBLO_XXS_R" width="25%" valign="top"><%=userAdminLocale.get("USER_NAME")%>&nbsp;</td>
			<td class="TX_XXS" ><UM:encode><%=userDisplayName%></UM:encode></td>
		</tr>
		<tr><td colspan="2"><IMG height="15" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	</table>

	<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
	<table cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr>
			<td class="TBLO_XS_L"><%=assigned_principal_table%>&nbsp;</td>
		</tr>
	</table>
		<!-- Start Result Table-->
		<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%">
		    <tr><td>
			<table cellpadding="1" cellspacing="1" border="0" width="100%">
			<% if ( length > 0 ) { %>
				<%@ include file="/include/rolegroup_pagenavigation.jsp"%>
			<% } %>
			<tr> 
				<td scope="col" class="TBDATA_HEAD" nowrap width="1%"><img width="17" height="22" src="<%=webpath%>layout/sp.gif" alt="<%=roleAdminLocale.get("ACCOUNT_STATUS")%>"></td>
			   	<td scope="col" class="TBDATA_HEAD" nowrap><%=roleAdminLocale.get("NAME")%></td>
			   	<td scope="col" class="TBDATA_HEAD" nowrap><%=roleAdminLocale.get("DESCRIPTION_TABLE_HEADER")%></td>
			</tr>
		   	<%
			for (int j=0; j < length; j++)
		    {
		    	String displayName = parentPrincipals[j].getDisplayName();
			  	String uid = parentPrincipals[j].getUniqueID();
			  	String[] descriptions = parentPrincipals[j].getAttribute(IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DESCRIPTION);
				String description = "";
			  	if ((descriptions != null) && (descriptions.length > 0))
			  		description = descriptions[0];
				boolean isDirectParent = false;
				if ((directParentIDs != null) && (directParentIDs.contains( uid )))
					isDirectParent = true;
		        %>
				<tr class="<%= (j % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
	    	    	<td class="TBDATA_XXS_C" nowrap>
	        		<% if ( isDirectParent ) { %>
			    		<img src="<%=webpath%>layout/bulletgreen.gif" width="16" height="15" border="0" alt="<%=roleAdminLocale.get("DIRECT_ASSIGN")%>"></td>
		       		<% } else { %>
	    	   		    <img src="<%=webpath%>layout/bulletwhite.gif" width="16" height="15" border="0" alt="<%=roleAdminLocale.get("INDIRECT_ASSIGN")%>">
	   				<% } %>
			    	<td scope="row" class="TBDATA_XS_L" height="22"><UM:encode><%=displayName%></UM:encode></td>
			    	<td class="TBDATA_XXS_L" nowrap><UM:encode><%=description%></UM:encode></td>
				</tr>
			<% }
			if ( length > 0 ) { %>
				<%@ include file="/include/rolegroup_pagenavigation.jsp"%>
			<% } %>
			</table>
			</td></tr>
		</table>
		<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
		<!-- Start Page Action Buttons -->
		<table width="98%" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="100%" align="left" class="TBLO_XXS_L" nowrap>
					<input class="BTN_LN" type="submit" name="<%=UserAdminLogic.cancelViewRolesGroupsAction%>" 
						value="&nbsp;<%=userAdminLocale.get("BACK")%>&nbsp;"
				</td>
			</tr>
   		</table>

<input type="hidden" name="principalType" value="<UM:encode><%=principalType%></UM:encode>">
<input type="hidden" name="uniqueID" value="<%=util.URLEncoder( user.getUniqueID() )%>">

</form>
<%@ include file="/contextspecific_includes_bottom.txt" %>



