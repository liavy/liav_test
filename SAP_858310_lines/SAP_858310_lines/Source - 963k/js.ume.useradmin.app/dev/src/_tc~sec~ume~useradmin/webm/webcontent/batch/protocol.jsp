<%@ page import="com.sap.security.core.admin.batch.BatchLogic" %>
<%@ page session="true"%>
<%@ page import = "com.sap.security.core.admin.batch.*" %>
<%@ page import = "com.sap.security.core.util.batch.*" %>
<%@ page import = "com.sap.security.core.*" %>
<%@ page import = "java.util.*" %>

<%@ include file="/proxy.txt" %>

<%-- start html--%>
<%if (!inPortal) {%> <html>
<head>
<TITLE><%=userAdminLocale.get("UPLOAD_COMPLETE")%></TITLE>
<link rel="stylesheet" href="css/main2.css" >
<script language="JavaScript" src="<%=webpath%>js/basic.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<%@ include file="/contextspecific_includes_top.txt" %>

<%
Exception ex = (Exception) (proxy.getRequestAttribute("exception"));

if (ex != null)
  {
%>

<b><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(ex.getMessage())%></b>
<p>
<a href=<%=util.alias(proxy, BatchLogic.servlet_name)%>?<%=BatchLogic.uploadAction%>=><%=userAdminLocale.get("BACK")%></a>

<%

}
else
{%>

<%
Vector protocol = (Vector) (proxy.getSessionAttribute("protocol"));

/*if (protocol == null)
{
protocol = (Vector) componentRequest.getServletRequest().getSession().getAttribute("ume.batchupload.protocol");
}
else
{
//put into HTTP session to allow for cross component use
componentRequest.getServletRequest().getSession().setAttribute("ume.batchupload.protocol", protocol);
}
*/

int size = 0; if (protocol!=null) size = protocol.size();

int createdCounter = 0;
int updatedCounter = 0;
int failedCounter = 0;
int updatefailedCounter = 0;
int existsCounter = 0;
int nopermissionCounter = 0;


for (int i=0; i<size; i++)
{
 HashMap entry = (HashMap)(protocol.elementAt(i));
 String st = ((String) entry.get(Batch.Status)).trim();
 if (st.equalsIgnoreCase(Batch.CREATED)) createdCounter++;
 else if (st.equalsIgnoreCase(Batch.UPDATED)) updatedCounter++;
 else if (st.equalsIgnoreCase(Batch.FAILED)) failedCounter++;
 else if (st.equalsIgnoreCase(Batch.UPDATE_FAILED)) updatefailedCounter++;
 else if (st.equalsIgnoreCase(Batch.EXISTS)) existsCounter++;
 else if (st.equalsIgnoreCase(Batch.NO_PERMISSION)) nopermissionCounter++;
}
%>


<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
				&nbsp;<%=userAdminLocale.get("UPLOAD_TOOK")%> <%=((Long)proxy.getSessionAttribute("lastRunTime")).longValue()%> 				<%=userAdminLocale.get("SECONDS")%>&nbsp;&nbsp;&nbsp;<%=userAdminLocale.get("NUMBER_OF_USERS_PROCESSED")%>:&nbsp;<%=size%>
	  </td></tr>
</table>

<!-- End Section Header -->

<table>
<tr>
						<td align=center class="TBDATA_HEAD" width="16%" NOWRAP><font color="green"><%=userAdminLocale.get("CREATED")%>: <b><%=createdCounter%></b></font></td>
						<td align=center class="TBDATA_HEAD" width="16%" NOWRAP><font color="green"><%=userAdminLocale.get("UPDATED")%>: <b><%=updatedCounter%></b></font></td>
						<td align=center class="TBDATA_HEAD" width="16%" NOWRAP><font color="gray"><%=userAdminLocale.get("EXISTS")%>: <b><%=existsCounter%></b></font></td>
						<td align=center class="TBDATA_HEAD" width="16%" NOWRAP><font color="red"><%=userAdminLocale.get("FAILED")%>: <b><%=failedCounter%></b></font></td>
						<td align=center class="TBDATA_HEAD" width="16%" NOWRAP><font color="red"><%=userAdminLocale.get("UPDATE_FAILED")%>: <b><%=updatefailedCounter%></b></font></td>
						<td align=center class="TBDATA_HEAD" width="16%" NOWRAP><font color="red"><%=userAdminLocale.get("NO_PERMISSION")%>: <b><%=nopermissionCounter%></b></font></td>
</tr>
</table>


<%
String text;
int usersOnPage = Integer.parseInt((String) proxy.getSessionAttribute(BatchLogic.usersOnPageId));
int currentPage = Integer.parseInt(((String)(proxy.getSessionAttribute(BatchLogic.currentPageId))));
int numberOfErrors = 0;
String filter = (String) proxy.getRequestAttribute("filter");
if (filter==null) filter = "ALL";

if (protocol!=null)
{
%>

<FORM name="filterform" method="POST" action="<%=batchAlias%>">
<input type="hidden" name="filterEvent" value="true">

<table border="1" width="100%>
<tr width="100%"><td class="TBDATA_HEAD" NOWRAP>

<a
<%
if (currentPage>1)
{
%>

href="<%=util.alias(proxy, BatchLogic.servlet_name)%>?<%=BatchLogic.ProtocolPageAction%>=&<%=BatchLogic.currentPageId%>=<%=currentPage-1%>&filter=<%=filter%>">
<%}%>

<img src="<%=webpath%>layout/left.gif" width="14" height="13" border="0" alt="Prev Page">
</a>
&nbsp;&nbsp;&nbsp;
<%=userAdminLocale.get("DISPLAY")%>
<input type="hidden" name="<%=BatchLogic.ProtocolPageAction%>" value="">
<select name="<%=BatchLogic.usersOnPageId%>" onChange='submit();' class="DROPDOWN">
<%for (int k=10; k<size; k=k+10)
{%>
<option <%=(usersOnPage==k)?"selected":""%>><%=k%></option>
<%}%>
<option <%=(usersOnPage==size)?"selected":""%>><%=size%></option>
</select>
<%=userAdminLocale.get("PER_PAGE")%>
&nbsp;&nbsp;&nbsp;&nbsp;
<%=userAdminLocale.get("THIS_IS")%>
<select name="<%=BatchLogic.currentPageId%>" onChange='submit();' class="DROPDOWN">
<%for (int j=1; j<=Math.round(Math.ceil((float)(size)/(float)(usersOnPage))); j++)
  {
%>
<option <%=(j==currentPage)?"selected":""%>><%=j%></option>
<%}%>
</select>
<%=userAdminLocale.get("OF")%>
<%=Math.round(Math.ceil((float)(size)/(float)(usersOnPage)))%> <%=userAdminLocale.get("PAGES")%>
&nbsp;&nbsp;&nbsp;&nbsp;
<a
<%
if (currentPage<Math.round(Math.ceil((float)(size)/(float)(usersOnPage))))
{
%>
href="<%=util.alias(proxy, BatchLogic.servlet_name)%>?<%=BatchLogic.ProtocolPageAction%>=&<%=BatchLogic.currentPageId%>=<%=currentPage+1%>&filter=<%=filter%>">
<%}%>
<img src="<%=webpath%>layout/right.gif" width="14" height="13" border="0" alt="Next Page"></a>&nbsp;&nbsp;&nbsp;



</table>



<table border="1" rules="all" width="100%">
<tr>
   <td class="TBDATA_XXS_L" >
                        <select name="filter" class="DROPDOWN" onChange='submit();'>
                        <option <%=filter.equals("ALL")?"selected":""%> value="ALL"><%=userAdminLocale.get("DISPLAY_ALL_RECORDS")%></option>
                        <option <%=filter.equals(Batch.CREATED)?"selected":""%> value="<%=Batch.CREATED%>"><%=userAdminLocale.get("DISPLAY_ONLY")%> <%=userAdminLocale.get(Batch.CREATED)%></option>
                        <option <%=filter.equals(Batch.FAILED)?"selected":""%> value="<%=Batch.FAILED%>"><%=userAdminLocale.get("DISPLAY_ONLY")%> <%=userAdminLocale.get(Batch.FAILED)%></option>
                        <option <%=filter.equals(Batch.UPDATED)?"selected":""%> value="<%=Batch.UPDATED%>"><%=userAdminLocale.get("DISPLAY_ONLY")%> <%=userAdminLocale.get(Batch.UPDATED)%></option>
                        <option <%=filter.equals(Batch.UPDATE_FAILED)?"selected":""%> value="<%=Batch.UPDATE_FAILED%>"><%=userAdminLocale.get("DISPLAY_ONLY")%> <%=userAdminLocale.get(Batch.UPDATE_FAILED.replace(' ', '_'))%></option>
                        <option <%=filter.equals(Batch.EXISTS)?"selected":""%> value="<%=Batch.EXISTS%>"><%=userAdminLocale.get("DISPLAY_ONLY")%> <%=userAdminLocale.get(Batch.EXISTS)%></option>
                                <option <%=filter.equals(Batch.NO_PERMISSION)?"selected":""%> value="<%=Batch.NO_PERMISSION%>"><%=userAdminLocale.get("DISPLAY_ONLY")%> <%=userAdminLocale.get(Batch.NO_PERMISSION.replace(' ', '_'))%></option>
                        </select>

   </td>
   <td class="TBDATA_XXS_L"  nowrap><center><b><%=userAdminLocale.get("FIRST_NAME")%></b></center></td>
   <td class="TBDATA_XXS_L"  nowrap><center><b><%=userAdminLocale.get("LAST_NAME")%></b></center></td>
   <td class="TBDATA_XXS_L"  nowrap><center><b><%=userAdminLocale.get("USER_LOGON_ID")%></b></center></td>
   <td class="TBDATA_XXS_L"  nowrap><b><%=userAdminLocale.get("WARNINGS")%></b></td>
   <td class="TBDATA_XXS_L"  nowrap><center><b><%=userAdminLocale.get("COMPANY")%></b></center></td>
 </tr>
<%
 int start = ((Integer)proxy.getSessionAttribute(BatchLogic.startindex)).intValue();
 int end = ((Integer)proxy.getSessionAttribute(BatchLogic.endindex)).intValue();

 if (end > size)
  {
    end = size - 1;
    start = end - (usersOnPage + 1);
  }

 for (int i = 0; i<size; i++)
 {
 HashMap entry = (HashMap)(protocol.elementAt(i));
 String status = (String) entry.get(Batch.Status);
 text= (String)(entry.get(Batch.Status));
 if (text!=null && (text.indexOf("ERROR")!=-1 || text.indexOf("FAILED")!=-1 || text.indexOf("PERMISSION")!=-1))  numberOfErrors++;

  if (!(i>=start  && i<=end)) continue;
  if (status!=null && !status.equals(filter) && !filter.equals("ALL")) continue;
%>

  <tr>
    <td class="TBDATA_XXS_L"  nowrap>
    <%
        boolean errors = false;
        text= (String)(entry.get(Batch.Status));
        text = text.replace(' ', '_');
    if (text!=null && (text.indexOf("ERROR")!=-1 || text.indexOf("FAILED")!=-1 || text.indexOf("PERMISSION")!=-1))
        {
          errors = true;
    %>
        <font color="red"><%=userAdminLocale.get(text)%></font>
    <%
    }
    else if (text!=null && (text.indexOf("ERROR")!=-1 || text.indexOf("EXISTS")!=-1))
    {%>
        <font color="gray"><%=userAdminLocale.get(text)%></font>
    <%}
    else
    {%>
        <font color="green"><%=userAdminLocale.get(text)%></font>
    <%}%>
    </td>

   <td class="TBDATA_XXS_L"  nowrap><center>
   <%
     String fn = (String)entry.get(Batch.FirstName); if (fn==null) fn="-";
   %>
   <a href="<%=util.alias(proxy, BatchLogic.servlet_name)%>?<%=BatchLogic.BatchUserDetailPageAction%>=&<%=BatchLogic.userNumber%>=<%=i%>"><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(fn)%></a>
   </center></td>

   <td class="TBDATA_XXS_L"  nowrap><center>
   <%
     String ln = (String)entry.get(Batch.LastName); if (ln==null) ln="-";
   %>
   <a href="<%=util.alias(proxy, BatchLogic.servlet_name)%>?<%=BatchLogic.BatchUserDetailPageAction%>=&<%=BatchLogic.userNumber%>=<%=i%>"><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(ln)%></a>
   </center></td>

   <td class="TBDATA_XXS_L"  nowrap><center><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)entry.get(Batch.DisplayName))%></center></td>
   <td class="TBDATA_XXS_L"  nowrap>
   <%String warnings = (String)entry.get(Batch.Warnings);

     if (warnings==null || warnings.length()==0)
     {
       out.print("-");
     }
     else
     {
      StringTokenizer t = new StringTokenizer(warnings, "&");
      if (false /* !errors */)
      {
   %>

   <select class="DROPDOWN">
   <option selected><%=errors?"Errors/Warnings:":"Warnings:"%></option>
   <%while (t.hasMoreElements())
    {
    %>
   <option><%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)(t.nextElement()))%></option>
   <%}%>
   </select>
<%
 }
else
{
 while (t.hasMoreElements())
  {
   String wline = com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)(t.nextElement()));
   if (wline.indexOf("Error")!=-1)
   { %>
  <font color="red" ><%=wline+"<br>"%></font>
  <%}
  else
  {%>
  <font color="blue" ><%=wline+"<br>"%></font>
<%
   }
  }
 }
}
   %>
   </td>
<td class="TBDATA_XXS_L"  nowrap><center><%=((String)entry.get(Batch.CompanyName)==null)?"-":com.sap.security.core.util.taglib.EncodeHtmlTag.encode((String)entry.get(Batch.CompanyName))%></center></td>

  </tr>

<%
} /*for*/
%>
</table>
<% }  /*protocoll == null*/%>

<%=numberOfErrors%> <%=userAdminLocale.get("ERRORS")%>
<%session.setAttribute(BatchLogic.NUMBER_OF_UPLOAD_ERRORS, new Integer(numberOfErrors));%>

<p>
<table cellpadding="0" cellspacing="0" width="99%" border="0">
   <tr><td align="left" nowrap class="TBLO_XXS_L">
	<input type="submit" class="BTN_LB" name="<%=BatchLogic.backToUploadAction%>" value="<%=userAdminLocale.get("BACK_TO_UPLOAD")%>">
   </td></tr>
</table>

</FORM>

<%}%>

<%@ include file="/contextspecific_includes_bottom.txt" %>

