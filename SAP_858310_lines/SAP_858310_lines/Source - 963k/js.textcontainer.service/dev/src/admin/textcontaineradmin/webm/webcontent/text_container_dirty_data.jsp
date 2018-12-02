<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.dbaccess.DirtyData" %>
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
		AdministrationTool.checkAdministrationPermission(request, response);
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
			<h2>Text Container Dirty Data</h2>
		</div>
<%
	if (err.length() == 0)
	{
		try
		{
			int lines = 0;

			DirtyData[] dirtyDatas = AdministrationTool.getDirtyData();

			if (dirtyDatas != null)
			{
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Original Component</th>
				<th class="data">Bundle</th>
				<th class="data">&nbsp;</th>
				<th class="data">OriginalComponentHash</th>
				<th class="data">BundleHash</th>
			</tr>
<%
				for (int i = 0; i < dirtyDatas.length; i++)
				{
					DirtyData dirtyData = dirtyDatas[i];
					String originalComponentHashString = AdministrationTool.ByteArrayToString(dirtyData.getOriginalComponentHash());
					String bundleHashString = AdministrationTool.ByteArrayToString(dirtyData.getBundleHash());
					String originalComponentName = AdministrationTool.getComponentName(AdministrationTool.ByteArrayToString(dirtyData.getOriginalComponentHash()));
					String bundleName = AdministrationTool.getBundleName(AdministrationTool.ByteArrayToString(dirtyData.getOriginalComponentHash()), AdministrationTool.ByteArrayToString(dirtyData.getBundleHash()));
%>
			<tr>
				<td class="data2"><%=originalComponentName %>&nbsp;</td>
				<td class="data2"><%=bundleName %>&nbsp;</td>
				<td class="data2">&nbsp;</td>
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
	document.getElementById("original_component").value=original_component;
	document.getElementById("bundle").value=bundle;
	document.getElementById("deployed_texts_form").submit()
}
</script>
		<form method="post" action="text_container_deployed_texts.jsp" id="deployed_texts_form">
			<input type="hidden" name="original_component" id="original_component" value=""/>
			<input type="hidden" name="bundle" id="bundle" value=""/>
		</form>
	</body>
</html>
<% } catch (Exception e) { %>
     <%= getDeepMessage(e) %>
<% } %>
