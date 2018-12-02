<%@ taglib uri="UM" prefix="UM" %>
<%@page import="java.security.cert.X509Certificate"%>

<% X509Certificate[] certs = null;
   boolean hasCert = ((Boolean)proxy.getSessionAttribute(UserAdminLogic.hasCert)).booleanValue();
   if ( hasCert ) {
     certs = (X509Certificate[]) proxy.getSessionAttribute(UserAdminLogic.certs);
   }
   int idx = 0;
%>

<script language="JavaScript">
    function viewDetail(url) {
      window.name = "<%=parent%>";
      window.open(url, "sub", "WIDTH=400, HEIGHT=400, status=yes, resizable=no scrollbars=no");
    }

    function doRemove(idx) {
      object = document.forms[0];
      inputTag1 = document.createElement("input");
      inputTag1.setAttribute("name","<%=UserAdminLogic.performUserCertRemoveAction%>");
      inputTag1.setAttribute("type","hidden");
      inputTag1.setAttribute("value","");
      object.appendChild( inputTag1 );
      inputTag2 = document.createElement("input");
      inputTag2.setAttribute("name","<%=UserAdminLogic.certIdx%>");
      inputTag2.setAttribute("type","hidden");
      inputTag2.setAttribute("value", idx);
      object.appendChild( inputTag2 );
      object.submit();
    }

    function importCert() {
        window.name = "<%=parent%>";
        <% StringBuffer certSB = new StringBuffer(userAdminAlias);
           certSB.append("?");
           certSB.append(UserAdminLogic.importUserCertAction);
           certSB.append("="); %>
        var url = "<%=util.filteringSpecialChar(certSB.toString())%>";
        window.open(url, "sub", "WIDTH=400, HEIGHT=400, status=yes, resizable=no scrollbars=no");
    }
</script>

<% String certificateTitle = userAdminLocale.get("CERTIFICATES");   
   if ( entryExit.length() > 0 ) entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("START_GROUP_BOX", certificateTitle))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     alt="<%=entryExit.toString()%>" 
     tabindex="0" 
     onkeypress="if(event.keyCode=='115') {document.getElementById('<%=certificateTitle%>').focus();}">
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
	<table id="ccert-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		<td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=certificateTitle%></td>
		<td class="BGCOLOR_ICONOPEN" align="right" width="20">
		<img id="ccert-exp"
             src="<%=webpath%>layout/icon_open.gif"
             tabIndex="0" 
             width="13"
             height="15"
             border="0"
             alt="<%=altmin%>"
             onClick="javascript:expandME('ccert', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
             class="IMG_BTN"></td>
	</tr></table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG" align="center"><dir id="ccert-bd">
	<table cellpadding="0" cellspacing="0" border="0" width="90%">
    <tr><td width=100% align="right">
        <img src="<%=webpath%>layout/add.gif"
             width="24"
             height="20"
             tabIndex="0" 
             border="0"
             alt="<%=userAdminLocale.get("ADD_CERT")%>"
             class="IMG_BTN"
             onClick="javascript:importCert();">
    </td></tr>
  </table>
	<table cellpadding="0" cellspacing="0" border="0" CLASS="TBDATA_BDR_BG" width="90%">
	  <tr><td>
		<table cellpadding="1" cellspacing="1" border="0" width="100%">
		<tr>
			<td scope="col" class="TBDATA_HEAD" tabIndex="0" nowrap><%=userAdminLocale.get("SUBJECT")%></td>
			<td scope="col" class="TBDATA_HEAD" tabIndex="0" nowrap><%=userAdminLocale.get("ISSUED_BY")%></td>
            <td scope="col" class="TBDATA_HEAD" tabIndex="0" nowrap><%=userAdminLocale.get("SERIAL_NUM")%></td>
            <td scope="col" class="TBDATA_HEAD" tabIndex="0" nowrap><%=userAdminLocale.get("ACTION")%></td>
		</tr>
    <% if ( hasCert) { 
       X509Certificate cert = null; 
       for ( int i=0; i<certs.length; i++) { 
           cert = certs[i]; %>
	   <tr class="<%= (i % 2 == 0) ? "TBDATA_CNT_ODD_BG":"TBDATA_CNT_EVEN_BG"%>">
          <td scope="row" tabIndex="0" class="TBDATA_XXS_L"><UM:encode><%=cert.getSubjectDN()%></UM:encode></td>
          <td class="TBDATA_XXS_L" tabIndex="0" nowrap><UM:encode><%=cert.getIssuerDN()%></UM:encode></td>
          <td class="TBDATA_XXS_L" tabIndex="0" nowrap><UM:encode><%=cert.getSerialNumber()%></UM:encode></td>
          <td tabIndex="0"><% idx = i; %>
                <% StringBuffer urlSB = new StringBuffer(userAdminAlias);
                   urlSB.append("?");
                   urlSB.append(UserAdminLogic.performUserCertViewAction);
                   urlSB.append("=&");
                   urlSB.append(UserAdminLogic.certIdx);
                   urlSB.append("=");
                   urlSB.append(idx);
                   String url = urlSB.toString(); %>
             <img src="<%=webpath%>layout/viewdoc.gif" width="18" height="16" border="0" alt="<%=userAdminLocale.get("VIEW_DETAILS")%>" class="IMG_BTN" onClick="javascript:viewDetail('<%=util.filteringSpecialChar(url)%>')">
             &nbsp;<img src="<%=webpath%>layout/delete.gif" width="18" height="16" border="0" alt="<%=userAdminLocale.get("REMOVE")%>" class="IMG_BTN" onClick="javascript:doRemove('<%=idx%>')"></td>
		</tr>
    <% } %>
    <% } %>
		</table>
	  </td></tr>
    </table>
  	<table cellpadding="0" cellspacing="0" border="0">
	  <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
	</table></div>
  </td></tr>
</table>
<% entryExit.delete(0, entryExit.length());
   entryExit.append(userAdminMessages.print(new Message("END_GROUP_BOX", certificateTitle))); %>
<img src="<%=webpath%>layout/sp.gif" 
     width="1" 
     height="1" 
     id="<%=certificateTitle%>"
     alt="<%=entryExit.toString()%>"
     tabindex="0">