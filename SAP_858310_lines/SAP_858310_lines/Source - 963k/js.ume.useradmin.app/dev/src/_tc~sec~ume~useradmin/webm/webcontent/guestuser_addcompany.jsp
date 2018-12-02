<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>

<%@ page import="com.sapmarkets.tpd.master.TradingPartnerInterface"%>

<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="companySelect"
             class="com.sap.security.core.admin.CompanySelectBean"
             scope="request"/>

<% String companyName = (String)proxy.getSessionAttribute(CompanySelectBean.companySearchNameId);
   boolean companyFound = false;
   boolean companyEmpty = false;
   TradingPartnerInterface[] companies = null;

   int currentPage = -1;
   int totalItems = -1;
   int totalOptions = -1;
   Integer[] itemPerPageOptions = null;
   int totalPages = -1;
   int currentItemPerPage = -1;

   if ( null != proxy.getSessionAttribute(CompanySelectBean.companiesId) ) {
       companyFound = true;
       java.util.Vector tmp = (java.util.Vector)proxy.getRequestAttribute(ListBean.objsPerPage);
       companies = (TradingPartnerInterface[]) tmp.toArray(new TradingPartnerInterface[1]);
       totalItems = ((Integer)proxy.getSessionAttribute(ListBean.totalItems)).intValue();;
       currentPage = ((Integer)proxy.getRequestAttribute(ListBean.currentPage)).intValue();
       totalPages = ((Integer)proxy.getRequestAttribute(ListBean.totalPages)).intValue();
       itemPerPageOptions = (Integer[])proxy.getRequestAttribute(ListBean.itemPerPageOptions);
       currentItemPerPage = ((Integer)proxy.getRequestAttribute(ListBean.currentItemPerPage)).intValue();
   } else {
       if ( ((Boolean)proxy.getSessionAttribute(SelfRegLogic.enableGuestReg)).equals(Boolean.FALSE) ) {
           companyEmpty = true;
       }
   }

   // prepare for page navigation
   int colspan = 7;
   StringBuffer sb = new StringBuffer(userAdminAlias);
   sb.append("?");
   sb.append(UserAdminLogic.performSearchResultNavigateAction);
   sb.append("=");
   String urlAndAction = new String(sb);
   String pageKey = UserAdminLogic.listPage;
   String pageName = UserAdminLogic.guestUserAddCompanyPage;
   String setListPage = pageKey + "=" + pageName;
%>

<%-- start html--%>
<%if (!inPortal)
{%> <html>
<head>
<TITLE><%=userAdminLocale.get("USER_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<form method="post" action="<%=userAdminAlias%>">
<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("SELECT_COMPANY")%></span>
      </td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(new Message("GUESTUSER_SLCT_COM_DESP", companyName))%></UM:encode></span>
  </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%" align="center">
  <tr><td>
    <table cellpadding="1" cellspacing="1" border="0" width="100%">
      <%@include file="/include/pagenavigation.jsp"%>
      <tr>
        <td class="TBDATA_HEAD" nowrap></td>
        <td scope="col" class="TBDATA_HEAD" tabindex="0" nowrap><%=userAdminLocale.get("COMPANY_NAME")%></td>
      </tr>

		  <% for(int i=0; i<companies.length; i++) { %>
			<tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
			  <td class="TBDATA_XXS_C" width="1%" nowrap>
			    <input type="radio"
		             name="<%=CompanySelectBean.companyIdId%>"
		             tabindex="0"
					 class="noborder"
				     value="<%=util.filteringSpecialChar(companies[i].getPartnerID().toString())%>"
				         <%=i==0?"checked":""%>>
        </td>
			  <td scope="row" tabindex="0" class="TBDATA_XS_L"><UM:encode><%=companies[i].getDisplayName()%></UM:encode></td>
		  </tr>
      <% } %>

      <%@include file="/include/pagenavigation.jsp"%>
  </table>
</td>
</tr>
</table>
<!-- end data table -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
</td></tr>
</table>

<!-- Start Page Action Buttons -->
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <% String accept = " "+userAdminLocale.get("ACCEPT")+" ";
       String search = userAdminLocale.get("SEARCHCOMPANY");
       String cancel = userAdminLocale.get("APPLYCOMPANYLATER"); %>
    <input type="submit"
           name="<%=UserAdminLogic.performGuestUsersApplyCompanyAction%>"
           tabindex="0"
           value="<%=accept%>"
           class="BTN_LB">&nbsp;
    <input type="submit"
           name="<%=UserAdminLogic.selectCompanyAction%>"
           tabindex="0"
           value="<%=search%>"
           class="BTN_LN">&nbsp;
    <input type="submit"
           name="<%=UserAdminLogic.performGuestUsersApplyCompanyLaterAction%>"
           tabindex="0"
           value="<%=cancel%>"
           class="BTN_LN">
  </td></tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

