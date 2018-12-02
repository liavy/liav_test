<!--
    ATTN: if you want to remove the entire top_bar, please comment the context
    below the page attribute definition.
-->
<% if ( util.isServlet23() ) proxy.setResponseContentType("text/html; charset=utf-8"); %>
<!-- end of page attribute definition-->

<% StringBuffer viewUserProfile = new StringBuffer(userAdminAlias);
   StringBuffer logOut = new StringBuffer(userAdminAlias);
   viewUserProfile.append("?");
   viewUserProfile.append(UserAdminLogic.viewUserProfileAction);
   viewUserProfile.append("=");
   logOut.append("?");
   logOut.append(UserAdminLogic.logOffAction);
   logOut.append("=");
%>

<center>
<TABLE height="100%" cellSpacing=0 cellPadding=0 width="100%" border=0>
<tr>

  <!-- Start Middle Content space -->
  <td width="100%" height="100%" valign="top" class="TB_CNT_BG">

    <!-- Start Content -->
    <table cellspacing="0" border="0" cellpadding="0" width="100%">
      <tr>
        <td class="logo" nowrap><img src="<%=webpath%>layout/sp.gif" tabindex="0" width="10" height="8" border="0" alt="<%=userAdminLocale.get("SKIP_TO_MAIN_CONTENT")%>"><%=userAdminLocale.get("USER_MANAGEMENT")%></td>
	<td class="headertop"><img src="layout/sp.gif" width="20" height="8"></td>
        <td class="headertop" tabindex="0" nowrap><a href="<%=viewUserProfile.toString()%>"><%=userAdminLocale.get("UM_HOME")%></a></td>
        <td class="headerline">&nbsp;&nbsp;|&nbsp;&nbsp;</td>
        <td class="headertop" tabindex="0" nowrap><a href="<%=logOut.toString()%>"><%=userAdminLocale.get("LOG_OUT")%></a></td>
        <td class="headertop" width="100%">&nbsp;</td>
        <td rowspan="2" valign="top" colspan="2" align="right"><img src="layout/logosap.gif" width="61" height="32" vspace="0" hspace="0" border="0" alt="<%=userAdminLocale.get("SAP")%>"></td>
      </tr>
      <tr>
        <td bgcolor="#FF9A00" colspan="6"><img src="<%=webpath%>layout/sp.gif" alt="" width="1" height="2" border="0"></td>
      </tr>
    </table>
<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="layout/sp.gif" width="1" height="15" alt="" border="0"></td></tr></table>

<!-- end new header bar -->

