<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="com.sapmarkets.tpd.master.TradingPartnerInterface"%>

<%@ include file="proxy.txt" %>

<jsp:useBean id="list"
             class="com.sap.security.core.admin.ListBean"
             scope="request"/>

<% java.util.Vector temp = list.getObjsOnCurrentPage();
   String[] orgUnitIds = new String[temp.size()];
   orgUnitIds = (String[]) temp.toArray(orgUnitIds);
   java.util.Map orgUnits = (java.util.Map) proxy.getSessionAttribute(UserAdminLogic.orgUnitMap);
   String parent = (String) proxy.getSessionAttribute(UserAdminLogic.parent);


   // prepare for page navigation
   int colspan = 7;
   int totalItems = list.getTotalItems();
   int currentPage = list.getCurrentPage();
   int totalPages = list.getTotalPages();
   Integer[] itemPerPageOptions = list.getItemPerPageOptions();
   int currentItemPerPage = list.getCurrentItemPerPage();
   StringBuffer sb = new StringBuffer(userAdminAlias);
   sb.append("?");
   sb.append(UserAdminLogic.performSearchResultNavigateAction);
   sb.append("=");
   String urlAndAction = new String(sb);

   String pageKey = UserAdminLogic.listPage;
   String pageName = UserAdminLogic.orgUnitSearchResultPage;
   String setListPage = pageKey + "=" + pageName;
%>

<%-- start html--%>
<%if (!inPortal) {%>
<html>
<head>
<TITLE><%=userAdminLocale.get("USER_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<script language="JavaScript">
  function setTarget() {
    document.forms["orgunitresult"].target="<%=parent%>";
    document.forms["orgunitresult"].submit;
    window.close();
  }
</script>


<form name="orgunitresult" method="post" action="<%=userAdminAlias%>">
<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("ORGUNIT_SEARCH_RESULT")%></span>
    </td></tr>
</table>
<!-- End Section Header -->


<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
    <span tabindex="0"><%=userAdminLocale.get("COM_SEARCH_RESULT_DESP")%></span>
  </td></tr>
</table>
<!-- End Section Description -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Result Table-->
<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%" align="center">
  <tr>
  <td>
  <table cellpadding="1" cellspacing="1" border="0" width="100%">

	<% String navigateBar = userAdminLocale.get("NAVIGATION_BAR"); %>  
    <%@ include file="/include/pagenavigation.jsp"%>
								         
    <tr>
    <td class="TBDATA_HEAD" nowrap>
	<% String resultTable = userAdminLocale.get("TABLE"); 
	 if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
	 entryExit.append(userAdminMessages.print(new Message("START_OF", resultTable))); %>
	<img src="<%=webpath%>layout/sp.gif" 
	     width="1" 
	     height="1" 
	     alt="<%=entryExit.toString()%>" 
	     tabindex="0" 
	     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=resultTable%>').focus();}">     
    </td>
      <td scope="col" tabindex="0" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("ORGUNIT_NAME")%></td>
    </tr>

    <% String orgUnitId = null;
       String orgUnitName = null;
       for(int i=0; i<orgUnitIds.length; i++) {
           orgUnitId = orgUnitIds[i];
           orgUnitName = (String) orgUnits.get(orgUnitId); %>
	<tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
    <td class="TBDATA_XXS_C" width="1%" nowrap>
      <input type="radio"
        name="<%=UserAdminLogic.orgUnitId%>"
        tabindex="0"
		class="noborder"
        value="<%=util.URLEncoder(orgUnitId)%>"
        <%=i==0?"checked":""%> >
    </td>
    <td scope="row" tabindex="0" class="TBDATA_XS_L">
    <UM:encode><%=orgUnitName%></UM:encode> 
    <% if ( i == (orgUnitIds.length-1) ) { %> 
	<% entryExit.delete(0, entryExit.length());
	   entryExit.append(userAdminMessages.print(new Message("END_OF", resultTable))); %>
	<img src="<%=webpath%>layout/sp.gif" 
	     width="1" 
	     height="1" 
	     id="<%=resultTable%>"
	     alt="<%=entryExit.toString()%>"
	     tabindex="0">     
    <% } %>
    </td>
    </tr>
    <% } %>
	     				
    <%@ include file="/include/pagenavigation.jsp" %>
    
    </table>
  </td>
</tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><a href="#sidemenu"><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="skip to sidemenu"></a></td></tr>
</table>

<table width="98%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td width="100%" align="left" class="TBLO_XXS_L" nowrap>
            <input type="submit"
                   class="BTN_LB"
                   tabindex="0"
                   name="<%=UserAdminLogic.selectOrgUnitAction%>"
                   value=" <%=userAdminLocale.get("SELECT")%> ">&nbsp;
            <input type="submit"
                   class="BTN_LN"
                   tabindex="0"
                   name="<%=UserAdminLogic.searchOrgUnitAction%>"
                   value="<%=userAdminLocale.get("SEARCH_AGAIN")%>">
      </td>
    </tr>
</table></form>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

