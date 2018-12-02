<%-- to include this page requires:
<%@page import="com.sap.security.core.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
StringBuffer entryExit = new StringBuffer(80);
--%>

<% String addInfo = userAdminLocale.get("ADDITIONAL_INFO");
   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
   entryExit.append(userAdminMessages.print(new Message("START_GROUP_BOX", addInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     alt="<%=entryExit.toString()%>" 
     tabindex="0" 
     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=addInfo%>').focus();}">
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
    <table id="sa-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" tabindex=0 width="100%">&nbsp;<%=addInfo%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="sa-exp"
                 tabindex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('sa', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
	    </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="sa-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h4">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <tr>
				<td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=user.positionId%>"><%=userAdminLocale.get("POSITION")%>:</LABEL></td>
				<td class="TX_XS" width="70%" nowrap>
          <input id="<%=user.positionId%>"
                 name="<%=user.positionId%>"
                 tabindex="0"
                 value="<%=user.getPosition()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.departmentId%>"><%=userAdminLocale.get("DEPARTMENT")%>:</LABEL></td>
				<td class="TX_XS" nowrap>
          <input id="<%=user.departmentId%>"
                 name="<%=user.departmentId%>"
                 value="<%=user.getDepartment()%>"
                 tabindex="0"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
			</tr>
    </table></div>
  </td></tr>
</table>
<% entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("END_GROUP_BOX", addInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     id="<%=addInfo%>"
     alt="<%=entryExit.toString()%>"
     tabindex="0">