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
<%
	MessagesDemo.checkLogin(request, response);
%>
<html>
	<head>
		<title>
			Text Container Demonstration Application
		</title>
		<style>
		  td.dark
		  {
			  background-color: #AAAAAA;
		  }
		  td.light
		  {
			  background-color: #EEEEEE;
		  }
		</style>
	</head>
	<body>
		<form method="post" action="start.jsp">
			<table border="1">
				<tr>
					<td colspan="4"><h1>Standard texts and replacements for industries:</h1>
					</td>
				</tr>
				<tr>
					<td class="dark" width="111px"><b>ID</b></td>
					<td class="dark" width="150px"><b>Standard</b></td>
					<td class="dark" width="150px"><b>OIL</b></td>
					<td class="dark" width="150px"><b>OIL-GAS</b></td>
				</tr>
				<tr>
					<td>THEAD</td>
					<td>This is a header text.</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="light">TEXT1</td>
					<td class="light">Product</td>
					<td class="light">Article</td>
					<td class="light">Good</td>
				</tr>
				<tr>
					<td>TEXT2</td>
					<td>Simple text.</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="light">TEXT3</td>
					<td class="light">Customer</td>
					<td class="light">&nbsp;</td>
					<td class="light">Recipient</td>
				</tr>
				<tr>
					<td>TEXT4</td>
					<td>Standard</td>
					<td>Industry</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="light">TFOOT</td>
					<td class="light">Finally a footer text for this product.</td>
					<td class="light">Finally a footer text for this article.</td>
					<td class="light">Finally a footer text for this good.</td>
				</tr>
			</table>
<%
	String ctx = request.getParameter("context");
	int context = 1;
	if (ctx != null)
	{
		context = new Integer(ctx).intValue();
	}
	switch (context)
	{
		case 2: MessagesDemo.setIndustry("OIL");
			break;
		case 3: MessagesDemo.setIndustry("OIL_GAS");
			break;
		default:MessagesDemo.setIndustry("");
			context = 1;
	}
%>
			<br/><br/>
			<h3>
				System context:
				<select id="context" name="context" onchange="this.form.submit();">
<%
					if (ctx == null)
					{
%>
	%				<option value="0"></option>
<%
					}
%>
					<option value="1">Standard</option>
					<option value="2">OIL</option>
					<option value="3">OIL_GAS</option>
				</select>
			</h3>
			<br/>
<%
			if (ctx != null)
			{
%>
				<script language="javascript">
					document.getElementById("context").options[<%= context-1 %>].selected="selected";
				</script>

				<table border="1">
					<tr>
						<td colspan="4"><h1>Texts retrieved from ResourceBundle:</h1>
						</td>
					</tr>
					<tr>
						<td class="dark" width="111px">ID</td>
						<td class="dark" width="333px">Texts from context: <%= (MessagesDemo.getIndustry() == "" ? "Standard": MessagesDemo.getIndustry()) %></td>
					</tr>
					<tr>
						<td width="111px">THEAD</td>
						<td width="333px"><%= MessagesDemo.getString("THEAD") %></td>
					</tr>
					<tr>
						<td class="light">TEXT1</td>
						<td class="light"><%= MessagesDemo.getString("TEXT1") %></td>
					</tr>
					<tr>
						<td>TEXT2</td>
						<td><%= MessagesDemo.getString("TEXT2") %></td>
					</tr>
					<tr>
						<td class="light">TEXT3</td>
						<td class="light"><%= MessagesDemo.getString("TEXT3") %></td>
					</tr>
					<tr>
						<td>TEXT4</td>
						<td><%= MessagesDemo.getString("TEXT4") %></td>
					</tr>
					<tr>
						<td class="light">TFOOT</td>
						<td class="light"><%= MessagesDemo.getString("TFOOT") %></td>
					</tr>
				</table>
<%
			}
%>
		</form>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
