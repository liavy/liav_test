<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>

<% if ( util.isServlet23() ) proxy.setResponseContentType("text/html; charset=utf-8"); %>
<%-- start jsp page attribute setting --%>
<%@ page session="true"%>
<%@ page import="com.sap.security.api.IUser"%>
<%com.sap.security.core.admin.LanguagesBean languages = (com.sap.security.core.admin.LanguagesBean) proxy.getSessionAttribute("languages");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>
<jsp:useBean id="companySelect"
			       class="com.sap.security.core.admin.CompanySelectBean"
			       scope="request" />
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<%-- end of page attribute setting --%>


<% boolean cEnabled = ((Boolean)proxy.getSessionAttribute(SelfRegLogic.enableCompanyReg)).booleanValue();
   boolean susEnabled = ((Boolean)proxy.getSessionAttribute(SelfRegLogic.enableSUSPlugin)).booleanValue();%>


<%if (!inPortal) {%>
<HTML>
<HEAD>
  <TITLE><%=userAdminLocale.get("SELF_REGISTRATION")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
<script language="JavaScript" src="js/adminutil.js"></script>
</HEAD>
<%}%>

<%if (!inPortal) {%>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="putFocus(0,0);">
<%}%>

<center>
<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
<tr>

<!-- Start Middle 780pxl Content space -->
	<td width="780" height="100%" valign="top" class="TB_CNT_BG">
<!-- Start Content -->
<table cellpadding="0" cellspacing="0" border="0" width="780" height="100%">
    <tr>
    <!-- Start Fuction Icons Shadow -->
    <td width="100%" valign="top" class="SIDE_N_BG">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
              <!-- td background="<%=webpath%>layout/topbkgd.gif" width="100%"><img src="<%=webpath%>layout/sp.gif" height="4" border="0" alt=""></td -->
          </tr>
        </table>
    </td>
    <!-- End Fuction Icons Shadow -->
    </tr>

	<tr>
	<!-- Start Transactional Content -->
    <td width="100%" valign="top">

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    </table>

    <form name="selfRegForm"
          method="post"
          action="<%=selfRegAlias%>">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h2">
    
 			<tr>
      	<h1><td colspan="2" valign="Top" height="72" class="SEC_TB_TD">
      	    <span tabindex="0"><%=userAdminLocale.get("WELCOME_TO_REGISTRATION")%></span>
      	    </td></h1>
			</tr>
    
    <tr><td colspan="2" class="TX_XSB_DGRAY">
    &nbsp;<span tabindex="0"><%=userAdminLocale.get("NEW_USER_REGISTRATION")%></span>
    </td></tr>
    <tr>
    <td class="TBLO_XXS_L" colspan="2">
    &nbsp;&nbsp;<span tabindex="0"><%=userAdminLocale.get("USE_THE_FORM_BELOW")%></span>
    </td>
    </tr>

    <% if ( error.isError() ) { %>
    <tr><td class="TX_ERROR_XSB" colspan="2">
      <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
      <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
    </td></tr>
    <% } %>

    <tr><td colspan="2"><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%=userAccount.logonuid%>"><%=userAdminLocale.get("USER_ID")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XXS" nowrap><input type="text"
                                         size="20"
                                         tabindex="0"
                                         style="width: 2in"
                                         id="<%=userAccount.logonuid%>"
                                         name="<%=userAccount.logonuid%>"
                                         value="<%=userAccount.getLogonUid()%>">
        </td>
    </tr>
    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%=userAccount.password%>"><%=userAdminLocale.get("PASSWORD")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XXS" nowrap><input type="password"
                                         size="20"
                                         tabindex="0"
                                         style="width: 2in"
                                         id="<%= userAccount.password %>"
                                         name="<%= userAccount.password %>"
                                         value="<%=userAccount.getPassword()%>"></td>
    </tr>
    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%=userAccount.passwordconfirm%>"><%= userAdminLocale.get("PASSWORD_CONFIRM") %>:<span class="required">*</span></LABEL></td>
        <td class="TX_XXS" nowrap><input type="password"
                                         size="20"
                                         tabindex="0"
                                         style="width: 2in"
                                         id="<%= userAccount.passwordconfirm %>"
                                         name="<%= userAccount.passwordconfirm %>"
                                         value="<%=userAccount.getPasswordConfirm()%>"></td>
    </tr>
    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%= user.lastNameId %>"><%= userAdminLocale.get("LAST_NAME") %>:<span class="required">*</span></LABEL></td>
        <td class="TX_XXS" nowrap>
        <input type="text"
               size="20"
               tabindex="0"
               style="width: 2in"
               id="<%= user.lastNameId %>"
               name="<%= user.lastNameId %>"
               value="<%=user.getLastName()%>"></td>
    </tr>
    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%= user.firstNameId %>"><%= userAdminLocale.get("FIRST_NAME") %>:<span class="required">*</span></LABEL></td>
        <td class="TX_XXS" nowrap><input type="text"
                                         size="20"
                                         style="width: 2in"
                                         tabindex="0"
                                         id="<%= user.firstNameId %>"
                                         name="<%= user.firstNameId %>"
                                         value="<%=user.getFirstName()%>"></td>
    </tr>

    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%= user.emailId %>"><%= userAdminLocale.get("EMAIL") %>:<span class="required">*</span></LABEL></td>
        <td class="TX_XXS" nowrap><input type="text"
                                         size="20"
                                         style="width: 2in"
                                         id="<%= user.emailId %>"
                                         name="<%= user.emailId %>"
                                         value="<%=user.getEmail()%>"></td>
    </tr>
    
    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%=user.salutationId%>"><%= userAdminLocale.get("SALUTATION") %>:</LABEL></td>
        <td class="TX_XXS" nowrap><input type="text"
                                         size="20"
                                         tabindex="0"
                                         style="width: 2in"
                                         id="<%=user.salutationId%>"
                                         name="<%=user.salutationId%>"
                                         value="<%=user.getSalutation()%>"></td>
    </tr>
        
    <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.preferredLanguageId%>"><%=userAdminLocale.get("LANGUAGE")%>:</LABEL></td>
        <td class="TBLO_XXS_L">
          <select id="<%=user.preferredLanguageId%>" tabindex="0" name="<%=user.preferredLanguageId%>" class="DROPDOWN">
            <option value="" <%=(user.getLocale()!=null)?"":"selected"%>><%=userAdminLocale.get("PLEASE_SELECT")%></option>
            <%=languages.getHtmlOptions(user.getPreferredLanguage())%>
        </td>
    </tr>
    <% if ( cEnabled ) { %>
        <% if ( susEnabled ) { %>
        <tr>
            <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.regId%>"><%=userAdminLocale.get("REG_ID")%>:</LABEL></td>
            <td class="TX_XXS" nowrap><input type="text"
                                             size="20"
                                             tabindex="0"
                                             style="width: 2in"
                                             id="<%=user.regId%>"
                                             name="<%=user.regId%>"
                                             value="<%=user.getRegId()%>"></td>
        </tr>
        <% } else { %>
        <tr>
            <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=companySelect.companySearchNameId%>"><%= userAdminLocale.get("COMPANY") %>:</LABEL></td>
            <td class="TX_XXS" nowrap>
                  <input type="text"
                         size="20"
                         tabindex="0"
                         style="width: 2in"
                         id="<%=companySelect.companySearchNameId%>"
                         name="<%=companySelect.companySearchNameId%>"
                         value="<%=util.filteringSpecialChar(companySelect.getCompanySearchName())%>">
                &nbsp;<img id="<%=userAdminLocale.get("SEARCHCOMPANY")%>"
                           src="<%=webpath%>layout/search.gif"
                           width="24"
                           height="20"
                           border="0"
                           tabindex="0"
                           alt="<%=userAdminLocale.get("SEARCHCOMPANY")%>"
                           title="<%=userAdminLocale.get("SEARCHCOMPANY")%>"
                           onClick="javascript:doSELFREGFORMSubmit('<%=SelfRegLogic.searchCompanyAction%>');"
                           CLASS="IMG_BTN" >
            </td>
        </tr>
        <% } %>
    <% } %>

      <tr>
        <td class="TBLO_XXS_R" nowrap><%=userAdminLocale.get("ACCESSIBILITY_LEVEL")%>:</td>
        <td class="TX_XS" nowrap>
            <input type="checkbox"
                   id="<%=user.accessibilitylevelId%>"
                   tabindex="0"
                   <%= (addCheckBoxStyle==true)?"class=\"noborder\"":"" %>
                   name="<%=user.accessibilitylevelId%>"
                   value="<%=user.getAccessibilityLevel()%>"
                   <%=(user.getAccessibilityLevel()==IUser.SCREENREADER_ACCESSIBILITY_LEVEL)?"checked":""%>>
                   <label for="<%=user.accessibilitylevelId%>"><%=userAdminLocale.get("SCREENREADER_NEEDED")%></label>
        </td>
      </tr>

    <tr><td colspan="2"><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    <tr>
        <td class="TBLO_XXS_R" valign="top" nowrap></td>
        <td class="TX_XXS" nowrap>
        		<% String submit = " "+userAdminLocale.get("SUBMIT")+" ";
        		   String reset = " "+userAdminLocale.get("RESET")+" ";
        		   String cancel = " "+userAdminLocale.get("CANCEL")+" "; %>
                <input id="<%=SelfRegLogic.applyUserAction%>"
                       type="submit"
                       tabindex="0"
                       class="BTN_LB"
                       name="<%=SelfRegLogic.applyUserAction%>"
                       value="<%=submit%>">&nbsp;
                <input id="<%=SelfRegLogic.resetApplyUserAction%>"
                       type="submit"
                       tabindex="0"
                       class="BTN_LN"
                       name="<%=SelfRegLogic.resetApplyUserAction%>"
                       value="<%=reset%>">&nbsp;
                <input id="<%=SelfRegLogic.cancelRegAction%>"
                       type="submit"
                       tabindex="0"
                       class="BTN_LN"
                       name="<%=SelfRegLogic.cancelRegAction%>"
                       value="<%=cancel%>">
        </td>
    </tr>
    <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    </table></form>

<!-- End Page Action Buttons -->
<%@ include file="contextspecific_includes_bottom.txt" %>

