<%-- to include this page requires:
<%@page import="com.sapmarkets.usermanagement.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="user"
             class="com.sapmarkets.usermanagement.admin.UserBean"
             scope="request"/>
<%com.sap.security.core.admin.LanguagesBean languages = (com.sap.security.core.admin.LanguagesBean) proxy.getSessionAttribute("languages");%>
             class="com.sapmarkets.usermanagement.admin.CompanySearchResultBean"
             scope="request"/>
--%>

<% UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   boolean toShowAutoPswdCheckBox = uac.toShowAutoPswdCheckBox();
%>
<script>
function disableOtherPswdFields(chckbox) {
    if ( chckbox.checked ) {
        document.getElementById("<%=userAccount.password%>").disabled = true;
        document.getElementById("<%=userAccount.passwordconfirm%>").disabled = true;
        document.getElementById("pswdlabel").className = "TBLO_XXS_R_D";
        document.getElementById("pswdcmlabel").className = "TBLO_XXS_R_D"; 
        document.getElementById("<%=userAccount.password%>").className = "TX_XS_D";
        document.getElementById("<%=userAccount.passwordconfirm%>").className = "TX_XS_D";        
    } else {
        document.getElementById("<%=userAccount.password%>").disabled = false;
        document.getElementById("<%=userAccount.passwordconfirm%>").disabled = false;
        document.getElementById("pswdlabel").className = "TBLO_XXS_R";
        document.getElementById("pswdcmlabel").className = "TBLO_XXS_R"; 
        document.getElementById("<%=userAccount.password%>").className = "TX_XS";
        document.getElementById("<%=userAccount.passwordconfirm%>").className = "TX_XS";        
    }
}
</script>

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
	  <table id="rb-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
        <td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("BASIC_INFO")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="rb-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 title="<%=altmin%>"
                 onClick="javascript:expandME('rb', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
      </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="rb-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <tr>
        <td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=userAccount.logonuid%>"><%=userAdminLocale.get("USER_ID")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XS" width="70%" nowrap>
          <input id="<%=userAccount.logonuid%>"
                 type="text"
                 name="<%=userAccount.logonuid%>"
                 value="<%=userAccount.getLogonUid()%>"
                 size="20"
                 style="width: 2in">
        </td>
      </tr>
      
      <% if ( toShowAutoPswdCheckBox ) { %>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.syspassword%>"><%=userAdminLocale.get("PASSWORD_SYSTEM_GENERATE_AUTO")%>:</LABEL></td>
        <td class="TX_XXS" nowrap>
          <input id="<%=userAccount.syspassword%>"
                 type="checkbox"
                 name="<%=userAccount.syspassword%>"
                 class="noborder"
                 value="<%=userAccount.isSystemGeneratedPassword()%>"
                 <%=(userAccount.isSystemGeneratedPassword()==true)?"checked":""%>
                 onClick="disableOtherPswdFields(this)" ></td>
      </tr>
      <% } %>
      
      <tr>
        <td nowrap><LABEL class="TBLO_XXS_R" id="pswdlabel" FOR="<%=userAccount.password%>"><%=userAdminLocale.get("ASSIGN_PASSWORD")%>:<span class="required">*</span></LABEL></td>
        <td  nowrap>
            <input id="<%=userAccount.password%>"
                 name="<%=userAccount.password%>"
                 value="<%=userAccount.getPassword()%>"
                 type="password"
                 size="20"
                 class="TX_XS"></td>
      </tr>
      <tr>
        <td nowrap><LABEL class="TBLO_XXS_R" id="pswdcmlabel" FOR="<%=userAccount.passwordconfirm%>"><%=userAdminLocale.get("PASSWORD_CONFIRM")%>:<span class="required">*</span></LABEL></td>
        <td  nowrap>
          <input id="<%=userAccount.passwordconfirm%>"
                 name="<%=userAccount.passwordconfirm%>"
                 value="<%=userAccount.getPasswordConfirm()%>"
                 type="password"
                 size="20"
                 class="TX_XS"></td>
      </tr>

      <tr>
        <td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=user.lastNameId%>"><%=userAdminLocale.get("LAST_NAME")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XS" width="70%" nowrap>
          <input id="<%=user.lastNameId%>"
                 type="text"
                 name="<%=user.lastNameId%>"
                 value="<%=user.getLastName()%>"
                 size="20"
                 style="width: 2in"></td>
      </tr>

      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.firstNameId%>"><%=userAdminLocale.get("FIRST_NAME")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.firstNameId%>"
                 type="text"
                 name="<%=user.firstNameId%>"
                 value="<%=user.getFirstName()%>"
                 size="20"
                 style="width: 2in"></td>
      </tr>

      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.emailId%>"><%=userAdminLocale.get("EMAIL")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.emailId%>"
                 name="<%=user.emailId%>"
                 value="<%=user.getEmail()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
      </tr>
      
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.salutationId%>"><%=userAdminLocale.get("SALUTATION")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.salutationId%>"
                 name="<%=user.salutationId%>"
                 value="<%=user.getSalutation()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
      </tr>      
      
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.preferredLanguageId%>"><%=userAdminLocale.get("LANGUAGE")%>:</LABEL></td>
        <td class="TBLO_XS_L">
          <select id="<%=user.preferredLanguageId%>"
                  name="<%=user.preferredLanguageId%>"
                  class="DROPDOWN">
            <option value="" <%=(user.getLocale()!=null)?"":"selected"%>><%=userAdminLocale.get("PLEASE_SELECT")%></option>
            <%=languages.getHtmlOptions(user.getPreferredLanguage())%>
        </td>
      </tr>

      <% String ID = util.empty; %>
      <% if ( UserAdminCustomization.isCompanyFieldEnabled(proxy) ) { %>
      <tr>
        <td class="TBLO_XXS_R" nowrap><%=userAdminLocale.get("COMPANY")%>:</td>
        <td class="TX_XS" nowrap>
        <input id="<%=companySearchResult.RESULT_COMPANY_NAME%>"
	           type="text"
	           name="<%=companySearchResult.RESULT_COMPANY_NAME%>"
	           value="<%=util.filteringSpecialChar(companySearchResult.getCompanyName())%>"
	           size="20"
	           style="width: 2in"
	           disabled >&nbsp;
        <% if ( null != companySearchResult.getCompanyId() ) { 
               ID = util.filteringSpecialChar(companySearchResult.getCompanyId()); 
           } %>
        <% if ( action.equals(UserAdminLogic.createNewUserAction) ) { %>
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
        <% } %>
        </td>
      </tr>
      <% }  else {
        ID = util.filteringSpecialChar(performer.getCompany());
      } %>
      <% if ( null == ID || "null".equalsIgnoreCase(ID) ) {  ID = util.empty; } %>
      <input type="hidden"
	         name="<%=companySearchResult.RESULT_COMPANY_ID%>"
	         value="<%=ID%>">
                
      <tr>
        <td class="TBLO_XXS_R" nowrap><%=userAdminLocale.get("ACCESSIBILITY_LEVEL")%>:</td>
        <td class="TX_XS" nowrap>
            <input type="checkbox"
                   id="<%=user.accessibilitylevelId%>"
                   name="<%=user.accessibilitylevelId%>"
                   <%= (addCheckBoxStyle==true)?"class=\"noborder\"":"" %>
                   value="<%=user.getAccessibilityLevel()%>"
                   <%=(user.getAccessibilityLevel()==IUser.SCREENREADER_ACCESSIBILITY_LEVEL)?"checked":""%>>
                   <label for="<%=user.accessibilitylevelId%>"><%=userAdminLocale.get("SCREENREADER_NEEDED")%></label>
        </td>
      </tr>
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
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