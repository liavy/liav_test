<%@ taglib uri="UM" prefix="UM" %>
<%@ page session="true"%>
<%@ page import = "com.sapmarkets.tpd.master.TradingPartnerInterface" %>
<%@ page import = "com.sap.security.core.*" %>
<%@ page import="com.sap.security.api.IUser"%>
<%@ page import = "com.sap.security.core.admin.role.RoleAdminServlet" %>
<%@ page import = "com.sap.security.core.role.*" %>
<%@ page import = "java.util.*" %>
<%@ include file="proxy.txt" %>

<%com.sap.security.core.admin.CompanyListBean companyList = (com.sap.security.core.admin.CompanyListBean) proxy.getSessionAttribute("companyList");%>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

<%
   TradingPartnerInterface [] companies = companyList.getPagedCompanies();
   int csize = companies.length;
   Boolean roleFlag=(Boolean)proxy.getRequestAttribute("roleFlag");
   //IUser performer= proxy.getActiveUser();
   Locale locale = proxy.getLocale();
   if ( null == locale ) locale = Locale.getDefault();
   int currentPage = companyList.getCurrentPage();
   int linesNo = companyList.getCurrentLines();
   IGroupDefinition group = null;

%>

<%if (!inPortal) {%>
<HTML>
<HEAD>
<TITLE><%=userAdminLocale.get("COMPANY_LIST")%></TITLE>
<!--link rel="stylesheet" href="css/main2.css" -->
<script language="JavaScript" src="js/basic.js"></script>
</HEAD>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<form id="listform" method="post" action="<%=companyListAlias%>">

<%@ include file="contextspecific_includes_top.txt" %>

<script language="JavaScript">
function setLinesNo()
{
    var frm, _linesNoObj, linesNoObj, itemString;

    if( -1 != navigator.userAgent.indexOf("MSIE") ) {
        frm = document.getElementById("listform");
        _linesNoObj = document.all["_linesNo"];
         linesNoObj = document.all["linesNo"];
    } else {
        frm = document.listform;
        _linesNoObj = frm.elements["_linesNo"];
         linesNoObj = frm.elements["linesNo"];
    }

    linesNoObj.selectedIndex=_linesNoObj.selectedIndex;
    linesNoObj.text =  _linesNoObj.options[_linesNoObj.selectedIndex].text;
    frm.submit();
}

function setPageNo()
{
    var frm, _pageNoObj, pageNoObj, itemString;

    if( -1 != navigator.userAgent.indexOf("MSIE") ) {
        frm = document.getElementById("listform");
        _pageNoObj = document.all["_pageNo"];
         pageNoObj = document.all["pageNo"];
    } else {
        frm = document.listform;
        _linesNoObj = frm.elements["_pageNo"];
         linesNoObj = frm.elements["pageNo"];
    }

    pageNoObj.selectedIndex=_pageNoObj.selectedIndex;
    pageNoObj.text =  _pageNoObj.options[_pageNoObj.selectedIndex].text;
    frm.submit();
}

</script>

<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("COMPANY_LIST")%></span>
      </td></tr>
</table>
<p>

<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
<input type="submit" tabindex="0" class="BTN_LN" name="<%=CompanyListLogic.searchAgainAction%>" value="<%=userAdminLocale.get("COMP_SELECT_AGAIN")%>">
</td></tr>
</table>

<!-- End Section Header -->
<!-- Start 5px spacing -->
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" alt="" border="0"></td></tr></table>
<!-- End 5px spacing -->

<% if ( info.isInfo() ) { %>
	      <table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
		<tr><td class="TX_CFM_XSB">
		  <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span></td>
  		</tr>
  	      </table>
    	     <table cellpadding="0" cellspacing="0" border="0"><tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr></table>
<% } %>

<table align="center" cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" width="98%">
<tr>
	<td>
    	<table id="srtable" cellpadding="1" cellspacing="1" border="0" width="100%">
<!-- Start Page Navigation -->
		<tr>
			<td class="NAV_PGNB" colspan="7" width="100%">

&nbsp;&nbsp;&nbsp;
<A
<%
 if (currentPage>1)
{
%>
href="<%=util.alias(proxy, CompanyListLogic.servlet_name, CompanyListLogic.component_name)%>?<%=CompanyListLogic.listCompaniesAction%>=&<%=CompanyListLogic.pageNo%>=<%=currentPage-1%>&<%=CompanyListLogic.linesNo%>=<%=linesNo%>">
<%}%>
			<img src="<%=webpath%>layout/leftnull.gif"
     			width="14"
     			height="13"
     			border="0"
     			tabindex="0"
     			alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>"
     			title="<%=userAdminLocale.get("PREVIOUS_PAGE")%>">
</A>&nbsp;&nbsp;&nbsp;
            <span tabindex="0">     
 			<%=userAdminLocale.get("DISPLAY")%></span>
 			 <select tabindex="0" name=<%=CompanyListLogic.linesNo%> onChange='submit();' class="DROPDOWN">
  			<%for (int i = 10; i<companyList.getNumberOfCompanies(); i=i+10)
			{%>
				<option <%=(linesNo==i)?"selected":""%>><%=i%></option>
			<%}%>
				<option <%=(linesNo==companyList.getNumberOfCompanies())?"selected":""%>><%=companyList.getNumberOfCompanies()%></option>
			</select>

			<span tabindex="0"><%=userAdminLocale.get("PER_PAGE")%>&nbsp;&nbsp;&nbsp;&nbsp;<%=userAdminLocale.get("THIS_IS")%></span>&nbsp;
			<select name=<%=CompanyListLogic.pageNo%> tabindex="0" onChange='submit();' class="DROPDOWN">
			<%for (int i = 1; i<=companyList.getNumberOfPages(); i++)
			 {%>
	 		  	<option <%=(currentPage==i)?"selected":""%>><%=i%></option>
			<%}%>
			</select>
				&nbsp;<span tabindex="0"><%=userAdminLocale.get("OF")%>
				<%=companyList.getNumberOfPages()%> <%=userAdminLocale.get("PAGES")%></span>

&nbsp;&nbsp;&nbsp;
<A
			<%
			if (currentPage<companyList.getNumberOfPages())
			{
			%>
				href="<%=util.alias(proxy, CompanyListLogic.servlet_name, CompanyListLogic.component_name)%>?<%=CompanyListLogic.listCompaniesAction%>=&<%=CompanyListLogic.pageNo%>=<%=currentPage+1%>&<%=CompanyListLogic.linesNo%>=<%=linesNo%>">
			<%}%>
			<img src="<%=webpath%>layout/right.gif"
     			 width="14"
    		  	 height="13"
     			 border="0"
     			 tabindex="0"
     			 alt="<%=userAdminLocale.get("NEXT_PAGE")%>"
     			 title="<%=userAdminLocale.get("NEXT_PAGE")%>">
</A>&nbsp;&nbsp;&nbsp;

 </td>
</tr>

<!-- End Page Navigation -->
<!-- Start Table Header row -->
<tr>
  	<td scope="col" class="TBDATA_HEAD" NOWRAP><%=userAdminLocale.get("COMPANY_NAME")%></td>
  	<td class="TBDATA_HEAD" tabindex="0" NOWRAP><%=userAdminLocale.get("GROUP")%></td>
  	<td class="TBDATA_HEAD" tabindex="0" NOWRAP><%=userAdminLocale.get("ACTIONS")%></td>
</tr>
<!-- End Table Header row -->
<!-- Start Table contents -->

<%-- listing the virtual company which individual users belong to --%>

<!---------------------------------------------------------------------------------------------->
<%
   boolean derived=false;
   for ( int i = 0; i < companies.length; i++ )
   {
    String partnerid = "";
    if (companies[i]!=null)  partnerid = companies[i].getPartnerID().toString();
    derived = false;
%>

 <tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
  <%
  if (companies[i]!=null)
  {
  %>
    <td scope="row" tabindex="0" class="TBDATA_XXS_L" NOWRAP><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(companies [i].getDisplayName())%>
	    <INPUT type="hidden" name="<%=companyList.cidsId%>" value="<%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(partnerid)%>">
    </td>
  <%}
  else
  {%>
    <td class="TBDATA_XXS_L" tabindex="0" NOWRAP><%=userAdminLocale.get("INDIVIDUAL_USERS")%>
	    <INPUT type="hidden" name="<%=companyList.cidsId%>" value="">
    </td>
  <%}%>


    <td class="TBDATA_XXS_L" tabindex="0">
	 <%
	    group = companyList.getDefinitionOfOwnGroup(companies[i]);
	    if (group==null)
	    {
	         derived = true;
	    		group = companyList.getDefinitionOfDerivedGroup(companies[i]);
	    }
    %>
    <b> <A  href_redo_to_SP2="<%=util.alias(proxy, CompanyListLogic.servlet_name, CompanyListLogic.component_name)%>?<%=CompanyListLogic.assignGroupAction%>=&<%=CompanySelectBean.companyIdId%>=<%=
partnerid%>"><% if (group!=null) out.print(group.getDescription(locale)); else out.print(group); %> <%=derived?"("+userAdminLocale.get("DERIVE_FROM_EBP")+")":""%></A>&nbsp; </b>
    </td>


    <td class="TBDATA_XXS_L" tabindex="0" NOWRAP>
      <% if ( roleFlag.equals(Boolean.FALSE) )
       { %>
         <A href="<%=userAdminAlias%>?<%=UserAdminLogic.createNewUserAction%>=&<%=CompanySearchResultBean.RESULT_COMPANY_ID%>=<%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(partnerid)%>"><%=userAdminLocale.get("ADD_USER")%></A>&nbsp;|
         <A href="<%=userAdminAlias%>?<%=UserAdminLogic.searchUsersAction%>=&<%=CompanySearchResultBean.RESULT_COMPANY_ID%>=<%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(partnerid)%>"><%=userAdminLocale.get("SEARCH_USERS")%></A>&nbsp;|
      <% } %>

      <A href="<%=RoleAdminServlet.alias%>?cmd=<%=RoleAdminServlet.ROLE_MANAGEMENT_MAIN%>&ID=<%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(partnerid)%>"><%=userAdminLocale.get("ROLE_ASSIGNMENT")%></A>&nbsp;

    </td>
  </tr>
<%
   } // for ( .. )
%>

<!-- Start Page Navigation -->
		<tr>
			<td class="NAV_PGNB" colspan="7" width="100%">

&nbsp;&nbsp;&nbsp;
<A
<%
 if (currentPage>1)
{
%>
href="<%=util.alias(proxy, CompanyListLogic.servlet_name, CompanyListLogic.component_name)%>?<%=CompanyListLogic.listCompaniesAction%>=&<%=CompanyListLogic.pageNo%>=<%=currentPage-1%>&<%=CompanyListLogic.linesNo%>=<%=linesNo%>">
<%}%>
			<img src="<%=webpath%>layout/leftnull.gif"
     			width="14"
     			height="13"
     			border="0"
     			tabindex="0" 
     			alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>"
     			title="<%=userAdminLocale.get("PREVIOUS_PAGE")%>">
</A>&nbsp;&nbsp;&nbsp;

 			<span tabindex="0"><%=userAdminLocale.get("DISPLAY")%></span>
 			 <select name="_<%=CompanyListLogic.linesNo%>" tabindex="0" onChange='setLinesNo();' class="DROPDOWN">
  			<%for (int i = 10; i<companyList.getNumberOfCompanies(); i=i+10)
			{%>
				<option <%=(linesNo==i)?"selected":""%>><%=i%></option>
			<%}%>
				<option <%=(linesNo==companyList.getNumberOfCompanies())?"selected":""%>><%=companyList.getNumberOfCompanies()%></option>
			</select>

			<span tabindex="0"><%=userAdminLocale.get("PER_PAGE")%>&nbsp;&nbsp;&nbsp;&nbsp;<%=userAdminLocale.get("THIS_IS")%></span>&nbsp;
			<select name="_<%=CompanyListLogic.pageNo%>" tabindex="0" onChange='setPageNo();' class="DROPDOWN">
			<%for (int i = 1; i<=companyList.getNumberOfPages(); i++)
			 {%>
	 		  	<option <%=(currentPage==i)?"selected":""%>><%=i%></option>
			<%}%>
			</select>
				&nbsp;<span tabindex="0"><%=userAdminLocale.get("OF")%>
				<%=companyList.getNumberOfPages()%> <%=userAdminLocale.get("PAGES")%></span>

&nbsp;&nbsp;&nbsp;
<A
			<%
			if (currentPage<companyList.getNumberOfPages())
			{
			%>
				href="<%=util.alias(proxy, CompanyListLogic.servlet_name, CompanyListLogic.component_name)%>?<%=CompanyListLogic.listCompaniesAction%>=&<%=CompanyListLogic.pageNo%>=<%=currentPage+1%>&<%=CompanyListLogic.linesNo%>=<%=linesNo%>">
			<%}%>
			<img src="<%=webpath%>layout/right.gif"
     			 width="14"
    		  	 height="13"
     			 border="0"
     			 tabindex="0" 
     			 alt="<%=userAdminLocale.get("NEXT_PAGE")%>"
     			 title="<%=userAdminLocale.get("NEXT_PAGE")%>">
</A>&nbsp;&nbsp;&nbsp;

 </td>
</tr>

<!-- End Page Navigation -->
</table>
 </td>
</tr>
</table>
<p><p>
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
<input type="submit" class="BTN_LN" name="<%=CompanyListLogic.searchAgainAction%>" value="<%=userAdminLocale.get("COMP_SELECT_AGAIN")%>">
</td></tr>
</table>


</form>

<%@ include file="contextspecific_includes_bottom.txt" %>

