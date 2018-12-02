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
			if (txv.equals("L"))
			{
				AdministrationTool.deleteLanguageAttributeValues();
				msg = "Processed languages sucessfully deleted!";
			}
			else if (txv.equals("D"))
			{
				AdministrationTool.deleteContextAttributeValues();
				msg = "Context attribute values sucessfully deleted!";
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
		<form method="post" action="administration_cleanup.jsp">
			<div align="left">
				<h2>Administration Cleanup</h2>
			</div>
			<table border="0" cellspacing="0" cellpadding="5">
				<tr>
					<td class="data"><input class="b_index1" type="button" name="Delete" value="Delete processed languages from database" onclick="document.getElementById('TXVRemote').value='L';this.form.submit();"/></td>
				</tr>
				<tr>
					<td class="data"><input class="b_index1" type="button" name="Delete" value="Delete all values from database" onclick="document.getElementById('TXVRemote').value='D';this.form.submit();"/></td>
				</tr>
			</table>
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
