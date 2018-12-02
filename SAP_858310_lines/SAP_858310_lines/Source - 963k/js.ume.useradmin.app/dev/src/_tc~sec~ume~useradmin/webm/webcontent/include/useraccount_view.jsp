<%@ taglib uri="UM" prefix="UM" %>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.sap.security.api.IUserAccount"%>
<%@page import="com.sap.security.api.UMException"%>
<%@page import="com.sap.security.api.IUserFactory"%>
<%@page import="com.sap.security.core.util.LocaleString"%>
<%@page import="com.sap.security.core.util.Message"%>

<%  java.util.Locale locale = proxy.getLocale();
    if ( null == locale ) locale = java.util.Locale.getDefault();
    UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
    boolean noLimited = uac.toShowAllAccountInfo();
%>

<% String acH = userAdminLocale.get("ACCOUNT_HISTORY");   
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
	<table id="vu-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		<td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=acH%></td>
		<td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="vu-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 title="<%=altmin%>"
                 onClick="javascript:expandME('vu', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
	  </tr>
    </table>
  </td></tr>

  <tr><td class="TBDATA_CNT_ODD_BG" align="center"><div id="vu-bd">
	<table cellpadding="0" cellspacing="0" border="0">
      <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    </table>
	<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%">
	  <tr><td>
		<table cellpadding="1" cellspacing="1" border="0" width="100%">
		<tr>
			<td scope="col" class="TBDATA_HEAD" tabIndex="0" nowrap><%=userAdminLocale.get("DATE")%></td>
			<td scope="col" class="TBDATA_HEAD" tabIndex="0" nowrap><%=userAdminLocale.get("DESCRIPTION")%></td>
		</tr>
		<tr class="TBDATA_CNT_ODD_BG">
		    <% DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
		       DateFormat dateOnlyFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		       Date tmp = account.created();
           String date = userAdminLocale.get("NOT_AVAILABLE");
           if ( null != tmp ) date = dateFormat.format(tmp); %>
			<td scope="row" tabIndex="0" class="TBDATA_XXS_L"><%=date%></td>
			<td class="TBDATA_XXS_L" tabIndex="0" nowrap><%=userAdminLocale.get("NEW_ACCOUNT_CREATED")%></td>
		</tr>
		<tr class="TBDATA_CNT_ODD_BG">
			<td scope="row" tabIndex="0" class="TBDATA_XXS_L">
			  <% Date fromDate = account.getValidFromDate();
                 String from = "";
			     if ( null != fromDate) { from = dateOnlyFormat.format(fromDate); }
                 Date toDate = DateUtil.decDateWhenMidnight(account.getValidToDate());
                 String to = "";
			     if ( null != toDate ) { to= dateOnlyFormat.format(toDate); } %>
			  <UM:encode><%=from%></UM:encode>&nbsp;-&nbsp;<UM:encode><%=to%></UM:encode>
			</td>
			<td class="TBDATA_XXS_L" tabIndex="0" nowrap><%=userAdminLocale.get("ACCOUNT_VALID_DATE")%></td>
		</tr>

		<% tmp = account.getLastSuccessfulLogonDate();
		   date = "";
		       if ( null != tmp && noLimited ) {
		           date = dateFormat.format(tmp); %>
		<% } %>

		<% tmp = account.getLastFailedLogonDate();
		       if ( null != tmp && noLimited  ) {
		           date = dateFormat.format(tmp); %>
		<tr class="TBDATA_CNT_ODD_BG">
			<td scope="row" tabIndex="0" class="TBDATA_XXS_L"><UM:encode><%=date%></UM:encode></td>
			<td class="TBDATA_XXS_L" tabIndex="0" nowrap><%=userAdminLocale.get("LAST_FAILED_LOGIN")%></td>
		</tr>
		<% } %>

		<% tmp = account.getLastPasswordChangedDate();
		       if ( null != tmp && noLimited  ) {
		           date = dateFormat.format(tmp); %>
		<tr class="TBDATA_CNT_ODD_BG">
			<td scope="row" tabIndex="0" class="TBDATA_XXS_L"><%=date%></td>
			<td class="TBDATA_XXS_L" tabIndex="0" nowrap><%=userAdminLocale.get("LAST_PASWD_CHANGE")%></td>
		</tr>
		<% } %>

        <% String[] uids = null;
           IUserFactory uf = UMFactory.getUserFactory();
           IUser executer = null;
           String locklogonid = "";
           String unlocklogonid = "";
           String lockName = "";
           String unlockName = "";           
           String[] unlockMsgs = null;
           String unlockdt = null;
           String lockDate = null;
           String[] lockMsgs = null;
           int currentLockReason = account.getLockReason();
           StringBuffer lockdisplayname = null;
           String[] unlockDate = subjugatedUser.getAttribute(UserBean.UM, UserBean.unlockDate);
           if ( null != unlockDate ) {
               java.util.Locale lc = LocaleString.getLocaleFromString(unlockDate[0]);
               String dt = unlockDate[1];
               if ( null == lc )  {
                   lc = LocaleString.getLocaleFromString(unlockDate[1]);
                   dt = unlockDate[0];
               }
               DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, lc);
               unlockdt = dateFormat.format(df.parse(dt));
               uids = subjugatedUser.getAttribute(UserBean.UM, UserBean.unlockPerson);
               if ( null != uids ) {
                   try {
                       executer = uf.getUser(uids[0]);
                       unlockName = executer.getDisplayName();
                       if ( null != executer.getUserAccounts() ) {
                           unlocklogonid = executer.getUserAccounts()[0].getLogonUid();
					   }	                                             
                   } catch (UMException ex) {
                       unlockName = uids[0];
                   }
               }
               unlockMsgs = subjugatedUser.getAttribute(UserBean.UM, UserBean.unlockMessage);
          }
          tmp = account.lockDate();
          if ( null != tmp ) {
            lockDate = dateFormat.format(tmp);
            uids = subjugatedUser.getAttribute(UserBean.UM, UserBean.lockPerson);
            if ( null != uids ) {
                try {
                    executer = uf.getUser(uids[0]);
                    lockName = executer.getDisplayName();
                    if ( executer.getUserAccounts().length > 0 ) {
                        locklogonid = executer.getUserAccounts()[0].getLogonUid();
                    }
                } catch (UMException ex) {
                    lockName = uids[0];
                }
            }
            lockdisplayname = new StringBuffer(lockName);
            if ( locklogonid.length()>0 ) {
                lockdisplayname.append(" ").append("(").append(locklogonid).append(")");
            }
            lockMsgs = subjugatedUser.getAttribute(UserBean.UM, UserBean.lockMessage);
          } %>

        <% if ( account.isLocked() ) { %>
            <% if ( null != unlockDate ) { %>
            <tr class="TBDATA_CNT_ODD_BG">
                <td scope="row" tabIndex="0" class="TBDATA_XXS_L"><UM:encode><%=(unlockdt==null)?"":unlockdt%></UM:encode></td>
                <td tabIndex="0" class="TBDATA_XXS_L">
                <%=userAdminLocale.get("ACTIVATED_BY")%>:<UM:encode><%=unlockName%></UM:encode><%if(unlocklogonid.length()>0){%>&nbsp;(<UM:encode><%=unlocklogonid%></UM:encode>)<%}%>
                <% if ( null != unlockMsgs && !unlockMsgs[0].equals("") ) { %>
                <br><%=userAdminLocale.get("MESSAGE_FROM_ADMIN")%>:&nbsp;<UM:encode><%=unlockMsgs[0]%></UM:encode>
                <% } %>
                </td>
            </tr>
            <% } %>
            <% if ( null != tmp ) { %>
            <tr class="TBDATA_CNT_ODD_BG">
                <td scope="row" tabIndex="0" class="TBDATA_XXS_L"><UM:encode><%=(lockDate==null)?"":lockDate%></UM:encode></td>
                <td tabIndex="0" class="TBDATA_XXS_L">
                <% if ( IUserAccount.LOCKED_BY_ADMIN == currentLockReason ) { %>
                    <UM:encode><%=userAdminMessages.print(new Message("DEACTIVATED_BY_ADMIN", lockdisplayname.toString()))%></UM:encode>
	                <% if ( null !=lockMsgs && !lockMsgs[0].equals("") ) { %>
	                <br><%=userAdminLocale.get("MESSAGE_FROM_ADMIN")%>:&nbsp;<UM:encode><%=lockMsgs[0]%></UM:encode>
	                <% } %>                
                <% } else { %>
                    <%=userAdminMessages.print(new Message("DEACTIVATED_BY_SYSTEM"))%>
                <% } %>
                </td>
            </tr>
            <% } %>
        <% } else { %>
            <% if ( null != tmp ) { 
                int lastLockReason = IUserAccount.LOCKED_BY_ADMIN;
                String[] lockReason = subjugatedUser.getAttribute(UserBean.UM, UserBean.lockReason);
                if ( null != lockReason )
                    lastLockReason = Integer.parseInt(lockReason[0]); %>
            <tr class="TBDATA_CNT_ODD_BG">
                <td scope="row" tabIndex="0" class="TBDATA_XXS_L"><UM:encode><%=(lockDate==null)?"":lockDate%></UM:encode></td>
                <td tabIndex="0" class="TBDATA_XXS_L">
                <% if ( IUserAccount.LOCKED_BY_ADMIN == lastLockReason ) { %>
                    <UM:encode><%=userAdminMessages.print(new Message("DEACTIVATED_BY_ADMIN", lockdisplayname.toString()))%></UM:encode>
	                <% if ( null !=lockMsgs && !lockMsgs[0].equals("") ) { %>
	                <br><%=userAdminLocale.get("MESSAGE_FROM_ADMIN")%>:&nbsp;<UM:encode><%=lockMsgs[0]%></UM:encode>
	                <% } %>                
                <% } else { %>
                    <%=userAdminMessages.print(new Message("DEACTIVATED_BY_SYSTEM"))%>
                <% } %>
                </td>
            </tr>
            <% } %>
            <% if ( null != unlockDate ) { %>
            <tr class="TBDATA_CNT_ODD_BG">
                <td scope="row" tabIndex="0" class="TBDATA_XXS_L"><UM:encode><%=(unlockdt==null)?"":unlockdt%></UM:encode></td>
                <td tabIndex="0" class="TBDATA_XXS_L">
                <%=userAdminLocale.get("ACTIVATED_BY")%>:<UM:encode><%=unlockName%></UM:encode><%if(unlocklogonid.length()>0){%>&nbsp;(<UM:encode><%=unlocklogonid%></UM:encode>)<%}%>
                <% if ( null != unlockMsgs && !unlockMsgs[0].equals("") ) { %>
                <br><%=userAdminLocale.get("MESSAGE_FROM_ADMIN")%>:&nbsp;<UM:encode><%=unlockMsgs[0]%></UM:encode>
                <% } %>
                </td>
            </tr>
            <% } %>
        <% } %>

        <% String[] expireDate = subjugatedUser.getAttribute(UserBean.UM, UserBean.expireDate);
           String[] expireMsgs = null;
           String expireName = "";
           String expirelogonid = "";
           String expiredt = null;
           if ( null != expireDate ) {
               java.util.Locale lc = LocaleString.getLocaleFromString(expireDate[0]);
               String dt = expireDate[1];
               if ( null == lc )  {
                   lc = LocaleString.getLocaleFromString(expireDate[1]);
                   dt = expireDate[0];
               }
               DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, lc);
               expiredt = dateFormat.format(df.parse(dt));
               uids = subjugatedUser.getAttribute(UserBean.UM, UserBean.expirePerson);
               try {
                   executer = uf.getUser(uids[0]);
                   expireName = executer.getDisplayName();
                   if ( null != executer.getUserAccounts() ) {
                       expirelogonid = executer.getUserAccounts()[0].getLogonUid();
                   }
               } catch (UMException ex) {
                   expireName = uids[0];
               }
               expireMsgs = subjugatedUser.getAttribute(UserBean.UM, UserBean.expireMessage);
          }
        %>
        <% if ( null != expireDate ) { %>
        <tr class="TBDATA_CNT_ODD_BG">
            <td scope="row" tabIndex="0" class="TBDATA_XXS_L"><UM:encode><%=(expiredt==null)?"":expiredt%></UM:encode></td>
            <td tabIndex="0" class="TBDATA_XXS_L">
            <%=userAdminLocale.get("EXPIRED_BY")%>:<UM:encode><%=expireName%></UM:encode><%if(expirelogonid.length()>0){%>&nbsp;(<UM:encode><%=expirelogonid%></UM:encode>)<%}%>
            <% if ( null != expireMsgs && !expireMsgs[0].equals("") ) { %>
            <br><%=userAdminLocale.get("MESSAGE_FROM_ADMIN")%>:&nbsp;<UM:encode><%=expireMsgs[0]%></UM:encode>
            <% } %>
            </td>
        </tr>
        <% } %>

		</table>
	  </td></tr>
	</table>
	<table cellpadding="0" cellspacing="0" border="0">
	  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0"></td></tr>
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
