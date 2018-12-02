
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
            itemObj = document.all["currentItemPerPage"];
        } else {
            itemObj = frm.elements["currentItemPerPage"];
        }
        itemInteger = itemObj[0].selectedIndex;
        itemString = itemObj[0].options[itemInteger].text;

        inputTag1 = document.createElement("input");
        inputTag1.setAttribute("name", "<%=SelfRegLogic.performSearchResultNavigateAction%>");
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
        frm.action = "<%=selfRegAlias%>";
        frm.submit();
	}

    function changeNumber(itemObj) {
        var frm = document.forms[0];
        var itemInteger, itemString;

        var itemInteger = itemObj.selectedIndex;
        var itemString = itemObj.options[itemInteger].text;

        inputTag1 = document.createElement("input");
        inputTag1.setAttribute("name", "<%=SelfRegLogic.performSearchResultNavigateAction%>");
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
        frm.action = "<%=selfRegAlias%>";
        frm.submit();
    }
	function jump(value) {
        var frm = document.forms[0];
        var itemObj, itemInteger, itemString;

        if( -1 != navigator.userAgent.indexOf("MSIE") ) {
            itemObj = document.all["currentItemPerPage"];
        } else {
            itemObj = frm.elements["currentItemPerPage"];
        }
        itemInteger = itemObj[0].selectedIndex;
        itemString = itemObj[0].options[itemInteger].text;

        inputTag1 = document.createElement("input");
        inputTag1.setAttribute("name", "<%=SelfRegLogic.performSearchResultNavigateAction%>");
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

        frm.action = "<%=selfRegAlias%>";
        frm.submit();
	}</script>

<input type="hidden" name="<%=SelfRegLogic.performSearchResultNavigateAction%>" value="">
<input type="hidden" name="<%=pageKey%>" value="<%=pageName%>">

<tr>
	<td class="NAV_PGNB" colspan="<%=colspan%>" width="100%">
	<% if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
	   entryExit.append(userAdminMessages.print(new Message("START_OF", navigateBar))); %>	
	<img src="<%=webpath%>layout/sp.gif" 
	     width="1" 
	     height="1" 
	     alt="<%=entryExit.toString()%>" 
	     tabindex="0" 
	     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=navigateBar%>').focus();}">	
	<!-- Start Page Navigation -->
	<IMG height=1 src="<%=webpath%>layout/sp.gif" width=20>
	
	<%-- start RTL icon switch --%>
	<% if ( util.isRTL(proxy.getLocale()) ) { %>
	  <% if ( currentPage < totalPages ) { %>
		    <input type="image"
		           src="<%=webpath%>layout/right.gif"
		           tabindex="0" 
		           width="14"
		           height="13"
		           border="0"
		           alt="<%=userAdminLocale.get("NEXT_PAGE")%>"
		           onClick="javascript:jump('<%=currentPage+1%>');">&nbsp;
      <% } else { %>
	      <img src="<%=webpath%>layout/rightnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("NEXT_PAGE")%>">&nbsp;&nbsp;&nbsp;
	  <% } %>	
	<% } else { %>
	  <% if ( currentPage < 2 ) { %>
	      <img src="<%=webpath%>layout/leftnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>">&nbsp;
	  <% } else { %>
	      <input type="image"
				 src="<%=webpath%>layout/left.gif"
				 tabindex="0" 
				 width="14"
				 height="13"
				 border="0"
				 alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>"
				 onClick="javascript:jump('<%=currentPage-1%>')">&nbsp;
	  <% } %>
	<% } %>
	<%-- end of RTL icon switch --%>
	
	<span tabindex="0"><%=userAdminLocale.get("TOTAL")%>:<%=totalItems%>&nbsp;<%=userAdminLocale.get("ITEMS")%> &nbsp;<%=userAdminLocale.get("DISPLAY")%></span>&nbsp;
	<select name="currentItemPerPage" tabindex="0" tabindex="0" size="1" class="DROPDOWN" onChange="javascript:changeNumber(this)">
		<% for (int i=0; i<itemPerPageOptions.length; i++ ) {
            int item = itemPerPageOptions[i].intValue();
            boolean selected = false;
            if (item==currentItemPerPage) {
                selected = true;
            }%>
			<option value="<%=item%>" <%=selected?"SELECTED":""%>><%=itemPerPageOptions[i].intValue()%></option>
		<% } %>
	</select>&nbsp;<span tabindex="0"><%=userAdminLocale.get("PER_PAGE")%>.&nbsp;<%=userAdminLocale.get("THIS_IS")%></span>&nbsp;
	<select name="requestPage" size="1" class="DROPDOWN" onChange="javascript:paging(this)">
		<% for(int i=1;i<=totalPages;i++) {
             boolean selected = false;
             if ( i == currentPage ) {
                selected = true;
             } %>
			<option value="<%=i%>" <%=selected?"SELECTED":""%>><%=i%></option>
		<% } %>
	</select>&nbsp;<span tabindex="0"><%=userAdminLocale.get("OF")%>&nbsp;<%=totalPages%> <%=userAdminLocale.get("PAGES")%></span>&nbsp;

	<%-- start RTL icon switch --%>
	<% if ( util.isRTL(proxy.getLocale()) ) { %>
	  <% if ( currentPage < 2 ) { %>
	      <img src="<%=webpath%>layout/leftnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>">&nbsp;
	  <% } else { %>
	      <input type="image"
				 src="<%=webpath%>layout/left.gif"
				 tabindex="0" 
				 width="14"
				 height="13"
				 border="0"
				 alt="<%=userAdminLocale.get("PREVIOUS_PAGE")%>"
				 onClick="javascript:jump('<%=currentPage-1%>')">&nbsp;&nbsp;&nbsp;
	  <% } %>	
	<% } else { %>
	  <% if ( currentPage < totalPages ) { %>
		    <input type="image"
		           src="<%=webpath%>layout/right.gif"
		           tabindex="0" 
		           width="14"
		           height="13"
		           border="0"
		           alt="<%=userAdminLocale.get("NEXT_PAGE")%>"
		           onClick="javascript:jump('<%=currentPage + 1%>');">&nbsp;&nbsp;&nbsp;
      <% } else {%>
	      <img src="<%=webpath%>layout/rightnull.gif" width="14" height="13" border="0" alt="<%=userAdminLocale.get("NEXT_PAGE")%>">&nbsp;&nbsp;&nbsp;
	  <% } %>
	<% } %>
	<%-- end of RTL icon switch --%>	
	
	<!-- End Page Navigation -->
	<% entryExit.delete(0, entryExit.length());
	   entryExit.append(userAdminMessages.print(new Message("END_OF", navigateBar))); %>
	<img src="<%=webpath%>layout/sp.gif" 
	     width="1" 
	     height="1" 
	     id="<%=navigateBar%>"
	     alt="<%=entryExit.toString()%>"
	     tabindex="0">	
	</td>
</tr>
