<%@ taglib uri="UM" prefix="UM" %>
<%@ page session="true"%>

<%@ include file="proxy.txt" %>

<% if ( util.isServlet23() ) proxy.setResponseContentType("text/html; charset=utf-8"); %>
<jsp:useBean id="throwable"
             class="java.lang.Throwable"
             scope="request"/>
<%
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
%>

<%if (!inPortal) {%>
<HTML>
<HEAD>
  <TITLE><%=userAdminLocale.get("EXCEPTION_PAGE_HEADER")%></TITLE>
  <script language="JavaScript" src="js/basic.js"></script>
</HEAD>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<center>
<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
<tr>

<!-- Start Middle 780pxl Content space -->
	<td width="780" height="100%" valign="top" class="TB_CNT_BG">
<!-- Start Content -->
<table cellpadding="0" cellspacing="0" border="0" width="780" height="100%">
    <tr>
    <!-- Start Fuction Icons Shadow -->
    <td width="100%" valign="top" class="SIDE_N_BG">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
              <td background="<%=webpath%>layout/topbkgd.gif" width="100%"><img src="<%=webpath%>layout/sp.gif" height="4" border="0" alt=""></td>
          </tr>
        </table>
    </td>
    <!-- End Fuction Icons Shadow -->
    </tr>

	<tr>
	<!-- Start Transactional Content -->
    <td width="100%" valign="top">
<!-- Start Section Header -->

<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
      <span tabindex="0"><%=userAdminLocale.get("EXCEPTION_PAGE_HEADER")%></span>
      </td></tr>
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
        <span tabindex="0"><%=userAdminLocale.get("EXCEPTION_HAS_OCCURED")%>&nbsp;<%=userAdminLocale.get("INFORM_SYSTEM_ADMIN")%></span>
    </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
    <tr><td>
    <IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
    </td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
    <tr><td class="TBLO_XXS_L">
        <table id="e1-hd" border="0" cellpadding="0" cellspacing="0"><tr class="TBDATA_CNT_EVEN_BG">
			<td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=userAdminLocale.get("WHAT_TO_DO_WITH_EXCEPTION")%></td>
			<td class="TBDATA_CNT_EVEN_BG" align="right" width="20">
			<img id="e1-exp"
                 tabIndex="0"
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
				<td class="TX_XS" width="70%" tabIndex="0" nowrap>userAdminLocale.get("EXCEPTION_DETAILED_STEPS")</td>
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
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>


