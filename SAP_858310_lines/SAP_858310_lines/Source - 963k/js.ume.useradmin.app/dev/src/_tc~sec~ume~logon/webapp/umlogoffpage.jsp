<%@ page session = "true"%>
<%@ page import = "com.sap.security.core.sapmimp.logon.*" %>
<%@ page import = "com.sap.security.core.sapmimp.rendering.Browsers" %>

<%@ include file="logon_proxy.txt" %>
<% SAPMLogonLogic.setUnicodeEnabled(proxy); %>

<jsp:useBean id="logonBean" class="com.sap.security.core.sapmimp.logon.LogonBean" scope="session"/>
<jsp:useBean id="logonMessage" class="com.sap.security.core.sapmimp.logon.LogonMessageBean" scope="session"/>
<jsp:useBean id="logonLocale" class="com.sap.security.core.sapmimp.logon.LogonLocaleBean" scope="session"/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope='request'/>
<%-- end page attribute setting--%>

<%-- start html--%>
        <%if( !inPortal ) { %>
<html>
<head>
<link rel=stylesheet href="<%=com.sap.security.core.logonadmin.ServletAccessToLogic.getAbsoluteURL(webpath,com.sap.security.core.imp.TenantFactory.getInstance().getLogonBrandingStyle(request))%>">
<TITLE><%=logonLocale.get("LOG_OUT")%></TITLE>
</head>
<% } %>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
<script language="JavaScript">
function setFocusToFirstField() {
        // do nothing...
}
</script>

<%@ include file="/umLogonTopArea.txt"%>

<table border="0" width="301" height="100" align="left" cellspacing="0" valign="top">
  <!-- message that logout succeeded -->
  <tr>
    <td colspan="2" height="33">
      <div class="urMsgBarStd" style="margin-bottom:3;">
        <table border="0" cellpadding="0" cellspacing="0">
          <tbody>
          <tr>
            <td class="urTxtStd"><span class="urMsgBarImgOk"><img height="12" width="12" src="<%=webpath%>css/common/1x1.gif"></span></td>
            <td><span class="urTxtStd" tabindex=0><%=logonLocale.get("LOGOUT_SUCCEEDED")%></span></td>
          </tr>
          </tbody>
        </table>
      </div>
    </td>
  </tr>
  <!-- link for relogin -->
  <tr>
    <td align="left" bgcolor="E9E9E9" colspan=2>
      <table border="0" cellpadding="0" cellspacing="0" >
        <tr>
          <td>
            <a class=urLnk href="<%=inPortal?request.getContextPath():logonBean.getLogonURL(proxy, logonBean.getLogoffRedirect())%>">
              <span class=urTxtStd><%=logonLocale.get("RE_LOGIN")%></span>
            </a>
          </td>
        </tr>
      </table>
    </td>
 </tr>
</table>

<%@ include file="/umLogonBotArea.txt"%>

<% if(!inPortal) proxy.sessionInvalidate(); %>
<%if(inPortal) { %>
</span>
<% } else { %>
</body>
</html>
<% } %>
