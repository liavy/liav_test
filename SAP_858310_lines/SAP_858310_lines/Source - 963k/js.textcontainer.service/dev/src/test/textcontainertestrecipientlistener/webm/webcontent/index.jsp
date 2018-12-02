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

	try
	{
		String txv = request.getParameter("TXVRequest");
		String vcv = request.getParameter("ValueContextAttributeValues");
		String vsc = request.getParameter("ValueSystemContext");
		if (txv != null)
		{
			if (txv.equals("R"))
			{
				TextContainerTestRecipientListener.registerRecipientListeners();
				msg = "Recipient listeners sucessfully registered!";
			}
			else if (txv.equals("U"))
			{
				TextContainerTestRecipientListener.unregisterRecipientListeners();
				msg = "Recipient listeners sucessfully unregistered!";
			}
			else if (txv.equals("D"))
			{
				TextContainerTestRecipientListener.deleteEventLists();
				msg = "Event lists sucessfully deleted!";
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
			Text Container Test Recipient Listener Application
		</title>
	    <link href="style.css" rel="stylesheet" type="text/css">
	</head>
	<body>
		<form method="post" action="index.jsp">
			<table border="0" cellspacing="0" cellpadding="5">
<%
	if (TextContainerTestRecipientListener.registered)
	{
%>
				<tr>
					<td class="data"><input disabled class="b_index1" type="button" name="Register" value="Register recipient listeners for PCD and Web Dynpro" onclick="document.getElementById('TXVRequest').value='R';this.form.submit();"/></td>
				</tr>
				<tr>
					<td class="data"><input class="b_index1" type="button" name="Unregister" value="Unregister recipient listeners for PCD and Web Dynpro" onclick="document.getElementById('TXVRequest').value='U';this.form.submit();"/></td>
				</tr>
<%
	}
	else
	{
%>
				<tr>
					<td class="data"><input class="b_index1" type="button" name="Register" value="Register recipient listeners for PCD and Web Dynpro" onclick="document.getElementById('TXVRequest').value='R';this.form.submit();"/></td>
				</tr>
				<tr>
					<td class="data"><input disabled class="b_index1" type="button" name="Unregister" value="Unregister recipient listeners for PCD and Web Dynpro" onclick="document.getElementById('TXVRequest').value='U';this.form.submit();"/></td>
				</tr>
<%
	}
%>
				<tr>
					<td class="data">&nbsp;</td>
				</tr>
				<tr>
					<td class="data"><input class="b_index1" type="button" name="Delete" value="Delete event lists" onclick="document.getElementById('TXVRequest').value='D';this.form.submit();"/></td>
				</tr>
				<tr>
					<td class="data">&nbsp;</td>
				</tr>
				<tr>
					<td class="data"><input class="b_index1" type="button" name="Update" value="Update event list" onclick="javascript:self.location.href='index.jsp';"/></td>
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
			<br>
			<div align="left">
				<h2>Events:</h2>
			</div>
			<table border="0" cellspacing="0" cellpadding="5" class="data">
				<tr>
					<th class="data" colspan="3">PCD</th>
				</tr>
				<tr>
					<td>Total: <%= TextContainerTestRecipientListener.getNumberOfPCDEvents() %></td>
<%
	if (TextContainerTestRecipientListener.getNumberOfPCDEvents() > 0)
	{
%>
					<td><a href="display_events.jsp?recipient=PCD" target="_blank">Display events</a></td>
<%
	}
%>
				</tr>
			</table>
			<br>
			<table border="0" cellspacing="0" cellpadding="5" class="data">
				<tr>
					<th class="data" colspan="3">Web Dynpro</th>
				</tr>
				<tr>
					<td>Total: <%= TextContainerTestRecipientListener.getNumberOfWebDynproEvents() %></td>
<%
	if (TextContainerTestRecipientListener.getNumberOfWebDynproEvents() > 0)
	{
%>
					<td><a href="display_events.jsp?recipient=WD" target="_blank">Display events</a></td>
<%
	}
%>
				</tr>
			</table>
			<input type="hidden" name="TXVRequest" id="TXVRequest" value=""/>
		</form>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
