<%@ page import="com.sap.security.core.admin.batch.BatchLogic" %>
<%@ page session="true"%>
<%@ page import = "com.sap.security.core.admin.batch.*" %>
<%@ page import = "com.sap.security.core.util.batch.*" %>
<%@ page import = "com.sap.security.core.*" %>
<%@ page import = "java.util.*" %>

<%@ include file="/proxy.txt" %>

<jsp:useBean id="throwable"
             class="java.lang.Throwable"
             scope="request"/>


<%-- start html--%>
<%if (!inPortal) {%> <html>
<head>
<TITLE><%=userAdminLocale.get("USER_DETAILS")%></TITLE>
<!--link rel="stylesheet" href="css/main2.css" -->
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<%@ include file="/contextspecific_includes_top.txt" %>


<%
Vector protocol = (Vector) (proxy.getSessionAttribute("protocol"));

HashMap user = (HashMap) protocol.elementAt(Integer.parseInt(proxy.getRequestParameter(BatchLogic.userNumber)));
String value="";
%>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=userAdminLocale.get("USER_DETAILS")%></td></tr>
</table>
<!-- End Section Header -->
<!-- Start 10px spacing -->
<table cellpadding="0" cellspacing="0" border="0">
	<tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="10" alt="" border="0"></td></tr>
</table>
<!-- End 10px spacing -->

<a href=<%=util.alias(proxy, BatchLogic.servlet_name)%>?<%=BatchLogic.ProtocolPageAction%>=><%=userAdminLocale.get("BACK_TO_PROTOCOL")%></a>
<table border="0" rules="all">
   <tr><td nowrap> <%=Batch.Status%></td> <td><%=(user.get(Batch.Status)==null)?"-":user.get(Batch.Status)%> </td></tr>
   <tr><td nowrap>
     <%
       String warnings = (String) (user.get(Batch.Warnings));
       if (warnings!=null)
       {
		       StringTokenizer st = new StringTokenizer(warnings, "&", false);
		       warnings="";
		       while (st.hasMoreElements())
		       {
		         warnings = warnings+"<br>"+com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String) st.nextElement());
		       }
       }
     %>
     <%=Batch.Warnings%></td> <td><%=(warnings==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode(warnings)%>
   </td></tr>
   <tr><td nowrap> <%=Batch.CompanyName%></td> <td><%=(user.get(Batch.CompanyName)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.CompanyName))%> </td></tr>
   <tr><td nowrap> <%=Batch.OrgUnit%></td> <td><%=(user.get(Batch.OrgUnit)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.OrgUnit))%> </td></tr>
   <tr><td nowrap> <%=Batch.InternalUID%></td> <td><%=(user.get(Batch.InternalUID)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.InternalUID))%> </td></tr>
   <tr><td nowrap> <%=Batch.DisplayName%></td> <td><%=(user.get(Batch.DisplayName)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.DisplayName))%> </td></tr>


      <td nowrap><%=Batch.Role%></td>
      <td>
         <%
         String rolestring = "";
         Vector roles = (Vector)( user.get(Batch.Role));
         if (roles!=null)
         {
                                 for (int j=0; j<roles.size(); j++)
                                 {
                                  rolestring = rolestring + " " + ((String)(roles.elementAt(j)));
                                 }
         }
         out.print(com.sap.security.core.util.taglib.EncodeHtmlTag.encode(rolestring));
         %>

   </td>
   </tr>


   <tr><td nowrap> <%=Batch.Email%></td> <td><%=(user.get(Batch.Email)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Email))%> </td></tr>
   <tr><td nowrap> <%=Batch.FirstName%></td> <td><%=(user.get(Batch.FirstName)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.FirstName))%> </td></tr>
   <tr><td nowrap> <%=Batch.LastName%></td> <td><%=(user.get(Batch.LastName)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.LastName))%> </td></tr>
   <tr><td nowrap> <%=Batch.JobTitle%></td> <td><%=(user.get(Batch.JobTitle)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.JobTitle))%> </td></tr>
   <tr><td nowrap> <%=Batch.Country%></td> <td><%=(user.get(Batch.Country)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Country))%> </td></tr>
   <tr><td nowrap> <%=Batch.Language%></td> <td><%=(user.get(Batch.Language)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Language))%> </td></tr>
   <tr><td nowrap> <%=Batch.TimeZone%></td> <td><%=(user.get(Batch.TimeZone)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.TimeZone))%> </td></tr>
   <tr><td nowrap> <%=Batch.Currency%></td> <td><%=(user.get(Batch.Currency)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Currency))%> </td></tr>
   <tr><td nowrap> <%=Batch.Street%></td> <td><%=(user.get(Batch.Street)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Street))%> </td></tr>
   <tr><td nowrap> <%=Batch.City%></td> <td><%=(user.get(Batch.City)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.City))%> </td></tr>
   <tr><td nowrap> <%=Batch.State%></td> <td><%=(user.get(Batch.State)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.State))%> </td></tr>
   <tr><td nowrap> <%=Batch.Province%></td> <td><%=(user.get(Batch.Province)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Province))%> </td></tr>
   <tr><td nowrap> <%=Batch.Zip%></td> <td><%=(user.get(Batch.Zip)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Zip))%> </td></tr>
   <tr><td nowrap> <%=Batch.Telephone%></td> <td><%=(user.get(Batch.Telephone)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Telephone))%> </td></tr>
   <tr><td nowrap> <%=Batch.Fax%></td> <td><%=(user.get(Batch.Fax)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Fax))%> </td></tr>
   <tr><td nowrap> <%=Batch.Password%></td> <td><%=(user.get(Batch.Password)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Password))%> </td></tr>
<!-- <tr><td nowrap> <%=Batch.Name%></td> <td><%=(user.get(Batch.Name)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Name))%> </td></tr> -->
   <tr><td nowrap> <%=Batch.Telephone_ext%></td> <td><%=(user.get(Batch.Telephone_ext)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Telephone_ext))%> </td></tr>
   <tr><td nowrap> <%=Batch.Fax_ext%></td> <td><%=(user.get(Batch.Fax_ext)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Fax_ext))%> </td></tr>
   <tr><td nowrap> <%=Batch.Building%></td> <td><%=(user.get(Batch.Building)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Building))%> </td></tr>
   <tr><td nowrap> <%=Batch.Room%></td> <td><%=(user.get(Batch.Room)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Room))%> </td></tr>
   <tr><td nowrap> <%=Batch.InhousePost%></td> <td><%=(user.get(Batch.InhousePost)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.InhousePost))%> </td></tr>
   <tr><td nowrap> <%=Batch.AcademicGrade%></td> <td><%=(user.get(Batch.AcademicGrade)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.AcademicGrade))%> </td></tr>
   <tr><td nowrap> <%=Batch.IsManager%></td> <td><%=(user.get(Batch.IsManager)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.IsManager))%> </td></tr>
   <tr><td nowrap> <%=Batch.Floor%></td> <td><%=(user.get(Batch.Floor)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Floor))%> </td></tr>
   <tr><td nowrap> <%=Batch.Salutation%></td> <td><%=(user.get(Batch.Salutation)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)user.get(Batch.Salutation))%> </td></tr>


</tr>
</table>
<p>
<a href=<%=util.alias(proxy, BatchLogic.servlet_name)%>?<%=BatchLogic.ProtocolPageAction%>=><%=userAdminLocale.get("BACK_TO_PROTOCOL")%></a>

<p>
<%@ include file="/contextspecific_includes_bottom.txt" %>


