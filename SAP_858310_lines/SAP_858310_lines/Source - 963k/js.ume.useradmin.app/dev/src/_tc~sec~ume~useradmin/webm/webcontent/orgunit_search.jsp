<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="java.util.Hashtable"%>

<%@ include file="proxy.txt" %>

<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>

<% String orgUnitName = new String();
   if ( null != proxy.getRequestAttribute(UserAdminLogic.orgUnitName) ) {
     orgUnitName = (String) proxy.getRequestAttribute(UserAdminLogic.orgUnitName);
   }
%>

<%-- start html--%>
<%if (!inPortal) {%>
<html>
<head>
<TITLE><%=userAdminLocale.get("USER_MANAGEMENT")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>

<%@ include file="contextspecific_includes_top.txt" %>

<script language="JavaScript">
  function toSearch(searchName) {
        object = document.forms["searchorgunit"];
        inputTag1 = document.createElement("input");
        inputTag1.setAttribute("name", "<%=UserAdminLogic.performOrgUnitSearchAction%>");
        inputTag1.setAttribute("type","hidden");
        inputTag1.setAttribute("value", "");
        object.appendChild( inputTag1 );
        inputTag2 = document.createElement("input");
        inputTag2.setAttribute("name", "orgUnitSearchName");
        inputTag2.setAttribute("type","hidden");
        inputTag2.setAttribute("value", searchName);
        object.appendChild( inputTag2 );
        object.submit();
  }
</script>

<!-- start content -->
<a name="main"></a><table cellspacing="3" cellpadding="3" border="0" width="98%" class="TB_CNT_BG">
<tr>
	<td width="100%">
    <!-- Satrt Section Header -->

	<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
	    <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
	      <span tabindex="0"><%=userAdminLocale.get("SEARCH_ORGUNIT")%></span>
	    </td></tr>
	</table>

    <!-- End Section Header -->

    <table cellpadding="0" cellspacing="0" border="0">
      <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    </table>

    <!-- Start Section Description -->
    <table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
      <tr><td width="100%" class="TBLO_XXS_L">
        <span tabindex="0"><%=userAdminLocale.get("SEARCH_ORGUNIT_DESP")%></span>
      </td></tr>
    </table>
    <!-- End Section Description -->

    <table cellpadding="0" cellspacing="0" border="0">
      <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    </table>

    <% if ( error.isError() ) { %>
    <!-- Start Error Msg-->
    <table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
      <tr><td width="100%" class="TX_ERROR_XSB">
        <img src="<%=webpath%>layout/ico12_msg_error.gif" width="12" height="12" border="0" />&nbsp;
        <span tabindex="0"><UM:encode><%=userAdminMessages.print(error.getMessage())%></UM:encode></span>
      </td></tr>
    </table>
    <!-- End Error Msg -->
    <% } %>

    <% if ( info.isInfo() ) { %>
    <!-- Start Info Msg-->
    <table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
    <tr><td width="100%" class="TX_CFM_XSB">
        <img src="<%=webpath%>layout/ico12_msg_warning.gif" width="12" height="12" border="0" />&nbsp;
        <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
    </td></tr>
    </table>
    <!-- End Info Msg -->
    <% } %>

    <form name="searchorgunit" method="post" action="<%=userAdminAlias%>">
    <table class="TBLO_XXS_L" cellpadding="0" cellspacing="0" width="98%" border="0">
      <tr><td CLASS="TBLO_XXS_L">
        <TABLE cellpadding="2" cellspacing="0" border="0" width="100%">
          <tr>
        <td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=UserAdminLogic.orgUnitName%>"><%=userAdminLocale.get("ORGUNIT_NAME")%>:</LABEL></td>
        <td class="TX_XS" width="70%" nowrap>
            <input type="text"
                   size="20"
                   tabindex="0"
                   style="width: 2in"
                   id = "<%=UserAdminLogic.orgUnitName%>"
                   name="<%=UserAdminLogic.orgUnitName%>"
                   value="<%=util.filteringSpecialChar(orgUnitName)%>">
        </td>
        </tr>
          <tr>
            <td class="TBLO_XXS_R" width="30%" nowrap><IMG height="10" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td>
          </tr>
          <tr>
            <td class="TBLO_XS_L" colspan="2" nowrap>
				<% String nameLinks = userAdminLocale.get("ORGUNIT_NAME");
				   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());		   
				   entryExit.append(userAdminMessages.print(new Message("START_GROUP_LINKS", nameLinks))); %>
				<img src="<%=webpath%>layout/sp.gif" 
				     width="1" 
				     height="1" 
				     alt="<%=entryExit.toString()%>" 
				     tabindex="0" 
				     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=nameLinks%>').focus();}">	            
            <% String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                        "U", "V", "W", "X", "Y", "Z" }; %>
             <% for (int i=0; i<alphabet.length; i++) { %>
               <% if (alphabet[i].equals("I") || alphabet[i].equals("S")) { %><br><% } %>
               <a href="#" onClick="toSearch('<%=alphabet[i]%>')"><%=alphabet[i]%></a><% if ((i+1) != alphabet.length) { %>&nbsp;|&nbsp;<% } %>
             <% } %>
				<% entryExit.delete(0, entryExit.length());
				   entryExit.append(userAdminMessages.print(new Message("END_GROUP_LINKS", nameLinks))); %>
				<img src="<%=webpath%>layout/sp.gif" 
				     width="1" 
				     height="1" 
				     id="<%=nameLinks%>"
				     alt="<%=entryExit.toString()%>"
				     tabindex="0">	             
            </td>
          </tr>
        </table>
      </td></tr>
    </table>
    <!-- End Table TBLO_XS -->

    <table cellpadding="0" cellspacing="0" border="0">
      <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
    </table>

    <!-- Start Page Action Buttons -->
    <table width="98%" border="0" cellpadding="0" cellspacing="0">
      <tr><td width="100%" class="TBLO_XS_L" nowrap>
       <% String search = " "+userAdminLocale.get("SEARCH")+" "; %>
        <input class="BTN_LB"
               type="submit"
               tabindex="0"
               name="<%=UserAdminLogic.performOrgUnitSearchAction%>"
               value="<%=search%>">&nbsp;
			 <input type="hidden"
				      name="<%=UserAdminLogic.performOrgUnitSearchAction%>"
				      value="">
      </td></tr>
    </table></form>
	</td>
</tr>
</table>
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

