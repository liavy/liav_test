<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.demo.MessagesDemo" %>
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
<% try { %>
<html>
	<head>
		<title>
			Text Container Demonstration Application
		</title>
	    <link href="style.css" rel="stylesheet" type="text/css">
	</head>
	<body>
		<div align="center">
			<h2>Texts retrieved from ResourceBundle</h2>
		</div>
		<table width="90%" border="0" cellspacing="0" cellpadding="5">
			<tr>
				<td valign="top"  width="10%">
					<table width="100%" border="0" cellpadding=5 cellspacing="0">
						<tr>
							<td>
<!--
								<input class="b_index" type="button" value="All texts" onclick="javascript:self.location.href='texts.jsp';">
-->
							</td>
						</tr>
					</table>
				</td>
				<td valign="top" align="left">
					<table width="90%" cellspacing="0" cellpadding="5" class="data">
						<tr>
							<th class="data">Texts</th>
						</tr>
						<tr>
							<td class="data2"><%= MessagesDemo.getString("THEAD") %></td>
						</tr>
						<tr>
							<td class="data2"><%= MessagesDemo.getString("TEXT1") %></td>
						</tr>
						<tr>
							<td class="data2"><%= MessagesDemo.getString("TEXT2") %></td>
						</tr>
						<tr>
							<td class="data2"><%= MessagesDemo.getString("TEXT3") %></td>
						</tr>
						<tr>
							<td class="data2"><%= MessagesDemo.getString("TEXT4") %></td>
						</tr>
						<tr>
							<td class="data2"><%= MessagesDemo.getString("TFOOT") %></td>
						</tr>
					</table>
					<p>
						System context industry: <%= (MessagesDemo.getIndustry() == "" ? "Standard": MessagesDemo.getIndustry()) %>
					</p>
					<p>
						<input class="b_index" type="button" value="Refresh" onclick="javascript:self.location.href='index.jsp';">
					</p>
				</td>
			</tr>
		</table>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
