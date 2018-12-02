<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.test.recipientlistener.TextContainerTestRecipientListener" %>
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

	String recipient = request.getParameter("recipient");
	String page_str = request.getParameter("page");
	int page_int = 0;
	if ((page_str != null) && (page_str.length() > 0))
		page_int = Integer.parseInt(page_str);
%>
<% try { %>
<html>
	<head>
		<title>
			Text Container Test Recipient Listener Application
		</title>
	    <link href="style.css" rel="stylesheet" type="text/css">
	</head>
	<body>
		<form method="post" action="display_events.jsp" id="display_events">
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
			<div align="left">
<%
	if ("PCD".equals(recipient))
	{
%>
				<h2>PCD Events:</h2>
<%
	}
	if ("WD".equals(recipient))
	{
%>
				<h2>Web Dynpro Events:</h2>
<%
	}
%>
			</div>
<%
	if (page_int > 0)
	{
%>
		<a href='javascript:previous_page();'>Previous page</a>
		<br/>
<%
	}

	if ((("PCD".equals(recipient)) && (page_int * 100 + 100 < TextContainerTestRecipientListener.getNumberOfPCDEvents())) ||
		(("WD".equals(recipient)) && (page_int * 100 + 100 < TextContainerTestRecipientListener.getNumberOfWebDynproEvents())))
	{
%>
		<a href='javascript:next_page();'>Next page</a>
		<br/>
<%
	}

	if ("PCD".equals(recipient))
	{
%>
			<table border="0" cellspacing="0" cellpadding="5" class="data">
				<%= TextContainerTestRecipientListener.getPCDEvents(page_int) %>
			</table>
<%
	}
	if ("WD".equals(recipient))
	{
%>
			<table border="0" cellspacing="0" cellpadding="5" class="data">
				<%= TextContainerTestRecipientListener.getWebDynproEvents(page_int) %>
			</table>
<%
	}
%>
			<input type="hidden" name="recipient" id="recipient" value="<%= recipient %>"/>
			<input type="hidden" name="page" id="page" value="<%= Integer.toString(page_int) %>"/>
		</form>
<script language="JavaScript">
function previous_page()
{
	document.getElementById("page").value=<%= Integer.toString(page_int-1) %>;
	document.getElementById("display_events").submit()
}
function next_page()
{
	document.getElementById("page").value=<%= Integer.toString(page_int+1) %>;
	document.getElementById("display_events").submit()
}
</script>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
