<%@page import="com.sap.security.core.imp.AbstractUserAccount"%>
<script language="JavaScript">
function openCal(fieldID) {
        window.name = "<%=parent%>";
        <% StringBuffer calSB = new StringBuffer(userAdminAlias);
           calSB.append("?");
           calSB.append("callCalendar");
           calSB.append("=").append("&").append("formID").append("="); %>
        var url = "<%=calSB.toString()%>" + document.forms["modifyuser"].name + "&" + "fieldID" + "="+ fieldID;
        newWindow = window.open(url, "sub", "width=200, height=490, resizable=1");
}
</script>

<% String acH = userAdminLocale.get("ACCOUNT_INFO");   
   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("START_GROUP_BOX", acH))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     alt="<%=entryExit.toString()%>" 
     tabindex="0" 
     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=acH%>').focus();}">
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
	<table id="cu-hd" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
		<td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=acH%></td>
		<td class="BGCOLOR_ICONOPEN" align="right" width="20">
		<img id="cu-exp"
             tabIndex="0"
             src="<%=webpath%>layout/icon_open.gif"
             width="13"
             height="15"
             border="0"
             alt="<%=altmin%>"
             onClick="javascript:expandME('cu', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
             class="IMG_BTN"></td>
	</tr></table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="cu-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>

      <tr>
        <% _readOnly = UserAdminFactory.isAttributeReadOnly(account.getUniqueID(), AbstractUserAccount.VALID_FROM); %>
        <td class="TBLO_XXS_R" nowrap>
          <% if ( _readOnly ) { %>
            <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
               spanTitle.append(userAdminLocale.get("VALID_FROM")).append(" ");
               spanTitle.append(userAccount.getValidFrom()).append(" ");
               spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
            <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=userAccount.validfrom%>"><%=userAdminLocale.get("VALID_FROM")%>:</LABEL></span>
          <% } else { %>
            <LABEL FOR="<%=userAccount.validfrom%>"><%=userAdminLocale.get("VALID_FROM")%>:</LABEL>
          <% } %> 
        </td>
        <td class="TX_XS" nowrap>          
          <input id="<%=userAccount.validfrom%>"
                 name="<%=userAccount.validfrom%>"
                 value="<%=userAccount.getValidFrom()%>"
                 tabindex="0"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>&nbsp;
                 <% if ( !_readOnly ) { %>                 
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
                     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
                     onClick="openCal('<%=userAccount.validfrom%>')">
                 <% } %>                     
          <% if ( _readOnly ) { %><input type="hidden" name="<%=userAccount.validfrom%>" value="<%=userAccount.getValidFrom()%>"><% } %>           
        </td>
      </tr>

      <tr>
        <% _readOnly = UserAdminFactory.isAttributeReadOnly(account.getUniqueID(), AbstractUserAccount.VALID_TO); %>        
        <td class="TBLO_XXS_R" nowrap>
          <% if ( _readOnly ) { %>
            <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
               spanTitle.append(userAdminLocale.get("VALID_TO")).append(" ");
               spanTitle.append(userAccount.getValidTo()).append(" ");
               spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
            <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=userAccount.validto%>"><%=userAdminLocale.get("VALID_TO")%>:</LABEL></span>
          <% } else { %>
            <LABEL FOR="<%=userAccount.validto%>"><%=userAdminLocale.get("VALID_TO")%>:</LABEL>
          <% } %>         
        </td>
        <td class="TX_XS" nowrap>          
          <input id="<%=userAccount.validto%>"
                 name="<%=userAccount.validto%>"
                 value="<%=userAccount.getValidTo()%>"
                 tabindex="0"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if ( _readOnly ) { %>DISABLED<%}%>>&nbsp;
                 <% if ( !_readOnly ) { %>
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
				     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
                     onClick="openCal('<%=userAccount.validto%>')">
                 <% } %>                     
          <% if ( _readOnly ) { %><input type="hidden" name="<%=userAccount.validto%>" value="<%=userAccount.getValidTo()%>"><% } %>                      
        </td>
      </tr>

      <tr>
        <td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td>
      </tr>
    </table></div>
  </td></tr>
</table>
<% entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("END_GROUP_BOX", acH))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     id="<%=acH%>"
     alt="<%=entryExit.toString()%>"
     tabindex="0">