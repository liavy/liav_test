<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.deploytest.TextContainerDeployTest" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.services.dc.api.model.Sdu" %>
<%@ page import = "com.sap.engine.services.dc.api.model.Sca" %>
<%@ page import = "com.sap.engine.services.dc.api.model.Sda" %>
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
		TextContainerDeployTest.checkLogin(request, response);
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
			Text Container Deploy Test Application
		</title>
	    <link href="style.css" rel="stylesheet" type="text/css">
	</head>
	<body>
<%
	Sdu[] sduArr = TextContainerDeployTest.getSduList();
%>
		<p>SCAs:</p>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Name</th>
				<th class="data">Version</th>
			</tr>
<%
	HashMap<String, Sdu> sdus = new HashMap<String, Sdu>();
	for (int i = 0; i < sduArr.length; i++)
	{
		Sdu sdu = sduArr[i];
		sdus.put(sdu.getId().toString(), sdu);
	}
	Vector<String> vector = new Vector<String>(sdus.keySet());
	Collections.sort(vector);

	Iterator<String> iterScas = vector.iterator();
	while (iterScas.hasNext())
	{
		Sdu sdu = sdus.get(iterScas.next());

		if (sdu instanceof Sca)
		{
%>
			<tr>
				<td class="data2"><a href='javascript:text_container_sca("<%=sdu.getName() %>", "<%=sdu.getVendor() %>");'><%=sdu.getName() %>(<%=sdu.getVendor() %>)</a>&nbsp;</td>
				<td class="data2"><%=sdu.getVersion() %>&nbsp;</td>
			</tr>
<%
		}
	}
%>
		</table>
		<br>
		<p>SDAs:</p>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Name</th>
				<th class="data">Version</th>
				<th class="data">Software Type</th>
				<th class="data">Location</th>
			</tr>
<%
	Iterator<String> iterSdus = vector.iterator();
	while (iterSdus.hasNext())
	{
		Sdu sdu = sdus.get(iterSdus.next());

		if ((sdu instanceof Sda) && (((Sda) sdu).getScaId() == null))
		{
			Sda sda = (Sda) sdu;
%>
			<tr>
				<td class="data2"><%=sda.getName() %>(<%=sda.getVendor() %>)&nbsp;</td>
				<td class="data2"><%=sda.getVersion() %>&nbsp;</td>
				<td class="data2"><%=sda.getSoftwareType().toString() %>&nbsp;</td>
				<td class="data2"><%=sda.getLocation() %>&nbsp;</td>
			</tr>
<%
		}
	}
%>
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
<script language="JavaScript">
function text_container_sca(sca_name, sca_vendor)
{
	document.getElementById("sca_name").value=sca_name;
	document.getElementById("sca_vendor").value=sca_vendor;
	document.getElementById("sca_form").submit()
}
</script>
		<form method="post" action="text_container_sca.jsp" id="sca_form">
			<input type="hidden" name="sca_name" id="sca_name" value=""/>
			<input type="hidden" name="sca_vendor" id="sca_vendor" value=""/>
		</form>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
