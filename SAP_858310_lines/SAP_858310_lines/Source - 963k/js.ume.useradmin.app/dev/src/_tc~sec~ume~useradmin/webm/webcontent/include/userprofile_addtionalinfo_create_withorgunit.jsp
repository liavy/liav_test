<%-- to include this page requires:
<%@page import="com.sap.security.core.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
StringBuffer entryExit = new StringBuffer(80);
--%>

<% String orgUnitName = (String) proxy.getRequestAttribute(UserAdminLogic.orgUnitName);
   if ( null == orgUnitName ) {
       orgUnitName = new String();
   }
%>

<script language="JavaScript">
  function doOrgUnitSearch() {
      frm = document.forms[0];
      inputTag = document.createElement("input");
      inputTag.setAttribute("name", "<%=UserAdminLogic.searchOrgUnitAction%>");
      inputTag.setAttribute("type", "hidden");
      inputTag.setAttribute("value", "");
      frm.appendChild( inputTag );
      inputTag2 = document.createElement("input");
      inputTag2.setAttribute("name", "<%=UserAdminLogic.preRequest%>");
      inputTag2.setAttribute("type", "hidden");
      inputTag2.setAttribute("value", "<%=util.filteringSpecialChar(action)%>");
      frm.appendChild( inputTag2 );
      frm.submit();
  }
</script>

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
    <table id="ra-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=addInfo%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="ra-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('ra', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
	    </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="ra-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h9">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>

      <tr>
		<td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=user.positionId%>"><%=userAdminLocale.get("POSITION")%>:</LABEL></td>
		<td class="TX_XS" width="70%" nowrap>
          <input id="<%=user.positionId%>"
                 name="<%=user.positionId%>"
                 tabIndex="0"
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
                 tabIndex="0"
                 value="<%=user.getDepartment()%>"
                 type="text"
                 size="20"
                 style="width: 2in"></td>
      </tr>
      <tr>
		<td class="TBLO_XXS_R" nowrap>
          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
             spanTitle.append(UserAdminLogic.orgUnitName).append(" ");
             spanTitle.append(orgUnitName).append(" ");
             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %> 		
		  <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=UserAdminLogic.orgUnitName%>"><%=userAdminLocale.get("ORGUNIT")%>:</LABEL></span><span class="required">*</span>
		</td>
		<td class="TX_XS" nowrap>
		<input type="hidden" name=<%=user.orgUnitId%> value="<%=user.getOrgUnit()%>">
		<input id="<%=UserAdminLogic.orgUnitName%>"
               type="text"
               name="<%=UserAdminLogic.orgUnitName%>"
               value="<%=util.filteringSpecialChar(orgUnitName)%>"
               size="20"
               style="width: 2in"
               disabled>
		&nbsp;<img src="<%=webpath%>layout/search.gif"
                   width="24"
                   tabindex="0"
                   height="20"
                   border="0"
                   alt="<%=userAdminLocale.get("SELECT_ORGUNIT")%>"
                   onClick="doOrgUnitSearch()"
                   CLASS="IMG_BTN">
         </td>
      </tr>
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
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