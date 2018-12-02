<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>
<%@ page isErrorPage="true" session="true"%>
<%@ page import="com.sap.security.api.UMFactory" %>
<jsp:useBean id="throwable"
             class="java.lang.Throwable"
             scope="request"/>
<%
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
%>

<%if (!inPortal) {%>
<html>
<HEAD>
  <TITLE><%=userAdminLocale.get("EXCEPTION_PAGE_HEADER")%></TITLE>
  <script language="JavaScript" src="js/basic.js"></script>
</head>


<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<%@ include file="contextspecific_includes_top.txt" %>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=userAdminLocale.get("EXCEPTION_PAGE_HEADER")%></td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
        <IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
    <tr><td width="100%" class="TBLO_XXS_L">
        <%=userAdminLocale.get("EXCEPTION_HAS_OCCURED")%>&nbsp;<%=userAdminLocale.get("INFORM_SYSTEM_ADMIN")%>
    </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>

<% if (UMFactory.getProperties().getBoolean("ume.admin.debug_internal", false) ) { %>
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
    <tr><td class="TBLO_XXS_L">
        <table id="e1-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
			<td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("EXCEPTION_REASON")%></td>
			<td class="TBDATA_CNT_EVEN_BG" align="right" width="20">
			<img id="e1-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('e1', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
			</tr>
        </table>
    </td></tr>
    <tr><td class="TBDATA_CNT_ODD_BG"><div id="e1-bd">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
			<tr><td><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr>
				<td class="TX_XS" width="70%" nowrap>
				<% String message = throwable.getLocalizedMessage();
				   if ( null == message ) {
				       message = userAdminLocale.get("EXCEPTION_NO_DETAILED_MSG");
				   } %>
				<UM:encode><%=message%></UM:encode>
				</td>
			</tr>
			<tr><td>
				<IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
			</td></tr>
        </table></div>
    </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
<tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
</td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
    <tr><td class="TBLO_XXS_L">
        <table id="e2-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
			<td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("CALLING_STACK")%></td>
			<td class="TBDATA_CNT_EVEN_BG" align="right" width="20">
			<img id="e2-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('e2', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
			</tr>
        </table>
    </td></tr>
    <tr><td class="TBDATA_CNT_ODD_BG"><div id="e2-bd">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h1">
			<tr><td><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr>
				<td class="TX_XS" width="70%" nowrap><pre><%throwable.printStackTrace(new java.io.PrintWriter(out));%></pre></td>
			</tr>
			<tr><td>
				<IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
			</td></tr>
        </table></div>
    </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
<tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
</td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
    <tr><td class="TBLO_XXS_L">
        <table id="e3-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
			<td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("REQUEST_URL")%></td>
			<td class="TBDATA_CNT_EVEN_BG" align="right" width="20">
			<img id="e3-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('e3', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
			</tr>
        </table>
    </td></tr>
    <tr><td class="TBDATA_CNT_ODD_BG"><div id="e3-bd">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h2">
			<tr><td><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr>
				<td class="TX_XS" width="70%" nowrap><UM:encode><%=HttpUtils.getRequestURL(proxy.getServletRequest()).toString()%></UM:encode></td>
			</tr>
			<tr><td>
				<IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
			</td></tr>
        </table></div>
    </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
<tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
</td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
    <tr><td class="TBLO_XXS_L">
        <table id="e4-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
			<td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("REQUEST_PARAMETER")%></td>
			<td class="TBDATA_CNT_EVEN_BG" align="right" width="20">
			<img id="e4-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('e4', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
			</tr>
        </table>
    </td></tr>
    <tr><td class="TBDATA_CNT_ODD_BG"><div id="e4-bd">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h3">
			<tr><td><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr>
				<td class="TX_XS" width="70%" nowrap>
            <% java.util.Enumeration parameterNames = proxy.getRequestParameterNames();
               String name = null;
               while ( parameterNames.hasMoreElements() ) {
                  name = (String) parameterNames.nextElement(); %>
                  <b><UM:encode><%=name%></UM:encode></b>:
                  <% if (name.indexOf("pass") == -1) {
                         String [] values = proxy.getRequestParameterValues(name);
                         for ( int i = 0; i < values.length; i++ ) { %>
                             "<UM:encode><%=values [i]%></UM:encode>"<br>
                         <% } 
                     } else { %>
                         <i>not disclosed</i><br>
                  <% } %>
            <% } %>
        </td>
			</tr>
			<tr><td>
				<IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
			</td></tr>
        </table></div>
    </td></tr>
</table>
<% } else { %>
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
    <tr><td class="TBLO_XXS_L">
        <table id="e1-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
			<td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("WHAT_TO_DO_WITH_EXCEPTION")%></td>
			<td class="TBDATA_CNT_EVEN_BG" align="right" width="20">
			<img id="e1-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('e1', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
			</tr>
        </table>
    </td></tr>
    <tr><td class="TBDATA_CNT_ODD_BG"><div id="e1-bd">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
			<tr><td><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
			<tr>
				<td class="TX_XS" width="70%" nowrap><%=userAdminLocale.get("EXCEPTION_DETAILED_STEPS")%></td>
			</tr>
			<tr><td>
				<IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
			</td></tr>
        </table></div>
    </td></tr>
</table>
<% } %>

<table cellpadding="0" cellspacing="0" border="0">
<tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
</td></tr>
</table>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>
