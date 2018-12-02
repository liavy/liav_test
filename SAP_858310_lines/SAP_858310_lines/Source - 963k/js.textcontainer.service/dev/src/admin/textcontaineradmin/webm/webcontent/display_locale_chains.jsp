<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.interfaces.textcontainer.TextContainerLocale" %>
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
			<h2>Display Locale Chains</h2>
		</div>
<%
	try
	{
		HashMap<String, TextContainerLocale[]> localeChains = AdministrationTool.getLocaleChains();
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Start Locale</th>
				<th class="data">Sequence Number</th>
				<th class="data">Locale</th>
			</tr>
<%
		Vector<String> vector = new Vector<String>(localeChains.keySet());
		Collections.sort(vector);
		Iterator<String> iterLocaleChains = vector.iterator();
		while (iterLocaleChains.hasNext())
		{
			TextContainerLocale[] localeChain = localeChains.get(iterLocaleChains.next());
			for (int i = 0; i < localeChain.length; i++)
			{
%>
			<tr>
				<td class="data2"><%=localeChain[i].getStartLocale() %>&nbsp;</td>
				<td class="data2"><%=localeChain[i].getSequenceNumber() %>&nbsp;</td>
				<td class="data2"><%=localeChain[i].getLocale() %>&nbsp;</td>
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
