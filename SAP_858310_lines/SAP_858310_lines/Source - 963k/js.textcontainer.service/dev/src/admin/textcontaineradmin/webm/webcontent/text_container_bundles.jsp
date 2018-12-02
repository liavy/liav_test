<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.dbaccess.BundleData" %>
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

	String component = null;
	String recipient = null;

	try
	{
		AdministrationTool.checkLogin(request, response);
		AdministrationTool.checkAdministrationPermission(request, response);

		component = request.getParameter("component");
		recipient = request.getParameter("recipient");
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
			<h2>Text Container Bundles</h2>
		</div>
<%
	if (err.length() == 0)
	{
		try
		{
			int lines = 0;

			HashMap<String, HashMap<String, BundleData>> bundles = AdministrationTool.getBundles(component, recipient);

			if ((component != null) && (component.length() > 0))
			{
%>
		Component: <%=bundles.keySet().iterator().next() %><br><br>
<%
			}

			if ((recipient != null) && (recipient.length() > 0))
			{
%>
		Recipient: <%=recipient %><br><br>
<%
			}
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Component</th>
				<th class="data">Bundle</th>
				<th class="data">Recipient</th>
				<th class="data">&nbsp;</th>
				<th class="data">ComponentHash</th>
				<th class="data">BundleHash</th>
			</tr>
<%
			Vector<String> vector = new Vector<String>(bundles.keySet());
			Collections.sort(vector);
			Iterator<String> iterComponentBundles = vector.iterator();
			while (iterComponentBundles.hasNext())
			{
				String componentName = iterComponentBundles.next();
				HashMap<String, BundleData> componentBundles = bundles.get(componentName);
				Vector<String> vector2 = new Vector<String>(componentBundles.keySet());
				Collections.sort(vector2);
				Iterator<String> iterBundles = vector2.iterator();
				while (iterBundles.hasNext())
				{
					BundleData bundle = componentBundles.get(iterBundles.next());
					String componentHashString = AdministrationTool.ByteArrayToString(bundle.getComponentHash());
					String bundleHashString = AdministrationTool.ByteArrayToString(bundle.getBundleHash());
%>
			<tr>
				<td class="data2"><a href='javascript:text_container_deployed_texts("", "<%=componentHashString %>", "");'><%=componentName %></a>&nbsp;</td>
				<td class="data2"><a href='javascript:text_container_deployed_texts("", "<%=componentHashString %>", "<%=bundleHashString %>");'><%=bundle.getBundle() %></a>&nbsp;</td>
				<td class="data2"><a href='javascript:text_container_bundles("", "<%=bundle.getRecipient() %>");'><%=bundle.getRecipient() %></a>&nbsp;</td>
				<td class="data2">&nbsp;</td>
				<td class="data2"><%=componentHashString %>&nbsp;</td>
				<td class="data2"><%=bundleHashString %>&nbsp;</td>
			</tr>
<%
					lines++;
				}
			}
%>
		</table>
		<br>
		Rows: <%=lines %>
<%
		}
		catch (Exception e)
		{
			err = getDeepMessage(e);
		}
		finally
		{
			AdministrationTool.closeConnection();
		}
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
<script language="JavaScript">
function text_container_bundles(component, recipient)
{
	document.getElementById("component").value=component;
	document.getElementById("recipient").value=recipient;
	document.getElementById("bundle_form").submit()
}
function text_container_deployed_texts(deployed_component, original_component, bundle)
{
	document.getElementById("deployed_component").value=deployed_component;
	document.getElementById("original_component").value=original_component;
	document.getElementById("bundle").value=bundle;
	document.getElementById("deployed_texts_form").submit()
}
</script>
		<form method="post" action="text_container_bundles.jsp" id="bundle_form">
			<input type="hidden" name="component" id="component" value=""/>
			<input type="hidden" name="recipient" id="recipient" value=""/>
		</form>
		<form method="post" action="text_container_deployed_texts.jsp" id="deployed_texts_form">
			<input type="hidden" name="deployed_component" id="deployed_component" value=""/>
			<input type="hidden" name="original_component" id="original_component" value=""/>
			<input type="hidden" name="bundle" id="bundle" value=""/>
		</form>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
