<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.deploytest.TextContainerDeployTest" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.services.dc.api.model.Sdu" %>
<%@ page import = "com.sap.engine.services.dc.api.model.Sca" %>
<%@ page import = "com.sap.engine.services.dc.api.model.Sda" %>
<%@ page import = "com.sap.engine.services.dc.api.model.SdaId" %>
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

	String sca_name = "";
	String sca_vendor = "";

	try
	{
		TextContainerDeployTest.checkLogin(request, response);

		sca_name = request.getParameter("sca_name");
		sca_vendor = request.getParameter("sca_vendor");
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
	Set sdaIdSet = TextContainerDeployTest.getSdaList(sca_name, sca_vendor);
	SdaId[] sdaIdArr = (SdaId[]) sdaIdSet.toArray(new SdaId[0]);
%>
<%
	HashMap<String, SdaId> sdaIds = new HashMap<String, SdaId>();
	for (int i = 0; i < sdaIdArr.length; i++)
	{
		SdaId sdaId = sdaIdArr[i];
		sdaIds.put(sdaId.toString(), sdaId);
	}
	Vector<String> vector = new Vector<String>(sdaIds.keySet());
	Collections.sort(vector);
%>
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
		SdaId sdaId = sdaIds.get(iterSdus.next());
		Sda sda = TextContainerDeployTest.getSda(sdaId.getName(), sdaId.getVendor());

%>
			<tr>
				<td class="data2"><%=sda.getName() %>(<%=sda.getVendor() %>)&nbsp;</td>
				<td class="data2"><%=sda.getVersion() %>&nbsp;</td>
				<td class="data2"><%=sda.getSoftwareType().toString() %>&nbsp;</td>
				<td class="data2"><%=sda.getLocation() %>&nbsp;</td>
			</tr>
<%
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
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
