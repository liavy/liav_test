<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.*"%>

<%@ include file="proxy.txt" %>
<jsp:useBean id="list"
             class="com.sap.security.core.admin.ListBean"
             scope="request"/>

<% IUser[] users = util.getUsers(list.getObjsOnCurrentPage());

   // navigation
   int colspan = 3;
   int totalItems = list.getTotalItems();
   int currentPage = list.getCurrentPage();
   int totalPages = list.getTotalPages();
   Integer[] itemPerPageOptions = list.getItemPerPageOptions();
   int currentItemPerPage = list.getCurrentItemPerPage();
   String pageKey = UserAdminLogic.listPage;
   String pageName = UserAdminLogic.unapprovedUsersDenyPage;
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

<% String messageToRequestor = new String();
   if ( null != proxy.getRequestAttribute(messageToRequestor) ) {
       messageToRequestor = (String) proxy.getRequestAttribute(messageToRequestor);
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
  function setPaging() {
    var frm = document.lockuserlist;
    var pageObj = document.getElementById("reqPage");
    var itemObj = document.getElementById("itemPerPage");
    var pageInteger, pageString;
    var itemInteger, itemString;

    pageInteger = pageObj.selectedIndex;
    pageString = pageObj.options[pageInteger].text;

    itemInteger = itemObj.selectedIndex;
    itemString = itemObj.options[itemInteger].text;

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
    frm.submit();
  }
</script>

<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
  <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
    <span tabindex="0"><%=userAdminLocale.get("DENY_MULT_USERS_HEADER")%></span>
  </td></tr>
</table>

<!-- End Section Header -->

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
	<span tabindex="0"><%=userAdminLocale.get("DENY_MULT_USERS_DESP")%></span>
  </td></tr>
</table>

<form method="post" action="<%=userAdminAlias%>">
<table cellspacing="1" cellpadding="1" border="0" width="98%">
  <tr>
	<td class="TBLO_XSB" colspan="2">
	  <span tabindex="0"><%=userAdminLocale.get("SELECTED_USERS_TO_DENY")%></span>
	</td>
  </tr>
  <tr>
	<td class="TBLO_XXS_L" colspan="2" nowrap>		
      <table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="99%">
	      <tr><td>
		      <table cellpadding="1" cellspacing="1" border="0" width="100%">
		      
		      <% String navigateBar = userAdminLocale.get("NAVIGATION_BAR"); %>
              <%@include file="/include/pagenavigation.jsp"%>
			              
		  	  <tr>
              <td scope="col" class="TBDATA_HEAD" tabindex="0" nowrap>
				<% String resultTable = userAdminLocale.get("TABLE"); 
				 if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
				 entryExit.append(userAdminMessages.print(new Message("START_OF", resultTable))); %>
				<img src="<%=webpath%>layout/sp.gif" 
				     width="1" 
				     height="1" 
				     alt="<%=entryExit.toString()%>" 
				     tabindex="0" 
				     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=resultTable%>').focus();}">               
              <%=userAdminLocale.get("FROM")%>
              </td>
              <td scope="col" class="TBDATA_HEAD" tabindex="0" nowrap><%=userAdminLocale.get("DEPT")%></td>
              <td scope="col" class="TBDATA_HEAD" tabindex="0" nowrap><%=userAdminLocale.get("CREATED")%></td>
		      </tr>
		      
            <% IUserAccount[] accounts = null;
               IUserAccount account = null; 
               IUser user = null; 
		       DateUtil du = new DateUtil(DateUtil.MEDIEM, proxy.getLocale()); %>            
            <% for ( int i=0; i<users.length; i++) { 
                   user = users[i]; %>
			 <tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
              <td scope="row" class="TBDATA_XS_L" tabindex="0"><UM:encode><%=util.checkNull(user.getDisplayName())%></UM:encode></td>
              <td class="TBDATA_XXS_L" tabindex="0" nowrap><UM:encode><%=util.checkNull(user.getDepartment())%></UM:encode></td>
              <td class="TBDATA_XXS_R" tabindex="0" nowrap>
                <% accounts = user.getUserAccounts(); 
                   if ( accounts.length > 0 ) {
                       account = accounts[0]; %> 
		        <%= du.getTime(account.created()) %>
		        <% } %>
				<% if ( i == (users.length-1) ) { %>
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
			            
            <%@include file="/include/pagenavigation.jsp"%>
    </table>      
  </td></tr>
</table>

	  <table cellpadding="0" cellspacing="0" border="0">
	    <tr><td>
	      <IMG height="10" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
	    </td></tr>
	  </table>

	  <table cellpadding="0" cellspacing="0" border="0" width="100%">
	    <tr><td>
	      <hr size="1" width="100%">
	    </td></tr>
	  </table>
	</td>
  </tr>

  <tr>
	<td class="TBLO_XSB" colspan="2"><span tabindex="0"><%=userAdminLocale.get("MESSAGES_TO_DENIEDUSERS")%></span></td>
  </tr>

  <tr>
    <td>
	  <table cellpadding="2" cellspacing="0" border="0">
	    <tr>
	    <td class="TBLO_XXS_R" nowrap valign="top">#
	      <LABEL FOR="<%=UserBean.messageToRequestor%>"><%=userAdminLocale.get("MESSAGE")%>:</LABEL>&nbsp;
	    </td>
		<td nowrap>
          <textarea id="<%=UserBean.messageToRequestor%>"
                    name="<%=UserBean.messageToRequestor%>"
                    value="<%=util.filteringSpecialChar(messageToRequestor)%>"
                    tabindex="0"
                    wrap="soft"
                    cols="25"
                    rows="3"
                    style="width: 2.5in"
                    CLASS="TX_XXS"><UM:encode><%=messageToRequestor%></UM:encode></textarea>
		    </td>
	    </tr>
	  </table>
	</td>
  </tr>
</table>
<!-- end data table -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td>
    <IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
  </td></tr>
</table>

<!-- Start Page Action Buttons -->
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
	<td width="100%" align="left" class="TBLO_XXS_L" nowrap>
	  <% String deny = " "+userAdminLocale.get("DENY")+" ";
	     String cancel = " "+userAdminLocale.get("CANCEL")+" ";%>
	  <input type="submit"
	         name="<%=UserAdminLogic.performUsersDenyAction%>"
	         class="BTN_LN"
	         tabindex="0"
	         value="<%=deny%>">&nbsp;
	  <input class="BTN_LN"
	         type="submit"
	         tabindex="0"
	         name="<%=UserAdminLogic.backToUnapprovedUserListAction%>"
	         value="<%=cancel%>">
	</td>
  </tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

