<%-- start page attribute setting--%>
<%@ page import="com.sap.security.core.admin.*" %>
<%@ page import="com.sap.security.api.IUser" %>
<%@ page import="com.sap.security.core.admin.role.RoleAdminServlet" %>
<%@ page import="com.sap.security.core.admin.group.GroupAdminLogic" %>
<%@ page import="com.sap.security.api.ISearchResult" %>
<%-- end page attribute setting--%>

<% IUser performer= proxy.getActiveUser();
   String slcAction = new String();  
   if ( null != proxy.getSessionAttribute(UserAdminLogic.currentAction) ) {
      slcAction = (String)proxy.getSessionAttribute(UserAdminLogic.currentAction);
   }
   String toAction = new String();
   boolean isSlc = false;
   if ( null != proxy.getSessionAttribute("notToSkip") ) {
       proxy.setSessionAttribute("notToSkip", Boolean.TRUE);
   }   
%>

<% if (!inPortal) { %>
<script language="JavaScript" src="<%=webpath%>js/include.js"></script>
<% } %>

<% String sideMenu = userAdminLocale.get("SIDEMENU");
   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
   entryExit.append(userAdminMessages.print(new Message("START_OF", sideMenu))); %>
<table cellpadding="0" cellspacing="0" border="0" width="780" height="100%">
	<tr class="SIDE_N_BG">
	<!-- start module sidebar-->
		<td width="180" rowspan="2" valign="top">
		<table id="sidebar" cellpadding="0" cellspacing="0" border="0" width="184">
			<tr class="launchpadHeadBackground">
				<td colspan="2">
				<a name="sidemenu"></a><img src="<%=webpath%>layout/sp16pxheight.gif" 
				                            width="1" 
				                            height="16" 
				                            border="0" 
				                            alt="<%=entryExit.toString()%>"
				                            tabindex="0" 
				                            onkeypress="if(event.keyCode=='115') {document.getElementById('<%=sideMenu%>').focus();}">   
				</td>
			</tr>
			<tr>
				<td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" border="0" alt=""></td>
				<td rowspan="13"><img src="<%=webpath%>layout/sp.gif" width="4" height="1" border="0" alt=""></td>
			</tr>
			<tr>

			</tr>
			<tr>
				<td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" border="0" alt=""></td>
			</tr>
			<tr>
			<!-- Start User Management label -->
				<td>
				<table cellpadding="1" cellspacing="0" border="0" class="SIDE_CNT_BDR_BG" width="180">

				<tr>
				<td>
				<table cellpadding="2" cellspacing="0" border="0" width="100%">
				    <% if ( UserAdminHelper.isCompanyConceptEnabled() 
				           && UserAdminHelper.hasAccess(performer, UserAdminHelper.APPROVE_USERS) ) {
				       toAction = UserAdminLogic.getUnapprovedUsersAction;
                       isSlc = false;
                       if ( slcAction.equals(toAction) ) {isSlc = true;} %>
					<tr>
					<td width="16"><img src="<%=webpath%>layout/sp.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("BULLET")%>"></td>
					<td><a href="<%=userAdminAlias%>?<%=UserAdminLogic.getUnapprovedUsersAction%>=" class=<%=isSlc?"SIDE_XS_CNT_SEL":"SIDE_XS_CNT_N"%>><%=userAdminLocale.get("UNAPPROVED_USER")%></a></td>
				    </tr>
				    <% } %>

                    <% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.SEARCH_USERS)
                           || UserAdminHelper.hasAccess(performer, UserAdminHelper.CREATE_USERS) ) { %>
 		    		<% boolean toSearch = false;
                       String _search = UserAdminLogic.searchUsersAction;
                       String _last = UserAdminLogic.viewLastSearchResultAction;
                       String _deactivated = UserAdminLogic.getDeactivatedUsersAction;
                       String _create = UserAdminLogic.createNewUserAction;
                       if (   slcAction.equals(_search)
                           || slcAction.equals(_last)
                           || slcAction.equals(_deactivated) 
                           || slcAction.equals(_create)) {
                           toSearch = true; }     %>
					<tr>
						<td width="11" valign="top"><img src="<%=webpath%>layout/texpander_open.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("RETRACT")%>"></td>
						<td class="SIDE_XS_CNT_B"><%=userAdminLocale.get("USERS")%><br>
						<table cellpadding="0" cellspacing="0" border="0" width="100%">
						
					      <% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.CREATE_USERS) ) {
		                           isSlc = false; if ( slcAction.equals(_create) ) {isSlc = true;} %>
						<tr>
								<td><a href="<%=userAdminAlias%>?<%=util.filteringSpecialChar(_create)%>=" class=<%=isSlc?"SIDE_XS_CNT_SUB_SEL":"SIDE_XS_CNT_SUB"%>><%=userAdminLocale.get("CREATE_NEW")%></a></td>
						</tr>
		                        	<% } %>

						<% isSlc = false; if ( slcAction.equals(_search) ) {isSlc = true;}%>
						<tr>
							<td><a href="<%=userAdminAlias%>?<%=util.filteringSpecialChar(_search)%>=" class=<%=isSlc?"SIDE_XS_CNT_SUB_SEL":"SIDE_XS_CNT_SUB"%>><%=userAdminLocale.get("SEARCH")%></a></td>
						</tr>
						<% isSlc = false; if ( slcAction.equals(_last) ) {isSlc = true;}%>
						<tr>
							<td><a href="<%=userAdminAlias%>?<%=util.filteringSpecialChar(_last)%>=" class=<%=isSlc?"SIDE_XS_CNT_SUB_SEL":"SIDE_XS_CNT_SUB"%>><%=userAdminLocale.get("LAST_SEARCH_RESULTS")%></a></td>
						</tr>
						<% isSlc = false; if ( slcAction.equals(_deactivated) ) {isSlc = true;}%>
						<tr>
							<td><a href="<%=userAdminAlias%>?<%=util.filteringSpecialChar(_deactivated)%>=" class=<%=isSlc?"SIDE_XS_CNT_SUB_SEL":"SIDE_XS_CNT_SUB"%>><%=userAdminLocale.get("INACTIVE_USERS")%></a></td>
						</tr>
						</table></td>
					</tr>
                    <% } %>


			<% if (!inPortal) { %>
					<% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.VIEW_ROLES) ) {
						String _role = RoleAdminServlet.ROLE_MANAGEMENT_MAIN;
			                  	isSlc = false;
			                 	if ( slcAction.equals(_role) ) {isSlc = true;} %>
								<tr>
									<td width="16">
									<% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) { %>
									<img src="<%=webpath%>layout/sp.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("BULLET")%>"></td>
									<td><a href="<%=RoleAdminServlet.alias%>" class=<%=isSlc?"SIDE_XS_CNT_SEL":"SIDE_XS_CNT_N"%>><%=userAdminLocale.get("ROLES")%></a>
									<% } else { %>
									<img src="layout/sp.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("BULLET")%>"></td>
									<td><a href="<%=RoleAdminServlet.alias%>?ID=<%=performer.isCompanyUser()?util.filteringSpecialChar(performer.getCompany()):""%>" class=<%=isSlc?"SIDE_XS_CNT_SEL":"SIDE_XS_CNT_N"%>><%=userAdminLocale.get("ROLES")%></a>
									<% } %>
									</td>
								</tr>
					<% } %>
			
					<!-- GROUP ADMIN -->
					<% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.VIEW_GROUPS) ) {
						String _group = GroupAdminLogic.GROUP_MANAGEMENT_MAIN;
			                  	isSlc = false;
			                 	if ( slcAction.equals(_group) ) {isSlc = true;} %>
								<tr>
									<td width="16">
									<% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) { %>
									<img src="<%=webpath%>layout/sp.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("BULLET")%>"></td>
									<td><a href="<%=groupAdminAlias%>" class=<%=isSlc?"SIDE_XS_CNT_SEL":"SIDE_XS_CNT_N"%>><%=userAdminLocale.get("GROUPS")%></a>
									<% } else { %>
									<img src="layout/sp.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("BULLET")%>"></td>
									<td><a href="<%=groupAdminAlias%>?ID=<%=performer.isCompanyUser()?util.filteringSpecialChar(performer.getCompany()):""%>" class=<%=isSlc?"SIDE_XS_CNT_SEL":"SIDE_XS_CNT_N"%>><%=userAdminLocale.get("GROUPS")%></a>
									<% } %>
									</td>
								</tr>
					<% } %>
			    <% } %>



			<% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.BATCH_ADMIN) ) {
			   boolean toCreate = false;
                       String _upload = BatchLogic.uploadAction;
                       String _download = BatchLogic.downloadAction;
                       if (slcAction.equals(_upload)
                           || slcAction.equals(_download) ) {
                           toCreate = true; }     %>
					<tr>
						<td width="11" valign="top"><img src="<%=webpath%>layout/texpander_open.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("RETRACT")%>"></td>
						<td class="SIDE_XS_CNT_B"><%=userAdminLocale.get("IMPORTEXPORT")%><br>
						<table cellpadding="0" cellspacing="0" border="0" width="100%">
						<% if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.CREATE_USERS) ) {
                           isSlc = false; if ( slcAction.equals(_upload) ) {isSlc = true;} %>
						<tr>
							<td><a href="<%=batchAlias%>?<%=util.filteringSpecialChar(_upload)%>=" class=<%=isSlc?"SIDE_XS_CNT_SUB_SEL":"SIDE_XS_CNT_SUB"%>><%=userAdminLocale.get("IMPORT")%></a></td>
						</tr>
                        <% } %>
						<% isSlc = false;
                           if ( slcAction.equals(_download) ) {isSlc = true;}  %>
						<tr>
							<td><a href="<%=batchAlias%>?<%=util.filteringSpecialChar(_download)%>=" class=<%=isSlc?"SIDE_XS_CNT_SUB_SEL":"SIDE_XS_CNT_SUB"%>><%=userAdminLocale.get("EXPORT")%></a></td>
						</tr>
						</table>
						</td>
					</tr>
			<% } %>



		<% if ( UserAdminCustomization.isCompanyFieldEnabled(proxy) ) {
                       String _listC = CompanyListLogic.listCompaniesAction;
                       if ( slcAction.equals(_listC) ) {isSlc = true;} %>
					<tr>
						<td width="16"><img src="<%=webpath%>layout/sp.gif" width="11" height="11" border="0" alt="<%=userAdminLocale.get("BULLET")%>"></a></td>
						<td><a href="<%=companyListAlias%>?<%=util.filteringSpecialChar(_listC)%>=" class=<%=isSlc?"SIDE_XS_CNT_SEL":"SIDE_XS_CNT_N"%>><%=userAdminLocale.get("COMPANY_LIST")%></a></td>
					</tr>
		<% } %>

					<tr>
					    <td class="SIDE_XS_CNT_N" colspan="2"><img src="<%=webpath%>layout/sp.gif" width="1" height="2" border="0" alt=""></td>
					</tr>
					</table>
				</td>
				</tr>
				</table>
				</td>
			<!-- End User Management label -->
			</tr>
			<tr>
			<td height="100%"><img src="<%=webpath%>layout/sp.gif" width="1" height="10" border="0" alt=""></td>
			</tr>
		</table>
		</td>
		<!-- End Module sideBar -->
		<!-- Start Retractable Tab Structure -->
		<td id="elOneParent" width="22" rowspan="2" valign="top">
			<table cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="launchpadHeadBackground">
                     <SCRIPT LANGUAGE="JavaScript" >
 						var sidebar= document.getElementById("sidebar");
 						var sidebar_cookie = new Cookie (document, "wef_cookie");
 						if ( !sidebar_cookie.load() || !sidebar_cookie.display) {
 						    <% boolean createBoolean = UserAdminHelper.hasAccess(performer, UserAdminHelper.CREATE_USERS); //ES
 						    if ( createBoolean ) { %>
 							sidebar_cookie.display = "";
 							<% } else { %>
 							sidebar_cookie.display = "none";
 							<% } %>
 						}
 						sidebar.style.display = sidebar_cookie.display;
 						var sidebar_store = new Image();
 						var primary_image_MouseOver = new Image();
 						primary_image_MouseOver.src = "<%=webpath%>layout/arrowon2RO.gif";
 						var substitute_image_MouseOver = new Image();
 						substitute_image_MouseOver.src = "<%=webpath%>layout/arrowoff2RO.gif";
 					</SCRIPT>
                     <img id="primary_image" src="<%=webpath%>layout/arrowon2.gif"
                     	style="cursor:hand" alt="<%=userAdminLocale.get("CLOSE_SIDEMENU")%>" title="<%=userAdminLocale.get("CLOSE_SIDEMENU")%>" style="cursor:hand"
 						onClick="swapObjects(primary_image,substitute_image,sidebar,sidebar_cookie);"
 						onMouseOver="onRetractorRollover(this,sidebar_store,'primary_image_MouseOver');"
 						onMouseOut="onRetractorRollover(this,sidebar_store,null);" >
                     <img id="substitute_image" src="<%=webpath%>layout/arrowoff2.gif"
                     	style="cursor:hand" alt="<%=userAdminLocale.get("OPEN_SIDEMENU")%>" title="<%=userAdminLocale.get("OPEN_SIDEMENU")%>" style="cursor:hand"
 						onClick="swapObjects(primary_image,substitute_image,sidebar,sidebar_cookie);"
 						onMouseOver="onRetractorRollover(this,sidebar_store,'substitute_image_MouseOver');"
 						onMouseOut="onRetractorRollover(this,sidebar_store,null);" >
                     <SCRIPT LANGUAGE="JavaScript" >
 						if (sidebar.style.display=="none") {
 							document.getElementById("primary_image").style.display="none";
 							document.getElementById("substitute_image").style.display=TABLECELLDISPLAY;
 						}
 						else {
 							document.getElementById("substitute_image").style.display="none";
 							document.getElementById("primary_image").style.display=TABLECELLDISPLAY;
 						}
 					   </SCRIPT>
					</td>
				</tr>
				<tr>
					<td height="100%"><img src="<%=webpath%>layout/sp.gif" width="22" height="100%" border="0" alt=""></td>
				</tr>
			</table>
		</td>
		<td width="10" rowspan="2" bgcolor="#ffffff"><img src="<%=webpath%>layout/sp.gif" width="10" height="1" alt="" border="0"></td>

		<!-- End Retractable Tab Structure -->
		<!-- Start Fuction Icons Shadow -->
		<td width="100%" valign="top" class="TB_CNT_BG">

			<table cellpadding="0" cellspacing="0" border="0" width="100%">
			  <tr>
				  <td background="<%=webpath%>layout/shadow.jpg" width="100%"><img src="<%=webpath%>layout/sp.gif" height="4" border="0" alt=""></td>
			  </tr>
			</table>
		</td>
	<!-- End Fuction Icons Shadow -->
	</tr>
	<tr>
	<!-- Start Transactional Content -->
		<td width="100%" valign="top">
			<table cellpadding="0" cellspacing="0" border="0">
			<tr><td>
			<% entryExit.delete(0, entryExit.length());
			   entryExit.append(userAdminMessages.print(new Message("END_OF", sideMenu))); %>
			<img src="<%=webpath%>layout/sp.gif" 
			     width="1" 
			     height="1" 
			     id="<%=sideMenu%>"
			     alt="<%=entryExit.toString()%>"
			     tabindex="0">
			</td></tr>
			</table>
			<!-- Start Section Header -->
