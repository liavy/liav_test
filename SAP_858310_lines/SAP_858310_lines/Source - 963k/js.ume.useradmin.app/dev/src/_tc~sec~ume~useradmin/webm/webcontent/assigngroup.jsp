<%@ taglib uri="UM" prefix="UM" %>
<%@ page session="true"%>
<%@ page import = "com.sapmarkets.tpd.master.TradingPartnerInterface" %>
<%@ page import = "com.sap.security.core.*" %>
<%@ page import="com.sap.security.api.IUser"%>
<%@ page import = "com.sap.security.core.role.*" %>
<%@ page import = "java.util.*" %>
<%@ include file="proxy.txt" %>

<%CompanyListBean companyList = (CompanyListBean) proxy.getSessionAttribute(CompanyListBean.beanId);%>

<% TradingPartnerInterface [] companies = companyList.getPagedCompanies();
   Boolean roleFlag=(Boolean)proxy.getRequestAttribute("roleFlag");
   java.util.Locale locale = proxy.getLocale();   
   if ( null == locale ) locale = java.util.Locale.getDefault();
%>

<% if (!inPortal) { %>
<HTML>
<HEAD>
<TITLE><%=userAdminLocale.get("ASSIGN_COMPANY_TO_GROUP")%></TITLE>
<!--link rel="stylesheet" href="css/main2.css" -->
<script language="JavaScript" src="js/basic.js"></script>
</HEAD>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<%@ include file="contextspecific_includes_top.txt" %>

<!-- Start Section Header -->
<a name="main"></a>
<form method="post" action="<%=companyListAlias%>">
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
        <span tabindex="0"><%=userAdminLocale.get("ASSIGN_COMPANY_TO_GROUP")%></span>
      </td></tr>
</table>
<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<table>
<tr>
<td>
<%
java.util.Iterator groupsIt = companyList.getServiceRepository().getAllGroupDefinitions();

String cid = (String)(proxy.getRequestAttribute(CompanySelectBean.companyIdId));
boolean derived = false;

TradingPartnerInterface companyobject = null;

if (cid.length()!=0) {
    companyobject = com.sapmarkets.tpd.TradingPartnerDirectoryCommon.getTPD().getPartner(cid);
}

IGroupDefinition pgroup = companyList.getDefinitionOfOwnGroup(companyobject);
if (pgroup == null) {
    pgroup = companyList.getDefinitionOfDerivedGroup(companyobject);
    derived = true;
}

//if pgroup is still null, there is a problem with groups so no page is displayed
if (pgroup != null) {
    if (companyobject!=null) {
%>

    <table class="SEC_TB_BDR" cellspacing="0" cellpadding="2" width="98%" border="0" >
    <tr>
        <td class="TBDATA_XSB_NBG" nowrap><span tabindex="0">&nbsp;<%=userAdminLocale.get("ASSIGN_COMPANY")%>
                          &nbsp;<UM:encode><%=companyobject.getDisplayName()%></UM:encode>
                          &nbsp;<%=userAdminLocale.get("TO_GROUP")%></span>
        </td>
    </tr>

    <% } else { %>
	<td class="TBDATA_XSB_NBG" tabindex="0" nowrap>&nbsp;<%=userAdminLocale.get("ASSIGN")%> </td>

	<td class="TBDATA_XSB_NBG" tabindex="0" nowrap>&nbsp;<%=userAdminLocale.get("INDIVIDUAL_USERS")%></td>
	<td class="TBDATA_XSB_NBG" tabindex="0" nowrap>&nbsp;<%=userAdminLocale.get("TO_GROUP")%></td>
    <%}%>

	<td class="box">
    <table border=0 cellspacing=0 cellpadding=2>
	<tr>
	   	<td>
		<table cellspacing="0" cellpadding="0" border="0">
	    <tr>
			<td class="TBLO_XS_L">
			<select name="<%=CompanyListBean.selectedGroupNameId%>" tabindex="0" class="DROPDOWN">
				<option <%=derived?"selected":""%> value="">
					<%=userAdminLocale.get("DERIVE_FROM_EBP")%> <UM:encode><%="("+companyList.getDefinitionOfExternalGroup(companyobject).getDescription(locale)+")"%></UM:encode>
				</option>
	<% IGroupDefinition group = null;
	   while (groupsIt.hasNext()) {
	       group = (IGroupDefinition) groupsIt.next(); %>
		   <option value="<%=util.filteringSpecialChar(group.getName())%>"<%if(group.getName().equals(pgroup.getName()) && !derived){%>"selected"<%}%>><UM:encode><%=group.getDescription(locale)%></UM:encode></option>
		<%}%>
		</select>
      		</td>
		</tr>
		</table>
		</td>
	</tr>
	</table>
    </td>
</tr>
</table>

<!-- start 30px spacing -->
<table cellpadding="0" cellspacing="0" border="0">
<tr>
	<td><img src="<%=webpath%>layout/sp.gif" width="1" height="30" alt="" border="0"></td>
</tr>
</table>
<!-- end 30px spacing -->

<table cellspacing="0" cellpadding="0" border="0" width="98%">
<tr>
	<td class="TBLO_XXS_L" width="100%">
		<input type="hidden" name="<%=CompanyListLogic.performAssignGroupAction%>" value="">
		<input type="hidden" name="<%=CompanySelectBean.companyIdId%>" value="<%=util.filteringSpecialChar(cid)%>">
		<input type="submit" tabindex="0" name="" value="<%=userAdminLocale.get("ASSIGN_TO_GROUP")%>" class="BTN_LN">
		<input type="submit" tabindex="0" name="cancel" value="<%=userAdminLocale.get("CANCEL")%>" class="BTN_LN">
	</td>
</tr>
</table>
<% } %>
</form>

<%@ include file="contextspecific_includes_bottom.txt" %>

