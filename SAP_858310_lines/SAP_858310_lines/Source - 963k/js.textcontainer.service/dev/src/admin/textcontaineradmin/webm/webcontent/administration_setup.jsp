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

		String vcv = request.getParameter("ValueContextAttributeValues");
		String vlv = request.getParameter("ValueLanguageValues");
		String vlc = request.getParameter("ValueLocaleChains");
		String vsc = request.getParameter("ValueSystemContext");
		if (txv != null)
		{
			if (txv.equals("R"))
			{
				AdministrationTool.retrieve(request.getParameter("TXVDestination"), vcv != null, vlv != null, vlc != null, vsc != null);
				msg = "Data retrieval successfully done!";
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
		<form method="post" action="administration_setup.jsp">
			<div align="left">
				<h2>Administration Setup</h2>
			</div>
			<table border="0" cellspacing="0" cellpadding="5">
				<tr>
					<td class="data">Destination (Type = RFC):</td>
					<td class="data"><input type="input" name="TXVDestination" size="30" value="TextContainerAdmin"><br></td>
				</tr>
				<tr>
					<td class="data">Data Retrieval:</td>
					<td class="data"><input type="checkbox" name="ValueContextAttributeValues" value="ValueContextAttributeValues" checked="checked"/> Context Attribute Values</td>
				</tr>
				<tr>
					<td class="data">&nbsp;</td>
					<td class="data"><input type="checkbox" name="ValueLanguageValues" value="ValueLanguageValues" checked="checked"/> Language Values</td>
				</tr>
				<tr>
					<td class="data">&nbsp;</td>
					<td class="data"><input type="checkbox" name="ValueLocaleChains" value="ValueLocaleChains" checked="checked"/> Locale Chains</td>
				</tr>
				<tr>
					<td class="data">&nbsp;</td>
					<td class="data"><input type="checkbox" name="ValueSystemContext" value="ValueSystemContext" checked="checked"/> System Context</td>
				</tr>
				<tr>
					<td class="data">&nbsp;</td>
					<td class="data"><input class="b_index1" type="button" name="ContextAttributeValues" value="Retrieve via RFC and store in database" onclick="document.getElementById('TXVRemote').value='R';this.form.submit();"/></td>
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
