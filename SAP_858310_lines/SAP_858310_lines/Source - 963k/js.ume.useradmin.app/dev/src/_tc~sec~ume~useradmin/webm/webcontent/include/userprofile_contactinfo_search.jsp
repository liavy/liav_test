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
	  <table id="sc-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=userAdminLocale.get("CONTACT_INFO")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="sc-exp"
                 tabindex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('sc', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
      </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="sc-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h1">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <tr>
      <td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=user.telephoneId%>"><%=userAdminLocale.get("PHONE")%>:</LABEL></td>
      <td class="TX_XS" width="70%" nowrap>
          <input id="<%=user.telephoneId%>"
                 name="<%=user.telephoneId%>"
                 tabIndex="0" 
                 value="<%=user.getTelephone()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
        </tr>

        <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.faxId%>"><%=userAdminLocale.get("FAX")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.faxId%>"
                 name="<%=user.faxId%>"
                 tabIndex="0" 
                 value="<%=user.getFax()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
        </tr>

        <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.mobileId%>"><%=userAdminLocale.get("MOBILE")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.mobileId%>"
                 name="<%=user.mobileId%>"
                 tabIndex="0" 
                 value="<%=user.getMobile()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
        </tr>

        <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.streetAddressId%>"><%=userAdminLocale.get("STREET")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.streetAddressId%>"
                 name="<%=user.streetAddressId%>"
                 tabIndex="0" 
                 value="<%=user.getStreetAddress()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
        </tr>

        <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.cityId%>"><%=userAdminLocale.get("CITY")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.cityId%>"
                 name="<%=user.cityId%>"
                 tabIndex="0" 
                 value="<%=user.getCity()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.stateId%>"><%=userAdminLocale.get("STATE")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
		          <input id="<%=user.stateId%>"
		                 name="<%=user.stateId%>"
		                 tabIndex="0" 
		                 value="<%=user.getState()%>"
		                 type="text"
		                 size="20"
		                 style="width: 2in"></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.zipId%>"><%=userAdminLocale.get("ZIP")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
          		  <input id="<%=user.zipId%>"
		                 name="<%=user.zipId%>"
		                 tabIndex="0" 
		                 value="<%=user.getZip()%>"
		                 type="text"
		                 size="20"
		                 style="width: 2in"></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.countryId%>"><%=userAdminLocale.get("COUNTRY")%>:</LABEL></td>
        <td class="TBLO_XS_L">
          <select id="Country" tabIndex="0" name="<%=user.countryId%>" class="DROPDOWN">
            <option value=""><%=userAdminLocale.get("PLEASE_SELECT")%></option><%=countries.getHtmlOptions(user.getCountry())%>
        </td>
        </tr>

        <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.timeZoneId%>"><%=userAdminLocale.get("TIME_ZONE")%>:</LABEL></td>
        <td class="TBLO_XS_L">
          <select id="<%=user.timeZoneId%>" tabIndex="0" name="<%=user.timeZoneId%>" class="DROPDOWN">
            <option value=""><%=userAdminLocale.get("PLEASE_SELECT")%></option><%=timezones.getHtmlOptions(user.getTimeZone())%>
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