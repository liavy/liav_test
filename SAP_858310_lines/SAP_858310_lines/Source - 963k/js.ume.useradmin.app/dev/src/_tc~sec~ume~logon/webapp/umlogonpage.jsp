<%@ page session = "true"%>
<%@ page import = "com.sap.security.core.sapmimp.logon.*" %>
<%@ page import = "com.sap.security.core.sapmimp.rendering.Browsers" %>
<%@ page import = "com.sap.security.core.util.taglib.EncodeHtmlTag" %>
<%@ page import = "com.sap.security.api.UMFactory" %>
<%@ page import = "com.sap.security.api.logon.IAuthScheme" %>
<%@ page import = "com.sap.security.api.logon.ILoginConstants" %>
<%@ page import = "com.sap.security.api.UMFactory" %>
<%@ page import = "com.sap.security.api.IPrincipal" %>
<%@ page import = "com.sap.security.api.logon.ILoginConstants" %>
<%@ page import = "com.sap.security.api.IUser" %>

<%@ include file="/logon_proxy.txt" %>
<% SAPMLogonLogic.setUnicodeEnabled(proxy); %>
<%//LanguagesBean languages = (LanguagesBean) proxy.getSessionAttribute(LanguagesBean.beanId);%>

<jsp:useBean id="languages" class="com.sap.security.core.sapmimp.logon.LanguagesBean" scope="session"/>
<jsp:useBean id="logonLocale" class="com.sap.security.core.sapmimp.logon.LogonLocaleBean" scope="session"/>
<jsp:useBean id="logonMessage" class="com.sap.security.core.sapmimp.logon.LogonMessageBean" scope="session"/>
<jsp:useBean id="logonBean" class="com.sap.security.core.sapmimp.logon.LogonBean" scope="session"/>
<jsp:useBean id="error" class="com.sap.security.core.util.ErrorBean" scope='request'/>

<% if (inPortal) {
      IUser user = proxy.getActiveUser ();
      boolean bUseEPCM = true;
      if (user!=null) {
          String attr = (String) user.getTransientAttribute (IPrincipal.DEFAULT_NAMESPACE,
              ILoginConstants.LOGON_AUTHSCHEME_ALIAS);
          if (attr!=null  && !"anonymous".equals (attr)) {
              //System.out.println ("AuthScheme is " + attr);
              bUseEPCM = false;
          }
          else {
              //System.out.println ("No Authscheme available.");
          }
      }
      else {
          //System.out.println ("in logon jsp user is not null");
      }
   }
%>

<% if( !inPortal ) { %>
<html>
<head>
<link rel=stylesheet href="<%=com.sap.security.core.logonadmin.ServletAccessToLogic.getAbsoluteURL(webpath,com.sap.security.core.imp.TenantFactory.getInstance().getLogonBrandingStyle(request))%>">
<title>User Management, SAP AG</title>
<% 
proxy.setResponseHeader ("pragma", "no-cache");
proxy.setResponseHeader ("cache-control", "no-cache");
proxy.setResponseHeader ("expires", "0");
 } %>
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
<script language="JavaScript">
function clearEntries() {
    document.logonForm.longUid.value="";
    document.logonForm.password.value="";
}

function setFocusToFirstField() {
        myform = document.logonForm;
        for(i=0; i<myform.length; i++) {
                elem = myform.elements[i];
                if(elem.readOnly==false && elem.type=="text") {
                        elem.focus();
                        break;
                }
        }
}

function addTenantPrefix()
{
	<%
	String tenantPrefix = com.sap.security.core.imp.TenantFactory.getInstance().getTenantLogonPrefix(request);
	if (!"".equals(tenantPrefix))
	{
	%>
	var userlogonid = document.logonForm.<%=ILoginConstants.LOGON_USER_ID%>.value;
	if (userlogonid.toLowerCase().indexOf("<%=tenantPrefix%>\".toLowerCase()) != 0)
	{
		document.logonForm.<%=ILoginConstants.LOGON_USER_ID%>.value = "<%=tenantPrefix%>\" + userlogonid;
	}
	<%
	}
	%>
	return true;
}

</script>
<% if( !inPortal ) { %>
</head>
<% } %>

<%@ include file="/umLogonTopArea.txt"%>

<FORM name="logonForm" method="post" action="<%=inPortal?"":"j_security_check"%>" <%=com.sap.security.core.imp.TenantFactory.getInstance().isBPOEnabled()&&UMFactory.getProperties().getBoolean(com.sap.security.core.imp.TenantFactory.MULTI_TENANCY_PREFIXING, true)?"onSubmit=\"javascript:addTenantPrefix();\"":""%>>
<% if( inPortal ) { %>
<input name="login_submit" type="hidden" value="on">
    <% if (UMFactory.getProperties().getBoolean ("ume.login.do_redirect", true)==true) { %>
    <input type="hidden" name="login_do_redirect" value="1" />
    <% } %>
<% } %>
<input name="no_cert_storing" type="hidden" value="on">
<table border="0" width="301" height="100" align="left" cellspacing="0" valign="top">
    <!-- display self-registration link if supposed to do so -->
    <% if ( logonBean.getSelfReg() ) { %>
        <tr>
            <td align="left" bgcolor="E9E9E9" colspan=2>
              <table border="0" cellpadding="0" cellspacing="0" >
                <tr>
                  <td>
                    <span class="urLblStdBar"><%=logonLocale.get("NEW_USERS")%></span>
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
    <% if ( error.getLocalizedMessage() != null ) { %>
        <tr>
          <td colspan="2" height="33">
            <div class="urMsgBarErr" style="margin-bottom:3;">
              <table border="0" cellpadding="0" cellspacing="0">
                <tbody>
                <tr>
                  <td class="urTxtStd"><span class="urMsgBarImgError"><img height="12" width="12" src="<%=webpath%>css/common/1x1.gif"></span></td>
                  <td><span class="urTxtStd" tabindex="0"><%=EncodeHtmlTag.encode(error.getLocalizedMessage())%></span></td>
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
    <!-- userid -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonuidfield">
          <%=logonLocale.get("USER")%>
          <span class=urLblReq>&nbsp;*</span>
        </label>
      </td>
      <td width="183" height="20">
        <input style="WIDTH: 21ex" class="urEdfTxtEnbl" id="logonuidfield" name="<%=ILoginConstants.LOGON_USER_ID%>" type="text" value="<%=EncodeHtmlTag.encode(logonBean.getLongUid(proxy)!=null?logonBean.getLongUid(proxy):UMFactory.getProperties().getBoolean(com.sap.security.core.imp.TenantFactory.MULTI_TENANCY_PREFIXING, true)?"":com.sap.security.core.imp.TenantFactory.getInstance().getTenantLogonPrefix(request))%>">
      </td>
    </tr>
    <!-- password -->
    <tr>
      <td width="161" height="20">
        <label class=urLblStd for="logonpassfield">
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
    <%
    	if( UMFactory.getProperties().getBoolean("ume.logon.locale", false) ) {

    %>
        <tr>
          <td width="161" height="26">
            <label class="urLblStd" for="preferredlanguage">
              <nobr><%=logonLocale.get("LANGUAGE")%></nobr>
            </label>
          </td>
          <td width="183" height="26">
          <select id="ume.logon.locale"
                  name="ume.logon.locale"
                   class="urDdlWhl" style="WIDTH: 20ex" size=1>
            <option value="" selected><%=logonLocale.get("PLEASE_SELECT")%></option><%=languages.getHtmlOptions((logonLocale != null)?logonLocale.getLocale().getLanguage():"-")%>
        </td>
      </tr>
	<%	
    }
    %>
    <!-- space above buttons -->
    <tr>
      <td colspan="2" height="20">&nbsp; </td>
    </tr>
    <!-- logon button -->
    <tr>
      <td colspan="2">
        <input style="height:3ex;" class="urBtnStd" type="submit" name="<%=SAPMLogonLogic.uidPasswordLogonAction%>" value="<%=logonLocale.get("LOGON")%>">
      </td>
    </tr>
    <!-- link to certificate logon -->
    <% if ( !inPortal && logonBean.getAllowCertLogon() ) { %>
        <tr>
            <td align="left" bgcolor="E9E9E9" colspan=2>
              <table border="0" cellpadding="0" cellspacing="0" >
                <tr>
                  <td>
                    <a class=urLnk href="<%=inPortal?proxy.getAlias("com.sap.portal.runtime.logon.certlogon",null):logonBean.getHttpsCertURL(proxy, null)%>">
                      <span class=urTxtStd><%=logonLocale.get("GOTO_CERT_LOGON_PAGE1")%></span>
                    </a>
                  </td>
                </tr>
              </table>
            </td>
        </tr>
    <% } %>
    <!-- logon help -->
    <% if ( logonBean.getLogonHelp() ) { %>
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
    <% } %>
</table>
</form>

<%@ include file="/umLogonBotArea.txt"%>

<% if(!inPortal) proxy.sessionInvalidate(); %>
<%if(inPortal) { %>
<% } else { %>
</html>
<% } %>