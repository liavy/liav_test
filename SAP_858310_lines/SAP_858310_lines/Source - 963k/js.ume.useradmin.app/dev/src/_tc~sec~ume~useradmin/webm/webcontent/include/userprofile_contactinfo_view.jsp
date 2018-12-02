<%-- to include this page requires:
<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.core.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
--%>
<%-- this jsp for user proprofile view --%>
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
      <table id="vc-hd" border="0" cellpadding="0" cellspacing="0">
        <tr class="TBDATA_CNT_EVEN_BG">
          <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=userAdminLocale.get("CONTACT_INFO")%></td>
          <td class="BGCOLOR_ICONOPEN" align="right" width="20">
          <img id="vc-exp"
               tabIndex="0"
               src="<%=webpath%>layout/icon_open.gif"
               width="13"
               height="15"
               border="0"
               alt="<%=altmin%>"
               onClick="javascript:expandME('vc', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
               class="IMG_BTN"></td>
        </tr>
      </table>
    </td></tr>
    <tr><td class="TBDATA_CNT_ODD_BG"><div id="vc-bd">
      <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h1">
        <tr><td colspan="2">
          <IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("PHONE")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.telephoneId%>" value="<%=user.getTelephone()%>"><%=user.getTelephone()%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("FAX")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.faxId%>" value="<%=user.getFax()%>"><%=user.getFax()%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("MOBILE")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.mobileId%>" value="<%=user.getMobile()%>"><%=user.getMobile()%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" width="30%" nowrap><%=userAdminLocale.get("STREET")%>:</td>
        <td class="TX_XS" width="70%" tabIndex="0" nowrap><input type="hidden" name="<%=user.streetAddressId%>" value="<%=user.getStreetAddress()%>"><%=user.getStreetAddress()%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("CITY")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.cityId%>" value="<%=user.getCity()%>"><%=user.getCity()%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("STATE")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.stateId%>" value="<%=user.getState()%>"><%=user.getState()%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("ZIP")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.zipId%>" value="<%=user.getZip()%>"><%=user.getZip()%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("COUNTRY")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.countryId%>" value="<%=user.getCountry()%>"><%=countries.getName(user.getCountry())%></td>
        </tr>
        <tr>
        <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("TIME_ZONE")%>:</td>
        <td class="TX_XS" tabIndex="0" nowrap><input type="hidden" name="<%=user.timeZoneId%>" value="<%=user.getTimeZone()%>"><%=timezones.getName(user.getTimeZone())%></td>
        </tr>

        <tr>
        <td colspan="2"><img src="<%=webpath%>layout/sp.gif" width="1" height="2" border="0" alt=""></td>
        </tr>
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

