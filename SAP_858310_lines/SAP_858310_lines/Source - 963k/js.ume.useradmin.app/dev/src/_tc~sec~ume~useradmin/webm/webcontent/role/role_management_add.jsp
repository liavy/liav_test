<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="com.sap.security.core.role.*" %>
<%@ page import="com.sap.security.core.role.imp.*" %>
<%@ page import="com.sap.security.core.*" %>
<%@ page import="com.sap.security.core.admin.util" %>
<%@ page import="com.sap.security.core.admin.role.*" %>
<%@ page import="com.sap.security.api.*" %>
<%@ page import="java.util.Iterator" %>
<%@ include file="/proxy.txt" %>


<jsp:useBean id="info" class="com.sap.security.core.util.InfoBean" scope='request'/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope="request"/>
<%com.sap.security.core.admin.role.RoleAdminMessagesBean roleAdminMessages = (com.sap.security.core.admin.role.RoleAdminMessagesBean) proxy.getSessionAttribute("roleAdminMessages");%>
<%com.sap.security.core.admin.role.RoleAdminLocaleBean roleAdminLocale = (com.sap.security.core.admin.role.RoleAdminLocaleBean) proxy.getSessionAttribute("roleAdminLocale");%>

<% 
Iterator actions = (Iterator)proxy.getRequestAttribute("allActions");
Iterator assignedActions = (Iterator)proxy.getRequestAttribute("assignedActions");
String[] unavailableActions = (String[])proxy.getRequestAttribute("unavailableActions");

String actionDisplayName = "";
String roleDisplayName = "";
String rolenamesReport = (String) HelperClass.getAttr(proxy,"rolenamesReport");
String roleID = "";
String roleDescription = "";

int  MaxRoleNameLength = RoleAdminServlet.MAX_ROLE_NAME_LENGTH;
int  MaxRoleDescriptionLength = RoleAdminServlet.MAX_ROLE_DESCRIPTION_LENGTH;

String principal = (String)HelperClass.getAttr(proxy,"principal");
IRole role = (IRole)proxy.getRequestAttribute("roleObj");
if( null != role)
{
   roleDisplayName = role.getDisplayName();
   roleID = role.getUniqueID();
   roleDescription = role.getDescription();
   if(null == roleDescription)
   {
   	roleDescription = "";
   }
}


String disabled = "";
IAction action=null;

String ID = (String)HelperClass.getAttr(proxy,"ID");
String modifyRole = (String)HelperClass.getAttr(proxy,"modifyRole");
String Header;
if("true".equals(modifyRole))
{
	Header = roleAdminLocale.get("MODIFY_ROLE_HEADER");
	disabled = "disabled";

}
else {
	Header = roleAdminLocale.get("CREATE_ROLE_HEADER");
}
String actionUniqueID = "";
%>
<script language="JavaScript">
function setCmd(value)
{
  document.frmRoleInfo.cmd.value = value;
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

<%if (!inPortal) {%> <html>
<head>
<title><%=roleAdminLocale.get("ROLE_MANAGEMENT")%></title>
<script language="JavaScript" src="<%=webpath%>js/util-role.js"></script>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
</head> <%}%>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="javascript:init(window.document.forms['frmRoleInfo'],'allActions','assignedActions')">
<form onSubmit='return validateMyForm("<%=util.convertDbQuotationToSingle(roleAdminLocale.get("BLANK_NOTIFICATION"))%>","<%=roleAdminLocale.get("ILLEGAL_ROLE_NAME")%>");' name="frmRoleInfo" action="<%=RoleAdminServlet.alias%>" method="post">
<%@ include file="/contextspecific_includes_top.txt" %>

<center>
<table cellpadding="0" align="center" cellspacing="0" border="0" width="99%" id="1">
	<a name="main"></a>
	
	<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
	      <tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=Header%></td></tr>
	</table>

	<!-- End Section Header -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
	<!-- Start Section Description -->
	<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
	<tr>
	    <td width="100%" class="TBLO_XXS_L"></td></tr>
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
	<!-- Start Error Msg-->
	<% if ( error.isError() ) { %>
	<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
	 	<tr><td width="100%" class="TX_ERROR_XSB">
	    	<img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;<UM:encode><%=roleAdminMessages.print(error.getMessage())%></UM:encode>
	  	</td></tr>
	</table>
	<% } %>
	<TABLE cellpadding="2" cellspacing="0" border="0" width="98%" id="h0">
	<tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	<tr>
 	    <td class="TBLO_XXS_R" nowrap><LABEL FOR="RoleName"><%=roleAdminLocale.get("ROLE_NAME")%></LABEL></td>
	    <td class="TX_XXS" nowrap><input id="RoleName" name = rolename value="<UM:encode><%=roleDisplayName%></UM:encode>" <%=disabled%> type="text" size="40" maxlength=<%=MaxRoleNameLength%>"></td>
	</tr>
    <% if (disabled.equals("disabled"))	{ %>
		<input type="hidden" name="rolename" value="<UM:encode><%=roleDisplayName%></UM:encode>">
    <% } %>
	<tr>
	    <td class="TBLO_XXS_R" nowrap width="25%" valign="top"><LABEL FOR="Description"><%=roleAdminLocale.get("DESCRIPTION")%></LABEL>&nbsp;</td>
	    <td nowrap>
	    <textarea id="Description" 
	    	      class="TX_XXS" 
	              name = roleDescription 
	              wrap="soft" 
	              onkeyup="textCounter(this.form.roleDescription,<%=MaxRoleDescriptionLength%>);" 
	              onkeypress="limitText(this.form.roleDescription,<%=MaxRoleDescriptionLength%>);" 
	              cols="40" 
	              rows="3" 
	              style="width: 3.5in" name="terms"><%=util.filteringSpecialChar(roleDescription)%></textarea>
	   </td>
	</tr>
	<tr>
	    <td class="TBLO_XXS_R" nowrap valign="top" colspan="2">
	    <table width="98%" border="0" cellspacing="1" cellpadding="2">
   	    <tr>
		<td colspan="5" nowrap><IMG height=10 src="<%=webpath%>layout/sp.gif" border="0" width="1" alt=""></td>
	    </tr>
	    <tr><td colspan="5" class="TBLO_XS"><%=roleAdminLocale.get("SELECT_ACTIONS")%></td></tr>
	    <tr>
		<td align="right" class="TBLO_XS"><LABEL FOR="AvailActions"><%=roleAdminLocale.get("AVAILABLE_ACTIONS")%></LABEL></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td align="left" class="TBLO_XS"><LABEL FOR="SelectActions"><%=roleAdminLocale.get("ASSIGNED_ACTIONS")%></LABEL></td>
	    </tr>
	    <tr>
	        <td class="TX_XS" align="right" valign="top">
		<select id="AvailActions" name="allActions" style="min-width: 6cm; overflow: visible" size="10" multiple>
	    <%
		if (actions != null)
		{
			while (actions.hasNext())
			{
				action = (IAction) actions.next();
		   		if(null != action)
				{
	            	actionDisplayName = action.toString();
	                actionUniqueID = action.getUniqueID();
	            }
	    %>
		<option value="<UM:encode><%=actionUniqueID%></UM:encode>"><UM:encode><%= actionDisplayName%></UM:encode></option>
	    <%	}
	    } %>
		</select><br>
		</td>
		<td>&nbsp;</td>
		<td class="TBLO_XXS_C" valign="middle">
		   <input type="button" onclick="javascript:move(window.document.forms['frmRoleInfo'],'allActions', 'assignedActions')" name="add" style="width= .9in" value="&nbsp;&nbsp;&nbsp;<%=roleAdminLocale.get("ADD")%> &gt;&nbsp;" class="BTN_S"><br><IMG height=3 src="<%=webpath%>layout/sp.gif" border="0" width="1" alt=""><BR>
		   <input type="button" onclick="javascript:moveAll(window.document.forms['frmRoleInfo'],'allActions', 'assignedActions')" name="addAll" style="width= .9in" value="<%=roleAdminLocale.get("ADD_ALL")%> &gt;&gt;" class="BTN_S"><br><IMG height=9 src="<%=webpath%>layout/sp.gif" border="0" width="1" alt=""><BR>
		   <input type="button" onclick="javascript:move(window.document.forms['frmRoleInfo'],'assignedActions', 'allActions')" name="remove" style="width= .9in" value="&lt; <%=roleAdminLocale.get("REMOVE")%>" class="BTN_S"><br><IMG height=3 src="<%=webpath%>layout/sp.gif" border="0" width="1" alt=""><BR>
		   <input type="button" onclick="javascript:moveAll(window.document.forms['frmRoleInfo'],'assignedActions', 'allActions')" name="removeAll" style="width= .9in" value="&lt;&lt; <%=roleAdminLocale.get("REMOVE_ALL")%>" class="BTN_S">
		</td>
		<td>&nbsp;</td>
		<td class="TX_XS" valign="top">
		   <select id="SelectActions" name="assignedActions" style="min-width: 6cm; overflow: visible" size="10" multiple>
		<%
	    if (assignedActions != null)
	    {
			while (assignedActions.hasNext())
			{
    			    action = (IAction) assignedActions.next();
    			    if(null != action)
    			    {
                        	actionDisplayName = action.toString();
                                actionUniqueID = action.getUniqueID();
                            }
		%>
		    <option value="<UM:encode><%=actionUniqueID%></UM:encode>"><UM:encode><%=actionDisplayName%></UM:encode></option>
		<%  }
		}
	    if (unavailableActions != null)
	    {
			for (int i=0; i<unavailableActions.length; i++ )
			{
				actionUniqueID = UpdateCommand.UNAV_ACTION_PREFIX + unavailableActions[i];
    			actionDisplayName = unavailableActions[i];
		%>
		    <option value="<UM:encode><%=actionUniqueID%></UM:encode>"><UM:encode><%=actionDisplayName%></UM:encode></option>
		<%  }
		}%>
		    </select>
	</td></tr>
	</table>
	</td></tr></table>
<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
	<!-- end data table -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
	<!-- Start Page Action Buttons -->
	<table width="98%" border="0" cellpadding="0" cellspacing="0">
  	   <tr>
		<td width="100%" align="left" class="TBLO_XXS_L" nowrap>
		   <input class="BTN_LB" type="submit" value="&nbsp;<%=roleAdminLocale.get("APPLY")%>"&nbsp;
                   onClick="saveActions(window.document.forms['frmRoleInfo'],'assignedActions','role-management-doupdate')">&nbsp
		   <input class="BTN_LN" type="submit" name="cancel" value="&nbsp;<%=roleAdminLocale.get("CANCEL")%>"&nbsp;
		   onClick="document.frmRoleInfo.cmd.value='abort'">
		</td>
	   </tr>
	 </table>
<input type="hidden" name="cmd" value="">
<input type="hidden" name="ID" value="<UM:encode><%=ID%></UM:encode>">
<input type="hidden" name="modifyRole" value="<%=modifyRole%>">
<input type="hidden" name="roleID" value="<%=util.URLEncoder(roleID)%>">
<input type="hidden" name="rolenamesReport" value="<UM:encode><%=rolenamesReport%></UM:encode>">
<input type="hidden" name="group" value="true">
<input type="hidden" name="principal" value="<UM:encode><%=principal%></UM:encode>">
</form>
<%@ include file="/contextspecific_includes_bottom.txt" %>


