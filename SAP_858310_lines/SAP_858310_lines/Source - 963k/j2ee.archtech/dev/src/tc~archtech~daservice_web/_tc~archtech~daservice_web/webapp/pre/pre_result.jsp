<%@ page errorPage="pre_error.jsp" %>

<%
String archivepath = request.getParameter("archivepath");
String responsecode = request.getParameter("responsecode");
if (responsecode.startsWith("200"))
{
%>

Packing started

<%
}
else
{
%>

Packing aborted with <%=responsecode%>

<%
}
%>

<br>
<br>

<form action="../pst/pst_enter.jsp" method="post">
  <input type="submit" value="Display Pack Progress">
  <input type="hidden" name="archivepath" value="<%=archivepath%>">  
</form>
       
<a href="/DataArchivingService/index.jsp">Home</a>
    
&nbsp;   
       
<a href="pre_enter.jsp">Pack Resources</a>

&nbsp;   
            
       
       
