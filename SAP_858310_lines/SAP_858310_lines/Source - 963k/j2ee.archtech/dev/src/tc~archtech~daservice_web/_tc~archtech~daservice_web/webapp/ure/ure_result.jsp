<%@ page errorPage="ure_error.jsp" %>

<%
String archivepath = request.getParameter("archivepath");
String responsecode = request.getParameter("responsecode");
if (responsecode.startsWith("200"))
{
%>

Unpacking started

<%
}
else
{
%>

Unpacking aborted with <%=responsecode%>

<%
}
%>

<br>
<br>

<form action="../pst/pst_enter.jsp" method="post">
  <input type="submit" value="Display Unpack Progress">
  <input type="hidden" name="archivepath" value="<%=archivepath%>">  
</form>
       
<a href="/DataArchivingService/index.jsp">Home</a>
    
&nbsp;   
       
<a href="ure_enter.jsp">Unpack Resources</a>

&nbsp;   
       