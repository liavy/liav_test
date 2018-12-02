<%@ include file="proxy.txt" %>
<%@ page session="true"%>
<jsp:useBean id="throwable"
             class="java.lang.Throwable"
             scope="request"/>

<%if (!inPortal) {%> 
<html>
<HEAD>
  <TITLE><%=userAdminLocale.get("EXCEPTION_PAGE_HEADER")%></TITLE>
  <script language="JavaScript" src="js/basic.js"></script>
</head> 
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("NOACCESS_PAGE_HEADER")%></span>
      </td></tr>
</table>

<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
        <IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
    <tr><td width="100%" class="TBLO_XXS_L">
        <span tabindex="0"><%=userAdminLocale.get("NOACCESS_DESCRIPTION")%></span>
    </td></tr>
</table>

<% String permissionRequired = (String)proxy.getRequestAttribute(com.sap.security.core.admin.UserAdminCommonLogic.PERMISSION_NAMES);
   if ( null != permissionRequired ) 
   { %>
	<table cellpadding="0" cellspacing="0" border="0">
	    <tr><td>
	    <IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
	    </td></tr>
	</table>
	
	
	<table cellpadding="0" cellspacing="0" border="0">
	    <tr><td class="TX_XS" width="100%">
        	<span tabindex="0"><%=userAdminLocale.get("REQUIRED_PERMISSIONS_ARE")%>&nbsp;<%=permissionRequired%></span>
	    </td></tr>
	</table>
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
<tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
</td></tr>
</table>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>



