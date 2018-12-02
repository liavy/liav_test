<%@ page session = "true"%>
<%@ page import = "com.sap.security.core.sapmimp.logon.*" %>
<%@ page import = "com.sap.security.api.UMFactory" %>
<%@ page import = "com.sap.security.core.sapmimp.rendering.Browsers" %>
<%@ page import = "com.sap.security.core.util.taglib.EncodeHtmlTag" %>
<%@ page import = "com.sap.security.api.logon.ILoginConstants" %>

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
<% } %>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
<script language="JavaScript">
function setFocusToFirstField() {
        myform = document.changePasswordForm;
        for(i=0; i<myform.length; i++) {
                elem = myform.elements[i];
                if(elem.readOnly==false && (elem.type=="text" || elem.type=="password")) {
                        elem.focus();
                        break;
                }
        }
}

function clearPasswords() {
	document.changePasswordForm.<%=ILoginConstants.NEW_PASSWORD%>.value="";
	document.changePasswordForm.<%=ILoginConstants.CONFIRM_PASSWORD%>.value="";
}
</script>
<%if( !inPortal ) { %>
</head>
<% } %>

<%@ include file="/umLogonTopArea.txt"%>

<FORM name="changePasswordForm" method="post" action="<%=inPortal?proxy.getAlias(SAPMLogonLogic.performChangePasswordAction):"sap_j_security_check" %>">
	<% if( inPortal ) { %>
		<input name="login_submit" type="hidden" value="on">

		<% if (UMFactory.getProperties().getBoolean ("ume.login.do_redirect", true)==true) { %>
			<input type="hidden" name="login_do_redirect" value="1" />
		<% } %>
	<% } %>
<table border="0" width="301" height="100%" align="left" cellspacing="0" valign="top">
    <!-- display error message if there is one -->
    <% if ( error.getLocalizedMessage() != null ) { %>
        <tr>
          <td colspan="2" height="33">
            <div class="urMsgBarErr" style="margin-bottom:3;">
              <table border="0" cellpadding="0" cellspacing="0">
                <tbody>
                <tr>
                  <td class="urTxtStd"><span class="urMsgBarImgError"><img height="12" width="12" src="<%=webpath%>css/common/1x1.gif"></span></td>
                  <td><span class="urTxtStd" tabindex=0><%=EncodeHtmlTag.encode(error.getLocalizedMessage())%></span></td>
                </tr>
                </tbody>
              </table>
            </div>
          </td>
        </tr>
    <% } else if ( error.getMessage() != null ) { %>
        <tr>
          <td colspan="2" height="33">
            <div class="urMsgBarErr" style="margin-bottom:3;">
              <table border="0" cellpadding="0" cellspacing="0">
                <tbody>
                <tr>
                  <td class="urTxtStd"><span class="urMsgBarImgError"><img height="12" width="12" src="<%=webpath%>css/common/1x1.gif"></span></td>
                  <td><span class="urTxtStd" tabindex=0><%=EncodeHtmlTag.encode(logonMessage.print(error.getMessage()))%></span></td>
                </tr>
                </tbody>
              </table>
            </div>
          </td>
        </tr>
    <% } else { %>
        <!-- no error message, display placeholder -->
        <tr>
          <td colspan="2" height="24"></td>
        </tr>
    <% } %>
  <!-- header line -->
  <tr>
    <td colspan="2">
      <div class="urTxtH3" ><%=logonLocale.get("CHANGE_PASSWORD")%></div>
    </td>
  </tr>
    <!-- old password -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonoldpassfield">
          <%=logonLocale.get("OLD_PASSWORD")%>
        </label>
      </td>
      <td width="183" height="20">
        <input class="urEdfTxtEnbl" id="logonoldpassfield" name="<%=ILoginConstants.OLD_PASSWORD%>" type="password">
      </td>
    </tr>
    <!-- new password -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonnewpassfield">
          <%=logonLocale.get("NEW_PASSWORD")%>
        </label>
      </td>
      <td width="183" height="20">
        <input class="urEdfTxtEnbl" id="logonnewpassfield" name="<%=ILoginConstants.NEW_PASSWORD%>" type="password">
      </td>
    </tr>
    <!-- retype new password -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonretypepassfield">
          <%=logonLocale.get("CONFIRM_NEW_PASSWORD")%>
        </label>
      </td>
      <td width="183" height="20">
        <input class="urEdfTxtEnbl" id="logonretypepassfield" name="<%=ILoginConstants.CONFIRM_PASSWORD%>" type="password">
      </td>
    </tr>

	<!-- authentication scheme -->
	<% if (inPortal) {
		String reqscheme = proxy.getRequiredAuthScheme();
		
		if (reqscheme != null) {
			%>
				<input name="<%=ILoginConstants.LOGON_AUTHSCHEME_ALIAS%>" type="hidden" value="<%=EncodeHtmlTag.encode(reqscheme)%>">
			<%
		}
	  }
	%>

    <!-- space above buttons -->
    <tr>
      <td colspan="2" height="20">&nbsp;</td>
    </tr>
    <!-- submit buttons -->
    <tr>
      <td colspan="2">
        <input style="height:20;" class="urBtnStd" type="submit" name="<%=SAPMLogonLogic.performChangePasswordAction%>" value="<%=logonLocale.get("CHANGE")%>">
<% if (inPortal) { %>
        <input style="height:20;" class="urBtnStd" type="submit" name="<%=SAPMLogonLogic.showUidPasswordLogonPage%>" value="<%=logonLocale.get("CANCEL")%>" onClick="clearPasswords();">
<% } else { %>
        <input style="height:20;" class="urBtnStd" type="submit" name="<%=SAPMLogonLogic.showUidPasswordLogonPage%>" value="<%=logonLocale.get("CANCEL")%>">
<% } %>
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