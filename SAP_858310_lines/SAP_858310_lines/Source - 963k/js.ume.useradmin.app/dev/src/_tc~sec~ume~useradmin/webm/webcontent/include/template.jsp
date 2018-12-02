<%-- content not commented is mandatory --%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<%-- start html--%>
<%if (!inPortal) {%> <html>
<head>
<TITLE>your jsp title</TITLE>
<!--link rel="stylesheet" href="css/main2.css" -->
<script language="JavaScript" src="js/basic.js"></script>
</head> <%}%>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" >
<%@ include file="contextspecific_includes_top.txt" %>

   <!-- start content -->
   <table cellpadding="0" align="center" cellspacing="0" border="0" width="99%" id="1">

   </table>
   <!-- end content -->
<%@ include file="contextspecific_includes_bottom.txt" %>

</body>
</html>
