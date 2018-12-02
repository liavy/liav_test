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
			<h2>Standard texts and industry-specific replacements</h2>
		</div>
		<table width="90%" border="0" cellspacing="0" cellpadding="5">
			<tr>
				<td valign="top"  width="10%">
					<table width="100%" border="0" cellpadding=5 cellspacing="0">
						<tr>
							<td>
<!--
								<input class="b_index" type="button" value="Start page" onclick="javascript:self.location.href='index.jsp';">
-->
							</td>
						</tr>
					</table>
				</td>
				<td valign="top" align="left">
					<table width="90%" cellspacing="0" cellpadding="5" class="data">
						<tr>
							<th class="data">Standard</th>
							<th class="data">Industry OIL</th>
							<th class="data">Industry OIL_GAS</th>
						</tr>
						<tr>
							<td class="data2">This is a header text.</td>
							<td class="data2">&nbsp;</td>
							<td class="data2">&nbsp;</td>
						</tr>
						<tr>
							<td class="data2">Product</td>
							<td class="data2">Article</td>
							<td class="data2">Good</td>
						</tr>
						<tr>
							<td class="data2">Simple text.</td>
							<td class="data2">&nbsp;</td>
							<td class="data2">&nbsp;</td>
						</tr>
						<tr>
							<td class="data2">Customer</td>
							<td class="data2">&nbsp;</td>
							<td class="data2">Recipient</td>
						</tr>
						<tr>
							<td class="data2">Standard</td>
							<td class="data2">Industry</td>
							<td class="data2">&nbsp;</td>
						</tr>
						<tr>
							<td class="data2">Finally a footer text for this product.</td>
							<td class="data2">Finally a footer text for this article.</td>
							<td class="data2">Finally a footer text for this good.</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
