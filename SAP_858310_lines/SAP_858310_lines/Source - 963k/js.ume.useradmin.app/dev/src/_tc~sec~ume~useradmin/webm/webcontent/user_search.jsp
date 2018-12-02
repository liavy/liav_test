<%@ taglib uri="UM" prefix="UM" %>
<%@page import="java.util.Hashtable"%>
<%@page import="com.sap.security.api.IRole"%>
<%@page import="com.sap.security.api.IUser"%>
<%@page import="com.sap.security.core.admin.UserAccountBean"%>

<%@ include file="proxy.txt" %>

<%CountriesBean countries = (CountriesBean) proxy.getSessionAttribute("countries");%>
<%LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute("languages");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>
<jsp:useBean id="companySearchResult"
             class="com.sap.security.core.admin.CompanySearchResultBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

<% String comManualName = (String) proxy.getRequestAttribute(CompanySelectBean.companySearchNameId);
   if ( null == comManualName ) { comManualName = util.empty; }
   Boolean mode = (Boolean) proxy.getRequestAttribute("allmode");
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
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
    function doCompanySelect() {
      object = document.getElementById("searchuser");
      inputTag1 = document.createElement("input");
      inputTag1.setAttribute("name", "<%=UserAdminLogic.selectCompanyAction%>");
      inputTag1.setAttribute("type", "hidden");
      inputTag1.setAttribute("value", "");
      object.appendChild(inputTag1);
      inputTag2 = document.createElement("input");
      inputTag2.setAttribute("name", "<%=UserAdminLogic.preRequest%>");
      inputTag2.setAttribute("type", "hidden");
      inputTag2.setAttribute("value", "<%=UserAdminLogic.searchUsersAction%>");
      object.appendChild(inputTag2);
      object.submit();
    }
</script>

<form id="searchuser"
      name="searchuser"
      method="post"
      action="<%=userAdminAlias%>">
<%@ include file="contextspecific_includes_top.txt" %>

<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("SEARCH_USER_HEADER")%></span>
      </td></tr>
</table>

<!-- End Section Header -->

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
    <tr><td width="100%" class="TBLO_XXS_L">
        <span tabindex="0"><%=userAdminLocale.get("SEARCH_USER_DESCRIPTION")%></span>
    </td></tr>
</table>

<% if ( error.isError() ) { %>
<!-- Start Error Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_ERROR_XSB">
    <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0"/>&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Error Msg -->
<% } %>

<!-- Start Info Msg-->
<% if ( info.isInfo() ) { %>
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
<tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_warning.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
</td></tr>
</table>
<% } %>
<!-- End Info Msg -->

<% String basicInfo = userAdminLocale.get("BASIC_INFO");
   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
   entryExit.append(userAdminMessages.print(new Message("START_GROUP_BOX", basicInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     alt="<%=entryExit.toString()%>" 
     tabindex="0" 
     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=basicInfo%>').focus();}">
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
    <tr><td class="TBLO_XXS_L">
        <table id="sb-hd" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
			<td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=basicInfo%></td>
			<td class="BGCOLOR_ICONOPEN" align="right" width="20">
			<img id="sb-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 title="<%=altmin%>"
                 onClick="javascript:expandME('sb', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
			</tr>
        </table>
    </td></tr>
    <tr><td class="TBDATA_CNT_ODD_BG"><div id="sb-bd">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
			<tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.logonuid%>"><%=userAdminLocale.get("USER_ID")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
				<input id="<%=userAccount.logonuid%>"
				       type="text"
				       tabIndex="0"
					   name="<%=userAccount.logonuid%>"
					   value="<%=userAccount.getLogonUid()%>"
					   size="20"
					   style="width: 2in"></td>
			</tr>

     		<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.lastNameId%>"><%=userAdminLocale.get("LAST_NAME")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
				<input id="<%=user.lastNameId%>"
				       type="text"
				       tabIndex="0"
					   name="<%=user.lastNameId%>"
					   value="<%=user.getLastName()%>"
					   size="20"
					   style="width: 2in"></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=user.firstNameId%>"><%=userAdminLocale.get("FIRST_NAME")%>:</LABEL></td>
				<td class="TX_XS" width="70%" nowrap>
                <input id="<%=user.firstNameId%>"
                       type="text"
                       tabIndex="0"
                       name="<%=user.firstNameId%>"
                       value="<%=user.getFirstName()%>"
                       size="20"
                       style="width: 2in"></td>
			</tr>

			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.emailId%>"><%=userAdminLocale.get("EMAIL")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
				<input id="<%=user.emailId%>"
				       type="text"
				       tabIndex="0"
					   name="<%=user.emailId%>"
					   value="<%=user.getEmail()%>"
					   size="20"
					   style="width: 2in"></td>
			</tr>
			
			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.salutationId%>"><%=userAdminLocale.get("SALUTATION")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
				<input id="<%=user.salutationId%>"
				       type="text"
				       tabIndex="0"
					   name="<%=user.salutationId%>"
					   value="<%=user.getSalutation()%>"
					   size="20"
					   style="width: 2in"></td>
			</tr>
						
			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.preferredLanguageId%>"><%=userAdminLocale.get("LANGUAGE")%>:</LABEL></td>
				<td class="TBLO_XS_L">
          		<select id="<%=user.preferredLanguageId%>"
                  name="<%=user.preferredLanguageId%>"
                  tabIndex="0"
                  class="DROPDOWN">
            <option value="" <%=(user.getLocale()!=null)?"":"selected"%>><%=userAdminLocale.get("PLEASE_SELECT")%></option>
            <%=languages.getHtmlOptions(user.getPreferredLanguage())%>
				</td>
			</tr>

            <% if ( UserAdminCustomization.isCompanyFieldEnabled(proxy) ) { %>
			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=companySearchResult.RESULT_COMPANY_NAME%>"><%=userAdminLocale.get("COMPANY")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
				<input id="<%=companySearchResult.RESULT_COMPANY_NAME%>"
					type="text"
					tabIndex="0"
					name="<%=companySearchResult.RESULT_COMPANY_NAME%>"
					value="<%=comManualName==util.empty?util.filteringSpecialChar(companySearchResult.getCompanyName()):comManualName%>"
					size="20"
					style="width: 2in">&nbsp;
			    <% String cid = companySearchResult.getCompanyId();
                   if ( null != cid ) { %>
                    <% if ( !"".equals(cid) ) { %>
                    <input type="hidden"
                        name="<%=companySearchResult.RESULT_COMPANY_ID%>"
                        value="<%=util.filteringSpecialChar(cid)%>">
                    <% } else { %>
                    <input type="hidden"
                        name="<%=user.companyModeId%>"
                        value="<%=user.individualUsersMode%>">
                    <% } %>
			    <% } %>
			    <img id="<%=userAdminLocale.get("SELECT_COMPANY")%>"
			         src="<%=webpath%>layout/search.gif"
			         width="24"
			         height="20"
			         border="0"
			         tabindex="0"
			         alt="<%=userAdminLocale.get("SELECT_COMPANY")%>"
                     title="<%=userAdminLocale.get("SELECT_COMPANY")%>"
			         onClick="javascript:doCompanySelect();"
			         CLASS="IMG_BTN" >
				</td>
			</tr>
			<% } %>

            <% if ( null != mode ) { %>
                <% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) { %>
                    <input type="hidden" name="<%=user.companyModeId%>" value="<%=user.companyUsersMode%>">
                <% } %>
            <% } else { %>
                <% if (null != companySearchResult.getCompanyId()) { %>
                <input type="hidden" name="<%=user.companyModeId%>" value="<%=user.companyUsersMode%>">
                <% } else { %>
                <input type="hidden" name="<%=user.companyModeId%>" value="<%=user.individualUsersMode%>">
                <% } %>
            <% } %>

			<tr><td colspan="2">
				<IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
			</td></tr>
        </table></div>
    </td></tr>
</table>
<% entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("END_GROUP_BOX", basicInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     id="<%=basicInfo%>"
     alt="<%=entryExit.toString()%>"
     tabindex="0">

<table cellpadding="0" cellspacing="0" border="0">
<tr><td>
    <IMG height="4" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
</td></tr>
</table>

<%@include file="/include/useraccount_search.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
        <IMG height="4" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>

<%@include file="/include/userprofile_contactinfo_search.jsp"%>

<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
        <IMG height="4" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>

<%@include file="/include/userprofile_additionalinfo_search.jsp"%>

<%-- following section could be customized by customers --%>
<%@include file="/include/userprofile_customizedattri_search.jsp"%>
<%-- end of the customized section--%>

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
        <% String go = " "+userAdminLocale.get("GO")+" ";
           String clear = " "+userAdminLocale.get("CLEAR")+" "; %>
        <input type="submit"
                  class="BTN_LB"
                  tabIndex="0"
                  name="<%=UserAdminLogic.performUserSearchAction%>"
                  value="<%=go%>">&nbsp;
            <input class="BTN_LN"
                   type="submit"
                   tabIndex="0"
                   name="<%=UserAdminLogic.clearUserSearchAction%>"
                   value="<%=clear%>">
        </td>
    </tr>
</table>
</form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

