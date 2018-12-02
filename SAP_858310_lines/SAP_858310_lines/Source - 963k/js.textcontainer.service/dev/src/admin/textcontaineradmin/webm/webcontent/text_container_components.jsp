<%@ page language="java" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.AdministrationTool" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.sap.engine.services.textcontainer.admin.dbaccess.ComponentData" %>
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
			<h2>Text Container Components</h2>
		</div>
<%
	if (err.length() == 0)
	{
		try
		{
			int lines = 0;

			HashMap<String, ComponentData> components = AdministrationTool.getComponents();
%>
		<table border="0" cellspacing="0" cellpadding="5" class="data">
			<tr>
				<th class="data">Component</th>
				<th class="data">&nbsp;</th>
				<th class="data">ComponentHash</th>
			</tr>
<%
			Vector<String> vector = new Vector<String>(components.keySet());
			Collections.sort(vector);
			Iterator<String> iterComponents = vector.iterator();
			while (iterComponents.hasNext())
			{
				ComponentData component = components.get(iterComponents.next());
				String componentHashString = AdministrationTool.ByteArrayToString(component.getComponentHash());
%>
			<tr>
				<td class="data2"><a href='javascript:text_container_deployed_texts("<%=componentHashString %>", "", "");'><%=component.getComponent() %></a>&nbsp;</td>
				<td class="data2">&nbsp;</td>
				<td class="data2"><%=componentHashString %>&nbsp;</td>
			</tr>
<%
				lines++;
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
