<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="java.util.Hashtable"%>
<%@ include file="proxy.txt" %>

<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

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
    function actionon (action) {
      var frm = document.forms["searchcompany"];
      var actionTag = document.createElement("input");
      actionTag.setAttribute("name", action);
      actionTag.setAttribute("type", "hidden");
      actionTag.setAttribute("value", "");
      frm.appendChild(actionTag); 
	  frm.submit();
    }
	function checkAction() {
	  actionon("<%=CompanySearchLogic.performCompanySearchAction%>");
	}
</script>

   <!-- start content -->
   <a name="main"></a>
<table cellspacing="3" cellpadding="3" border="0" width="98%" class="TB_CNT_BG">
<tr>
	<td width="100%">
	<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="5" border="0" alt=""></td></tr></table>
		<!-- Start Section Header -->
		<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
		   <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
		      <span tabindex="0"><%=userAdminLocale.get("SEARCHCOMPANY")%></span>
		   </td></tr>
		</table>
		<!-- End Section Header -->

		<!-- Start Section Description -->
		<table width="99%" cellspacing="0" cellpadding="0" border="0">
			<tr>
			  <td valign="top" width="22">&nbsp;</td>
			  <td  class="TBLO_XXS_L" width="100%">
				 <span tabindex="0"><%=userAdminLocale.get("SEARCH_COMPANY_HEADER")%></span>
			  </td>
			</tr>
		</table>
		<!-- End Description -->

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

		<!-- Start Confirm Msg-->
		<% if ( info.isInfo() ) { %>
		<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
		<tr><td width="100%" class="TX_CFM_XSB">
          <img src="<%=webpath%>layout/ico12_msg_warning.gif" width="12" height="12" border="0" />&nbsp;
          <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
        </td></tr>
		</table>
		<!-- End Confirm Msg -->
		<% } %>
		
		<form name="searchcompany" method="post" action="<%=companySearchAlias%>" onSubmit="checkAction()">
		<TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h1">
     	<tr>
        <td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=CompanySearchLogic.SEARCH_COMPANY_NAME%>"><%=userAdminLocale.get("COMPANY_NAME")%>:</LABEL></td>
        <td class="TX_XS" width="70%" nowrap>
        <%
        String companySearchName = request.getParameter(CompanySearchLogic.SEARCH_COMPANY_NAME);
        if (null == companySearchName) companySearchName = "";
        %>
        <input id="<%=CompanySearchLogic.SEARCH_COMPANY_NAME%>"
               name="<%=CompanySearchLogic.SEARCH_COMPANY_NAME%>"
               tabindex="0"
               type="text"
               size="20"
               style="width: 2in"
               value="<%=util.filteringSpecialChar(companySearchName)%>">
        </td>
      </tr>
			<tr>
				<td class="TBLO_XXS_R" nowrap></td>
				<td class="TBLO_XXS_L" nowrap><span tabindex="0"><%=userAdminLocale.get("PART_OR_COMP_NAME")%></span></td>
			</tr>
			<tr>
				<td class="TBLO_XXS_R" width="30%" nowrap><IMG height="10" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td>
			</tr>
			<tr>
				<td class="TBLO_XS_L" colspan="2" nowrap>
				<% String nameLinks = userAdminLocale.get("PART_OR_COMP_NAME");
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
				&nbsp;<a tabindex="0" href="<%=companySearchAlias%>?<%=CompanySearchLogic.performCompanySearchAction%>=&<%=CompanySearchLogic.SEARCH_COMPANY_CHARS%>=0123456789">0,1,2...</a>
				 <% for (int i=0; i<alphabet.length; i++) { %>
					&nbsp;|&nbsp;<% if (alphabet[i].equals("I") || alphabet[i].equals("S")) { %><br><% } %><a tabindex="0" href="<%=companySearchAlias%>?<%=CompanySearchLogic.performCompanySearchAction%>=&<%=CompanySearchLogic.SEARCH_COMPANY_NAME%>=<%=alphabet[i]+"*"%>"><%=alphabet[i]%></a>
				 <% } %>
				<% if ( CompanySearchLogic.guestUsersAllowed(proxy) ) { %>
					<span class="TBLO_XS_L"><a tabindex="0" href="<%=companySearchAlias%>?<%=CompanySearchLogic.finishCompanySearchAction%>=&<%=CompanySearchResultBean.RESULT_COMPANY_ID%>="><%=userAdminLocale.get("SELECT_GUEST_COMPANY")%></a></span>
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
        <tr><td colspan="2">
            <IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
        </td></tr>
    </table>
		<!-- End Overview Content -->

		<table cellpadding="0" cellspacing="0" border="0">
        <tr><td><img src="<%=webpath%>layout/sp.gif" width="1" height="30" alt="" border="0">
        </td></tr>
        </table>
			<!-- start buttons -->
			<table cellpadding="0" cellspacing="0" width="99%" border="0">
			<tr><td align="left" nowrap class="TBLO_XXS_L">
			<% String search = " "+userAdminLocale.get("SEARCH")+" ";
				String cancel = " "+userAdminLocale.get("CANCEL")+" "; %>
			<input class="BTN_LB"
					type="button"
					tabindex="0"
					name="<%=CompanySearchLogic.performCompanySearchAction%>"
					value="<%=search%>"
					onClick="actionon('<%=CompanySearchLogic.performCompanySearchAction%>')">&nbsp;
			<input class="BTN_LN"
					type="button"
					tabindex="0"
					name="<%=CompanySearchLogic.cancelCompanySearchAction%>"
					value="<%=cancel%>"
					onClick="actionon('<%=CompanySearchLogic.cancelCompanySearchAction%>')">
			</td></tr>
			</table>
	</td>
</tr>
</table>
<!-- end content -->
</form>
<%@ include file="contextspecific_includes_bottom.txt" %>

