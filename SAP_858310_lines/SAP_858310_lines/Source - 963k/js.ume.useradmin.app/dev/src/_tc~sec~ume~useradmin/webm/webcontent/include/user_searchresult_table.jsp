<%-- to include this page, you need to define
<%@ taglib uri="UM" prefix="UM" %>
     pageKey, pageName, user
--%>

<% String sort = (String) proxy.getSessionAttribute(UserAdminLogic.sortFieldName);
   boolean order = ((Boolean)proxy.getSessionAttribute(UserAdminLogic.orderBy)).booleanValue(); // true is as; false is de

   StringBuffer altTextSB = new StringBuffer(userAdminLocale.get("SORT_BY"));
   altTextSB.append(": ");
   int altLength = altTextSB.length();
   String altText;

   boolean allowedToView = UserAdminHelper.hasAccess(performer, UserAdminHelper.VIEW_PROFILE);
   boolean allowedToModify = UserAdminHelper.hasAccess(performer, UserAdminHelper.CHANGE_PROFILE);
   boolean allowedToLock = UserAdminHelper.hasAccess(performer, UserAdminHelper.LOCK_USERS);
   boolean allowedToUnlock = UserAdminHelper.hasAccess(performer, UserAdminHelper.UNLOCK_USERS);
   boolean allowedToCreate = UserAdminHelper.hasAccess(performer, UserAdminHelper.CREATE_USERS);
   boolean allowedToViewRoles = UserAdminHelper.hasAccess(performer, UserAdminHelper.VIEW_ROLES);
   boolean allowedToViewGroups = UserAdminHelper.hasAccess(performer, UserAdminHelper.VIEW_GROUPS);
%>

<script language="JavaScript">
    function doSomething(action, index) {
      frm = document.forms[0];
      actionTag = document.createElement("input");
      actionTag.setAttribute("name", action);
      actionTag.setAttribute("type", "hidden");
      actionTag.setAttribute("value", "");
      frm.appendChild( actionTag );

      slctObjTag = document.createElement("input");
      slctObjTag.setAttribute("name", "<%=ListBean.selectedObjId%>");
      slctObjTag.setAttribute("type", "hidden");
      slctObjTag.setAttribute("value", index);
      frm.appendChild( slctObjTag );

      pageTag = document.createElement("input");
      pageTag.setAttribute("name", "<%=pageKey%>");
      pageTag.setAttribute("type", "hidden");
      pageTag.setAttribute("value", "<%=pageName%>");
      frm.appendChild(pageTag);

      frm.action = "<%=userAdminAlias%>";
      setPaging();
      frm.submit();
    }

    function doSorting(sortName, orderBy) {
      frm = document.forms[0];
      actionTag = document.createElement("input");
      actionTag.setAttribute("name", "<%=UserAdminLogic.performSearchResultSortingAction%>");
      actionTag.setAttribute("type", "hidden");
      actionTag.setAttribute("value", "");
      frm.appendChild( actionTag );

      sortFieldNameTag = document.createElement("input");
      sortFieldNameTag.setAttribute("name", "<%=UserAdminLogic.sortFieldName%>");
      sortFieldNameTag.setAttribute("type", "hidden");
      sortFieldNameTag.setAttribute("value", sortName);
      frm.appendChild( sortFieldNameTag );

      orderByTag = document.createElement("input");
      orderByTag.setAttribute("name", "<%=UserAdminLogic.orderBy%>");
      orderByTag.setAttribute("type", "hidden");
      orderByTag.setAttribute("value", orderBy);
      frm.appendChild( orderByTag );

      pageTag = document.createElement("input");
      pageTag.setAttribute("name", "<%=pageKey%>");
      pageTag.setAttribute("type", "hidden");
      pageTag.setAttribute("value", "<%=pageName%>");
      frm.appendChild(pageTag);

      frm.action = "<%=userAdminAlias%>";
      setPaging();
      frm.submit();
    } // doSorting

    function toggle_select(size) {
        var toggleslct = document.getElementById("toggleSelect");
        if (toggleslct.checked == true) {
            select_all(size);
        }
        else {
            deselect_all(size);
        }
    }

    function select_all(size) {
        var cks = document.all("<%=ListBean.selectedObjId%>");
        if ( size < 2 ) {
            cks.checked = true;
        } else {
            for (var i=0; i<size; i++) {
                cks[i].checked = true;
            }
        }
    }

    function deselect_all(size) {
        var cks = document.all("<%=ListBean.selectedObjId%>");
        if ( size < 2 ) {
            cks.checked = false;
        } else {
            for (var i=0; i<size; i++) {
                cks[i].checked = false;
            }
        }
    }
</script>

<tr>
  <td class="TBDATA_HEAD" nowrap>
	<% String resultTable = userAdminLocale.get("TABLE"); 
	 if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
	 entryExit.append(userAdminMessages.print(new Message("START_OF", resultTable))); %>
	<img src="<%=webpath%>layout/sp.gif" 
	     width="1" 
	     height="1" 
	     alt="<%=entryExit.toString()%>" 
	     tabindex="0" 
	     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=resultTable%>').focus();}">     
    <INPUT TYPE="CHECKBOX" tabindex="0" id="toggleSelect" NAME="toggleSelect" class="noborder" onClick="toggle_select('<%=users.length%>')" alt="<%=userAdminLocale.get("SELECT_OR_DESELECT")%>">
  </td>
  <td scope="col" class="TBDATA_HEAD" nowrap width="1%">
    <% if ( !pageName.equals(UserAdminLogic.lockedUsersListPage) ) {%>
        <% altTextSB.append(userAdminLocale.get("ACCOUNT_STATUS"));
           altText = altTextSB.toString();
           if ( sort.equals(UserAccountBean.locked) ) {
               if ( order ) { %>
            <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.locked%>' ,'false');"><img src="<%=webpath%>layout/sortdown.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
          <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.locked%>' ,'true');"><img src="<%=webpath%>layout/sortup.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
          <% } %>
        <% } else { %>
            <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.locked%>' ,'false');"><img src="<%=webpath%>layout/sort.gif" tabindex="0" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>" title="<%=userAdminLocale.get("SORT")%>"></a>
        <% } %>
    <% } else { %>
        <img src="<%=webpath%>layout/icon_freeze.gif" width="10" height="10" border="0" alt="<%=userAdminLocale.get("DEACTIVATED")%>" title="<%=userAdminLocale.get("DEACTIVATED")%>">
    <% } %>
  </td>
  <td scope="col" class="TBDATA_HEAD" nowrap>
    <table><tr><td scope="col" tabindex="0" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("NAME")%>&nbsp;</td><td valign="center">
    <% altTextSB.replace(altLength, altTextSB.length(), userAdminLocale.get("NAME"));
       altText = altTextSB.toString();
       if ( sort.equals(UserBean.displayNameId) ) {
           if ( order ) { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.displayNameId%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.displayNameId%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } %>
    <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.displayNameId%>', 'false');"><img src="<%=webpath%>layout/sort.gif" tabindex="0" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>" title="<%=userAdminLocale.get("SORT")%>"></a>
    <% } %>
    </td></tr></table>
  </td>
  <td scope="col" class="TBDATA_HEAD" nowrap>
    <table><tr><td scope="col" tabindex="0" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("USER_ID")%>&nbsp;</td><td valign="center">
    <% altTextSB.replace(altLength, altTextSB.length(), userAdminLocale.get("USER_ID"));
       altText = altTextSB.toString();
       if ( sort.equals(UserAccountBean.logonuid) ) {
           if ( order ) { %>
        <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.logonuid%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.logonuid%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } %>
    <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserAccountBean.logonuid%>', 'false');"><img src="<%=webpath%>layout/sort.gif" tabindex="0" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>" title="<%=userAdminLocale.get("SORT")%>"></a>
    <% } %>
    </td></tr></table>
  </td>
  <td scope="col" class="TBDATA_HEAD" nowrap>
    <% if ( UserAdminCustomization.isCompanyFieldEnabled(proxy) ) { %>
    <table><tr><td scope="col" tabindex="0" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("COMPANY")%>&nbsp;</td><td valign="center">
    <% altTextSB.replace(altLength, altTextSB.length(), userAdminLocale.get("COMPANY"));
       altText = altTextSB.toString();
       if ( sort.equals(UserBean.companyId) ) {
           if ( order ) { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.companyId%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.companyId%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } %>
    <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.companyId%>', 'false');"><img src="<%=webpath%>layout/sort.gif" tabindex="0" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>" title="<%=userAdminLocale.get("SORT")%>"></a>
    <% } %>
    </td></tr></table>
    <% } else { %>
    <table><tr><td scope="col" tabindex="0" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("DEPARTMENT")%>&nbsp;</td><td valign="center">
    <% altTextSB.replace(altLength, altTextSB.length(), userAdminLocale.get("DEPARTMENT"));
       altText = altTextSB.toString();
       if ( sort.equals(UserBean.departmentId) ) {
           if ( order ) { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.departmentId%>', 'false');"><img src="<%=webpath%>layout/sortdown.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.departmentId%>', 'true');"><img src="<%=webpath%>layout/sortup.gif" tabindex="0" width="8" height="7" border="0" alt="<%=altText%>" title="<%=altText%>"></a>
      <% } %>
    <% } else { %>
        <A href="#" onClick="javascript:doSorting('<%=UserBean.departmentId%>', 'false');"><img src="<%=webpath%>layout/sort.gif" tabindex="0" width="8" height="7" border="0" alt="<%=userAdminLocale.get("SORT")%>" title="<%=userAdminLocale.get("SORT")%>"></a>
    <% } %>
    </td></tr></table>
    <% } %>
  </td>
  <td scope="col" tabindex="0" class="TBDATA_HEAD" nowrap><%=userAdminLocale.get("ACTIONS")%></td>
</tr>

<% boolean toAllowChangeStatus = true;
   boolean isLocked = false;
   boolean toDisable = false;  
   int lockMutableStatus = 1;
   int changeMutableStatus = 1;
   String companyName = null;
   String companyId = null;
   com.sapmarkets.tpd.master.TradingPartnerInterface tp = null;   
   String logonId = " ";
   for (int i=0; i<users.length; i++) {
       user = users[i];
       accounts = user.getUserAccounts();
       isLocked = false;
       if ( ( null != accounts ) && ( accounts.length > 0 ) ) {
           isLocked = accounts[0].isLocked();
           logonId = accounts[0].getLogonUid();
       }      
       lockMutableStatus = readOnlys[2*i];
       changeMutableStatus = readOnlys[2*i+1];
       toAllowChangeStatus = (lockMutableStatus>0)?true:((lockMutableStatus==-3)?true:false);
       toDisable = (changeMutableStatus>0)?false:((changeMutableStatus==-2)?false:true); 
%>
<tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
  <td class="TBDATA_XXS_C" width="1%" nowrap>
    <input type="checkbox" id="<%=ListBean.selectedObjId%>" tabindex="0" name="<%=ListBean.selectedObjId%>" class="noborder" value="<%=i%>">
    <input type="hidden" id="readOnly" name="readOnly" value="<%=(changeMutableStatus==-2)?"true":"false"%>">
  </td>
  <td class="TBDATA_XXS_C" nowrap>
    <% if ( !isLocked ) { %>
      <img src="<%=webpath%>layout/bulletgreen.gif" tabindex="0" alt="<%=userAdminLocale.get("ACTIVE")%>" width="16" height="16" border="0">
    <% } else { %>
      <img src="<%=webpath%>layout/bulletwhite.gif" tabindex="0" alt="<%=userAdminLocale.get("DEACTIVATED")%>" width="16" height="16" border="0">
    <% } %>
  </td>
  <td scope="row" tabindex="0" class="TBDATA_XS_L">
      <% if ( allowedToView ) { %>
      <a href="javascript:doSomething('<%=UserAdminLogic.viewUserProfileAction%>', '<%=i%>');"><UM:encode><%=user.getDisplayName()%></UM:encode></a>
      <% } else { %>
      <UM:encode><%=user.getDisplayName()%></UM:encode>
      <% } %>
  </td>
  <td tabindex="0" class="TBDATA_XXS_L">
      <%=logonId%>
  </td>
  <td tabindex="0" class="TBDATA_XXS_L" nowrap>
    <% if ( UserAdminCustomization.isCompanyFieldEnabled(proxy) ) { 
	       companyName = "----";
	       companyId = user.getCompany();
	       if ( null != util.checkEmpty(companyId) ) {
	           tp = util.getTP(companyId);
	           if ( null != tp ) {
	               companyName = tp.getDisplayName();
	               if ( null == util.checkEmpty(companyName) )
	                   companyName = "----";
	           }
       } %><UM:encode><%=companyName%></UM:encode>
    <% } else { %>
        <UM:encode><%=(user.getDepartment()==null)?"":user.getDepartment()%></UM:encode>
    <% } %>
  </td>
  <td class="TBDATA_XXS_L" nowrap>
      <% if ( allowedToCreate ) { %>
	  <img src="<%=webpath%>layout/createByRef2.gif"
         width="16"
         tabindex="0" 
         height="16"
         border="0"
         alt="<%=userAdminLocale.get("COPY_TO_NEW")%>"
         title="<%=userAdminLocale.get("COPY_TO_NEW")%>"
         onClick="javascript:doSomething('<%=UserAdminLogic.createUserFromReferenceAction%>', '<%=i%>');"
         CLASS="IMG_BTN">&nbsp;
      <% } %>
      <% if ( allowedToModify && !toDisable ) { %>
	  <img src="<%=webpath%>layout/modify2.gif"
         width="15"
         tabindex="0" 
         height="15"
         border="0"
         alt="<%=userAdminLocale.get("MODIFY")%>"
         title="<%=userAdminLocale.get("MODIFY")%>"
         onClick="javascript:doSomething('<%=UserAdminLogic.modifyUserAction%>', '<%=i%>');"
         CLASS="IMG_BTN">&nbsp;
      <% } %>
    <% if ( !isLocked ) { %>
      <% if ( allowedToLock && toAllowChangeStatus ) { %>
	  <img src="<%=webpath%>layout/deactivateTbl.gif"
         width="20"
         height="16"
         tabindex="0" 
         border="0"
         alt="<%=userAdminLocale.get("DEACTIVATE")%>"
         title="<%=userAdminLocale.get("DEACTIVATE")%>"
         onClick="javascript:doSomething('<%=UserAdminLogic.lockUserAction%>', '<%=i%>');"
         CLASS="IMG_BTN">&nbsp;
      <% } %>
    <% } else { %>
      <% if ( allowedToUnlock && toAllowChangeStatus ) { %>
	  <img src="<%=webpath%>layout/activateTbl.gif"
         width="20"
         height="16"
         tabindex="0" 
         border="0"
         alt="<%=userAdminLocale.get("ACTIVATE")%>"
         title="<%=userAdminLocale.get("ACTIVATE")%>"
         onClick="javascript:doSomething('<%=UserAdminLogic.unlockUserAction%>', '<%=i%>');"
         CLASS="IMG_BTN">&nbsp;
      <% } %>
    <% } %>
    <% if ( allowedToViewRoles ) { %>
	  <img src="<%=webpath%>layout/role.gif"
         width="16"
         height="16"
         tabindex="0" 
         border="0"
         alt="<%=userAdminLocale.get("ASSIGNED_ROLES")%>"
         title="<%=userAdminLocale.get("ASSIGNED_ROLES")%>"
         onClick="javascript:doSomething('<%=UserAdminLogic.viewRolesAction%>', '<%=i%>');"
         CLASS="IMG_BTN">&nbsp;
    <% } %>
    <% if ( allowedToViewGroups ) { %>
	  <img src="<%=webpath%>layout/group.gif"
         width="16"
         height="16"
         tabindex="0" 
         border="0"
         alt="<%=userAdminLocale.get("GROUPS")%>"
         title="<%=userAdminLocale.get("GROUPS")%>"
         onClick="javascript:doSomething('<%=UserAdminLogic.viewGroupsAction%>', '<%=i%>');"
         CLASS="IMG_BTN">
    <% } %>
    <% if ( i == (users.length-1) ) { %>
	<% entryExit.delete(0, entryExit.length());
	   entryExit.append(userAdminMessages.print(new Message("END_OF", resultTable))); %>
	<img src="<%=webpath%>layout/sp.gif" 
	     width="1" 
	     height="1" 
	     id="<%=resultTable%>"
	     alt="<%=entryExit.toString()%>"
	     tabindex="0">    
    <% } %>
  </td>
</tr>
<% } %>




