<%@ include file="proxy.txt" %>
<%@ page session="true"%>
<%Boolean isOrNot = (Boolean) proxy.getSessionAttribute(SelfRegLogic.isCompanyUserId);
  boolean isCompanyUser = isOrNot.booleanValue();
  String errorMessage = (String)proxy.getRequestAttribute(SelfRegLogic.errorMessage);
%>

<% if ( util.isServlet23() ) proxy.setResponseContentType("text/html; charset=utf-8"); %>

<%if (!inPortal) {%>
<HTML>
<HEAD>
  <TITLE><%=userAdminLocale.get("SELF_REGISTRATION")%></TITLE>
  <script language="JavaScript" src="js/basic.js"></script>
</HEAD>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" >
<%}%>

<center>
<form>
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
        <table cellpadding="5" cellspacing="0" border="0" width="100%">
    	<tr class="SIDE_N_BG">
    	  <td class="TX_XSB_DGRAY">
    	    <span tabindex="0"><%=userAdminLocale.get("CONFIRMATION_HEADER")%></span>
    	  </td>
    	</tr>
        <tr class="SIDE_N_BG">
			  <td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" border="0" alt=""></td>
    	</tr>
        <tr class="SIDE_N_BG" align="center">
          <td class="TBLO_XS_L">
            <span tabindex="0">
            <% if (!isCompanyUser){ %>
                <%= userAdminLocale.get("INFO_TO_SELFREGED_USER") %>
            <% } else { %>
    		    <%= userAdminLocale.get("INFO_TO_UNAPPROVED_COMPANYUSER") %>
            <%  } %>
            </span>
          </td>
		</tr>

        <tr class="SIDE_N_BG">
          <td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" border="0" alt=""></td>
        </tr>
        <% if ( null == errorMessage ) { %>
        <tr class="SIDE_N_BG">
          <td class="TX_XXS" nowrap>
            <% String goon = " "+userAdminLocale.get("CONTINUE")+" "; %>
            <form method="post" action="<%=selfRegAlias%>">
            <input type="submit"
                   class="BTN_LB"
                   tabindex="0"
                   name="<%=SelfRegLogic.redirectToServiceAction%>"
                   value="<%=goon%>">&nbsp;
            </form>
          </td>
        </tr>
        <% } else { %>
        <tr class="SIDE_N_BG">
          <td width="100%" class="TX_ERROR_XSB">
            <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
            <span tabindex="0"><%=userAdminLocale.get(errorMessage)%></span>
          </td>
        </tr>
        <% } %>
        <!-- tr class="SIDE_N_BG" align="center">
          <td><img src="<%=webpath%>layout/workflow.gif" tabindex="0" width="515" height="210" border="0" alt="<%=userAdminLocale.get("SELFREG_PROCESS")%>"></td>
        </tr -->
        <tr class="SIDE_N_BG">
          <td><img src="<%=webpath%>layout/sp.gif" width="1" height="12" border="0" alt=""></td>
        </tr>
    	</table>
    <!-- End Page Action Buttons -->
<%@ include file="contextspecific_includes_bottom.txt" %>

