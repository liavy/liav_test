<%@ taglib uri="UM" prefix="UM" %>
<%@ include file="proxy.txt" %>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>

<% String parent = UserAdminLogic.userModifyPage;
   String certStr = (String) proxy.getRequestAttribute(UserAdminLogic.certString);
   if ( null == certStr) certStr = util.empty; %>

<%if (!inPortal) {%>
<html>
<head>
<title><%=userAdminLocale.get("USER_MANAGEMENT")%></title>
<script language="JavaScript" src="js/basic.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<script language="JavaScript">
  function selfclose() {
    window.close();
  }
</script>

<form name="certimport" method="post" action ="<%=userAdminAlias%>">
<center>
<table width="98%" height="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
  <!-- Start Middle 780pxl Content space -->
	<td width="780%" height="100%" valign="top" class="TB_CNT_BG">

    <!-- Start Content -->
    <table cellpadding="0" cellspacing="0" border="0" width="100%" height="100%">
	    <tr>
	    <!-- Start Transactional Content -->
		  <td width="100%" valign="top">

        <!-- Satrt Section Header -->

			<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
			      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
			      <span tabindex="0"><%=userAdminLocale.get("CERT_HEADER")%></span>
			      </td></tr>
			</table>
			  <!-- End Section Header -->

	<table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>

			  <!-- Start Section Description -->
			  <table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
			    <tr><td width="100%" class="TBLO_XXS_L">
			    <span tabindex="0"><%=userAdminLocale.get("IMPORT_CERT")%></span>
			    </td></tr>
			  </table>
			  <!-- End Section Description -->

        <% if ( error.isError() ) { certStr = util.empty; %>
	  <table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>
        <!-- Start Error Msg-->
        <table align="center" cellpadding="0" cellspacing="0" width="98%" border="0">
          <tr><td width="100%" class="TX_ERROR_XSB">
            <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
            <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
          </td></tr>
        </table>
        <!-- End Error Msg -->
        <% } %>

	<table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>

	    <table class="TBLO_XXS_L" cellpadding="0" cellspacing="0" width="100%" border="0">
            <tr><td width="100%">
              <textarea id="<%=UserAdminLogic.certString%>"
                        name="<%=UserAdminLogic.certString%>"
                        tabindex="0"
                        value="<%=util.filteringSpecialChar(certStr)%>"
                        cols="80"
                        rows="10"
                        style="width: 4in"><UM:encode><%=certStr%></UM:encode></textarea>
		    	</td></tr>
			  </table>

			  <table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="10" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>

			  <!-- Start Page Action Buttons -->
			  <table width="98%" border="0" cellpadding="0" cellspacing="0">
				  <tr><td width="100%" class="TBLO_XS_L" nowrap>
            <input class="BTN_LB"
                   type="submit"
                   tabindex="0"
                   name="<%=UserAdminLogic.performUserCertImportAction%>"
                   value="&nbsp;<%=userAdminLocale.get("DO_IMPORT_CERT")%>&nbsp;">&nbsp;
            <input class="BTN_LN"
                   type="reset"
                   tabindex="0"
                   name="reset"
                   value="&nbsp;<%=userAdminLocale.get("RESET")%>&nbsp;">&nbsp;
            <input class="BTN_LN"
                   type="button"
                   tabindex="0"
                   name="OK"
                   value="&nbsp;<%=userAdminLocale.get("CANCEL")%>&nbsp;"
                   onClick="javascript:selfclose();">
					</td></tr>
			  </table>
        </form>

			  <table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="50" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>

			  <!-- Start Footer -->
			  <table cellspacing="0" cellpadding="0" width="100%" border="0">
			    <tr><td><hr size="1" color="#CBCCCC" width="50%" align="left"></td></tr>
          <tr><td class="TBLO_XXS_L">&nbsp;&nbsp;<%=userAdminLocale.get("COPY_RIGHT")%><br></td> </tr>
          <tr><td class="TBLO_XXS_L" valign="top"><img src="<%=webpath%>layout/sapLogo.gif" width="44" height="22" border="0" alt="<%=userAdminLocale.get("SAP")%>" title="<%=userAdminLocale.get("SAP")%>">&nbsp;&nbsp;</td></tr>
			  </table>
			  <!-- End footer-->
		  </td>
	    <!-- End Transactional Content -->
	    </tr>
    </table>
    <!-- End Content -->
  </td>
  <!-- End Middle 780pxl Content space -->
  <!-- Right space -->
  </tr>
</table>
</form>
</center>
</body>
</html>
