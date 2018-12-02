<%-- to include this page requires:
<%@page import="com.sap.security.core.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
StringBuffer entryExit = new StringBuffer(80);
--%>
<%-- this jsp for user proprofile view --%>

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
       <table id="va-hd" border="0" cellpadding="0" cellspacing="0">
         <tr class="TBDATA_CNT_EVEN_BG">
           <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=addInfo%></td>
           <td class="TBDATA_CNT_EVEN_BG" align="right" width="20">
           <img id="va-exp"
                tabIndex="0"
                src="<%=webpath%>layout/icon_close.gif"
                width="13"
                height="15"
                border="0"
                alt="<%=altmax%>"
                onClick="javascript:expandME('va', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                class="IMG_BTN"></td>
          </tr>
        </table>
      </td></tr>
      <tr><td class="TBDATA_CNT_ODD_BG"><div id="va-bd">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h9">
          <tr>
            <td colspan="2">
              <IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
            </td>
          </tr>
          <tr>
            <td class="TBLO_XXS_R" tabIndex="0" width="30%" nowrap><%=userAdminLocale.get("POSITION")%>:</td>
            <td class="TX_XS" width="70%" nowrap><input type="hidden" name="<%=user.positionId%>" value="<%=user.getPosition()%>"><span tabIndex="0"><%=user.getPosition()%></span></td>
          </tr>
          <tr>
            <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("DEPARTMENT")%>:</td>
            <td class="TX_XS" nowrap><input type="hidden" name="<%=user.departmentId%>" value="<%=user.getDepartment()%>"><span tabIndex="0"><%=user.getDepartment()%></span></td>
          </tr>
          <tr>
            <td class="TBLO_XXS_R" tabIndex="0" nowrap><%=userAdminLocale.get("ORGUNIT")%></td>
            <td class="TX_XS" nowrap><input type="hidden" name="<%=user.orgUnitId%>" value="<%=user.getOrgUnit()%>"><span tabIndex="0"><%=user.getOrgUnit()%></span></td>
          </tr>
          <tr>
            <td colspan="2">
              <IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
            </td>
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