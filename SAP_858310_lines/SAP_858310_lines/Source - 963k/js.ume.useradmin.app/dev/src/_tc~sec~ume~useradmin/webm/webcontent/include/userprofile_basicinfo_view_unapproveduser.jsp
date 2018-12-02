<%-- to include this page requires:
<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.core.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
--%>

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
    <table cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
        <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""><input type="hidden" name="<%=user.uidId%>" value="<%=user.getUid()%>"></td></tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("USER_ID")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><%=userAccount.getLogonUid()%><input type="hidden" name="<%=userAccount.logonuid%>" value="<%=userAccount.getLogonUid()%>"></td>
        </tr>
        
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap width="30%"><%=userAdminLocale.get("LAST_NAME")%>:</td>
        <td class="TX_XS" tabIndex="0" width="70%" nowrap><UM:encode><%=user.getLastName()%></UM:encode><input type="hidden" name="<%=user.lastNameId%>" value="<%=user.getLastName()%>"></td>
        </tr>
        
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("FIRST_NAME")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><UM:encode><%=user.getFirstName()%></UM:encode><input type="hidden" name="<%=user.firstNameId%>" value="<%=user.getFirstName()%>"></td>
        </tr>
        
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("EMAIL")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><UM:encode><%=user.getEmail()%></UM:encode><input type="hidden" name="<%=user.emailId%>" value="<%=user.getEmail()%>"></td>
        </tr>
        <tr>
        
        <% if ( UserAdminHelper.hasAccess(proxy.getActiveUser(), UserAdminHelper.MANAGE_ALL_COMPANIES) ) { %>
        <td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("COMPANY")%>:</td>
        <td class="TX_XS" tabindex="0" nowrap><UM:encode><%=self.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0]%></UM:encode>                
        </td>
        </tr>
        <% } %>
        
        <% if (orgReq) { %>
        <tr>
        <td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("COUNTRY")%>:</td>
        <td class="TX_XS" tabindex="0" nowrap><%=user.getCountry()%></UM:encode><input type="hidden" name="<%=user.countryId%>" value="<%=user.getCountry()%>"></td>
        </tr>
        <% } %>  
                  
        <tr>
        <td class="TBLO_XXS_R" tabindex="0" nowrap><%=userAdminLocale.get("COMMENTS")%>:</td>
        <td class="TX_XS" tabindex="0" nowrap><UM:encode><%=user.getNoteToAdmin()%></UM:encode><input type="hidden" name="<%=user.noteToAdmin%>" value="<%=user.getNoteToAdmin()%>"></td>
        </tr>
        
        <tr>
        <td colspan="2"><img src="<%=webpath%>layout/sp.gif" width="1" height="2" border="0" alt=""></td>
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