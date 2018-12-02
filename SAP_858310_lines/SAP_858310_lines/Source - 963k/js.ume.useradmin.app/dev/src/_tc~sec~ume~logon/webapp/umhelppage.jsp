<%@ page session = "true"%>
<%@ page import = "com.sap.security.core.sapmimp.logon.*" %>
<%@ page import = "com.sap.security.core.sapmimp.rendering.Browsers" %>

<%@ include file="logon_proxy.txt" %>
<% SAPMLogonLogic.setUnicodeEnabled(proxy); %>

<jsp:useBean id="logonLocale" class="com.sap.security.core.sapmimp.logon.LogonLocaleBean" scope="session"/>
<jsp:useBean id="logonMessage" class="com.sap.security.core.sapmimp.logon.LogonMessageBean" scope="session"/>
<jsp:useBean id="logonBean" class="com.sap.security.core.sapmimp.logon.LogonBean" scope="session"/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope='request'/>

<%if( !inPortal ) { %>
<html>
<head>
<link rel=stylesheet href="<%=com.sap.security.core.logonadmin.ServletAccessToLogic.getAbsoluteURL(webpath,com.sap.security.core.imp.TenantFactory.getInstance().getLogonBrandingStyle(request))%>">
<title>User Management, SAP AG</title>
</head>
<% } %>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
<script language="JavaScript">
function setFocusToFirstField() {
        myform = document.helpForm;
        for(i=0; i<myform.length; i++) {
                elem = myform.elements[i];
                if(elem.readOnly==false && (elem.type=="select" || elem.type=="text")) {
                        elem.focus();
                        break;
                }
        }
}
</script>

<%@ include file="/umLogonTopArea.txt"%>

<FORM class="form" name="helpForm" method="post" action="<%=inPortal?proxy.getAlias("HPFS"):logonBean.getLogonURL(proxy, null) %>">
<table border="0" width="301" height="100" align="left" cellspacing="0" valign="top">
  <!-- placeholder since there is no error message -->
  <tr>
    <td colspan="2" height="24"></td>
  </tr>
  <!-- header line -->
<%--
  <tr>
    <td colspan="2">
      <div class="urTxtH3" ><%=logonLocale.get("HAVE_TROUBLE")%>&nbsp;<%=logonLocale.get("SELECT_HELP")%></div>
    </td>
  </tr>
--%>
  <tr>
    <td colspan="2">
      <div class="urTxtH3" tabindex=0><%=logonLocale.get("HAVE_TROUBLE")%></div>
    </td>
  </tr>
  <tr>
    <td colspan="2">
      <div class="urTxtLbl" tabindex=0><%=logonLocale.get("SELECT_HELP")%></div>
    </td>
  </tr>

  <!-- line with select box for help type -->
  <tr>
    <td width="161" height="26">
      <label class="urLblStd" for="helptypefield">
        <nobr><%=logonLocale.get("HOW_HELP")%></nobr>
      </label>
    </td>
    <td width="183" height="26">
      <select name="<%=SAPMLogonLogic.helpActionPage%>" id="helptypefield" class="urDdlWhl" size=1>
        <% if ( logonBean.getPasswordReset() ) { %>
            <option value="<%=SAPMLogonLogic.helpResetPasswordPage%>"><%=logonLocale.get("REQUEST_PASSWORD_RESET")%></option>
        <% } %>
        <option class="gSAPOption" value="<%=SAPMLogonLogic.helpLogonProblemPage%>"><%=logonLocale.get("OTHER_LOGON_PROBLEM")%></option>
      </select>
    </td>
  </tr>
  <!-- space above buttons -->
  <tr>
    <td colspan="2" height="20">&nbsp; </td>
  </tr>
  <!-- submit button -->
  <tr>
    <td colspan="2">
      <input style="height:3ex;" class="urBtnStd" type="submit" NAME="submitHelpPage" VALUE="<%=logonLocale.get("SUBMIT")%>">
      <input style="height:3ex;" class="urBtnStd" type="submit" NAME="<%=SAPMLogonLogic.showUidPasswordLogonPage%>" VALUE="<%=logonLocale.get("CANCEL")%>">
    </td>
  </tr>
</table>
</form>

<%@ include file="/umLogonBotArea.txt"%>

<% if(!inPortal) proxy.sessionInvalidate(); %>
<%if(inPortal) { %>
</span>
<% } else { %>
</body>
</html>
<% } %>
