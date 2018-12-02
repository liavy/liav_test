<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.dbaccess.ContainerData" %>
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

	String deployed_component = null;
	String original_component = null;
	String bundle = null;

	String deployed_component_name = "";
	String original_component_name = "";
	String bundle_name = "";

	try
	{
		AdministrationTool.checkLogin(request, response);
		AdministrationTool.checkAdministrationPermission(request, response);

		deployed_component = request.getParameter("deployed_component");
		original_component = request.getParameter("original_component");
		bundle = request.getParameter("bundle");
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
			<h2>Text Container Deployed Texts</h2>
		</div>
<%
	if (err.length() == 0)
	{
		try
		{
			int lines = 0;

			ContainerData[] texts = AdministrationTool.getDeployedTexts(deployed_component, original_component, bundle);

			if ((deployed_component != null) && (deployed_component.length() > 0))
			{
				deployed_component_name = AdministrationTool.getComponentName(deployed_component);
%>
		Deployed Component: <%=deployed_component_name %><br><br>
<%
			}

			if ((original_component != null) && (original_component.length() > 0))
			{
				original_component_name = AdministrationTool.getComponentName(original_component);
%>
		Original Component: <%=original_component_name %><br><br>
<%
			}

			if ((bundle != null) && (bundle.length() > 0))
			{
				AdministrationTool.getBundleName(original_component, bundle);
%>
		Bundle: <%=bundle_name %><br><br>
<%
			}

			if (texts != null)
			{
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Deployed Component</th>
				<th class="data">Sequence Number</th>
				<th class="data">Original Component</th>
				<th class="data">Bundle</th>
				<th class="data">Context Id</th>
				<th class="data">Text Key</th>
				<th class="data">Text</th>
				<th class="data">&nbsp;</th>
				<th class="data">DeployedComponentHash</th>
				<th class="data">OriginalComponentHash</th>
				<th class="data">BundleHash</th>
			</tr>
<%
				for (int i = 0; i < texts.length; i++)
				{
					ContainerData text = texts[i];
					String deployedComponentHashString = AdministrationTool.ByteArrayToString(text.getDeployComponentHash());
					String originalComponentHashString = AdministrationTool.ByteArrayToString(text.getOriginalComponentHash());
					String bundleHashString = AdministrationTool.ByteArrayToString(text.getBundleHash());
					String deployedComponentName = deployed_component_name;
					if ((deployedComponentName.length() == 0))
					{
						deployedComponentName = AdministrationTool.getComponentName(AdministrationTool.ByteArrayToString(text.getDeployComponentHash()));
					}
					String originalComponentName = original_component_name;
					if ((originalComponentName.length() == 0))
					{
						originalComponentName = AdministrationTool.getComponentName(AdministrationTool.ByteArrayToString(text.getOriginalComponentHash()));
					}
					String bundleName = bundle_name;
					if ((bundleName.length() == 0))
					{
						bundleName = AdministrationTool.getBundleName(AdministrationTool.ByteArrayToString(text.getOriginalComponentHash()), AdministrationTool.ByteArrayToString(text.getBundleHash()));
					}
%>
			<tr>
				<td class="data2"><%=deployedComponentName %></a>&nbsp;</td>
				<td class="data2"><%=text.getSequenceNumber() %></a>&nbsp;</td>
				<td class="data2"><%=originalComponentName %>&nbsp;</td>
				<td class="data2"><%=bundleName %>&nbsp;</td>
				<td class="data2"><%=text.getContextId() %>&nbsp;</td>
				<td class="data2"><%=text.getTextKey() %>&nbsp;</td>
				<td class="data2"><%=text.getText() %>&nbsp;</td>
				<td class="data2">&nbsp;</td>
				<td class="data2"><%=deployedComponentHashString %>&nbsp;</td>
				<td class="data2"><%=originalComponentHashString %>&nbsp;</td>
				<td class="data2"><%=bundleHashString %>&nbsp;</td>
			</tr>
<%
					lines++;
				}
%>
		</table>
		<br>
<%
			}
%>
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
function text_container_deployed_texts(deployed_component, original_component, bundle)
{
	document.getElementById("deployed_component").value=deployed_component;
	document.getElementById("original_component").value=original_component;
	document.getElementById("bundle").value=bundle;
	document.getElementById("deployed_texts_form").submit()
}
</script>
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
