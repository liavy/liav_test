<%@page import="com.sap.security.api.IUserAccount"%>
<% java.util.Locale locale = proxy.getLocale();
   if ( null == locale ) locale = java.util.Locale.getDefault();
   Boolean isLocked = userAccount.isLocked();
   int locked = 0;
   if ( null != isLocked ) {
       if ( isLocked.equals(Boolean.TRUE) ) {
           locked = 1;
       } else {
           locked = 2;
       }
   }
   int lockedReason = userAccount.getLockReason().intValue();
   UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   boolean noLimited = uac.toShowAllAccountInfo();
%>

<script language="JavaScript">
function openCal(fieldID) {
	<% String parent = "userSearch"; %>
        window.name = "<%=parent%>";
        <% StringBuffer certSB = new StringBuffer(userAdminAlias);
           certSB.append("?");
           certSB.append("callCalendar");
           certSB.append("=").append("&").append("formID").append("=");
           String cert = certSB.toString(); %>
        var url = "<%=cert%>" + document.forms["searchuser"].name + "&" + "fieldID" + "="+ fieldID;
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
	    <table id="su-hd" border="0" cellpadding="0" cellspacing="0">
            <tr class="TBDATA_CNT_EVEN_BG">
            <td class="TBDATA_XSB_NBG" tabIndex=0 width="100%">&nbsp;<%=acH%></td>
            <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="su-exp"
                 tabindex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 title="<%=altmin%>"
                 onClick="javascript:expandME('su', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
            </tr>
	    </table>
	</td></tr>
	<tr><td class="TBDATA_CNT_ODD_BG"><div id="su-bd">
	    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h2">
		    <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
     		<tr>
				<td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=userAccount.locked%>"><%=userAdminLocale.get("STATUS")%></LABEL></td>
				<td class="TBLO_XXS_L" width="70%">
				<select id="<%=userAccount.locked%>" tabIndex="0" name="<%=userAccount.locked%>" class="DROPDOWN">
					<option value="" <%=locked==0?"selected":""%>><%=userAdminLocale.get("ANY")%></option>
					<option value="true" <%=locked==1?"selected":""%>><%=userAdminLocale.get("DEACTIVATED")%></option>
				</select>&nbsp;<LABEL FOR="<%=userAccount.lockreason%>"><%=userAdminLocale.get("SET_BY")%></LABEL>&nbsp;
				<select id="<%=userAccount.lockreason%>" tabIndex="0" name="<%=userAccount.lockreason%>" class="DROPDOWN">
					<option value="<%=IUserAccount.LOCKED_BY_ADMIN%>" <%=lockedReason==IUserAccount.LOCKED_BY_ADMIN?"selected":""%>><%=userAdminLocale.get("ADMINISTRATOR")%></option>
					<option value="<%=IUserAccount.LOCKED_AUTO%>" <%=lockedReason==IUserAccount.LOCKED_AUTO?"selected":""%>><%=userAdminLocale.get("SYSTEM")%></option>
					<option value="" <%=lockedReason==IUserAccount.LOCKED_NO?"selected":""%>><%=userAdminLocale.get("EITHER")%></option>
				</select>
				</td>
			</tr>
			<tr>
				<td CLASS="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.createdbegin%>"><%=userAdminLocale.get("CREATION_DATE")%>:</LABEL>&nbsp;</td>
				<td CLASS="TX_XS" nowrap>
				<input id="<%=userAccount.createdbegin%>"
				       type="text"
				       tabIndex="0" 
					     name="<%=userAccount.createdbegin%>"
					     value="<%=userAccount.getCreatedbegin()%>"
					     size="6"
					     style="width=1in"
					     maxlength="12">&nbsp;
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
				     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
				     onClick="openCal( '<%=userAccount.createdbegin%>')">&nbsp;&nbsp;&nbsp;
				  <LABEL FOR="<%=userAccount.createdend%>"><%=userAdminLocale.get("TO")%></LABEL>
				&nbsp;&nbsp;&nbsp;
				<input id="<%=userAccount.createdend%>"
				       type="text"
				       tabIndex="0" 
					     name="<%=userAccount.createdend%>"
					     value="<%=userAccount.getCreatedend()%>"
					     size="6"
					     style="width=1in"
					     maxlength="12">&nbsp;
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
				     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
				     onClick="openCal('<%=userAccount.createdend%>')" >
				 </td>
			</tr>
            <% if (noLimited) { %>
			<tr>
				<td CLASS="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.loggedinbegin%>"><%=userAdminLocale.get("LAST_LOGIN_DATE")%>:</LABEL>&nbsp;</td>
				<td CLASS="TX_XS" nowrap>
				<input id="<%=userAccount.loggedinbegin%>"
					   type="text"
					   tabIndex="0" 
					   name="<%=userAccount.loggedinbegin%>"
					   value="<%=userAccount.getLoggedinbegin()%>"
					   size="6"
					   style="width=1in"
					   maxlength="12">&nbsp;
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
				     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
				     onClick="openCal('<%=userAccount.loggedinbegin%>')" >&nbsp;&nbsp;&nbsp;
					<LABEL FOR="<%=userAccount.loggedinend%>"><%=userAdminLocale.get("TO")%></LABEL>
				&nbsp;&nbsp;&nbsp;
				<input id="<%=userAccount.loggedinend%>"
					   type="text"
					   tabIndex="0" 
					   name="<%=userAccount.loggedinend%>"
					   value="<%=userAccount.getLoggedinend()%>"
					   size="6"
					   style="width=1in"
					   maxlength="12">&nbsp;
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
				     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
				     onClick="openCal('<%=userAccount.loggedinend%>')" >
				 </td>
			</tr>
			<tr>
				<td CLASS="TBLO_XXS_R" nowrap><LABEL FOR="<%=userAccount.pwdchangebegin%>"><%=userAdminLocale.get("DATE_OF_LAST_PSWD_CHANGE")%>:</LABEL>&nbsp;</td>
				<td CLASS="TX_XS" nowrap>
				<input id="<%=userAccount.pwdchangebegin%>"
				       type="text"
				       tabIndex="0" 
					   name="<%=userAccount.pwdchangebegin%>"
					   value="<%=userAccount.getPwdchangebegin()%>"
					   size="6"
					   style="width=1in"
					   maxlength="12">&nbsp;
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
				     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
				     onClick="openCal('<%=userAccount.pwdchangebegin%>')" >&nbsp;&nbsp;&nbsp;
					<LABEL FOR="<%=userAccount.pwdchangeend%>"><%=userAdminLocale.get("TO")%></LABEL>
				&nbsp;&nbsp;&nbsp;
				<input id="<%=userAccount.pwdchangeend%>"
					   type="text"
					   tabIndex="0" 
					   name="<%=userAccount.pwdchangeend%>"
					   value="<%=userAccount.getPwdchangeend()%>"
					   size="6"
					   style="width=1in"
					   maxlength="12">&nbsp;
				<img id="<%=userAdminLocale.get("CALENDAR")%>"
				     src="<%=webpath%>layout/calendar.gif"
				     width="24"
				     height="20"
				     tabindex="0"
				     alt="<%=userAdminLocale.get("CALENDAR")%>"
                     title="<%=userAdminLocale.get("CALENDAR")%>"
				     style="border:0; cursor:pointer; cursor:hand"
				     onClick="openCal('<%=userAccount.pwdchangeend%>')" >
			    </td>
			</tr>
            <% } %>
			<tr><td colspan="2">
                <IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
            </td></tr>
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