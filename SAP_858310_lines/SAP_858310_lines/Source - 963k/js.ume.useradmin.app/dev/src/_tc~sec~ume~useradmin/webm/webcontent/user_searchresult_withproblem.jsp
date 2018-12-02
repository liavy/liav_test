<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.ISearchResult"%>
<%@page import="com.sap.security.api.UMFactory"%>

<%@ include file="proxy.txt" %>

<% int state = ((Integer)proxy.getRequestAttribute(UserAdminLogic.searchResultState)).intValue();
   int size = ((Integer)proxy.getRequestAttribute(UserAdminLogic.searchResultSize)).intValue();
   int maxhits_w = UMFactory.getProperties().getNumber(UserAdminCustomization.UM_ADMIN_SEARCH_MAXHITS_W, 200);
   int maxhits = UMFactory.getProperties().getNumber(UserAdminCustomization.UM_ADMIN_SEARCH_MAXHITS, 10000);
   StringBuffer msg = new StringBuffer();
   Message msgId = null;
   if ( ISearchResult.SEARCH_RESULT_INCOMPLETE == state ) {
       msgId = new Message(UserAdminMessagesBean.SEARCH_RESULT_INCOMPLETE);
   } else if ( ISearchResult.SIZE_LIMIT_EXCEEDED == state ) {
       msgId = new Message(UserAdminMessagesBean.SEARCH_RESULT_SIZE_LIMIT_EXCEEDED);
   } else if ( ISearchResult.TIME_LIMIT_EXCEEDED  == state ) {
       msgId = new Message(UserAdminMessagesBean.SEARCH_RESULT_TIME_LIMIT_EXCEEDED);
   } else if ( ISearchResult.SEARCH_RESULT_OK == state ) {
       if ( size > maxhits ) {
           msgId = new Message(UserAdminMessagesBean.SEARCH_RESULT_BEYOND_MAXHITS, new Integer[]{new Integer(size), new Integer(maxhits)});
       } else {
           msgId = new Message(UserAdminMessagesBean.SEARCH_RESULT_BEYOND_MAXHITS_W, new Integer[]{new Integer(size), new Integer(maxhits_w)});
       }
   } else {
       msgId = new Message(UserAdminMessagesBean.SEARCH_RESULT_UNDEFINED);
   }
   msg.append(userAdminMessages.print(msgId));
   msg.append(" ");
   msg.append(userAdminMessages.print(new Message(UserAdminMessagesBean.SEARCH_RESULT_TO_SHOW)));
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
<script>
function doSomething(action) {
    var frm = document.forms["searchresultwithproblem"];
    
    inputTag1 = document.createElement("input");
    inputTag1.setAttribute("name", "problem");
    inputTag1.setAttribute("type", "hidden");
    inputTag1.setAttribute("value", "");
    frm.appendChild(inputTag1);    
    
    inputTag2 = document.createElement("input");
    inputTag2.setAttribute("name", action);
    inputTag2.setAttribute("type", "hidden");
    inputTag2.setAttribute("value", "");
    frm.appendChild(inputTag2);
    
    frm.submit();
}
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
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
  <span tabindex="0"><UM:encode><%=msg.toString()%></UM:encode></span>
  </td></tr>
</table>
<!-- End Section Description -->

<!-- Start Result Table-->
<form id="searchresultwithproblem" name="searchresultwithproblem" method="post" action="<%=userAdminAlias%>">
<TABLE cellpadding="2" cellspacing="0" border="0" width="98%" id="h0">
      <% if ( size > 0 ) { %>
      <tr>
       <td class="TBLO_XXS_R" nowrap><input type="radio" tabindex="0" id="toShow" name="toChoose" class="noborder" onClick="doSomething('<%=UserAdminLogic.toShowLastSearchResultAction%>')">
       </td>
       <td class="TX_XS" nowrap><label for="toShow"><%=userAdminLocale.get("CONTINUE_WITH_DISPLAY_RESULT")%></label>
       </td>
      </tr>
      <% } %>
      <tr>
       <td class="TBLO_XXS_R" nowrap><input type="radio" tabindex="0" id="reSearch" name="toChoose" class="noborder" onClick="doSomething('<%=UserAdminLogic.searchUsersAction%>')">
       </td>
       <td class="TX_XS" nowrap><label for="reSearch"><%=userAdminLocale.get("GO_BACK_TO_SEARCH")%></label>
       </td>
      </tr>
</table>
</form>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

