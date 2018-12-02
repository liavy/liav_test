<%-- to include this page requires:
<%@page import="com.sap.security.core.admin.*"%>
<%UserAdminLocaleBean userAdminLocale = (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<%com.sap.security.core.admin.LanguagesBean languages = (com.sap.security.core.admin.LanguagesBean) proxy.getSessionAttribute("languages");%>
--%>
<%@page import="com.sap.security.api.logon.ILoginConstants"%>
<% boolean isSelfRegCompanyUserAllowed = uac.isSelfRegCompanyUserAllowed();
   boolean isLocked = (account==null)?false:account.isLocked();
   boolean unapproved = (self.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)==null)?false:true;
   boolean toShowPswdFields = true;
   if ( !UserAdminCustomization.isPasswordChangeAllowed() ) {
       toShowPswdFields = false;
   } else {	
       if (UserAdminFactory.isAttributeReadOnly(account.getUniqueID(), ILoginConstants.LOGON_PWD_ALIAS))
           toShowPswdFields = false;
   }
%>

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
	  <table id="cb-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
        <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=basicInfo%></td>
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
      <tr><td colspan="2"><input type="hidden" name="<%=user.uidId%>" value="<%=user.getUid()%>"></td></tr>
      <tr>
        <td class="TBLO_XXS_R" tabIndex="0" width="30%" nowrap><%=userAdminLocale.get("USER_ID")%>:</td>
        <td class="TX_XS" tabIndex="0" width="70%" nowrap><%=userAccount.getLogonUid()%>
        <input type="hidden" name="<%=userAccount.logonuid%>" value="<%=userAccount.getLogonUid()%>"></td>
      </tr>
      <% if (toShowPswdFields) { %>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.oldpassword%>"><%=userAdminLocale.get("OLD_PASSWORD")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=userAccount.oldpassword%>"
                 name="<%=userAccount.oldpassword%>"
                 tabIndex="0"
                 value="<%=userAccount.getOldPassword()%>"
                 type="password"
                 size="20"
                 style="width: 2in"></td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.password%>"><%=userAdminLocale.get("PASSWORD")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=userAccount.password%>"
                 name="<%=userAccount.password%>"
                 tabIndex="0"
                 value="<%=userAccount.getPassword()%>"
                 type="password"
                 size="20"
                 style="width: 2in"></td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.passwordconfirm%>"><%=userAdminLocale.get("PASSWORD_CONFIRM")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=userAccount.passwordconfirm%>"
                 name="<%=userAccount.passwordconfirm%>"
                 tabIndex="0"
                 value="<%=userAccount.getPasswordConfirm()%>"
                 type="password"
                 size="20"
                 style="width: 2in"></td>
      </tr>
      <% } %>

      <tr>
        <% if(null!=self)
               _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.lastNameId);
           else
               _readOnly = false; %>        
        <td class="TBLO_XXS_R" nowrap>
          <% if ( _readOnly) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("LAST_NAME")).append(" ");
             spanTitle.append(user.getLastName()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL for="<%=user.lastNameId%>"><%=userAdminLocale.get("LAST_NAME")%>:</LABEL></span><span class="required">*</span>
          <% } else {  %>
          <LABEL for="<%=user.lastNameId%>"><%=userAdminLocale.get("LAST_NAME")%>:<span class="required">*</span></LABEL>
          <% } %>         
        </td>
        <td class="TX_XS" nowrap>      
          <input id="<%=user.lastNameId%>"
                 name="<%=user.lastNameId%>"
                 tabindex="0"
                 value="<%=user.getLastName()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.lastNameId%>" value="<%=user.getLastName()%>"><% } %>
        </td>
      </tr>
      
      <tr>
        <% if(null!=self)
               _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.firstNameId);
           else
               _readOnly = false; %>        
		<td class="TBLO_XXS_R" nowrap>
          <% if ( _readOnly) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("FIRST_NAME")).append(" ");
             spanTitle.append(user.getFirstName()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL for="<%=user.firstNameId%>"><%=userAdminLocale.get("FIRST_NAME")%>:</LABEL></span><span class="required">*</span>
          <% } else {  %>
          <LABEL for="<%=user.firstNameId%>"><%=userAdminLocale.get("FIRST_NAME")%>:<span class="required">*</span></LABEL>
          <% } %>  		  
		</td>
        <td class="TX_XS" nowrap>         
          <input id="<%=user.firstNameId%>"
                 name="<%=user.firstNameId%>"
                 tabindex="0"
                 value="<%=user.getFirstName()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.firstNameId%>" value="<%=user.getFirstName()%>"><% } %>
        </td>
      </tr>

      <tr>
        <% if(null!=self)
               _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.emailId);
           else
               _readOnly = false; %>       
        <td class="TBLO_XXS_R" nowrap>
          <% if ( _readOnly) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("EMAIL")).append(" ");
             spanTitle.append(user.getEmail()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL for="<%=user.emailId%>"><%=userAdminLocale.get("EMAIL")%>:</LABEL></span><span class="required">*</span>
          <% } else {  %>
          <LABEL for="<%=user.emailId%>"><%=userAdminLocale.get("EMAIL")%>:<span class="required">*</span></LABEL>
          <% } %>          
        </td>
        <td class="TX_XS" nowrap>        
          <input id="<%=user.emailId%>"
                 name="<%=user.emailId%>"
                 tabindex="0"
                 value="<%=user.getEmail()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.emailId%>" value="<%=user.getEmail()%>"><% } %>
        </td>
      </tr>

      <tr>
        <% if(null!=self)
               _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.salutationId);
           else
               _readOnly = false; %>      
        <td class="TBLO_XXS_R" nowrap>
          <% if ( _readOnly) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("SALUTATION")).append(" ");
             spanTitle.append(user.getSalutation()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL for="<%=user.salutationId%>"><%=userAdminLocale.get("SALUTATION")%>:</LABEL></span>
          <% } else {  %>
          <LABEL for="<%=user.salutationId%>"><%=userAdminLocale.get("SALUTATION")%>:</LABEL>
          <% } %>          
        </td>
        <td class="TX_XS" nowrap>         
          <input id="<%=user.salutationId%>"
                 name="<%=user.salutationId%>"
                 tabindex="0"
                 value="<%=user.getSalutation()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.salutationId%>" value="<%=user.getSalutation()%>"><% } %>
        </td>
      </tr>
      
      <tr>
        <% if(null!=self)
               _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), com.sap.security.core.imp.User.LOCALE);
           else
               _readOnly = false; %>       
        <td class="TBLO_XXS_R" nowrap>
          <% if ( _readOnly) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("LANGUAGE")).append(" ");
             spanTitle.append(user.getPreferredLanguage()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL for="<%=user.preferredLanguageId%>"><%=userAdminLocale.get("LANGUAGE")%>:</LABEL></span>
          <% } else {  %>
          <LABEL for="<%=user.preferredLanguageId%>"><%=userAdminLocale.get("LANGUAGE")%>:</LABEL>
          <% } %>          
        </td>
        <td class="TBLO_XS_L">        
          <select tabindex="0" id="<%=user.preferredLanguageId%>"
                  name="<%=user.preferredLanguageId%>"
                  class="DROPDOWN"
                  <%if( _readOnly ){ %>DISABLED<%}%>>
            <option value="" <%=(user.getLocale()!=null)?"":"selected"%>><%=userAdminLocale.get("PLEASE_SELECT")%></option>
            <%=languages.getHtmlOptions(user.getPreferredLanguage())%>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.preferredLanguageId%>" value="<%=user.getPreferredLanguage()%>"><% } %>            
        </td>
      </tr>

	  <% if ( UserAdminHelper.isCompanyConceptEnabled()) { %>
      <tr>
        <td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("COMPANY")%>:</td>
        <td class="TX_XS" tabindex="0" nowrap>
        <% if ( null != util.checkEmpty(user.getCompanyId()) ) { %><%=user.getCompanyName()%></td>
        <% } else { %>        
            <% if (unapproved) { %>
                <%=self.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0]%>(<%=userAdminLocale.get("UNAPPROVED")%>)
            <% } else { %>
		      <% if ( isSelfRegCompanyUserAllowed ) { %>
		          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
		             spanTitle.append(userAdminLocale.get("COMPANY")).append(" ");
		             spanTitle.append(companySearchResult.getCompanyName()).append(" ");
		             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %> 		
				  <span title="<%=spanTitle.toString()%>" tabindex="0">		      		
		            <input id="<%=companySearchResult.RESULT_COMPANY_NAME%>"
		                type="text"
		                name="<%=companySearchResult.RESULT_COMPANY_NAME%>"
		                value="<%=util.filteringSpecialChar(companySearchResult.getCompanyName())%>"
		                size="20"
		                style="width: 2in"
		                disabled ></span>&nbsp;
		            <% if (null != companySearchResult.getCompanyId()) { %>
		                <input type="hidden"
		                    name="<%=companySearchResult.RESULT_COMPANY_ID%>"
		                    value="<%=util.filteringSpecialChar(companySearchResult.getCompanyId())%>">
		            <% } %>
		             <img id="<%=userAdminLocale.get("SELECT_COMPANY")%>"
		                  src="<%=webpath%>layout/search.gif" 
		                  width="24" 
		                  height="20" 
		                  border="0" 
		                  tabindex="0"
		                  alt="<%=userAdminLocale.get("SELECT_COMPANY")%>" 
		                  onClick="javascript:doCompanySelect();" 
		                  CLASS="IMG_BTN" >

		      <% } // end of isSelfRegCompanyUserAllowed %>            
            <% } // end else of unapproved %>
        <% } // end else of util.checkEmpty(user.getCompanyId()) %>
        </td>
      </tr> 
      <% } %>

      <tr>
        <td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("ACCOUNT_STATUS")%>:</td>
        <td class="TX_XS" tabindex="0" nowrap>
          <%if (isLocked){ %>
              <%=userAdminLocale.get("DEACTIVE")%>
          <% }else{ %>
              <%=userAdminLocale.get("ACTIVE")%>
          <% }%>
        </td>
      </tr>

      <tr>
        <% if(null!=self)
               _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.accessibilitylevelId);
           else
               _readOnly = false; %>       
        <td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("ACCESSIBILITY_LEVEL")%>:</td>
        <td class="TX_XS" nowrap>        
            <input type="checkbox"
                   id="<%=user.accessibilitylevelId%>"
                   name="<%=user.accessibilitylevelId%>"
                   tabindex="0"
                   <%= (addCheckBoxStyle==true)?"class=\"noborder\"":"" %>
                   value="<%=user.getAccessibilityLevel()%>"
                   <%=(user.getAccessibilityLevel()==IUser.SCREENREADER_ACCESSIBILITY_LEVEL)?"checked":""%>
                   <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(userAdminLocale.get("SCREENREADER_NEEDED")).append(" ");
             spanTitle.append(user.getAccessibilityLevel()).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL for="<%=user.accessibilitylevelId%>"><%=userAdminLocale.get("SCREENREADER_NEEDED")%>:</LABEL></span>
          <% } else {  %>
          <LABEL for="<%=user.accessibilitylevelId%>"><%=userAdminLocale.get("SCREENREADER_NEEDED")%>:</LABEL>
          <% } %>                                                
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.accessibilitylevelId%>" value="<%=user.getAccessibilityLevel()%>"><% } %>                   
        </td>
      </tr>

      <tr>
        <td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td>
      </tr>
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