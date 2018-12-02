<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.interfaces.textcontainer.TextContainerIndustry" %>
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
			<h2>Display Industries</h2>
		</div>
<%
	try
	{
		HashMap<String, TextContainerIndustry> industries = AdministrationTool.getIndustries();
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Industry</th>
				<th class="data">Father</th>
				<th class="data">Terminology Domain</th>
				<th class="data">Collection Key</th>
			</tr>
<%
		Vector<String> vector = new Vector<String>(industries.keySet());
		Collections.sort(vector);
		Iterator<String> iterIndustries = vector.iterator();
		while (iterIndustries.hasNext())
		{
			TextContainerIndustry industry = industries.get(iterIndustries.next());
			if (industry.getIndustry().length() > 0)
			{
%>
			<tr>
				<td class="data2"><%=industry.getIndustry() %>&nbsp;</td>
				<td class="data2"><%=industry.getFather() %>&nbsp;</td>
				<td class="data2"><%=industry.getTermDomain() %>&nbsp;</td>
				<td class="data2"><%=industry.getCollKey() %>&nbsp;</td>
			</tr>
<%
			}
		}
%>
		</table>
<%
	}
	catch (Exception e)
	{
		err = getDeepMessage(e);
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
		</form>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
