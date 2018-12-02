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
	<body>
<%
	if (err.length() > 0)
	{
%>
		<div style="color=red"><%= err %></div>
<%
	}
	else
	{
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data" width="100%">
			<tr>
				<th class="data">System Context</th>
			</tr>
			<tr>
				<td class="data2"><a href="system_context_settings.jsp" target="content">Settings</a></td>
			</tr>
		</table>
		<br>
		<table border="0" cellspacing="0" cellpadding="5" class="data" width="100%">
			<tr>
				<th class="data">Administration</th>
			</tr>
			<tr>
				<td class="data2"><a href="administration_setup.jsp" target="content">Setup</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="administration_cleanup.jsp" target="content">Cleanup</a></td>
			</tr>
		</table>
		<br>
		<table border="0" cellspacing="0" cellpadding="5" class="data" width="100%">
			<tr>
				<th class="data">Display Values</th>
			</tr>
			<tr>
				<td class="data2"><a href="display_industries.jsp" target="content">Industries</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="display_regions.jsp" target="content">Regions</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="display_extensions.jsp" target="content">Extensions</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="display_locales.jsp" target="content">Locales</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="display_locale_chains.jsp" target="content">Locale Chains</a></td>
			</tr>
		</table>
<%
/*
		<br>
		<table border="0" cellspacing="0" cellpadding="5" class="data" width="100%">
			<tr>
				<th class="data">Text Container</th>
			</tr>
			<tr>
				<td class="data2"><a href="text_container_contexts.jsp" target="content">Contexts</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="text_container_components.jsp" target="content">Components</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="text_container_bundles.jsp" target="content">Bundles</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="text_container_deployed_texts.jsp" target="content">Deployed Texts</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="text_container_text_load.jsp" target="content">Text Load</a></td>
			</tr>
			<tr>
				<td class="data2"><a href="text_container_dirty_data.jsp" target="content">Dirty Data</a></td>
			</tr>
		</table>
*/
%>
<%
	}
%>
	</body>
</html>
