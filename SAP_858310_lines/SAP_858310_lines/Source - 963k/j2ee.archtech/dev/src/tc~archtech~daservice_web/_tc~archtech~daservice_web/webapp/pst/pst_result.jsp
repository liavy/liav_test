<%@ page errorPage="pst_error.jsp" %>

<%
String archivepath = request.getParameter("archivepath");
String responsecode = request.getParameter("responsecode");
%>

Archive Path: <%=archivepath%>
<br>
<br> 

<%
if (responsecode.startsWith("200"))
{
  responsecode = responsecode.substring(6);
}	
%>

Status:       <%=responsecode%>

<br>
<br>
    
<form action="pst_execute.jsp" method="post">
  <input type="submit" value="Refresh">
  <input type="hidden" name="archivepath" value="<%=archivepath%>">  
</form>

<a href="/DataArchivingService/index.jsp">Home</a>
    
&nbsp;   
       
<a href="pst_enter.jsp">Display (Un)Pack Progress</a>

&nbsp;   
