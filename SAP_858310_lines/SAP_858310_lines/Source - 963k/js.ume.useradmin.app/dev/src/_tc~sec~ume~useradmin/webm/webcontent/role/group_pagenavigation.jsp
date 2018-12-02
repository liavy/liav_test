
<script language="JavaScript">
function paging(pageObj,command) 
{
    var pageInteger, pageString;
    var object = document.forms[0];
    pageInteger = pageObj.selectedIndex;
    pageString = pageObj.options[pageInteger].text;
    var itemObj = document.getElementById("itemPerPage");
    var itemInteger = itemObj.selectedIndex;
    var itemString = itemObj.options[itemInteger].text;	
      
    object.cmd.value = command;

    
    inputTag2 = document.createElement("input");
    inputTag2.setAttribute("name", "requestPage");
    inputTag2.setAttribute("type", "hidden");
    inputTag2.setAttribute("value", pageString);
    object.appendChild( inputTag2 );
   
    inputTag3 = document.createElement("input");
    inputTag3.setAttribute("name", "currentItemPerPage");
    inputTag3.setAttribute("type", "hidden");
    inputTag3.setAttribute("value", itemString);
    object.appendChild( inputTag3 );     
    object.action = "<%=groupAdminAlias%>";
    object.submit();
}

function changeNumber(itemObj,command) 
{
    var itemInteger, itemString;
    var object = document.forms[0];
	
    var itemInteger = itemObj.selectedIndex;
    var itemString = itemObj.options[itemInteger].text;    
   
    object.cmd.value = command;

    inputTag2 = document.createElement("input");
    inputTag2.setAttribute("name", "currentItemPerPage");
    inputTag2.setAttribute("type", "hidden");
    inputTag2.setAttribute("value", itemString);
    object.appendChild( inputTag2 );     
    object.action = "<%=groupAdminAlias%>";
    object.submit();
}
	
function jump(value,command) 
{
    var object = document.forms[0];
    var itemObj = document.getElementById("itemPerPage");
    var itemInteger = itemObj.selectedIndex;
    var itemString = itemObj.options[itemInteger].text;	
	    
    object.cmd.value = command;

    inputTag2 = document.createElement("input");
    inputTag2.setAttribute("name", "requestPage");
    inputTag2.setAttribute("type", "hidden");
    inputTag2.setAttribute("value", value);
    object.appendChild( inputTag2 );
    inputTag3 = document.createElement("input");
    inputTag3.setAttribute("name", "currentItemPerPage");
    inputTag3.setAttribute("type", "hidden");
    inputTag3.setAttribute("value", itemString);
    object.appendChild( inputTag3 );    
    object.action = "<%=groupAdminAlias%>";
    object.submit();
}	
</script>

        <tr>
				<td class="NAV_PGNB" colspan="<%=colspan%>" width="100%">
				<!-- Start Page Navigation -->
				<IMG height=1 src="<%=webpath%>layout/sp.gif" width=20>
				<% if ( currentPage < 2 ) { %>
				<img src="<%=webpath%>layout/leftnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>" title="<%=userAdminLocale.get("PREVIOUS_PAGE")%>">&nbsp;
				<% } else {
					int previous = currentPage -1; %>
				<a href="#" onClick="jump('<%=previous%>','group-assignment-page-navigation')"><img src="<%=webpath%>layout/left.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>" title="<%=userAdminLocale.get("PREVIOUS_PAGE")%>"></a>&nbsp;
				<% } %>
<%=userAdminLocale.get("TOTAL")%>:<%=totalItems%>&nbsp;<%=userAdminLocale.get("ITEMS")%> &nbsp;<%=userAdminLocale.get("DISPLAY")%>&nbsp;
				<select name="itemPerPage" size="1" class="DROPDOWN" onChange="javascript:changeNumber(this,'group-assignment-page-navigation')">
					<% for (int i=0; i<itemPerPageOptions.length; i++ ) {
                        int item = itemPerPageOptions[i].intValue();
                        boolean selected = false;
                        if (item==currentItemPerPage) {
                            selected = true;
                        }%>
						<option value="<%=item%>" <%=selected?"SELECTED":""%>><%=itemPerPageOptions[i].intValue()%></option>
					<% } %>
				</select>&nbsp;<%=userAdminLocale.get("PER_PAGE")%>.&nbsp;<%=userAdminLocale.get("THIS_IS")%>&nbsp;
				<select name="reqPage" size="1" class="DROPDOWN" onChange="javascript:paging(this,'group-assignment-page-navigation')">
					<% for(int i=1;i<=totalPages;i++) {
				                         boolean selected = false;
				                         if ( i == currentPage ) {
				                            selected = true;
				                         } %>
						<option value="<%=i%>" <%=selected?"SELECTED":""%>><%=i%></option>
					<% } %>
				</select>&nbsp;<%=userAdminLocale.get("OF")%>&nbsp;<%=totalPages%><%=userAdminLocale.get("PAGES")%>&nbsp;
				<%  if ( currentPage < totalPages ) {
					    int next = currentPage + 1; %>
                <a href="#" onClick="javascript:jump('<%=next%>','group-assignment-page-navigation')"><img src="<%=webpath%>layout/right.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("NEXT_PAGE")%>" title="<%=userAdminLocale.get("NEXT_PAGE")%>"></a>&nbsp;&nbsp;&nbsp;
                <% } else {%>
				<img src="<%=webpath%>layout/rightnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("NEXT_PAGE")%>" title="<%=userAdminLocale.get("NEXT_PAGE")%>">&nbsp;&nbsp;&nbsp;
				<% } %>
				<!-- End Page Navigation -->
				</td>
	</tr>