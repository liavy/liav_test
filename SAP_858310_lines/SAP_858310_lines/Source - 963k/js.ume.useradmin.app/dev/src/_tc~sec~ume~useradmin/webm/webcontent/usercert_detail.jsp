<%@ taglib uri="UM" prefix="UM" %>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.security.cert.X509Certificate"%>

<%@ include file="proxy.txt" %>

<% X509Certificate cert = (X509Certificate) proxy.getRequestAttribute(UserAdminLogic.userCertificate);
   DateFormat dateFormat = DateFormat.getDateInstance();
   String begin = dateFormat.format(cert.getNotBefore());
   String end = dateFormat.format(cert.getNotAfter());
   Boolean imported = (Boolean) proxy.getRequestAttribute(UserAdminLogic.certImported);
   String parent = UserAdminLogic.userModifyPage;
%>

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

<center>
<table width="80%" height="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
  <!-- Start Middle 780pxl Content space -->
	<td width="780" height="100%" valign="top" class="TB_CNT_BG">

    <!-- Start Content -->
    <table cellpadding="0" cellspacing="0" border="0" width="100%" height="100%">
	    <tr>
	    <!-- Start Transactional Content -->
		  <td width="100%" valign="top">

		    <table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>
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
			    <span tabindex="0"><%=userAdminLocale.get("CERT_DESCRIPTION")%></span>
			    </td></tr>
			  </table>
			  <!-- End Section Description -->

			  <table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>

			  <table class="TBLO_XXS_L" cellpadding="0" cellspacing="0" width="98%" border="0">
			    <tr><td CLASS="TBLO_XXS_L">
			      <table cellpadding="2" cellspacing="1" border="0">
              <tr>
                <td CLASS="TBLO_XXS_R" tabindex="0"nowrap><%=userAdminLocale.get("VERSION")%>:</td>
                <td CLASS="TBLO_XSB" tabindex="0"nowrap><UM:encode><%=cert.getVersion()%></UM:encode></td>
              </tr>
              <tr>
                <td CLASS="TBLO_XXS_R" tabindex="0"nowrap><%=userAdminLocale.get("SERIAL_NUM")%>:</td>
                <td CLASS="TBLO_XSB" tabindex="0"nowrap><UM:encode><%=cert.getSerialNumber()%></UM:encode></td>
              </tr>
              <tr>
                <td CLASS="TBLO_XXS_R" tabindex="0"nowrap><%=userAdminLocale.get("ISSUER")%>:</td>
                <td CLASS="TBLO_XSB" tabindex="0"nowrap><UM:encode><%=cert.getIssuerDN()%></UM:encode></td>
              </tr>
              <tr>
                <td CLASS="TBLO_XXS_R" tabindex="0"nowrap><%=userAdminLocale.get("VALID_FROM")%>:</td>
                <td CLASS="TBLO_XSB" tabindex="0"nowrap><UM:encode><%=begin%></UM:encode></td>
              </tr>
              <tr>
                <td CLASS="TBLO_XXS_R" tabindex="0"nowrap><%=userAdminLocale.get("VALID_TO")%>:</td>
                <td CLASS="TBLO_XSB" tabindex="0"nowrap><UM:encode><%=end%></UM:encode></td>
              </tr>
              <tr>
                <td CLASS="TBLO_XXS_R" tabindex="0"nowrap><%=userAdminLocale.get("SUBJECT")%>:</td>
                <td CLASS="TBLO_XSB" tabindex="0"nowrap><UM:encode><%=cert.getSubjectDN()%></UM:encode></td>
              </tr>
				    </table>
		    	</td></tr>
			  </table>
			  <!-- End Table TBLO_XS -->

			  <table cellpadding="0" cellspacing="0" border="0">
          <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
        </table>

			  <!-- Start Page Action Buttons -->
        <% if ( null == imported ) { %>
			  <table width="98%" border="0" cellpadding="0" cellspacing="0">
				  <tr><td width="100%" class="TBLO_XS_L" nowrap>
					<input class="BTN_LB"
		                   type="button"
		                   tabindex="0"
		                   name="OK"
		                   value="&nbsp;<%=userAdminLocale.get("OK")%>&nbsp;"
		                   onClick="javascript:selfclose();">&nbsp;
					</td></tr>
			  </table>
        <% } else { %>
        <form name="certdetail" method="post" target="<%=parent%>" action="<%=userAdminAlias%>" onSubmit="javascript:selfclose();">
			  <table width="98%" border="0" cellpadding="0" cellspacing="0">
				  <tr><td width="100%" class="TBLO_XS_L" nowrap>
					<input class="BTN_LB"
		                   type="submit"
		                   tabindex="0"
		                   name="<%=UserAdminLogic.backToUserModifyAction%>"
		                   value="&nbsp;<%=userAdminLocale.get("OK")%>&nbsp;">
					</td></tr>
			  </table>
        </form>
        <% } %>

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
