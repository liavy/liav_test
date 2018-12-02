<%@ page session = "true"%>
<%@ page import = "com.sap.security.core.sapmimp.logon.*" %>
<%@ page import = "com.sap.security.core.sapmimp.rendering.Browsers" %>
<%@ page import = "com.sap.security.core.util.taglib.EncodeHtmlTag" %>
<%@ page import = "com.sap.security.api.logon.IAuthScheme" %>
<%@ page import = "com.sap.security.api.logon.ILoginConstants" %>
<%@ page import = "com.sap.security.api.UMFactory" %>

<%@ include file="certlogon_proxy.txt" %>
<% SAPMLogonCertLogic.setUnicodeEnabled(proxy); %>

<jsp:useBean id="logonLocale" class="com.sap.security.core.sapmimp.logon.LogonLocaleBean" scope="session"/>
<jsp:useBean id="logonMessage" class="com.sap.security.core.sapmimp.logon.LogonMessageBean" scope="session"/>
<jsp:useBean id="logonBean" class="com.sap.security.core.sapmimp.logon.LogonBean" scope="session"/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope='request'/>

<% if (inPortal) { %>
   <!-- Include epcm-event in order to make logon
        in the portal more smooth-->
   <script language="JavaScript">
     EPCM.raiseEvent("urn:com.sapportals:navigation", "RefreshPortal", "");
   </script>
<% } %>

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
        myform = document.certLogonForm;
        for(i=0; i<myform.length; i++) {
                elem = myform.elements[i];
                if(elem.readOnly==false && elem.type=="text") {
                        elem.focus();
                        break;
                }
        }
}
</script>

<%
String stop = (String)proxy.getSessionAttribute("STOP");
if (stop != null)
    proxy.removeSessionAttribute("STOP");
%>

<%@ include file="/umLogonTopArea.txt"%>

<FORM name="certLogonForm" method="post" action="<%=inPortal?"":logonBean.getLogonCertURL(proxy, null)%>">
<% if( inPortal ) { %>
<input name="login_submit" type="hidden" value="on">
    <% if (UMFactory.getProperties().getBoolean ("ume.login.do_redirect", true)==true) { %>
    <input type="hidden" name="login_do_redirect" value="1" />
<%     } 
   }
%>
<table border="0" width="301" height="100" align="left" cellspacing="0" valign="top">
    <!-- display self-registration link if supposed to do so -->
    <% if ( logonBean.getSelfReg() ) { %>
        <tr>
            <td align="left" bgcolor="E9E9E9" colspan=2>
              <table border="0" cellpadding="0" cellspacing="0" >
                <tr>
                  <td>
                    <span class="urLblStdBar" tabindex=0><%=logonLocale.get("NEW_USERS")%></span>
                    <a class=urLnk href="<%=proxy.getAlias("/useradmin/selfReg","redirectURL")%>">
                      <span class=urTxtStd><%=logonLocale.get("SIGN_UP")%></span>
                    </a>
                  </td>
                </tr>
              </table>
            </td>
        </tr>
    <% } %>
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
    <!-- info that saving cert  and link to uid/pw logon -->
    <tr>
        <td colspan="2">
            <span class="urTxtEmph" tabindex=0><%=logonLocale.get("xmsg_SAVE_CERT_INFO")%> </span><br>
						<a class=urLnk href="<%=inPortal?proxy.getAlias("gotouidpwlogon"):logonBean.getLogonURL(proxy,null)%>"><span class=urTxtStd><%=logonLocale.get("xlnk_goto_normal_logon")%></a></span>
        </td>
    </tr>
    <!-- userid -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonuidfield">
          <%=logonLocale.get("USER")%>
          <span class=urLblReq>&nbsp;*</span>
        </label>
      </td>
      <td width="183" height="20">
        <input style="WIDTH: 21ex" class="urEdfTxtEnbl" id="logonuidfield" name="<%=ILoginConstants.LOGON_USER_ID%>" type="text" value="<%=EncodeHtmlTag.encode(logonBean.getLongUid(proxy)!=null?logonBean.getLongUid(proxy):"")%>">
      </td>
    </tr>
    <!-- password -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd title="User ID" for="logonpassfield">
          <%=logonLocale.get("PASSWORD")%>
          <span class=urLblReq>&nbsp;*</span>
        </label>
      </td>
      <td width="183" height="20">
        <input style="WIDTH: 21ex" class="urEdfTxtEnbl" id="logonpassfield" name="<%=ILoginConstants.LOGON_PWD_ALIAS%>" type="password">
      </td>
    </tr>
    <!-- authentication scheme -->
    <% if( inPortal ) {
        String reqscheme = proxy.getRequiredAuthScheme();
        if( reqscheme != null )
        {
        %>
            <input name="<%=ILoginConstants.LOGON_AUTHSCHEME_ALIAS%>" type="hidden" value="<%=EncodeHtmlTag.encode(reqscheme)%>">
        <%
        }
        else
        {
            IAuthScheme[] asarr = proxy.getAuthSchemes();
    %>
        <tr>
          <td width="161" height="26">
            <label class="urLblStd" for="logonauthschemefield">
              <nobr><%=logonLocale.get("xfld_AUTHSCHEME")%></nobr>
            </label>
          </td>
          <td width="183" height="26">
            <select name="<%=ILoginConstants.LOGON_AUTHSCHEME_ALIAS%>" id="logonauthschemefield" class="urDdlWhl" style="WIDTH: 20ex" size=1>
            <%
                for (int i = 0; i < asarr.length; i++)
                {
                    if( !"anonymous".equals(asarr[i].getName()) )
                    {
            %>
                        <option value="<%=asarr[i].getName()%>"><%=asarr[i].getName()%></option>
            <%
                    }
                }
            %>
            </select>
          </td>
        </tr>
    <%
      }
    }
    %>
    <!-- logon button -->
    <tr>
      <td colspan="2">
        <input style="height:3ex;" class="urBtnStd" type="submit" name="<%=SAPMLogonCertLogic.uidPasswordLogonAction%>" value="<%=logonLocale.get("LOGON")%>">
      </td>
    </tr>
    <!-- logon help -->
    <tr>
        <td align="left" bgcolor="E9E9E9" colspan=2>
          <table border="0" cellpadding="0" cellspacing="0" >
            <tr>
              <td>
                <span class="urLblStdBar"> <%=logonLocale.get("LOGON_IN_PROBLEM")%></span>
                  <a class=urLnk href="<%=inPortal?proxy.getAlias("gotoHelpPage"):logonBean.getLogonURL(proxy,"gotoHelpPage=")%>">
                    <span class=urTxtStd><%=logonLocale.get("GET_SUPPORT")%></span>
                  </a>
              </td>
            </tr>
          </table>
        </td>
    </tr>
</table>
<input type="hidden" name="save_cert" value="1" />
</form>

<%@ include file="/umLogonBotArea.txt"%>

<% if(!inPortal) proxy.sessionInvalidate(); %>
<%if(inPortal) { %>
</span>
<% } else { %>
</body>
</html>
<% } %>