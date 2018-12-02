<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>
<%@page import="com.sap.security.api.IUser"%>
<%@page import="com.sap.security.api.IUserAccount"%>
<%@page import="com.sap.security.api.logon.ILoginConstants"%>

<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>
<%-- end page attribute setting--%>

<% IUser performer = proxy.getActiveUser();
   boolean toDisable = false;
   IUserAccount[] account = performer.getUserAccounts();
   String logonId = "";
   if ( account.length > 0 ) {
       logonId = account[0].getLogonUid();
   }   
   if ( !UserAdminCustomization.isPasswordChangeAllowed() ) {
       toDisable = true;
   } else {	
	   if ( account.length > 0 ) {
	       if (UserAdminFactory.isAttributeReadOnly(account[0].getUniqueID(), ILoginConstants.LOGON_PWD_ALIAS)) {
	           toDisable = true; 
	       }      
	   }
   }

   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
%>

<%-- start html--%>
<%--  used in Portal environment, no header, only body --%>

<%if (!inPortal) {%>
<body leftmargin="0" topmargin="0" marginheight="0">
<%}%>

<script>
  function checkPassword(action) {
    var frm = document.forms["passwordmodify"];
    var oldpswd = frm.elements["<%=userAccount.oldpassword%>"];
    var newpswd = frm.elements["<%=userAccount.password%>"];
    var pswdconfirm = frm.elements["<%=userAccount.passwordconfirm%>"];
    if ( oldpswd.value == "" || newpswd.value == "" || pswdconfirm.value == "" ) {
        var msg = "<%=userAdminLocale.get("FILL_IN_PSWDS")%>";
        alert(msg);
        return false;
    } else {
        if ( newpswd.value != pswdconfirm.value ) {
            alert("<%=userAdminLocale.get("PASSWORDS_MISMATCH")%>");
            return false;
        } else {
            actionTag = document.createElement("input");
            actionTag.setAttribute("name", action);
            actionTag.setAttribute("type", "hidden");
            actionTag.setAttribute("value", "");
            frm.appendChild(actionTag);
            frm.submit();
        }
    }
  }
</script>
<table  height="100%" cellspacing="0" cellpadding="0" border="0">
<tr>

<!-- Start Middle 780pxl Content space -->
	<td  height="100%" valign="top" class="TB_CNT_BG">
<!-- Start Content -->
<table cellpadding="0" cellspacing="0" border="0"  height="100%">

	<tr>
	<!-- Start Transactional Content -->
    <td  valign="top">

<form id="passwordmodify" name="passwordmodify" method="post" action="<%=userAdminAlias%>">

<!-- Start Section Header -->
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif"  border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<% if ( info.isInfo() ) { %>
<!-- Start Confirm Msg-->
<table align="center" cellpadding="0" cellspacing="0"  border="0">
  <tr><td  class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_success.gif"  height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Confirm Msg -->
<% } %>

<% if ( error.isError() ) { %>
<!-- Start Error Msg-->
<table align="center" cellpadding="0" cellspacing="0"  border="0">
  <tr><td  class="TX_ERROR_XSB">
    <img src="<%=webpath%>layout/ico12_msg_error.gif"  height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Error Msg -->
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif"  border="0" alt=""></td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" >
  <tr><td class="TBLO_XXS_L">
	  <table id="cb-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
        <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=userAdminLocale.get("USER_PSWD")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" >
            <img id="cb-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"                 
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('cb', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
      </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="cb-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0"  id="h0">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif"  border="0" alt=""></td></tr>
      <% if (toDisable) { %>
      <tr><td colspan="2"><span tabindex="0"><%=userAdminLocale.get("PASSWORD_CHANGE_PROHIBITED")%></span></td></tr>
      <% } %>
      <tr><td colspan="2"><input type="hidden" name="<%=user.uidId%>" value="<%=util.filteringSpecialChar(performer.getUniqueID())%>"></td></tr>
      <tr><td colspan="2"><input type="hidden" name="<%=userAccount.logonuid%>" value="<%=util.filteringSpecialChar(logonId)%>"></td></tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap>
          <% if ( toDisable ) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("OLD_PASSWORD")).append(" ");
             spanTitle.append(userAccount.getOldPassword()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>          
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=userAccount.oldpassword%>"><%=userAdminLocale.get("OLD_PASSWORD")%>:</LABEL></span>
          <% } else {  %>
          <LABEL FOR="<%=userAccount.oldpassword%>"><%=userAdminLocale.get("OLD_PASSWORD")%>:</LABEL>
          <% } %>
        </td>
        <td class="TX_XS" nowrap>
          <input id="<%=userAccount.oldpassword%>"
                 name="<%=userAccount.oldpassword%>"
                 value="<%=userAccount.getOldPassword()%>"
                 type="password"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%}%>></td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap>
          <% if ( toDisable ) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("NEW_PASSWORD")).append(" ");
             spanTitle.append(userAccount.getPassword()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>          
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=userAccount.password%>"><%=userAdminLocale.get("NEW_PASSWORD")%>:</LABEL></span>
          <% } else {  %>
          <LABEL FOR="<%=userAccount.password%>"><%=userAdminLocale.get("NEW_PASSWORD")%>:</LABEL>
          <% } %>
        </td>
        <td class="TX_XS" nowrap>
          <input id="<%=userAccount.password%>"
                 name="<%=userAccount.password%>"
                 value="<%=userAccount.getPassword()%>"
                 type="password"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%}%>></td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap>
          <% if ( toDisable ) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("PASSWORD_CONFIRM")).append(" ");
             spanTitle.append(userAccount.getPasswordConfirm()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>          
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=userAccount.passwordconfirm%>"><%=userAdminLocale.get("PASSWORD_CONFIRM")%>:</LABEL></span>
          <% } else {  %>
          <LABEL FOR="<%=userAccount.passwordconfirm%>"><%=userAdminLocale.get("PASSWORD_CONFIRM")%>:</LABEL>
          <% } %>
        </td>
        <td class="TX_XS" nowrap>
          <input id="<%=userAccount.passwordconfirm%>"
                 name="<%=userAccount.passwordconfirm%>"
                 value="<%=userAccount.getPasswordConfirm()%>"
                 type="password"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%}%>></td>
      </tr>
    </table></div>
  </td></tr>
</table>
<!-- end data table -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif"  border="0" alt=""></td></tr>
</table>

<!-- Start Page Action Buttons -->
<table  border="0" cellpadding="0" cellspacing="0">
  <tr><td  align="left" class="TBLO_XXS_L" nowrap>
    <% String apply = " "+userAdminLocale.get("APPLY")+" ";
       String clear = " "+userAdminLocale.get("CLEAR")+" ";
       String close = " "+userAdminLocale.get("CLOSE")+" "; %>
	<input type="button"
	       name="<%=UserAdminLogic.performUserPswdChangeAction%>"
	       value="<%=apply%>"
	       class="BTN_LB"
	       onClick="checkPassword('<%=UserAdminLogic.performUserPswdChangeAction%>')">&nbsp;
	<input type="reset"
	       name="clear"
	       value="<%=clear%>"
	       class="BTN_LN">&nbsp;
	<input type="button"
	       name="close"
	       value="<%=close%>"
	       class="BTN_LN"
           onClick="javascript:top.close();">
  </td></tr>
</table>
</form>
<!-- End Page Action Buttons -->
</td></tr></table>
</td></tr></table>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

