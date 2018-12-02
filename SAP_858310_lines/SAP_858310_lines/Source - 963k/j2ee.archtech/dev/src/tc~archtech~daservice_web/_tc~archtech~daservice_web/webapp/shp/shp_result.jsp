<%@ page errorPage="shp_error.jsp" %>

<%
String responsecode = request.getParameter("responsecode");
if (responsecode.startsWith("200"))
{
%>

Function successfully finished

<%
}
else
{
%>

Function finished with <%=responsecode%>

<%
}
%>

<br>
<br>

<a href="/DataArchivingService/index.jsp">Home</a>
    
&nbsp;   
       
<a href="shp_enter.jsp">Synchronize Home Path</a>
