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
	String msg = "";

	String txv = request.getParameter("TXVRemote");

	try
	{
		AdministrationTool.checkLogin(request, response);

		if (txv != null)
		{
			if (txv.equals("C"))
			{
				AdministrationTool.setSystemContext(request.getParameter("ValueIndustry"), request.getParameter("ValueRegion"), request.getParameter("ValueExtension"));
				msg = "System context sucessfully changed!";
			}
		}
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
		<form method="post" action="system_context_settings.jsp">
			<div align="left">
				<h2>System Context Settings</h2>
			</div>
			<table border="0" cellspacing="0" cellpadding="5" class="data">
				<tr>
					<th class="data">System Context</th>
					<th class="data">Current</th>
					<th class="data">New</th>
				</tr>
				<tr>
					<td class="data2">Industry&nbsp;&nbsp;</td>
					<td class="data2"><%=AdministrationTool.getSystemContextIndustry() %>&nbsp;</td>
					<td class="data2"><input type="text" name="ValueIndustry" size="25" value="<%=AdministrationTool.getSystemContextIndustry() %>"/></td>
				</tr>
				<tr>
					<td class="data2">Region&nbsp;&nbsp;</td>
					<td class="data2"><%=AdministrationTool.getSystemContextRegion() %>&nbsp;</td>
					<td class="data2"><input type="text" name="ValueRegion" size="25" value="<%=AdministrationTool.getSystemContextRegion() %>"/></td>
				</tr>
				<tr>
					<td class="data2">Extension&nbsp;&nbsp;</td>
					<td class="data2"><%=AdministrationTool.getSystemContextExtension() %>&nbsp;</td>
					<td class="data2"><input type="text" name="ValueExtension" size="25" value="<%=AdministrationTool.getSystemContextExtension() %>"/></td>
				</tr>
				<tr>
					<td class="data2">&nbsp;</td>
					<td class="data2"><input class="b_index" type="button" name="SystemContextUpdate" value="Refresh" onclick="javascript:self.location.href='system_context_settings.jsp';"/></td>
					<td class="data2"><input class="b_index" type="button" name="SystemContextChange" value="Change" onclick="document.getElementById('TXVRemote').value='C';this.form.submit();"/></td>
				</tr>
			</table>
			<p>
				Depending on the Text Container text load setting and<br/>the amount of texts in the Text Container changing the system<br/>context can take some time and lead to a timeout in this application!
			</p>
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
<%
	if (msg.length() > 0)
	{
%>
			<p>
				<hr>
				<div style="color=green"><%= msg %></div>
			</p>
<%
	}
%>
			<input type="hidden" name="TXVRemote" id="TXVRemote" value=""/>
		</form>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
