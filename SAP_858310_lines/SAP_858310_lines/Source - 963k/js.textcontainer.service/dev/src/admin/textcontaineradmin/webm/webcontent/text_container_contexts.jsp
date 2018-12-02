<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.dbaccess.ContextIterator" %>
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
		AdministrationTool.checkAdministrationPermission(request, response);
	}
	catch (Exception e)
	{
		err = getDeepMessage(e);
	}
%>
<% try { %>
<html>
	<head>
		<title>
			Text Container Administration Application
		</title>
	    <link href="style.css" rel="stylesheet" type="text/css">
	</head>
	<body>
		<div align="left">
			<h2>Text Container Contexts</h2>
		</div>
<%
	if (err.length() == 0)
	{
		try
		{
			int lines = 0;

		ContextIterator iterContext = AdministrationTool.getContexts();
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">ContextId</th>
				<th class="data">Locale</th>
				<th class="data">Industry</th>
				<th class="data">Region</th>
				<th class="data">Extension</th>
			</tr>
<%
			if (iterContext != null)
			{
				while (iterContext.next())
				{
%>
			<tr>
				<td class="data2"><%=iterContext.contextId() %>&nbsp;</td>
				<td class="data2"><%=iterContext.locale() %>&nbsp;</td>
				<td class="data2"><%=iterContext.industry() %>&nbsp;</td>
				<td class="data2"><%=iterContext.region() %>&nbsp;</td>
				<td class="data2"><%=iterContext.extension() %>&nbsp;</td>
			</tr>
<%
					lines++;
				}
			}
%>
		</table>
		<br>
		Rows: <%=lines %>
<%
		}
		catch (Exception e)
		{
			err = getDeepMessage(e);
		}
		finally
		{
			AdministrationTool.closeConnection();
		}
	}
%>
<%
	if (err.length() > 0)
	{
%>
			<p>
				<hr>
				<div style="color=red"><%= err %></div>
			</p>
<%
	}
%>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
