<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.IUser"%>
<%@page import="com.sap.security.api.IUserAccount"%>
<%@page import="com.sap.security.api.logon.ILoginConstants"%>
<%@page import="com.sap.security.core.imp.AbstractUserAccount"%>

<%@ include file="proxy.txt" %>
<jsp:useBean id="list"
             class="com.sap.security.core.admin.ListBean"
             scope="request"/>

<% String parent = UserAdminLogic.lockedUsersListPage;;
   IUser[] users = util.getUsers(list.getObjsOnCurrentPage());

   // navigation
   // with role column
   // int colspan = 7;
   // without role
   int colspan = 6;
   int totalItems = list.getTotalItems();
   int currentPage = list.getCurrentPage();
   int totalPages = list.getTotalPages();
   Integer[] itemPerPageOptions = list.getItemPerPageOptions();
   int currentItemPerPage = list.getCurrentItemPerPage();
   String pageKey = UserAdminLogic.listPage;
   String pageName = UserAdminLogic.lockedUsersListPage;
   String setListPage = pageKey + "=" + pageName;
   StringBuffer sb = new StringBuffer(userAdminAlias);
   sb.append("?");
   sb.append(UserAdminLogic.performSearchResultNavigateAction);
   sb.append("=");
   sb.append("&");
   sb.append(pageKey);
   sb.append("=");
   sb.append(pageName);
   String urlAndAction = new String(sb);
   
   // to show action buttons or not
   // UserAccount: -1 means readOnly -2 means lockReadOnly -3 means passwordChangeReadOnly
   // 1 means mutable
   // User: -1 means ReadOnly -2 means notDeletable
   
   boolean toShowUnlockButton = UserAdminHelper.hasAccess(proxy.getActiveUser(), UserAdminHelper.UNLOCK_USERS);
   int lng = users.length;
   IUser user = null;
   IUserAccount[] accounts = null;
   String uniqueId = null;
   int[] readOnlys = new int[lng*2];
   int lockMutables = 0;
   for (int i=0; i<lng; i++) {
       user = users[i];
       accounts = user.getUserAccounts();
       if ( (null!=accounts) && (accounts.length>0) ) {
           uniqueId = accounts[0].getUniqueID();
           if ( UserAdminFactory.isUserReadOnly(uniqueId) ) {
               readOnlys[i*2] = -1;
               lockMutables += 1;
           } else {
               // check attribute readonly
               int flag = 1;
               if ( UserAdminFactory.isAttributeReadOnly(uniqueId, AbstractUserAccount.IS_LOCKED) )	{
                   flag = -2;
                   lockMutables += 1;
               }               
               readOnlys[i*2] = flag;
           }
       } else {
           readOnlys[i*2] = -1;
           lockMutables += 1;
       }
       
       int flag = 1;
       if ( UserAdminFactory.isUserReadOnly(user.getUniqueID()) ) {
           flag = -1;
       }
       readOnlys[i*2+1] = flag;
   }
   
   if ( toShowUnlockButton ) {
       if ( lockMutables == lng ) toShowUnlockButton = false;
   }  
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
<%@ include file="contextspecific_includes_top.txt" %>


<script language="JavaScript">
  function setPaging() {
    var frm = document.lockuserlist;
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
</script>
<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("LOCKED_USERS_LIST_HEADER")%></span>
    </td></tr>
</table>

<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<!-- End Section Description -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1"></td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0"></td></tr>
</table>

<form name="lockuserlist" method="post" action="<%=userAdminAlias%>" onSubmit="setPaging()">

<!-- Start Result Table-->

<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%">
  <tr><td>
    <table cellpadding="1" cellspacing="1" border="0" width="100%">
      <% String navigateBar = userAdminLocale.get("NAVIGATION_BAR"); %>
      <%@include file="/include/pagenavigation.jsp"%>
  
      <%@include file="/include/user_searchresult_table.jsp"%>
      
      <% if (toShowUnlockButton) { %>
      <tr class="TBDATA_CNT_ODD_BG">
        <td class="TBDATA_XXS_L" colspan="7">
          <input type="submit"
                 name="<%=UserAdminLogic.unlockUsersAction%>"
                 tabindex="0"
                 value="<%=userAdminLocale.get("ACTIVATE")%>"
                 class="BTN_S">
        </td>
      </tr>
      <% } %>
      
      <%@include file="/include/pagenavigation.jsp"%>
    </table>
  </td></tr>
</table></form>
<!-- End result Table -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

