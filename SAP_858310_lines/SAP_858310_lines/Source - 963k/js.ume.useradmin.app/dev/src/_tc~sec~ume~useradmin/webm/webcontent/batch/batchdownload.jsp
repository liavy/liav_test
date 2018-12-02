<%@ page import="com.sap.security.core.admin.batch.BatchLogic" %>
<%@ page session="true"%>
<%@ page import = "com.sap.security.core.*" %>
<%@ page import = "com.sap.security.core.util.batch.*" %>
<%@ page import = "com.sapmarkets.tpd.master.TradingPartnerInterface" %>
<%@ include file="/proxy.txt" %>

<jsp:useBean id="companyList"
             class="com.sap.security.core.admin.CompanyListBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

<%-- start html--%>
<%if (!inPortal) {%> <html>
<head>
<TITLE><%=userAdminLocale.get("BATCH_DOWNLOAD_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
</head> 
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<form name="batchform" method="POST" action="<%=batchAlias%>">
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
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=userAdminLocale.get("BATCH_DOWNLOAD_MANAGEMENT")%></td></tr>
</table>

<!-- End Section Header -->
<!-- Start 5px spacing -->
<table cellpadding="0" cellspacing="0" border="0">
	<tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" alt="" border="0"></td></tr>
</table>
<!-- End 5px spacing -->	

<%
   if ( info.isInfo() )
   {
%>
<b><font color=red><%=userAdminMessages.print(info.getMessage())%></font></b>
<% } %>

<%
   TradingPartnerInterface [] companies = companyList.getCompanies();
   //Boolean roleFlag=(Boolean)proxy.getRequestAttribute("roleFlag");
   //IUser performer= proxy.getActiveUser();
   //Locale locale = proxy.getLocale();
%>

<table cellpadding="2" cellspacing="1" border="0">
<tr>
<td class="TBLO_XXS_L" valign="left"><%=userAdminLocale.get("SELECT_ONE_OR_MORE_COMPANIES")%></td>
<td class="TBLO_XXS_L">
<select size="10" name=<%=CompanyListBean.selectedCidsId%> multiple>
<%if (performer.getCompany()==null || UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES))
{%>
<option value="*">< <%=userAdminLocale.get("ALL_USERS").toUpperCase()%> ></option>
<option value=""><%=userAdminLocale.get("INDIVIDUAL_USERS")%></option>
<%}%>

<% for (int i=0; i<companies.length; i++)
  {%>
<option value="<%=companies[i].getPartnerID().toString()%>"><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(companies[i].getDisplayName())%></option>
<%}%>
</select>
</td>
</tr>
</table>
<!-- Start 30px spacing -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="30" alt="" border="0"></td></tr></table>
<!-- End 30px spacing -->	

<table cellpadding="0" cellspacing="0" border="0" width="99%">
   <tr><td align="left" nowrap class="TBLO_XXS_L">
	<input class="BTN_LN" type="submit" value="<%=userAdminLocale.get("DOWNLOAD_USERS_NOW")%>" name="<%=BatchLogic.performDownloadAction%>"></p>
    </td></tr>
</table>
</form>
<% } %>

<%@ include file="/contextspecific_includes_bottom.txt" %>


