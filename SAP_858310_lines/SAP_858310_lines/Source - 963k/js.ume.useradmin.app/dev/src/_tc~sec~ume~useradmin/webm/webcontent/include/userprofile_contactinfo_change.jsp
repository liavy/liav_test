<%-- to include this page requires:
<%@page import="com.sap.security.core.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<%com.sap.security.core.admin.CountriesBean countries = (com.sap.security.core.admin.CountriesBean) proxy.getSessionAttribute("countries");%>
--%>
<%com.sap.security.core.admin.TimeZonesBean timezones = (com.sap.security.core.admin.TimeZonesBean) proxy.getSessionAttribute("timezones");%>

<% String contactInfo = userAdminLocale.get("CONTACT_INFO");
   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
   entryExit.append(userAdminMessages.print(new Message("START_GROUP_BOX", contactInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     alt="<%=entryExit.toString()%>" 
     tabindex="0" 
     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=contactInfo%>').focus();}">
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
	  <table id="cc-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=contactInfo%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="cc-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('cc', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
      </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="cc-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h1">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.telephoneId);
         else
             _readOnly = false; %>       
      <td class="TBLO_XXS_R" width="30%" nowrap>
      <% if ( _readOnly) { %>
      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
         spanTitle.append(userAdminLocale.get("PHONE")).append(" ");
         spanTitle.append(user.getTelephone()).append(" ");
         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.telephoneId%>"><%=userAdminLocale.get("PHONE")%>:</LABEL></span>
      <% } else {  %>
      <LABEL FOR="<%=user.telephoneId%>"><%=userAdminLocale.get("PHONE")%>:</LABEL>
      <% } %>   
      </td>
      <td class="TX_XS" width="70%" nowrap>      
          <input id="<%=user.telephoneId%>"
                 name="<%=user.telephoneId%>"
                 tabIndex="0" 
                 value="<%=user.getTelephone()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.telephoneId%>" value="<%=user.getTelephone()%>"><% } %>
      </td>
      </tr>

      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.faxId);
         else
             _readOnly = false; %>        
        <td class="TBLO_XXS_R" nowrap>
	      <% if ( _readOnly) { %>
	      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	         spanTitle.append(userAdminLocale.get("FAX")).append(" ");
	         spanTitle.append(user.getFax()).append(" ");
	         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.faxId%>"><%=userAdminLocale.get("FAX")%>:</LABEL></span>
	      <% } else {  %>
	      <LABEL FOR="<%=user.faxId%>"><%=userAdminLocale.get("FAX")%>:</LABEL>
	      <% } %>
        </td>
        <td class="TX_XS" nowrap>       
          <input id="<%=user.faxId%>"
                 name="<%=user.faxId%>"
                 tabIndex="0" 
                 value="<%=user.getFax()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.faxId%>" value="<%=user.getFax()%>"><% } %>
        </td>
      </tr>

      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.mobileId);
         else
             _readOnly = false; %>       
        <td class="TBLO_XXS_R" nowrap>
	      <% if ( _readOnly) { %>
	      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	         spanTitle.append(userAdminLocale.get("MOBILE")).append(" ");
	         spanTitle.append(user.getMobile()).append(" ");
	         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.mobileId%>"><%=userAdminLocale.get("MOBILE")%>:</LABEL></span>
	      <% } else {  %>
	      <LABEL FOR="<%=user.mobileId%>"><%=userAdminLocale.get("MOBILE")%>:</LABEL>
	      <% } %>
        </td>
        <td class="TX_XS" nowrap>        
          <input id="<%=user.mobileId%>"
                 name="<%=user.mobileId%>"
                 tabIndex="0" 
                 value="<%=user.getMobile()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.mobileId%>" value="<%=user.getMobile()%>"><% } %>
        </td>
      </tr>

      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.streetAddressId);
         else
             _readOnly = false; %>        
        <td class="TBLO_XXS_R" nowrap>
	      <% if ( _readOnly) { %>
	      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	         spanTitle.append(userAdminLocale.get("STREET")).append(" ");
	         spanTitle.append(user.getStreetAddress()).append(" ");
	         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.streetAddressId%>"><%=userAdminLocale.get("STREET")%>:</LABEL></span>
	      <% } else {  %>
	      <LABEL FOR="<%=user.streetAddressId%>"><%=userAdminLocale.get("STREET")%>:</LABEL>
	      <% } %> 
        </td>
        <td class="TX_XS" nowrap>       
          <input id="<%=user.streetAddressId%>"
                 name="<%=user.streetAddressId%>"
                 tabIndex="0" 
                 value="<%=user.getStreetAddress()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.streetAddressId%>" value="<%=user.getStreetAddress()%>"><% } %>
        </td>
      </tr>
      
      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.cityId);
         else
             _readOnly = false; %> 	      
        <td class="TBLO_XXS_R" nowrap>
	      <% if ( _readOnly) { %>
	      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	         spanTitle.append(userAdminLocale.get("CITY")).append(" ");
	         spanTitle.append(user.getCity()).append(" ");
	         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.cityId%>"><%=userAdminLocale.get("CITY")%>:</LABEL></span>
	      <% } else {  %>
	      <LABEL FOR="<%=user.cityId%>"><%=userAdminLocale.get("CITY")%>:</LABEL>
	      <% } %>  
        </td>
		<td class="TX_XS" nowrap>	
          <input id="<%=user.cityId%>"
                 name="<%=user.cityId%>"
                 tabIndex="0" 
                 value="<%=user.getCity()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.cityId%>" value="<%=user.getCity()%>"><% } %>
        </td>
      </tr>
      
      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.stateId);
         else
             _readOnly = false; %>       
        <td class="TBLO_XXS_R" nowrap>
	      <% if ( _readOnly) { %>
	      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	         spanTitle.append(userAdminLocale.get("STATE")).append(" ");
	         spanTitle.append(user.getState()).append(" ");
	         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	      <span title="<=%spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.stateId%>"><%=userAdminLocale.get("STATE")%>:</LABEL></span>
	      <% } else {  %>
	      <LABEL FOR="<%=user.stateId%>"><%=userAdminLocale.get("STATE")%>:</LABEL>
	      <% } %>   
        </td>
        <td class="TX_XS" nowrap>        
          <input id="<%=user.stateId%>"
                 name="<%=user.stateId%>"
                 tabIndex="0" 
                 value="<%=user.getState()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.stateId%>" value="<%=user.getState()%>"><% } %>
        </td>
      </tr>

      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.zipId);
         else
             _readOnly = false; %>       
      <td class="TBLO_XXS_R" nowrap>
	      <% if ( _readOnly) { %>
	      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	         spanTitle.append(userAdminLocale.get("ZIP")).append(" ");
	         spanTitle.append(user.getZip()).append(" ");
	         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.zipId%>"><%=userAdminLocale.get("ZIP")%>:</LABEL></span>
	      <% } else {  %>
	      <LABEL FOR="<%=user.zipId%>"><%=userAdminLocale.get("ZIP")%>:</LABEL>
	      <% } %>  
      </td>
      <td class="TX_XS" nowrap>      
          <input id="<%=user.zipId%>"
                 name="<%=user.zipId%>"
                 tabIndex="0" 
                 value="<%=user.getZip()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.zipId%>" value="<%=user.getZip()%>"><% } %>
        </td>
      </tr>

      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.countryId);
         else
             _readOnly = false; %>       
      <td class="TBLO_XXS_R" nowrap>
	      <% if ( _readOnly) { %>
	      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	         spanTitle.append(userAdminLocale.get("COUNTRY")).append(" ");
	         spanTitle.append(user.getCountry()).append(" ");
	         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.countryId%>"><%=userAdminLocale.get("COUNTRY")%>:</LABEL></span><%if(orgReq){%><span class="required">*</span><%}%>
	      <% } else {  %>
	      <LABEL FOR="<%=user.countryId%>"><%=userAdminLocale.get("COUNTRY")%>:<%if(orgReq){%> <span class="required">*</span><%}%></LABEL>
	      <% } %>
      </td>
      <td class="TBLO_XS_L">      
          <select id="<%=user.countryId%>"
                  name="<%=user.countryId%>"
                  tabIndex="0" 
                  class="DROPDOWN"
                  <%if( _readOnly ){ %>DISABLED<%}%>>
            <option value=""><%=userAdminLocale.get("PLEASE_SELECT")%></option><%=countries.getHtmlOptions(user.getCountry())%>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.countryId%>" value="<%=user.getCountry()%>"><% } %>            
      </td>
      </tr>

      <tr>
      <% if(null!=self)
             _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.timeZoneId);
         else
             _readOnly = false; %>         
      <td class="TBLO_XXS_R" nowrap>
      <% if ( _readOnly) { %>
      <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
         spanTitle.append(userAdminLocale.get("TIME_ZONE")).append(" ");
         spanTitle.append(user.getTimeZone()).append(" ");
         spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
      <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=user.timeZoneId%>"><%=userAdminLocale.get("TIME_ZONE")%>:</LABEL></span>
      <% } else {  %>
      <LABEL FOR="<%=user.timeZoneId%>"><%=userAdminLocale.get("TIME_ZONE")%>:</LABEL>
      <% } %>             
      </td>
      <td class="TBLO_XS_L">    
          <select id="<%=user.timeZoneId%>"
                  name="<%=user.timeZoneId%>"
                  tabIndex="0" 
                  class="DROPDOWN"
                  <%if( _readOnly ) { %>DISABLED<%}%>>
            <option value=""><%=userAdminLocale.get("PLEASE_SELECT")%></option><%=timezones.getHtmlOptions(user.getTimeZone())%>
          <% if ( _readOnly) { %><input type="hidden" name="<%=user.timeZoneId%>" value="<%=user.getTimeZone()%>"><% } %>            
      </td>
      </tr>

      <tr><td colspan="2">
        <IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
      </td></tr>
    </table></div>
  </td></tr>
</table>
<% entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("END_GROUP_BOX", contactInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     id="<%=contactInfo%>"
     alt="<%=entryExit.toString()%>"
     tabindex="0">