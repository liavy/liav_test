<%@ include file="proxy.txt" %>
<%-- to include this page requires:--%>
<%@ taglib uri="UM" prefix="UM" %>
<%@page import="com.sap.security.api.*"%>
<%com.sap.security.core.admin.CountriesBean countries = (com.sap.security.core.admin.CountriesBean) proxy.getSessionAttribute("countries");%>

<%com.sap.security.core.admin.TimeZonesBean timezones = (com.sap.security.core.admin.TimeZonesBean) proxy.getSessionAttribute("timezones");%>
<jsp:useBean id="user"
             class="com.sap.security.core.admin.UserBean"
             scope="request"/>
<jsp:useBean id="userAccount"
             class="com.sap.security.core.admin.UserAccountBean"
             scope="request"/>
<jsp:useBean id="companySearchResult"
             class="com.sap.security.core.admin.CompanySearchResultBean"
             scope="request"/>
<jsp:useBean id="error"
             class="com.sap.security.core.util.ErrorBean"
             scope="request"/>
<jsp:useBean id="info"
             class="com.sap.security.core.util.InfoBean"
             scope="request"/>
<%-- end page attribute setting--%>

<% IUser self = user.getUser();
   IUserAccount account = userAccount.getUserAccount();
   String logonId = account.getLogonUid();
   boolean toDisable = false;
   if ( UserAdminFactory.isUserReadOnly(self.getUniqueID()) )
       toDisable = true;
   String parent = (String) proxy.getSessionAttribute(UserAdminLogic.parent);

   UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   if ( null == uac ) uac = new UserAdminCustomization();
   boolean orgReq = uac.isOrgUnitRequired(proxy, self);
   
   boolean isLocked = (account==null)?false:account.isLocked();
   boolean unapproved = (self.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)==null)?false:true; 
  
   String altmin = userAdminLocale.get("MINIMIZE_THIS_SECTION");
   String altmax = userAdminLocale.get("MAXIMIZE_THIS_SECTION");
%>

<%-- start html--%>
<%--  used in Portal environment, no header, only body --%>


<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
<tr>

<!-- Start Middle 780pxl Content space -->
	<td width="780" height="100%" valign="top" class="TB_CNT_BG">
<!-- Start Content -->
<table cellpadding="0" cellspacing="0" border="0" width="780" height="100%">

	<tr>
	<!-- Start Transactional Content -->
    <td width="100%" valign="top">

<form method="post" action="<%=userAdminAlias%>">

<!-- Start Section Header -->
<a name="main"></a>
<table cellpadding="0" cellspacing="0" border="0" width="99%" align="center">
      <tr class="SEC_TB_TD"><td class="SEC_TB_TD"><%=userAdminLocale.get("MODIFY_USERPROFILE_HEADER")%></td></tr>
</table>
<!-- End Section Header -->

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="5" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Section Description -->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TBLO_XXS_L">
  <span tabindex="0"><%=userAdminLocale.get("MODIFY_USERPROFILE_DESCRIPTION")%></span></td></tr>
</table>

<% if ( info.isInfo() ) { %>
<!-- Start Confirm Msg-->
<table align="center" cellpadding="0" cellspacing="0" width="99%" border="0">
  <tr><td width="100%" class="TX_CFM_XSB">
    <img src="<%=webpath%>layout/ico12_msg_success.gif" width="12" height="12" border="0" />&nbsp;
    <span tabindex="0"><UM:encode><%=userAdminMessages.print(info.getMessage())%></UM:encode></span>
  </td></tr>
</table>
<!-- End Confirm Msg -->
<% } %>

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

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
	  <table id="cb-hd" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
        <td class="TBDATA_XSB_NBG" tabIndex="0" width="100%">&nbsp;<%=userAdminLocale.get("BASIC_INFO")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="cb-exp"
                 tabIndex="0"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('cb', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
      </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="cb-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h0">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <% if (toDisable) { %>
      <tr><td colspan="2"><%=userAdminLocale.get("USER_IS_READONLY")%></td></tr>
      <% } %>
      <tr><td colspan="2">
        <input type="hidden" name="<%=user.uidId%>" value="<%=user.getUid()%>">
        <input type="hidden" name="<%=UserAdminLogic.personalization%>" value="<%=UserAdminLogic.personalization%>">
      </td></tr>
      <tr>
        <td class="TBLO_XXS_R" width="30%" nowrap><span tabindex="0"><%=userAdminLocale.get("USER_ID")%>:</span></td>
        <td class="TX_XS" width="70%" nowrap><span tabindex="0"><%=userAccount.getLogonUid()%></span>
        <input type="hidden" name="<%=userAccount.logonuid%>" value="<%=userAccount.getLogonUid()%>"></td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%= user.lastNameId %>"><%=userAdminLocale.get("LAST_NAME")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.lastNameId%>"
                 name="<%=user.lastNameId%>"
                 value="<%=user.getLastName()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.lastNameId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>

      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL for="<%= user.firstNameId %>"><%=userAdminLocale.get("FIRST_NAME")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.firstNameId%>"
                 name="<%=user.firstNameId%>"
                 value="<%=user.getFirstName()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.firstNameId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>

      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.emailId%>"><%=userAdminLocale.get("EMAIL")%>:<span class="required">*</span></LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.emailId%>"
                 name="<%=user.emailId%>"
                 value="<%=user.getEmail()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.emailId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>

      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.salutationId%>"><%=userAdminLocale.get("SALUTATION")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.salutationId%>"
                 name="<%=user.salutationId%>"
                 value="<%=user.getSalutation()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.salutationId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      
      <% if ( null != util.checkEmpty(user.getCompanyId()) ) { %>
      <tr>
          <td class="TBLO_XXS_R" nowrap><%=userAdminLocale.get("COMPANY")%>:</td>
          <td class="TX_XS" nowrap>
            <%=user.getCompanyName()%><%if(unapproved){%>(<%=userAdminLocale.get("UNAPPROVED")%>)<%}%>
          </td>
      </tr>
      <% } %>

      <tr>
        <td class="TBLO_XXS_R" nowrap><span tabindex="0"><%=userAdminLocale.get("ACCOUNT_STATUS")%>:</span></td>
        <td class="TX_XS" nowrap>
        <span tabindex="0">
          <%if (isLocked){ %>
              <%=userAdminLocale.get("DEACTIVE")%>
          <% }else{ %>
              <%=userAdminLocale.get("ACTIVE")%>
          <% }%>
        </span>
        </td>
      </tr>

      <tr>
        <td class="TBLO_XXS_R" nowrap><label for="<%=user.accessibilitylevelId%>"><%=userAdminLocale.get("ACCESSIBILITY_LEVEL")%>:<br><%=userAdminLocale.get("SCREENREADER_NEEDED")%></label></td>
        <td class="TX_XS" nowrap>
            <input type="checkbox"
                   id="<%=user.accessibilitylevelId%>"
                   name="<%=user.accessibilitylevelId%>"
                   class="noborder"
				   value="<%=user.getAccessibilityLevel()%>"
                   <%=(user.getAccessibilityLevel()==IUser.SCREENREADER_ACCESSIBILITY_LEVEL)?"checked":""%>
                   <% if (toDisable) {%>disabled<%} else { %>
                   <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.accessibilitylevelId)){%>DISABLED<%}%>
                   <%}%>>
        </td>
      </tr>
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
      </td></tr>
    </table></div>
  </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>
<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
	  <table id="cc-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("CONTACT_INFO")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="cc-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('cc', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
      </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="cc-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h1">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <tr>
        <td class="TBLO_XXS_R" width="30%" nowrap><LABEL FOR="<%=user.telephoneId%>"><%=userAdminLocale.get("PHONE")%>:</LABEL></td>
        <td class="TX_XS" width="70%" nowrap>
          <input id="<%=user.telephoneId%>"
                 name="<%=user.telephoneId%>"
                 value="<%=user.getTelephone()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.telephoneId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.faxId%>"><%=userAdminLocale.get("FAX")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.faxId%>"
                 name="<%=user.faxId%>"
                 value="<%=user.getFax()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.faxId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.mobileId%>"><%=userAdminLocale.get("MOBILE")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.mobileId%>"
                 name="<%=user.mobileId%>"
                 value="<%=user.getMobile()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.mobileId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.streetAddressId%>"><%=userAdminLocale.get("STREET")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.streetAddressId%>"
                 name="<%=user.streetAddressId%>"
                 value="<%=user.getStreetAddress()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.streetAddressId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.cityId%>"><%=userAdminLocale.get("CITY")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.cityId%>"
                 name="<%=user.cityId%>"
                 value="<%=user.getCity()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.cityId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.stateId%>"><%=userAdminLocale.get("STATE")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.stateId%>"
                 name="<%=user.stateId%>"
                 value="<%=user.getState()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.stateId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.zipId%>"><%=userAdminLocale.get("ZIP")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.zipId%>"
                 name="<%=user.zipId%>"
                 value="<%=user.getZip()%>"
                 type="text"
                 size="20"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.zipId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.countryId%>"><%=userAdminLocale.get("COUNTRY")%>:<%if(orgReq){%><span class="required">*</span><%}%></LABEL></td>
        <td class="TBLO_XS_L">
          <select id="<%=user.countryId%>"
                  name="<%=user.countryId%>"
                  class="DROPDOWN"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.countryId)){%>DISABLED<%}%>
                 <%}%>>
            <option value=""><%=userAdminLocale.get("PLEASE_SELECT")%></option><%=countries.getHtmlOptions(user.getCountry())%>
                </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.timeZoneId%>"><%=userAdminLocale.get("TIME_ZONE")%>:</LABEL></td>
        <td class="TBLO_XS_L">
          <select id="<%=user.timeZoneId%>"
                  name="<%=user.timeZoneId%>"
                  class="DROPDOWN"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.timeZoneId)){%>DISABLED<%}%>
                 <%}%>>
            <option value=""><%=userAdminLocale.get("PLEASE_SELECT")%></option><%=timezones.getHtmlOptions(user.getTimeZone())%>
                </td>
      </tr>
      <tr><td colspan="2">
        <IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt="">
      </td></tr>
    </table></div>
  </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<table cellpadding="0" cellspacing="1" border="0" class="SEC_TB_BDR" width="98%">
  <tr><td class="TBLO_XXS_L">
    <table id="ca-hd" tabIndex="0" border="0" cellpadding="0" cellspacing="0">
      <tr class="TBDATA_CNT_EVEN_BG">
		    <td class="TBDATA_XSB_NBG" width="100%">&nbsp;<%=userAdminLocale.get("ADDITIONAL_INFO")%></td>
		    <td class="BGCOLOR_ICONOPEN" align="right" width="20">
            <img id="ca-exp"
                 tabIndex="2"
                 src="<%=webpath%>layout/icon_open.gif"
                 width="13"
                 height="15"
                 border="0"
                 alt="<%=altmin%>"
                 onClick="javascript:expandME('ca', 0, '<%=altmin%>', '<%=altmax%>', '<%=webpath%>');"
                 class="IMG_BTN"></td>
	    </tr>
    </table>
  </td></tr>
  <tr><td class="TBDATA_CNT_ODD_BG"><div id="ca-bd">
    <TABLE cellpadding="2" cellspacing="0" border="0" width="100%" id="h2">
      <tr><td colspan="2"><IMG height="2" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
      <tr>
        <td class="TBLO_XXS_R" width="30%" nowrap>
          <LABEL FOR="<%=user.positionId%>"><%=userAdminLocale.get("POSITION")%>:</LABEL>
        </td>
		<td class="TX_XS" width="70%" nowrap>
          <input id="<%=user.positionId%>"
                 name="<%=user.positionId%>"
                 value="<%=user.getPosition()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.positionId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <tr>
        <td class="TBLO_XXS_R" nowrap><LABEL FOR="<%=user.departmentId%>"><%=userAdminLocale.get("DEPARTMENT")%>:</LABEL></td>
        <td class="TX_XS" nowrap>
          <input id="<%=user.departmentId%>"
                 name="<%=user.departmentId%>"
                 value="<%=user.getDepartment()%>"
                 type="text"
                 size="20"
                 style="width: 2in"
                 <% if (toDisable) {%>disabled<%} else { %>
                 <%if (UserAdminFactory.isAttributeReadOnly(self.getUniqueID(), UserBean.departmentId)){%>DISABLED<%}%>
                 <%}%>>
        </td>
      </tr>
      <% if (orgReq) { %>
      <tr>
        <td class="TX_XS" nowrap><input type="hidden" name="<%=user.orgUnitId%>" value="<%=user.getOrgUnit()%>"></td>
      </tr>
      <% } %>      
    </table></div>
  </td></tr>
</table>

<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="8" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<%-- following section could be customized by customers --%>
<%@include file="/include/userprofile_customizedattri_change.jsp"%>
<%-- end of the customized section--%>

<!-- end data table -->
<table cellpadding="0" cellspacing="0" border="0">
  <tr><td><IMG height="30" src="<%=webpath%>layout/sp.gif" width="1" border="0" alt=""></td></tr>
</table>

<!-- Start Page Action Buttons -->
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" align="left" class="TBLO_XXS_L" nowrap>
    <% String apply = " "+userAdminLocale.get("APPLY")+" ";
       String reset = " "+userAdminLocale.get("RESET")+" ";
       String close = " "+userAdminLocale.get("CLOSE")+" "; %>
	<input type="submit"
	       name="<%=UserAdminLogic.performUserProfileChangeAction%>"
	       value="<%=apply%>"
	       class="BTN_LB">&nbsp;
	<input type="reset"
	       name="reset"
	       value="<%=reset%>"
	       class="BTN_LN">&nbsp;
	<input type="button"
	       name="close"
	       value="<%=close%>"
	       class="BTN_LN"
           onClick="javascript:top.close();">
  </td></tr>
</table>
</form>
<!-- End Page Action Buttons -->
<!-- end content -->

<%@ include file="contextspecific_includes_bottom.txt" %>

