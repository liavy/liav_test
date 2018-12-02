<%@ taglib uri="UM" prefix="UM" %>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.sap.security.api.*"%>
<%@page import="com.sap.security.core.imp.User"%>
<%@page import="com.sapmarkets.tpd.TradingPartnerDirectoryCommon"%>
<%@page import="com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface"%>
<%@page import="com.sapmarkets.tpd.master.TradingPartnerInterface"%>
<%@page import="com.sapmarkets.tpd.master.PartnerID"%>

<%@ include file="proxy.txt" %>

<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>
<jsp:useBean id="list"
             class="com.sap.security.core.admin.ListBean"
             scope="request"/>

<% IUser[] users = util.getUsers(list.getObjsOnCurrentPage());

   // navigation
   int colspan = 7;
   int totalItems = list.getTotalItems();
   int currentPage = list.getCurrentPage();
   int totalPages = list.getTotalPages();
   Integer[] itemPerPageOptions = list.getItemPerPageOptions();
   int currentItemPerPage = list.getCurrentItemPerPage();
   String pageKey = UserAdminLogic.listPage;
   String pageName = UserAdminLogic.unapprovedUsersListPage;
   String setListPage = pageKey + "=" + pageName;
   StringBuffer sb = new StringBuffer(userAdminAlias);
   sb.append("?");
   sb.append(UserAdminLogic.performSearchResultNavigateAction);
   sb.append("=");
   sb.append("&");
   sb.append(pageKey);
   sb.append("=");
   sb.append(pageName);
   String urlAndAction = sb.toString();
%>

<% String sort = (String) proxy.getSessionAttribute(UserAdminLogic.sortFieldName);
   boolean order = ((Boolean)proxy.getSessionAttribute(UserAdminLogic.orderBy)).booleanValue(); // true is as; false is de

   StringBuffer altTextSB = new StringBuffer(userAdminLocale.get("SORT_BY"));
   altTextSB.append(":");
   String altText = altTextSB.toString();
%>

<%-- start html--%>
<%if (!inPortal) {%>
<html>
<head>
<TITLE><%=userAdminLocale.get("USER_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
</head>
<%}%>

<script language="JavaScript">
    function doSomething(action, index)
    {
      var frm = document.getElementById("unapprovedusers");
      actionTag = document.createElement("input");
      actionTag.setAttribute("name", action);
      actionTag.setAttribute("type","hidden");
      actionTag.setAttribute("value","");
      frm.appendChild( actionTag );
      slctObjTag = document.createElement("input");
      slctObjTag.setAttribute("name", "<%=ListBean.selectedObjId%>");
      slctObjTag.setAttribute("type","hidden");
      slctObjTag.setAttribute("value", index);
      frm.appendChild( slctObjTag );
      setPaging();
    }

    function doSorting(sortName, orderBy) {
      var frm = document.getElementById("unapprovedusers");
      actionTag = document.createElement("input");
      actionTag.setAttribute("name", "<%=UserAdminLogic.performSearchResultSortingAction%>");
      actionTag.setAttribute("type", "hidden");
      actionTag.setAttribute("value", "");
      frm.appendChild( actionTag );
      sortFieldNameTag = document.createElement("input");
      sortFieldNameTag.setAttribute("name", "<%=UserAdminLogic.sortFieldName%>");
      sortFieldNameTag.setAttribute("type", "hidden");
      sortFieldNameTag.setAttribute("value", sortName);
      frm.appendChild( sortFieldNameTag );
      orderByTag = document.createElement("input");
      orderByTag.setAttribute("name", "<%=UserAdminLogic.orderBy%>");
      orderByTag.setAttribute("type", "hidden");
      orderByTag.setAttribute("value", orderBy);
      frm.appendChild( orderByTag );
      frm.action = "<%=userAdminAlias%>";
      setPaging();
    } // doSorting

  function setPaging() {
    var frm = document.getElementById("unapprovedusers");
    var pageObj = frm.elements["reqPage"];
    var itemObj = frm.elements["itemPerPage"];
    var pageInteger, pageString;
    var itemInteger, itemString;

    pageInteger = pageObj[0].selectedIndex;
    pageString = pageObj[0].options[pageInteger].text;

    itemInteger = itemObj[0].selectedIndex;
    itemString = itemObj[0].options[itemInteger].text;

    inputTag1 = document.createElement("input");
    inputTag1.setAttribute("name", "requestPage");
    inputTag1.setAttribute("type", "hidden");
    inputTag1.setAttribute("value", pageString);
    frm.appendChild( inputTag1 );

    inputTag2 = document.createElement("input");
    inputTag2.setAttribute("name", "currentItemPerPage");
    inputTag2.setAttribute("type", "hidden");
    inputTag2.setAttribute("value", itemString);
    frm.appendChild( inputTag2 );
    
    pageKey = document.createElement("input");
    pageKey.setAttribute("name", "<%=pageKey%>");
    pageKey.setAttribute("type", "hidden");
    pageKey.setAttribute("value", "<%=pageName%>");
    frm.appendChild(pageKey);  
        
    frm.submit();
  }

    function toggle_select(size) {
    	var toggleSlct = document.getElementById("toggleSlct");
        if (toggleSlct.checked == true) {
            select_all(size);
        }
        else {
            deselect_all(size);
        }
    }

    function select_all(size) {
        var cks = document.all("<%=ListBean.selectedObjId%>");
        if ( size < 2 ) {
            cks.checked = true;
        } else {
            for (var i=0; i<size; i++) {
                cks[i].checked = true;
            }
        }
    }
    
	function deselect_all(size) {
    	var cks = document.all("<%=ListBean.selectedObjId%>");
        if ( size < 2 ) {
            cks.checked = false;
        } else {
            for (var i=0; i<size; i++) {
                cks[i].checked = false;
            }
        }
    }
</script>


<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>

<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("NEWUSER_APPV_LIST_HEADER")%></span>
    </td></tr>
</table>
<%-- End Section Header --%>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
    <span tabindex="0"><%=userAdminLocale.get("NEWUSER_APPV_LIST_DESP")%></span>
  </td></tr>
</table>
<!-- End Section Description -->

<% if ( info.isInfo() ) { %>
<!-- Start Confirm Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_success.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Confirm Msg -->
<% } %>

<% if ( error.isError() ) { %>
<!-- Start Error Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_ERROR_XSB">
    <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Error Msg -->
<% } %>

<%-- Start Result Table--%>
<form id="unapprovedusers" name="unapprovedusers" method="post" action="<%=userAdminAlias%>" onSubmit="setPaging()">
<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%">
  <tr><td>
    <table cellpadding="1" cellspacing="1" border="0" width="100%">
      <%-- Start Page Navigation --%>
      <% String navigateBar = userAdminLocale.get("NAVIGATION_BAR"); %>  
      <%@ include file="/include/pagenavigation.jsp"%>
      <%-- End Page Navigation --%>
      <tr>
        <td class="TBDATA_HEAD" nowrap>
          <INPUT TYPE="CHECKBOX" id="toggleSlct" NAME="toggleSlct" class="noborder" onClick="toggle_select('<%=users.length%>')" alt="<%=userAdminLocale.get("SELECT_OR_DESELECT")%>"></td>
        <td scope="col" class="TBDATA_HEAD" nowrap>
          <table><tr><td scope="col" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("FROM")%>&nbsp;</td><td valign="center">
        <% StringBuffer altText1SB = new StringBuffer(altText);
           altText1SB.append(" ");
           altText1SB.append(userAdminLocale.get("NAME"));
           String altText1 = altText1SB.toString();
           if ( sort.equals(UserBean.displayNameId) ) {
               if ( order ) { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.displayNameId%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" width="8" height="7" border="0" alt="<%=altText1%>"></a>
          <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.displayNameId%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" width="8" height="7" border="0" alt="<%=altText1%>"></a>
          <% } %>
        <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.displayNameId%>', 'false');"><img src="<%=webpath%>layout/sort.gif" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>"></a>
        <% } %></td></tr></table>
        </td>
        <% if ( !UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) { %>
        <td scope="col" class="TBDATA_HEAD" nowrap>
          <table><tr><td scope="col" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("DEPT")%>&nbsp;</td><td valign="center">
        <% StringBuffer altText2SB = new StringBuffer(altText);
           altText2SB.append(" ");
           altText2SB.append(userAdminLocale.get("DEPARTMENT"));
           String altText2 = altText2SB.toString();
           if ( sort.equals(UserBean.departmentId) ) {
               if ( order ) { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.departmentId%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" width="8" height="7" border="0" alt="<%=altText2%>"></a>
          <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.departmentId%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" width="8" height="7" border="0" alt="<%=altText2%>"></a>
          <% } %>
        <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.departmentId%>', 'false');"><img src="<%=webpath%>layout/sort.gif" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>"></a>
        <% } %></td></tr></table>
        </td>
        <% } else { %>
        <td scope="col" class="TBDATA_HEAD" nowrap>
          <table><tr><td scope="col" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("COMPANY")%>&nbsp;</td><td valign="center">
        <% StringBuffer altText3SB = new StringBuffer(altText);
           altText3SB.append(" ");
           altText3SB.append(userAdminLocale.get("COMPANY"));
           String altText3 = altText3SB.toString();
           if ( sort.equals(UserBean.companyId) ) {
               if ( order ) { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.companyId%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" width="8" height="7" border="0" alt="<%=altText3%>"></a>
          <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.companyId%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" width="8" height="7" border="0" alt="<%=altText3%>"></a>
          <% } %>
        <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserBean.companyId%>', 'false');"><img src="<%=webpath%>layout/sort.gif" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>"></a>
        <% } %></td></tr></table>
        </td>
        <% } %>
        <td scope="col" class="TBDATA_HEAD" nowrap>
          <%=userAdminLocale.get("NOTE")%>&nbsp;
        </td>
        <td scope="col" class="TBDATA_HEAD" nowrap>
          <table><tr><td scope="col" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("CREATED")%>&nbsp;</td><td valign="center">
        <% StringBuffer altText4SB = new StringBuffer(altText);
           altText4SB.append(" ");
           altText4SB.append(userAdminLocale.get("CREATED"));
           String altText4 = altText4SB.toString();
           if ( sort.equals(UserAccountBean.created) ) {
               if ( order ) { %>
           <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.created%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" width="8" height="7" border="0" alt="<%=altText4%>"></a>
           <% } else { %>
           <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.created%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" width="8" height="7" border="0" alt="<%=altText4%>"></a>
           <% } %>
        <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.created%>', 'true');"><img src="<%=webpath%>layout/sort.gif" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>"></a>
        <% } %></td></tr></table>
        </td>
      </tr>
      <% java.util.Locale locale = proxy.getLocale();
         if ( null == locale ) locale = java.util.Locale.getDefault();
         DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
         IUserFactory uf = UMFactory.getUserFactory();
         TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
         String companyId = null;
         String companyName = null;
         IUser user = null;
         PartnerID mpid = null;
         TradingPartnerInterface company = null;         
         for (int i=0; i<users.length; i++) {
             user = users[i];
             companyId = user.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0];
             companyName = util.empty;
             if (companyId != null) {
                 mpid = PartnerID.instantiatePartnerID( companyId );
                 company = tpd.getPartner( mpid );
                 if ( null != company) companyName = company.getDisplayName();
             }
      %>
	 <tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
        <td class="TBDATA_XXS_C" width="1%" nowrap>
          <input type="checkbox"
                 name="<%=ListBean.selectedObjId%>"
                 value="<%=i%>"
				 class="noborder">
        </td>
        <td scope="row" class="TBDATA_XS_L">
          <a href="javascript:doSomething('<%=UserAdminLogic.userApproveOrDenyAction%>', '<%=i%>');"><UM:encode><%=user.getDisplayName()%></UM:encode></a>
        </td>

        <td class="TBDATA_XXS_L" nowrap>
          <% if ( !UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) { %>
            <% if (null!=user.getDepartment()) { %><UM:encode><%=user.getDepartment()%></UM:encode><%}else{%><%=util.empty%><%}%>
          <% } else { %>
            <UM:encode><%=companyName%></UM:encode>
          <% } %>
        </td>

        <td class="TBDATA_XXS_L">
          <% if ( null != user.getAttribute(UserBean.UM, UserBean.noteToAdmin)) { %>
            <UM:encode><%=user.getAttribute(UserBean.UM, UserBean.noteToAdmin)[0]%></UM:encode>
          <% } else { %>
            <%=util.empty%>
          <% } %>
        </td>
        <td class="TBDATA_XXS_R" nowrap>
          <UM:encode><%=(user.created()==null)?userAdminLocale.get("NOT_AVAILABLE"):dateFormat.format(user.created())%></UM:encode>
        </td>
      </tr>
      <% } %>
      <tr class="TBDATA_CNT_ODD_BG"><td class="TBDATA_XXS_L" colspan="7">
        <input type="submit"
               name="<%=UserAdminLogic.usersApproveAction%>"
               tabindex="0"
               value="<%=userAdminLocale.get("APPROVE")%>"
               class="BTN_S">&nbsp;
        <input type="submit"
               name="<%=UserAdminLogic.usersDenyAction%>"
               tabindex="0"
               value="<%=userAdminLocale.get("DENY")%>"
               class="BTN_S">
      </td></tr>
      <!-- Start Page Navigation -->
      <%@ include file="/include/pagenavigation.jsp"%>
      <!-- End Page Navigation -->
    </table>
  </td></tr>
</table></form>
<!-- End result Table -->

<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

