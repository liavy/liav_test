<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>
<%@page import="com.sap.security.api.IUser"%>
<%LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute(LanguagesBean.beanId);%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>
<%-- end page attribute setting--%>

<% IUser self = user.getUser();   
   boolean toDisable = false;
   if ( UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.localeId) ) {
       toDisable = true;
   } 
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
%>

<%-- start html--%>
<%--  used in Portal environment, no header, only body --%>

<%if (!inPortal) {%>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
<tr>

<!-- Start Middle 780pxl Content space -->
	<td width="780" height="100%" valign="top" class="TB_CNT_BG">
<!-- Start Content -->
<table cellpadding="0" cellspacing="0" border="0" width="780" height="100%">

	<tr>
	<!-- Start Transactional Content -->
    <td width="100%" valign="top">

<form name="languagemodify" method="post" action="<%=userAdminAlias%>">

<!-- Start Section Header -->
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<% if ( info.isInfo() ) { %>
<!-- Start Confirm Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_success.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
    <% if (inPortal) { %>
    <script>
        EPCM.raiseEvent("urn:com.sapportals:navigation", "RefreshPersonalizePortal" , ""); 
    </script>      
    <% } %>
  </td></tr>
</table>
<!-- End Confirm Msg -->
<% } %>

<% if ( error.isError() ) { %>
<!-- Start Error Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_ERROR_XSB">
    <img src="<%=webpath%>layout/ico12_msg_error.gif" border="0" />
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Error Msg -->
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
	  <table id="cb-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
        <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=userAdminLocale.get("SET_PORTAL_LANG")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="cb-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('cb', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
      </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="cb-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <% if (toDisable) { %>
      <tr><td colspan="2"><span tabindex="0"><%=userAdminLocale.get("ATTRIBUTE_IS_READONLY")%></span></td></tr>
      <% } %>
      <tr><td colspan="2"><input type="hidden" name="<%=user.uidId%>" value="<%=user.getUid()%>"></td></tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap>
          <% if ( toDisable ) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("PORTAL_LANGUAGE")).append(" ");
             spanTitle.append(user.getPreferredLanguage()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.preferredLanguageId%>"><%=userAdminLocale.get("PORTAL_LANGUAGE")%>:</LABEL></span>
          <% } else {  %>
          <LABEL FOR="<%=user.preferredLanguageId%>"><%=userAdminLocale.get("PORTAL_LANGUAGE")%>:</LABEL>
          <% } %> 
        </td>
        <td class="TBLO_XS_L">
          <select id="<%=user.preferredLanguageId%>"
                  name="<%=user.preferredLanguageId%>"
                  tabindex="0"
                  class="DROPDOWN"
                  <% if (toDisable) {%>disabled<%}%>>
            <option value="" <%=(user.getLocale()!=null)?"":"selected"%>><%=userAdminLocale.get("PLEASE_SELECT")%></option>
            <%=languages.getHtmlOptions(user.getPreferredLanguage())%>
        </td>
      </tr>
    </table></div>
  </td></tr>
</table>
<!-- end data table -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Page Action Buttons -->
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <% String apply = " "+userAdminLocale.get("APPLY")+" ";
       String close = " "+userAdminLocale.get("CLOSE")+" "; %>
	<input type="submit"
	       name="<%=UserAdminLogic.performUserLanguageChangeAction%>"
	       tabindex="0"
	       value="<%=apply%>"
	       class="BTN_LB">&nbsp;
	<input type="button"
	       name="close"
	       tabindex="0"
	       value="<%=close%>"
	       class="BTN_LN"
           onClick="javascript:top.close();">
  </td></tr>
</table>
</form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

