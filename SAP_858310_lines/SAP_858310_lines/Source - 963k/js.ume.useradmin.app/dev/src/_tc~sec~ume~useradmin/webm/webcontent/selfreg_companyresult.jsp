<%@ taglib uri="UM" prefix="UM" %>
<%@ page import="com.sap.security.core.admin.util"%>
<%@ include file="proxy.txt" %>

<%-- start jsp page attribute setting --%>
<% if ( util.isServlet23() ) proxy.setResponseContentType("text/html; charset=utf-8"); %>
<%@ page session="true"%>
<%@ page import="com.sapmarkets.tpd.master.TradingPartnerInterface"%>
<jsp:useBean id="list"
             class="com.sap.security.core.admin.ListBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>

<%@ taglib uri="UM" prefix="UM" %>

<% java.util.Vector tmp = list.getObjsOnCurrentPage();
   TradingPartnerInterface[] companies = (TradingPartnerInterface[]) tmp.toArray(new TradingPartnerInterface[1]);
   int totalItems = list.getTotalItems();
   int currentPage = list.getCurrentPage();
   int totalPages = list.getTotalPages();
   Integer[] itemPerPageOptions = list.getItemPerPageOptions();
   int currentItemPerPage = list.getCurrentItemPerPage();

   // prepare for page navigation
   int colspan = 7;
   StringBuffer sb = new StringBuffer(selfRegAlias);
   sb.append("?");
   sb.append(SelfRegLogic.performSearchResultNavigateAction);
   sb.append("=");
   String urlAndAction = new String(sb);
   String pageKey = UserAdminLogic.listPage;
   String pageName = SelfRegLogic.companyResultPage;
   String setListPage = pageKey + "=" + pageName;
%>
<%-- end of page attribute setting --%>

<script language="JavaScript">
	function addToFormAndSubmit(name, value) {
        var frm = document.forms[0];
        var itemObj;

        if( -1 != navigator.userAgent.indexOf("MSIE") ) {
            itemObj = document.all["selfRegForm"];
        } else {
            itemObj = frm.elements["selfRegForm"];
        }
        inputTag1 = document.createElement("input");
        inputTag1.setAttribute("name", name);
        inputTag1.setAttribute("type", "hidden");
        inputTag1.setAttribute("value", value);
        frm.appendChild(inputTag1);
        frm.submit();
        }
</script>

<%if (!inPortal) {%>
<HTML>
<HEAD>
  <TITLE><%=userAdminLocale.get("SELF_REGISTRATION")%></TITLE>
<script language="JavaScript" src="js/basic.js"></script>
</HEAD>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%}%>
<center>
<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
<tr>

<!-- Start Middle 780pxl Content space -->
	<td width="780" height="100%" valign="top" class="TB_CNT_BG">
<!-- Start Content -->
<table cellspacing="3" cellpadding="3" border="0" width="780" height="100%" class="TB_CNT_BG">
    <tr>
    <td width="98%" valign="top">


    <!-- Start Section Header -->
	<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD">
        <span tabindex="0"><%=userAdminLocale.get("SELECT_COMPANY")%></span>
      </td></tr>
    </table>

    <!-- End Section Header -->

    <table cellpadding="0" cellspacing="0" border="0">
      <tr><td>
      <IMG height="10" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
      </td></tr>
    </table>

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

    <table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="98%" align="center">
    <tr><td>
        <table cellpadding="1" cellspacing="1" border="0" width="100%">
        
        <% String navigateBar = userAdminLocale.get("NAVIGATION_BAR"); %>
        <%@ include file="/include/selfreg_pagenavigation.jsp"%>
				        
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
          </td>
          <td scope="col" class="TBDATA_HEAD" tabindex="0" nowrap><%=userAdminLocale.get("COMPANY_NAME")%></td>
        </tr>
      <form name="selfRegForm"
          method="post"
          action="<%=selfRegAlias%>">

        <% for(int i=0; i<companies.length; i++) { %>
		<tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
          <td class="TBDATA_XXS_C" width="1%" nowrap>
          <input id="<%=i%>"
                 type="radio"
                 name="<%=CompanySelectBean.companyIdId%>"	
                 tabindex="0" 		   
  	             class="noborder"
                 value="<%=util.filteringSpecialChar(companies[i].getPartnerID().toString())%>"
                 <%=i==0?"checked":""%>>
          </td>
          <td scope="row" class="TBDATA_XS_L">
          <label for="<%=i%>"><UM:encode><%=companies[i].getDisplayName()%></UM:encode></label>
			<% if ( i == (companies.length-1) ) { %>
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
        </form>
				        
        <%@ include file="/include/selfreg_pagenavigation.jsp"%>
        </table>
    </td></tr>
    </table>

    <table cellpadding="0" cellspacing="0" border="0">
      <tr><td>
      <IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
      </td></tr>
	  </table>

	<!-- Start Page Action Buttons -->
	<table width="98%" border="0" cellpadding="5" cellspacing="0">
	<tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <% String accept = " "+userAdminLocale.get("ACCEPT")+" ";
       String searchCompany = " "+userAdminLocale.get("SEARCH_AGAIN")+" ";
       String applyCompanyLater = " "+userAdminLocale.get("APPLYCOMPANYLATER")+" "; %>

        <input type="submit"
               class="BTN_LB"
               tabindex="0"
               name="<%=SelfRegLogic.acceptCompanyAction%>"
               value="<%=accept%>"
               onClick="addToFormAndSubmit('<%=SelfRegLogic.acceptCompanyAction%>', '<%=accept%>')";>&nbsp;
    <input type="submit"
           class="BTN_LN"
           tabindex="0"
           name="<%=SelfRegLogic.searchCompanyAction%>"
           value="<%=searchCompany%>"
           onClick="addToFormAndSubmit('<%=SelfRegLogic.searchCompanyAction%>', '<%=searchCompany%>')";>&nbsp;
    <input type="submit"
           class="BTN_LN"
           tabindex="0"
           name="<%=SelfRegLogic.applyCompanyLaterAction%>"
           value="<%=applyCompanyLater%>"
           onClick="addToFormAndSubmit('<%=SelfRegLogic.applyCompanyLaterAction%>', '<%=applyCompanyLater%>')";>&nbsp;
    </td></tr>
    </table>
	<!-- End Page Action Buttons -->


<%@ include file="contextspecific_includes_bottom.txt" %>

