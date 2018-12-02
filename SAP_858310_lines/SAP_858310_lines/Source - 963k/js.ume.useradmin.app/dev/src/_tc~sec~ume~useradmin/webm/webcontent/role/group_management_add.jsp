<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="com.sap.security.core.role.*" %>
<%@ page import="com.sap.security.core.role.imp.*" %>
<%@ page import="com.sap.security.core.*" %>
<%@ page import="com.sap.security.core.admin.util" %>
<%@ page import="com.sap.security.core.admin.role.*" %>
<%@ page import="com.sap.security.core.admin.group.*" %>
<%@page import="com.sap.security.api.*"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.*" %>
<%@ include file="/proxy.txt" %>


<jsp:useBean id="info" class="com.sap.security.core.util.InfoBean" scope='request'/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope="request"/>
<%com.sap.security.core.admin.role.RoleAdminMessagesBean roleAdminMessages = (com.sap.security.core.admin.role.RoleAdminMessagesBean) proxy.getSessionAttribute("roleAdminMessages");%>
<%com.sap.security.core.admin.role.RoleAdminLocaleBean roleAdminLocale = (com.sap.security.core.admin.role.RoleAdminLocaleBean) proxy.getSessionAttribute("roleAdminLocale");%>

<% 
String groupDisplayName = "";
String groupNamesReport = (String) HelperClass.getAttr(proxy,"groupNamesReport");
String groupID = "";
String groupDescription = "";

String principal = (String)HelperClass.getAttr(proxy,"principal");
IGroup group = (IGroup)proxy.getRequestAttribute("groupObj");
if( null != group)
{
   groupDisplayName = group.getUniqueName();
   groupID = group.getUniqueID();
   groupDescription = group.getDescription();
   if (null == groupDescription)
   {
   	groupDescription = "";
   }
}

String disabled = "";

String modifyGroup = (String)HelperClass.getAttr(proxy,"modifyGroup");
String Header;
if ("true".equals(modifyGroup))
{
	Header = roleAdminLocale.get("MODIFY_GROUP_HEADER");
	disabled = "disabled";
}
else {
	Header = roleAdminLocale.get("CREATE_GROUP_HEADER");
}

Boolean tempReadOnly = (Boolean)proxy.getRequestAttribute("readOnly");
boolean readonly = (tempReadOnly!=null)?tempReadOnly.booleanValue():false;

int  maxGroupNameLength = GroupAdminLogic.MAX_GROUP_NAME_LENGTH;
int  maxGroupDescriptionLength = GroupAdminLogic.MAX_GROUP_DESCRIPTION_LENGTH;

%>

<%if (!inPortal) {%> <html>
<head>
<title><%=roleAdminLocale.get("ROLE_MANAGEMENT")%></title>
<script language="JavaScript" src="<%=webpath%>js/util-role.js"></script>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
</head> 
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<script language="JavaScript">
function setCmd(value)
{
  document.frmRoleInfo.cmd.value = value;
}
</script>

<form onSubmit="return validateMyGroupForm('<%=util.convertDbQuotationToSingle(roleAdminLocale.get("BLANK_NOTIFICATION"))%>','<%=roleAdminLocale.get("ILLEGAL_GROUP_NAME")%>','groupname');" name="frmRoleInfo" action="<%=groupAdminAlias%>" method="post">
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

	<TABLE cellpadding="2" cellspacing="0" border="0" width="98%" id="h0">
	   <tr><td colspan="2"><IMG height="10" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	   <tr>
 	      <td class="TBLO_XXS_R" nowrap><LABEL FOR="GroupName"><%=roleAdminLocale.get("GROUP_NAME")%></LABEL></td>
	      <td class="TX_XXS" nowrap><input id="GroupName" name = groupname value="<UM:encode><%=groupDisplayName%></UM:encode>" <%=disabled%> type="text" size="40" maxlength=<%=maxGroupNameLength%>"></td>
	   </tr>
    <% if (disabled.equals("disabled"))	{ %>
		<input type="hidden" name="groupname" value="<UM:encode><%=groupDisplayName%></UM:encode>">
    <% } %>
	<tr>
	      <td class="TBLO_XXS_R" nowrap width="25%" valign="top"><LABEL FOR="Description"><%=roleAdminLocale.get("DESCRIPTION")%></LABEL>&nbsp;</td>
	      <td nowrap>
	      <textarea id="Description" 
	    	      class="TX_XXS" 
	              name = groupDescription 
	              wrap="soft" 
	              onkeyup="textCounter(this.form.groupDescription,<%=maxGroupDescriptionLength%>);" 
	              onkeypress="limitText(this.form.groupDescription,<%=maxGroupDescriptionLength%>);" 
	              cols="40" 
	              rows="5" 
	              style="width: 3.5in" name="terms"><%=util.filteringSpecialChar(groupDescription)%></textarea>
	     </td>
	  </tr>
	  <tr>
	     <td class="TBLO_XXS_R" nowrap valign="top" colspan="2">
	        <table width="98%" border="0" cellspacing="1" cellpadding="2">
   	           <tr>
		      <td colspan="5" nowrap><IMG height=3 src="<%=webpath%>layout/sp.gif" border="0" width="1" alt=""></td>
	           </tr>
	           <tr><td class="TX_XS" align="right" valign="top"></td></tr>
	       </table>
	    </td>
	  </tr>
      </table>
	<!-- end data table -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
	<!-- Start Page Action Buttons -->
	<table width="98%" border="0" cellpadding="0" cellspacing="0">
  	   <tr>
		<td width="100%" align="left" class="TBLO_XXS_L" nowrap>
		<% if (!readonly) { %>
		   <input class="BTN_LB" type="submit" value="&nbsp;<%=roleAdminLocale.get("APPLY")%>"&nbsp;
                   onClick="setCmd('group-management-doupdate')">&nbsp
		<% } %>
		   <input class="BTN_LN" type="submit" name="cancel" value="&nbsp;<%=roleAdminLocale.get("CANCEL")%>"&nbsp;
		   onClick="document.frmRoleInfo.cmd.value='group-abort'">
		</td>
	   </tr>
	 </table>
<input type="hidden" name="cmd" value="">
<input type="hidden" name="modifyGroup" value="<UM:encode><%=modifyGroup%></UM:encode>">
<input type="hidden" name="groupID" value="<%=util.URLEncoder(groupID)%>">
<input type="hidden" name="groupNamesReport" value="<UM:encode><%=groupNamesReport%></UM:encode>">
<input type="hidden" name="group" value="true">
<input type="hidden" name="principal" value="<UM:encode><%=principal%></UM:encode>">
</form>
<%@ include file="/contextspecific_includes_bottom.txt" %>


