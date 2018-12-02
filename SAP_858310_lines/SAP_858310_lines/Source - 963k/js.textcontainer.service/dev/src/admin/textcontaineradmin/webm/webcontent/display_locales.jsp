<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.interfaces.textcontainer.TextContainerLanguage" %>
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
			<h2>Display Locales</h2>
		</div>
<%
	try
	{
		HashMap<String, TextContainerLanguage> languages = AdministrationTool.getLocales();
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Locale</th>
				<th class="data">Secondary Locale</th>
			</tr>
<%
		Vector<String> vector = new Vector<String>(languages.keySet());
		Collections.sort(vector);
		Iterator<String> iterLanguages = vector.iterator();
		while (iterLanguages.hasNext())
		{
			TextContainerLanguage language = languages.get(iterLanguages.next());
%>
			<tr>
				<td class="data2"><%=language.getLocale() %>&nbsp;</td>
				<td class="data2"><%=language.getIsSecondaryLocale() %>&nbsp;</td>
			</tr>
<%
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
