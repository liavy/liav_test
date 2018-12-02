<%-- to include this page requires:
<%@ taglib uri="UM" prefix="UM" %>
import="com.sap.security.core.admin.*"
useBean id="userAdminLocale"
             class="com.sap.security.core.admin.UserAdminLocaleBean"
             scope="session"
useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"
<%com.sap.security.core.admin.LanguagesBean languages = (com.sap.security.core.admin.LanguagesBean) proxy.getSessionAttribute("languages");%>
boolean unapproved = ((Boolean)proxy.getRequestAttribute("un_user")).booleanValue();
--%>
<%@page import="com.sap.security.api.IUserMaint"%>

<% boolean isLocked = (account==null)?false:account.isLocked();
   boolean unapproved = (subjugatedUser.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)==null)?false:true;
   String companyName = util.empty;
   if ( unapproved ) {
       companyName = util.getTP(subjugatedUser.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0]).getDisplayName();
   } else {
       if ( util.checkEmpty(subjugatedUser.getCompany())==null  ) {
           companyName = util.empty;
       } else {
           try {
               companyName = util.getTP(subjugatedUser.getCompany()).getDisplayName();
           } catch ( Exception ex ) {
               companyName = util.empty;
           }
       }
       
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
	  <table id="vb-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=userAdminLocale.get("BASIC_INFO")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="vb-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('vb', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
	    </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="vb-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr>
				<td class="TBLO_XXS_R" width="30%" tabindex="0" nowrap><%=userAdminLocale.get("USER_ID")%>:</td>
				<td class="TX_XS" width="70%" tabindex="0" nowrap><input type="hidden" name="<%=user.uidId%>" value="<%=user.getUid()%>">
                <input type="hidden" name="<%=userAccount.logonuid%>" value="<%=userAccount.getLogonUid()%>"><%=userAccount.getLogonUid()%></td>
			</tr>

     		<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("LAST_NAME")%>:</td>
				<td class="TX_XS" tabindex="0" nowrap><%=user.getLastName()%></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("FIRST_NAME")%>:</td>
				<td class="TX_XS" tabindex="0" nowrap><%=user.getFirstName()%></td>
			</tr>

			<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("EMAIL")%>:</td>
				<td class="TX_XS" tabindex="0" nowrap><%=user.getEmail()%></td>
			</tr>
			
			<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("SALUTATION")%>:</td>
				<td class="TX_XS" tabindex="0" nowrap><%=user.getSalutation()%></td>
			</tr>
						
			<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("LANGUAGE")%>:</td>
				<td class="TX_XS" tabindex="0" nowrap><%=languages.getName(user.getPreferredLanguage())%></td>
			</tr>
			
			<% if ( UserAdminHelper.isCompanyConceptEnabled()) { %>
			<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("COMPANY")%>:</td>
				<td class="TX_XS" tabindex="0" nowrap><%=companyName%><%=(unapproved==true)?" ("+userAdminLocale.get("UNAPPROVED")+")":""%></td>
			</tr>
			<% } %>
			
			<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("ACCOUNT_STATUS")%>:</td>
				<td class="TX_XS" tabindex="0" nowrap>
                  <%if (isLocked) { %>
                      <%=userAdminLocale.get("DEACTIVATED")%>
                  <% }else{ %>
                      <%=userAdminLocale.get("ACTIVE")%>
                  <% }%>
                </td>
			</tr>

            <% if ( user.getAccessibilityLevel() == IUser.SCREENREADER_ACCESSIBILITY_LEVEL ) { %>
			<tr>
				<td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("ACCESSIBILITY_LEVEL")%>:</td>
				<td tabindex="0" class="TX_XS"><%=userAdminLocale.get("SCREENREADER_NEEDED")%></td>
			</tr>
            <% } %>
            
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
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