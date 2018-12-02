<%@ taglib uri="UM" prefix="UM" %>
<%@page import="java.util.Hashtable"%>
<%@page import="com.sap.security.api.IUser"%>
<%@page import="com.sap.security.api.IUserAccount"%>
<%@page import="com.sap.security.api.logon.ILoginConstants"%>
<%@page import="com.sap.security.core.imp.AbstractUserAccount"%>

<%@ include file="proxy.txt" %>

<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute("countries");%>
<jsp:useBean id="list"
             class="com.sap.security.core.admin.ListBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

<% String frmAction = userAdminAlias;
   boolean intruder = false;
   Hashtable _parameters = (Hashtable) proxy.getSessionAttribute(UserAdminLogic.frm_parameters);
   if (_parameters != null) {
	   frmAction = (String) _parameters.get(UserAdminLogic.servletName);
	   if ( null == util.checkEmpty(frmAction) ) {
		   frmAction = userAdminAlias;
	   }
	   if ( !frmAction.equals(userAdminAlias) ) {
		   intruder = true;
	   }
   }

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
   String pageName = UserAdminLogic.userSearchResultPage;
   String setListPage = pageKey + "=" + pageName;
   StringBuffer sb = new StringBuffer(userAdminAlias);
   sb.append("?");
   sb.append(UserAdminLogic.performSearchResultNavigateAction);
   sb.append("=&");
   sb.append(pageKey);
   sb.append("=");
   sb.append(pageName);
   String urlAndAction = new String(sb);
   
   // to show action buttons or not
   // UserAccount: -1 means readOnly -2 means lockReadOnly -3 means passwordChangeReadOnly
   // 1 means mutable
   // User: -1 means ReadOnly -2 means notDeletable
   
   IUser doer = proxy.getActiveUser();
   boolean toShowLockButton = UserAdminHelper.hasAccess(doer, UserAdminHelper.LOCK_USERS);
   boolean toShowUnlockButton = UserAdminHelper.hasAccess(doer, UserAdminHelper.UNLOCK_USERS);
   boolean toShowDeleteButton = UserAdminHelper.hasAccess(doer, UserAdminHelper.DELETE_USERS);
   boolean toShowExpirePasswordButton = UserAdminHelper.hasAccess(doer, UserAdminHelper.CHANGE_PASSWORD);
   int lng = users.length;
   IUser user = null;
   IUserAccount[] accounts = null;
   String uniqueId = null;
   int[] readOnlys = new int[lng*2];
   int lockMutables = 0;
   int changePasswordMutables = 0;
   int deleteMutables = 0;
   for (int i=0; i<lng; i++) {
       user = users[i];
       accounts = user.getUserAccounts();
       if ( (null!=accounts) && (accounts.length>0) ) {
           uniqueId = accounts[0].getUniqueID();
           if ( UserAdminFactory.isUserReadOnly(uniqueId) ) {
               readOnlys[i*2] = -1;
               lockMutables += 1;
               changePasswordMutables += 1;
           } else {
               // check attribute readonly               
               int flag = 1;
               if ( UserAdminFactory.isAttributeReadOnly(uniqueId, AbstractUserAccount.IS_LOCKED) )	{
                   flag = -2;
                   lockMutables += 1;
               } 	
               if ( UserAdminFactory.isAttributeReadOnly(uniqueId, AbstractUserAccount.PASSWORD_CHANGE_REQUIRED)
                   || UserAdminFactory.isAttributeReadOnly(uniqueId, ILoginConstants.LOGON_PWD_ALIAS) ) {
                   flag = (flag<0)?-1:-3;
                   changePasswordMutables += 1;
               }               
               readOnlys[i*2] = flag;
           }
       } else {
           readOnlys[i*2] = -1;
           lockMutables += 1;
           changePasswordMutables += 1;
       }
       
       int flag = 1;
       if ( UserAdminFactory.isUserReadOnly(user.getUniqueID()) ) {
           flag = -1;
           deleteMutables += 1;
       } else {      
	       if ( !UserAdminFactory.isUserDeletable(user.getUniqueID()) ) {
	           flag = -2;
	           deleteMutables += 1;
	       } 
	   }
       readOnlys[i*2+1] = flag;
   }
   
   if ( toShowLockButton ) {
       if ( lockMutables == lng ) toShowLockButton = false;
   } 
   
   if ( toShowUnlockButton ) {
       if ( lockMutables == lng ) toShowUnlockButton = false;
   } 
   
   if ( toShowExpirePasswordButton ) {
       if ( changePasswordMutables == lng ) toShowExpirePasswordButton = false;
   }
   
   if ( toShowDeleteButton ) {
       if ( deleteMutables == lng ) toShowDeleteButton = false;
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

<script language="JavaScript">
  function confirmDelete(action) {
      var cmsg = "<%=userAdminLocale.get("CONFIRM_DELETE")%>"; 
      var total = <%=users.length%>;
      var readOnly = 0;
      var slcted = 0;
      var usersSlcted, readOnlys;
      if( -1 != navigator.userAgent.indexOf("MSIE") ) {
      	usersSlcted = document.all["<%=ListBean.selectedObjId%>"];
        readOnlys = document.all["readOnly"];
      } else {
        var frm = document.forms[0];
        usersSlcted = frm.elements["<%=ListBean.selectedObjId%>"];
        readOnlys = frm.elements["readOnly"];
      }
      if ( null == usersSlcted  ) {
          var chckBox = document.getElementById("<%=ListBean.selectedObjId%>");
          if ( chckBox.checked == true ) {
              slcted = 1;
              if ( document.getElementById("readOnly").value == "true" )
                  readOnly = 1;
          }      
      } else if (total == 1) {
	  if ( usersSlcted.checked == true ) {
	      slcted++;
              if ( document.getElementById("readOnly").value == "true" )
		  readOnly++; 
	  }         
      } else {
	      for ( var i=0; i<total; i++ ) {
	          if ( usersSlcted[i].checked == true ) {
	              slcted++;
	              if ( readOnlys[i].value == "true" )
	                  readOnly++; 
	          }         
	      }
      }
 
      if ( readOnly > 0 ) {
          if ( readOnly == slcted ) { 
              cmsg = "<%=userAdminLocale.get("CONFIRM_DELETE_WITH_READONLYS")%>"; 
          } else {
              cmsg = "<%=userAdminLocale.get("CONFIRM_DELETE_WITH_DOUBTS")%>";
          }
      } 
      
	  if ( confirm(cmsg) == true ) {
          setPagingAndSubmit(action);
	  } else {
          return false;
    }
  } // confirmDelete

  function setPaging() {
    var frm, pageObj, itemObj;
    var pageInteger, pageString;
    var itemInteger, itemString;

    if( -1 != navigator.userAgent.indexOf("MSIE") ) {
        frm = document.getElementById("searchresult");
        pageObj = document.all["reqPage"];
        itemObj = document.all["itemPerPage"];
    } else {
        frm = document.searchresult;
        pageObj = frm.elements["reqPage"];
        itemObj = frm.elements["itemPerPage"];
    }

    pageInteger = pageObj[0].selectedIndex;
    pageString = pageObj[0].options[pageInteger].text;

    itemInteger = itemObj[0].selectedIndex;
    itemString = itemObj[0].options[itemInteger].text;

    pageTag = document.createElement("input");
    pageTag.setAttribute("name", "requestPage");
    pageTag.setAttribute("type", "hidden");
    pageTag.setAttribute("value", pageString);
    frm.appendChild(pageTag);

    itemTag = document.createElement("input");
    itemTag.setAttribute("name", "currentItemPerPage");
    itemTag.setAttribute("type", "hidden");
    itemTag.setAttribute("value", itemString);
    frm.appendChild(itemTag);
  } // setPaging

  function setPagingAndSubmit(action) {
    setPaging();
    actionTag = document.createElement("input");
    actionTag.setAttribute("name", action);
    actionTag.setAttribute("type", "hidden");
    actionTag.setAttribute("value", "");
    document.searchresult.appendChild(actionTag);
    pageKey = document.createElement("input");
    pageKey.setAttribute("name", "<%=pageKey%>");
    pageKey.setAttribute("type", "hidden");
    pageKey.setAttribute("value", "<%=pageName%>");
    document.searchresult.appendChild(pageKey);    
    document.searchresult.submit();
  } // setPagingAndSubmit

  function doSearchAgain() {
      var frm = document.getElementById("searchresult");
      inputTag1 = document.createElement("input");
      inputTag1.setAttribute("name", "<%=UserAdminLogic.searchUsersAction%>");
      inputTag1.setAttribute("type", "hidden");
      inputTag1.setAttribute("value", "");
      frm.appendChild(inputTag1);
      inputTag2 = document.createElement("input");
      inputTag2.setAttribute("name","<%=UserAdminLogic.servletName%>");
      inputTag2.setAttribute("type", "hidden");
      inputTag2.setAttribute("value", "<%=util.filteringSpecialChar(frmAction)%>");
      frm.appendChild(inputTag2);
      frm.action = "<%=userAdminAlias%>";
      frm.submit();
  } // doSearchAgain
</script>


<%@ include file="contextspecific_includes_top.txt" %>
<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("SEARCH_RESULT_HEADER")%></span>
    </td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<!-- End Section Description -->

<% if ( error.isError() ) { %>
<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>
<!-- Start Error Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_ERROR_XSB">
    <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Error Msg -->
<% } %>

<!-- Start Info Msg-->
<% if ( info.isInfo() ) { %>
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
<tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_success.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
</td></tr>
</table>
<% } %>
<!-- End Info Msg -->

<!-- Start Result Table-->
<form id="searchresult" name="searchresult" method="post" action="<%=frmAction%>">
<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%">
  <tr><td>
    <table id="srtable" cellpadding="1" cellspacing="1" border="0" width="100%">
    
      <% String navigateBar = userAdminLocale.get("NAVIGATION_BAR"); %>
      <%@include file="/include/pagenavigation.jsp"%>

      <%@include file="/include/user_searchresult_table.jsp"%>

      <tr class="TBDATA_CNT_ODD_BG"><td class="TBDATA_XXS_L" colspan="7">
        <% if ( !intruder ) { %>
            <% if ( toShowUnlockButton ) { %>
            <input type="button"
                   name="<%=UserAdminLogic.unlockUsersAction%>"
                   tabindex="0"
                   value="<%=userAdminLocale.get("ACTIVATE")%>"
                   class="BTN_S"
                   onClick="javascript:setPagingAndSubmit('<%=UserAdminLogic.unlockUsersAction%>');">&nbsp;
            <% } %>
            <% if ( toShowLockButton ) { %>
            <input type="button"
                    name="<%=UserAdminLogic.lockUsersAction%>"
                    tabindex="0"
                    value="<%=userAdminLocale.get("DEACTIVATE")%>"
                    class="BTN_S"
                    onClick="javascript:setPagingAndSubmit('<%=UserAdminLogic.lockUsersAction%>');">&nbsp;
            <% } %>
            
            <input type="button"
                   name="<%=UserAdminLogic.performUsersDeleteAction%>"
                   tabindex="0"
                   value="<%=userAdminLocale.get("DELETE")%>"
                   class="BTN_S"
                   onClick="javascript:return confirmDelete('<%=UserAdminLogic.performUsersDeleteAction%>');">&nbsp;

            <% if ( toShowExpirePasswordButton ) { %>
            <input type="button"
                    name="<%=UserAdminLogic.expirePswdAction%>"
                    tabindex="0"
                    value="<%=userAdminLocale.get("EXPIRE_PSWD")%>"
                    class="BTN_S"
                    onClick="javascript:setPagingAndSubmit('<%=UserAdminLogic.expirePswdAction%>');">
            <% } %>
        <% } else { %>
          <% String select = " "+userAdminLocale.get("SELECT")+" ";
             String cancel = " "+userAdminLocale.get("CANCEL")+" ";
             java.util.Enumeration keys = _parameters.keys();
             String key = null;
             String value = null;
             while ( keys.hasMoreElements() ) { 
                 key = (String) keys.nextElement();
                 value= (String) _parameters.get(key); %>
                 <input type="hidden" name="<%=util.filteringSpecialChar(key)%>" value="<%=util.filteringSpecialChar(value)%>">
          <% } %>
              <input type="button"
                     name="add"
                     tabindex="0"
                     value="<%=select%>"
                     class="BTN_LB"
                     onClick="javascript:setPagingAndSubmit('add');">&nbsp;
        <% } %>
        </td>
      </tr>
      <%@ include file="/include/pagenavigation.jsp" %>
    </table>
  </td></tr>
</table>
     
<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><a href="#sidemenu"><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="<%=userAdminLocale.get("SKIP_TO_SIDEMENU")%>"></a></td></tr>
</table>

<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <% if (!intruder) { %>
    <input type="button"
           name="<%=UserAdminLogic.searchUsersAction%>"
           tabindex="0"
           value="&nbsp;<%=userAdminLocale.get("SEARCH_AGAIN")%>&nbsp;"
           class="BTN_LB"
           onClick="javascript:setPagingAndSubmit('<%=UserAdminLogic.searchUsersAction%>');">&nbsp;
    <% } else { %>
    <%--
    <input type="button"
           name="<%=UserAdminLogic.searchUsersAction%>"
           tabindex="0"
           value="&nbsp;<%=userAdminLocale.get("SEARCH_AGAIN")%>&nbsp;"
           class="BTN_LB"
           onClick="doSearchAgain()">&nbsp;
    --%>
    <% } %>
  </td></tr>
</table>
</form>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

<% users = null;
   request.removeAttribute(ListBean.beanId); %>
