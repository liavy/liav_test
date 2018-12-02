<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%!
	public String getDeepMessage(Exception e)
	{
		String msg = "Exception:";
		Throwable t = e;
		while (t != null)
		{
			msg += "<br>" + t.toString();
			t = t.getCause();
		}
		return msg;
	}
%>
<%
	String err = "";

	try
	{
		AdministrationTool.checkLogin(request, response);
	}
	catch (Exception e)
	{
		err = getDeepMessage(e);
	}
%>
<html>
	<head>
		<title>
			Text Container Administration Application
		</title>
		<link href="style.css" rel="stylesheet" type="text/css">
	</head>
<%
	if (err.length() > 0)
	{
%>
	<body>
		<div style="color=red"><%= err %></div>
	</body>
<%
	}
	else
	{
%>
	<frameset cols="20%,80%">
		<frame src="menu.jsp" name="menu" frameborder="0">
		<frame src="system_context_settings.jsp" name="content" frameborder="0">
	</frameset>
<%
	}
%>
</html>
