<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.net.URLEncoder" %>

<%
HttpServletRequest httpRequest = (HttpServletRequest) request;
StringBuffer userAdminServletURL = new StringBuffer();
userAdminServletURL.append(httpRequest.getContextPath());
userAdminServletURL.append("/userAdminServlet");

String nextRedirectURL = request.getParameter("redirectURL");
if(nextRedirectURL != null) {
    userAdminServletURL.append("?redirectURL=");
    userAdminServletURL.append(URLEncoder.encode(URLDecoder.decode(nextRedirectURL, "UTF-8"), "UTF-8"));
}
%>

<html>
<head>
<meta http-equiv="refresh" content="0;url=<%=userAdminServletURL.toString()%>">
</head>
<body>
</body>
</html>
