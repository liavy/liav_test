<%-- to include this page requires:
<%@page import="com.sap.security.core.admin.*"%>
<%com.sap.security.core.admin.UserAdminLocaleBean userAdminLocale = (com.sap.security.core.admin.UserAdminLocaleBean) proxy.getSessionAttribute("userAdminLocale");%>
<jsp:useBean id="list"
             class="com.sap.security.core.admin.ListBean"
             scope="request"/>
  // navigation
   int colspan;
   int totalItems = list.getTotalItems();
   int currentPage = list.getCurrentPage();
   int totalPages = list.getTotalPages();
   Integer[] itemPerPageOptions = list.getItemPerPageOptions();
   int currentItemPerPage = list.getCurrentItemPerPage();
   String urlAndAction, setListPage, pageKey, pageName
--%>

<script language="JavaScript">
	function paging(pageObj) {
        var frm = document.forms[0];
        var pageInteger = pageObj.selectedIndex;
        var pageString = pageObj.options[pageInteger].text;
        var itemObj, itemInteger, itemString;

        if( -1 != navigator.userAgent.indexOf("MSIE") ) {
            itemObj = document.all["itemPerPage"];
        } else {
            itemObj = frm.elements["itemPerPage"];
        }
        itemInteger = itemObj[0].selectedIndex;
        itemString = itemObj[0].options[itemInteger].text;

        inputTag1 = document.createElement("input");
        var action = "<%=UserAdminLogic.performRolesGroupsNavigateAction%>";
        inputTag1.setAttribute("name", action);
        inputTag1.setAttribute("type", "hidden");
        inputTag1.setAttribute("value", "");
        frm.appendChild( inputTag1 );
        inputTag2 = document.createElement("input");
        inputTag2.setAttribute("name", "requestPage");
        inputTag2.setAttribute("type", "hidden");
        inputTag2.setAttribute("value", pageString);
        frm.appendChild( inputTag2 );

        inputTag3 = document.createElement("input");
        inputTag3.setAttribute("name", "currentItemPerPage");
        inputTag3.setAttribute("type", "hidden");
        inputTag3.setAttribute("value", itemString);
        inputTag4 = document.createElement("input");

        inputTag4.setAttribute("name", "<%=pageKey%>");
        inputTag4.setAttribute("type", "hidden");
        inputTag4.setAttribute("value", "<%=pageName%>");
        frm.appendChild(inputTag4);
        frm.appendChild( inputTag3 );
//        frm.action = "<%=userAdminAlias%>";
        frm.submit();
	}

    function changeNumber(itemObj) {
        var frm = document.forms[0];
        var itemInteger, itemString;

        var itemInteger = itemObj.selectedIndex;
        var itemString = itemObj.options[itemInteger].text;

        inputTag1 = document.createElement("input");
        var action = "<%=UserAdminLogic.performRolesGroupsNavigateAction%>";
        inputTag1.setAttribute("name", action);
        inputTag1.setAttribute("type", "hidden");
        inputTag1.setAttribute("value", "");
        frm.appendChild( inputTag1 );

        inputTag2 = document.createElement("input");
        inputTag2.setAttribute("name", "currentItemPerPage");
        inputTag2.setAttribute("type", "hidden");
        inputTag2.setAttribute("value", itemString);
        frm.appendChild( inputTag2 );

        inputTag3 = document.createElement("input");
        inputTag3.setAttribute("name", "<%=pageKey%>");
        inputTag3.setAttribute("type", "hidden");
        inputTag3.setAttribute("value", "<%=pageName%>");
        frm.appendChild(inputTag3);

//        frm.action = "<%=userAdminAlias%>";
        frm.submit();
    }

	function jump(value) {
        var frm = document.forms[0];
        var itemObj, itemInteger, itemString;

        if( -1 != navigator.userAgent.indexOf("MSIE") ) {
            itemObj = document.all["itemPerPage"];
        } else {
            itemObj = frm.elements["itemPerPage"];
        }
        itemInteger = itemObj[0].selectedIndex;
        itemString = itemObj[0].options[itemInteger].text;

        inputTag1 = document.createElement("input");
        var action = "<%=UserAdminLogic.performRolesGroupsNavigateAction%>";
        inputTag1.setAttribute("name", action);
        inputTag1.setAttribute("type", "hidden");
        inputTag1.setAttribute("value", "");
        frm.appendChild(inputTag1);

        inputTag2 = document.createElement("input");
        inputTag2.setAttribute("name", "requestPage");
        inputTag2.setAttribute("type", "hidden");
        inputTag2.setAttribute("value", value);
        frm.appendChild(inputTag2);

        inputTag3 = document.createElement("input");
        inputTag3.setAttribute("name", "currentItemPerPage");
        inputTag3.setAttribute("type", "hidden");
        inputTag3.setAttribute("value", itemString);
        frm.appendChild(inputTag3);

        inputTag4 = document.createElement("input");
        inputTag4.setAttribute("name", "<%=pageKey%>");
        inputTag4.setAttribute("type", "hidden");
        inputTag4.setAttribute("value", "<%=pageName%>");
        frm.appendChild(inputTag4);

//        frm.action = "<%=userAdminAlias%>";
        frm.submit();
	}
</script>

        <input type="hidden" name="<%=pageKey%>" value="<%=pageName%>">

        <tr>
				<td class="NAV_PGNB" colspan="<%=colspan%>" width="100%">
				<!-- Start Page Navigation -->
				<IMG height=1 src="<%=webpath%>layout/sp.gif" width=20>
				<% if ( currentPage < 2 ) { %>
				<img src="<%=webpath%>layout/leftnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>">&nbsp;
				<% } else {
					int previous = currentPage -1; %>
				<img src="<%=webpath%>layout/left.gif"
                     CLASS="IMG_BTN"
                     width="14"
                     height="13"
                     border="0"
                     alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>"
                     title="<%=userAdminLocale.get("PREVIOUS_PAGE")%>"
                     onClick="jump('<%=previous%>')">&nbsp;
				<% } %>
				<%=userAdminLocale.get("TOTAL")%>:<%=totalItems%>&nbsp;<%=userAdminLocale.get("DISPLAY")%>&nbsp;
				<select id="itemPerPage" name="itemPerPage" size="1" class="DROPDOWN" onChange="javascript:changeNumber(this)">
					<% for (int i=0; i<itemPerPageOptions.length; i++ ) {
                        int item = itemPerPageOptions[i].intValue();
                        boolean selected = false;
                        if (item==currentItemPerPage) {
                            selected = true;
                        }%>
						<option value="<%=item%>" <%=selected?"SELECTED":""%>><%=itemPerPageOptions[i].intValue()%></option>
					<% } %>
				</select>&nbsp;<%=userAdminLocale.get("PER_PAGE")%>.&nbsp;<%=userAdminLocale.get("THIS_IS")%>&nbsp;
				<select id="reqPage" name="reqPage" size="1" class="DROPDOWN" onChange="javascript:paging(this)">
					<% for(int i=1;i<=totalPages;i++) {
                         boolean selected = false;
                         if ( i == currentPage ) {
                            selected = true;
                         } %>
						<option value="<%=i%>" <%=selected?"SELECTED":""%>><%=i%></option>
					<% } %>
				</select>&nbsp;<%=userAdminLocale.get("OF")%>&nbsp;<%=totalPages%> <%=userAdminLocale.get("PAGES")%>&nbsp;
				<%  if ( currentPage < totalPages ) {
					    int next = currentPage + 1; %>
                <img src="<%=webpath%>layout/right.gif"
                     CLASS="IMG_BTN"
                     width="14"
                     height="13"
                     border="0"
                     alt="<%=userAdminLocale.get("NEXT_PAGE")%>"
                     title="<%=userAdminLocale.get("NEXT_PAGE")%>"
                     onClick="javascript:jump('<%=next%>')">&nbsp;&nbsp;&nbsp;
                <% } else {%>
				<img src="<%=webpath%>layout/rightnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("NEXT_PAGE")%>">&nbsp;&nbsp;&nbsp;
				<% } %>
				<!-- End Page Navigation -->
				</td>
		</tr>
