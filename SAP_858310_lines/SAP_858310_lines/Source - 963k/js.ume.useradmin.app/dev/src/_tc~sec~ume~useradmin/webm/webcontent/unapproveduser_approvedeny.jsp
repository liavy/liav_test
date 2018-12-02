<%@ taglib uri="UM" prefix="UM" %>

<%@ include file="proxy.txt" %>

<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute("countries");%>

<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>

<% IUser self = user.getUser();
   String orgUnitName = (String) proxy.getRequestAttribute(UserAdminLogic.orgUnitName);
   String messageToRequestor = (String) proxy.getRequestAttribute(user.messageToRequestor);

   if ( null ==  messageToRequestor) {
       messageToRequestor =  new String();
   }
   if ( null == orgUnitName ) {
       orgUnitName = new String();
   }

   UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   boolean orgReq = uac.isOrgUnitRequired(proxy, self);

   String parent = (String) proxy.getSessionAttribute(UserAdminLogic.parent);
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
%>

<%-- start html--%>
<%if (!inPortal) {%>
<html>
<head>
<TITLE><%=userAdminLocale.get("USER_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
<script language="JavaScript" src="js/roleList.js"></script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<script language="JavaScript">
  function doOrgUnitSearch() {
      var frm = document.getElementById("approveuser");
      inputTag = document.createElement("input");
      inputTag.setAttribute("name", "<%=UserAdminLogic.searchOrgUnitAction%>");
      inputTag.setAttribute("type", "hidden");
      inputTag.setAttribute("value", "");
      frm.appendChild( inputTag );
      inputTag2 = document.createElement("input");
      inputTag2.setAttribute("name","<%=UserAdminLogic.preRequest%>");
      inputTag2.setAttribute("type","hidden");
      inputTag2.setAttribute("value","<%=UserAdminLogic.userApproveOrDenyAction%>");
      frm.appendChild( inputTag2 );
      frm.submit();
  }
</script>

<%@ include file="contextspecific_includes_top.txt" %>

<%-- Start Section Header --%>
<a name="main"></a>

<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
  <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
    <span tabindex="0"><%=userAdminLocale.get("NEWUSER_REQ_DETLS_HEADER")%></span>
  </td></tr>
</table>
<!-- End Section Header -->

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
    <span tabindex="0"><%=userAdminLocale.get("NEWUSER_REQ_DETLS_DESP")%></span>
  </td></tr>
</table>

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

<form id="approveuser"
      name="approveuser"
      method="post"
      action="<%=userAdminAlias%>">
<table cellspacing="1" cellpadding="1" border="0" width="98%">
  <tr><td class="TBLO_XXS_R" colspan="2" nowrap>
    <%@ include file="/include/userprofile_basicinfo_view_unapproveduser.jsp"%>
    <%-- include file="/include/userprofile_contactinfo_change.jsp"--%>
    <%-- include file="/include/userprofile_additionalinfo_change.jsp"--%>

    <%-- following section could be customized by customers --%>
    <%--include file="/include/userprofile_customizedattri_change.jsp"--%>
    <%-- end of the customized section--%>

    <table cellpadding="0" cellspacing="0" border="0">
      <tr><td>
        <IMG height="10" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
      </td></tr>
    </table>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr><td><hr size="1" width="100%"></td></tr>
    </table>
  </td></tr>
	
  <tr><td>
	<table width="98%" border="0" cellspacing="1" cellpadding="2">
	  <tr><td colspan="2">
        <IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
      </td></tr>
      <% if ( orgReq ) { %>
	  <tr>
		<td class="TBLO_XXS_R" nowrap>
		  <% StringBuffer content= new StringBuffer(100);
		     content.append(UserAdminLogic.orgUnitName).append(" ");
		     content.append(orgUnitName).append(" ");
		     content.append(userAdminLocale.get("NOT_AVAILABLE"));%>
		  <span title="<%=content.toString()%>" tabindex="0"><LABEL FOR="<%=UserAdminLogic.orgUnitName%>"><%=userAdminLocale.get("ORGUNIT")%>:</LABEL></span><span class="required">*</span>
		</td>
		<td class="TX_XS" nowrap>
          <input type="hidden" name=<%=user.orgUnitId%> value="<%=util.filteringSpecialChar(user.getOrgUnit())%>">
          <input id="<%=UserAdminLogic.orgUnitName%>"
                 type="text"
                 name="<%=UserAdminLogic.orgUnitName%>"
                 value="<%=util.filteringSpecialChar(orgUnitName)%>"
                 size="20"
                 style="width: 2in"
                 disabled>&nbsp;
          <img src="<%=webpath%>layout/search.gif" width="24" height="20" border="0" alt="<%=userAdminLocale.get("SELECT_ORGUNIT")%>" onClick="doOrgUnitSearch()" CLASS="IMG_BTN">
        </td>
	  </tr>
      <% } %>
	</table>
  </td></tr>
  
  <tr>
    <td class="TBLO_XSB" colspan="2"><span tabindex="0"><%=userAdminLocale.get("MESSAGES_TO_REQUESTOR")%></span></td>
  </tr>

  <tr><td>
	<table cellpadding="2" cellspacing="0" border="0">
      <tr>
        <td class="TBLO_XXS_R" nowrap valign="top">
          <LABEL FOR="<%=user.messageToRequestor%>"><%=userAdminLocale.get("MESSAGE")%>:</LABEL>&nbsp;
        </td>
		<td nowrap>
          <textarea id="<%=user.messageToRequestor%>"
                    name="<%=user.messageToRequestor%>"
                    tabindex="0"
                    wrap="soft"
                    cols="25"
                    rows="3"
                    style="width: 2.5in"
                    CLASS="TX_XXS"
                    value="<%=util.filteringSpecialChar(messageToRequestor)%>"><UM:encode><%=messageToRequestor%></UM:encode></textarea>
        </td>
      </tr>
    </table>
  </td></tr>
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
    <input type="submit"
           class="BTN_LB"
           tabindex="0"
           name="<%=UserAdminLogic.performUserApproveAction%>"
           value="&nbsp;<%=userAdminLocale.get("APPROVE")%>&nbsp;">&nbsp;
    <input type="submit"
           class="BTN_LN"
           tabindex="0"
           name="<%=UserAdminLogic.performUserDenyAction%>"           
           value="&nbsp;<%=userAdminLocale.get("DENY")%>&nbsp;">&nbsp;
    <input type="submit"
           class="BTN_LN"
           tabindex="0"
           name="<%=UserAdminLogic.backToUnapprovedUserListAction%>"
           value="&nbsp;<%=userAdminLocale.get("CANCEL")%>&nbsp;">
  </td>
</tr>
</table></form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

