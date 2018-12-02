<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.interfaces.textcontainer.TextContainerRegion" %>
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
			<h2>Display Regions</h2>
		</div>
<%
	try
	{
		HashMap<String, TextContainerRegion> regions = AdministrationTool.getRegions();
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Region</th>
				<th class="data">Father</th>
				<th class="data">Terminology Domain</th>
				<th class="data">Collection Key</th>
			</tr>
<%
		Vector<String> vector = new Vector<String>(regions.keySet());
		Collections.sort(vector);
		Iterator<String> iterRegions = vector.iterator();
		while (iterRegions.hasNext())
		{
			TextContainerRegion region = regions.get(iterRegions.next());
			if (region.getRegion().length() > 0)
			{
%>
			<tr>
				<td class="data2"><%=region.getRegion() %>&nbsp;</td>
				<td class="data2"><%=region.getFather() %>&nbsp;</td>
				<td class="data2"><%=region.getTermDomain() %>&nbsp;</td>
				<td class="data2"><%=region.getCollKey() %>&nbsp;</td>
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
