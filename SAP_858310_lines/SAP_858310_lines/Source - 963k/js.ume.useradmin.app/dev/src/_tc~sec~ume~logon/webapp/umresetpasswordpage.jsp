<%@ page session = "true"%>
<%@ page import = "com.sap.security.core.sapmimp.logon.*" %>
<%@ page import = "com.sap.security.core.sapmimp.rendering.Browsers" %>
<%@ page import = "com.sap.security.core.util.taglib.EncodeHtmlTag" %>

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
                if(elem.readOnly==false && elem.type=="text") {
                        elem.focus();
                        break;
                }
        }
}
</script>

<%@ include file="/umLogonTopArea.txt"%>

<FORM name="helpForm" method="post" action="<%=inPortal?proxy.getAlias("RPWFS"):logonBean.getLogonURL(proxy, null)%>">
<table border="0" width="301" height="100" align="left" cellspacing="0" valign="top">
    <!-- display error message if there is one -->
    <% if ( error.getMessage() != null ) { %>
        <tr>
          <td colspan="2" height="33">
            <div class="urMsgBarErr" style="margin-bottom:3;">
              <table border="0" cellpadding="0" cellspacing="0">
                <tbody>
                <tr>
                  <td class="urTxtStd"><span class="urMsgBarImgError"><img height="12" width="12" src="<%=webpath%>css/common/1x1.gif"></span></td>
                  <td><span class="urTxtStd" tabindex="0"><%=EncodeHtmlTag.encode(logonMessage.print(error.getMessage()))%></span></td>
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
  <!-- 2 header lines -->
  <tr>
    <td colspan="2">
      <div class="urTxtH3" tabindex=0><%=logonLocale.get("HAVE_TROUBLE")%></div>
    </td>
  </tr>
  <tr>
    <td colspan="2">
      <div class="urTxtLbl" tabindex=0><%=logonLocale.get("RESET_PASSWORD_MSG")%></div>
    </td>
  </tr>
    <!-- userid -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonuseridfield">
          <%=logonLocale.get("USER")%>
          <span class=urLblReq>&nbsp;*</span>
        </label>
      </td>
      <td width="183" height="20">
        <input class="urEdfTxtEnbl" id="logonuseridfield" name="<%=logonBean.LONGUID%>" type="text" value="<%=EncodeHtmlTag.encode(logonBean.getLongUid(proxy)!=null?logonBean.getLongUid(proxy):"")%>">
      </td>
    </tr>
    <!-- lastname -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonlastnamefield">
          <%=logonLocale.get("LAST_NAME")%>
          <span class=urLblReq>&nbsp;*</span>
        </label>
      </td>
      <td width="183" height="20">
        <input class="urEdfTxtEnbl" id="logonlastnamefield" name="lastname" type="text">
      </td>
    </tr>
    <!-- firstname -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonfirstnamefield">
          <%=logonLocale.get("FIRST_NAME")%>
          <span class=urLblReq>&nbsp;*</span>
        </label>
      </td>
      <td width="183" height="20">
        <input class="urEdfTxtEnbl" id="logonfirstnamefield" name="firstname" type="text">
      </td>
    </tr>
    <!-- email -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonemailfield">
          <%=logonLocale.get("EMAIL")%>
          <span class=urLblReq>&nbsp;*</span>
        </label>
      </td>
      <td width="183" height="20">
        <input class="urEdfTxtEnbl" id="logonemailfield" name="email" type="text">
      </td>
    </tr>
    <!-- space above buttons -->
    <tr>
      <td colspan="2" height="20">&nbsp;</td>
    </tr>
    <!-- submit buttons -->
    <tr>
      <td colspan="2">
        <input style="height:3ex;" class="urBtnStd" type="submit" name="RPWFS" value="<%=logonLocale.get("SUBMIT")%>">
        <input style="height:3ex;" class="urBtnStd" type="submit" name="<%=SAPMLogonLogic.showUidPasswordLogonPage%>" value="<%=logonLocale.get("CANCEL")%>">
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