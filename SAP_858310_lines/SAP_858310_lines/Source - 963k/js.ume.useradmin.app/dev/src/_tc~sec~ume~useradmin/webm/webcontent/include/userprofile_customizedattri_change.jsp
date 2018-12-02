<%-- this jsp is used for user profile modify and create --%>
<%@include file="/include/useradmincustomization.jsp"%>

<% if ( isAddnRe ) { %>
<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>

<% String customizedInfo = userAdminLocale.get("CUSTOMIZED_INFO");
   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
   entryExit.append(userAdminMessages.print(new Message("START_GROUP_BOX", customizedInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     alt="<%=entryExit.toString()%>" 
     tabindex="0" 
     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=customizedInfo%>').focus();}">
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
    <table id="cz-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=customizedInfo%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="cz-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('cz', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
	    </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="cz-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="customer">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
          <% String addnName = null;
             String addnLabel = null;
             for (int i=0; i<addnLabels.length; i++ ) {
                 addnName = addnNames[i];
                 addnLabel = addnLabels[i]; 
	             if(null!=self)
	                 _readOnly = UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), addnName);
	             else
	                 _readOnly = false; %> 
          <tr>          
            <td class="TBLO_XXS_R" width="30%" nowrap>
	          <% if ( _readOnly) { %>
	          <% if ( spanTitle.length() > 0 ) spanTitle.delete(0, spanTitle.length());
	             spanTitle.append(addnName).append(" ");
	             spanTitle.append(user.getAttribute(addnLabel)[0]).append(" ");
	             spanTitle.append(userAdminLocale.get("NOT_AVAILABLE")); %>
	          <span title="<%=spanTitle.toString()%>" tabindex="0"><LABEL FOR="<%=addnLabel%>"><%=addnName%>:</LABEL></span>
	          <% } else {  %>
	          <LABEL FOR="<%=addnLabel%>"><%=addnName%>:</LABEL>
	          <% } %> 
            </td>
            <td class="TX_XS" width="70%" nowrap>
             
              <input id="<%=addnLabel%>"
                     name="<%=addnLabel%>"
                     tabIndex="0" 
                     value="<%=user.getAttribute(addnLabel)[0]%>"
                     type="text"
                     size="20"
                     style="width: 2in"
                     <% if ( _readOnly ) { %>DISABLED<%}%>>
              <% if ( _readOnly) { %><input type="hidden" name="<%=addnLabel%>" value="<%=user.getAttribute(addnLabel)[0]%>"><% } %>
            </td>
          </tr>
          <% } %>
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    </table></div>
  </td></tr>
</table>
<% entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("END_GROUP_BOX", customizedInfo))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     id="<%=customizedInfo%>"
     alt="<%=entryExit.toString()%>"
     tabindex="0">
<% } %>
<%-- this jsp is used for user profile modify and create --%>
