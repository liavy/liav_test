<%@ page isErrorPage="true" %>

<html>
  <head>
    <title>
      (Un)Pack Status - Error Page
    </title>
  </head>
  <body>
    <h3>
      (Un)Pack Status - Error Page
    </h3>
    Following exception occured: <%= exception.toString() %>
    <br>
    <br>
    <a href="/DataArchivingService/index.jsp">Home</a>
    &nbsp;   
    <a href="pst_enter.jsp">Back</a>   
  </body>
</html>










