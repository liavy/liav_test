<% Boolean nottoskip = (Boolean) proxy.getSessionAttribute("notToSkip"); %>
			<table cellpadding="0" cellspacing="0" border="0">
			  <tr><td>
			    <% if (null != nottoskip) { %>
			    <a href="#sidemenu"><IMG height="50" tabindex="0" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="<%=userAdminLocale.get("SKIP_TO_SIDEMENU")%>"></a>
			    <% } else { %>
			    <IMG height="50" src="<%=webpath%>layout/sp.gif" width="1" border="0">
			    <% } %>
			  </td></tr>
			</table>

			<!-- Start Footer -->
			<table cellspacing="0" cellpadding="0" width="100%" border="0">
			<tr>
		      <td><hr size="1" color="#CBCCCC" width="50%" align="left"></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_L">&nbsp;&nbsp;<span tabindex="0"><%=userAdminLocale.get("COPY_RIGHT")%></span><br></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_L" valign="top"><img src="<%=webpath%>layout/sapLogo.gif" width="44" height="22" border="0" alt="<%=userAdminLocale.get("SAP")%>" title="<%=userAdminLocale.get("SAP")%>">&nbsp;&nbsp;</td>
			</tr>
			</table>
			<!-- End footer-->
		</td>
	<!-- End Transactional Content -->
	</tr>
</table>
<!-- End Content -->
  </td>
<!-- End Middle 780pxl Content space -->
<!-- Right space -->
