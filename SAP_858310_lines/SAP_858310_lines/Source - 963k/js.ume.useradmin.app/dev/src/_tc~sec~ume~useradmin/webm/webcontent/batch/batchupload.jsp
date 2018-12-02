<%@ page import="com.sap.security.core.admin.batch.*" %>
<%@ page session="true"%>
<%@ page import = "com.sap.security.core.*" %>
<%@ page import = "com.sap.security.core.util.batch.*" %>
<%@ include file="/proxy.txt" %>

<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

<%-- start html--%>
<%if (!inPortal) {%> <html>
<head>
<TITLE><%=userAdminLocale.get("BATCH_UPLOAD_MANAGEMENT")%></TITLE>
<!--link rel="stylesheet" href="css/main2.css" -->
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
</head> 

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<form name="batchform" method="POST" action="<%=batchAlias%>" <% if (inPortal) {%> ENCTYPE="multipart/form-data"<%}%> >
<%@ include file="/contextspecific_includes_top.txt" %>



<%
Exception ex = (Exception) (proxy.getRequestAttribute("exception"));

if (ex != null)
  {
%>

<h2><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(ex.getMessage())%></h2>

<%

}
else
{%>

<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=userAdminLocale.get("BATCH_UPLOAD_MANAGEMENT")%></td></tr>
</table>

<!-- End Section Header -->
<!-- Start 5px spacing -->
<table cellpadding="0" cellspacing="0" border="0">
	<tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" alt="" border="0"></td></tr>
</table>
<!-- End 5px spacing -->	


<table>
<!-- tr><td width="100%" class="TBLO_XXS_L"><%=userAdminLocale.get("PASTE_OR_FILE")%></td></tr -->
</table>
<!-- Start 10px spacing -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="10" alt="" border="0"></td></tr></table>
<!-- End 10px spacing -->	
<%--
   if ( info.isInfo() )
   {
%>
<p>
<font class="TX_ERROR_XSB"><%=userAdminMessages.print(info.getMessage())%></font>
<p>
<% } --%>
<!-- Start 10px spacing -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="10" alt="" border="0"></td></tr></table>
<!-- End 10px spacing -->	

<table cellpadding="2" cellspacing="0" border="0" width="100%">
<%if (inPortal) {%>
	<tr>
		<td class="TBLO_XSB" colspan="2"><%=userAdminLocale.get("UPLOAD_USER_DATA_FILE")%></td>
	</tr>
	<tr>
		<td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="File"><%=userAdminLocale.get("FILE_LOCATION")%></LABEL></td>
		<td class="TBLO_XXS_R" width="70%" nowrap><input name="file" id="File" type="file" size="20" style="width: 2.5in">&nbsp;</td>
	</tr>
<%}%>
</table>

<table cellpadding="0" cellspacing="0" border="0" width="100%"><tr><td><hr size="1" width="100%"></td></tr></table>

<table cellpadding="2" cellspacing="0" border="0" width="100%">
<tr>
	<!-- td class="TBLO_XSB" colspan="2"><%=userAdminLocale.get("OR_PASTE_DATA")%></td> -->
</tr>
<tr>
	<td class="TBLO_XXS_R" width="30%" nowrap valign="top"><LABEL FOR="PasteData"><%=userAdminLocale.get("PASTE_DATA")%></LABEL>&nbsp;</td>
	<td width="70%" nowrap><textarea name="<%=BatchLogic.textInputField%>" wrap="soft" cols="25" rows="15" style="width: 2.5in" CLASS="TBLO_XXB"></textarea></td>
</tr>
</table>
<!-- Start 10px spacing -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="10" alt="" border="0"></td></tr></table>
<!-- End 10px spacing -->	

<input type=hidden name="format" value="standard">
<br>
<table cellpadding="0" cellspacing="0" border="0">
	<tr><td class="TBLO_XS">
		<input name="overwrite" type="checkbox" name="Overwrite" class="noborder" id="Overwrite" value="">
		<LABEL FOR="Overwrite"><%=userAdminLocale.get("OVERWRITE_CHECKBOX_TEXT")%></LABEL>
	</td></tr>
</table>
<br>


<table cellpadding="0" cellspacing="0" width="99%" border="0">
   <tr><td align="left" nowrap class="TBLO_XXS_L">
	<input type="submit" class="BTN_LB" value="<%=userAdminLocale.get("UPLOAD_NOW")%> ">
	
   </td></tr>
</table>
</form>

<% } %>

<%@ include file="/contextspecific_includes_bottom.txt" %>


