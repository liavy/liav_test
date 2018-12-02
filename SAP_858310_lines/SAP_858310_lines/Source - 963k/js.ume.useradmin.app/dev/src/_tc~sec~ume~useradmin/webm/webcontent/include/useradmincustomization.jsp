<%@ page import = "com.sap.security.core.admin.UserAdminCustomization" %>
<% UserAdminCustomization cus = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
   if ( null == cus ) cus = new UserAdminCustomization();
   IUser cusUser = proxy.getActiveUser();
   boolean isAddnRe = cus.isAddnExist(cusUser);
   String[] addnNames = null;
   String[] addnLabels = null;
   if ( isAddnRe ) {
       addnNames = cus.getAddnNames(cusUser);
       addnLabels = cus.getAddnLabels(cusUser);
   }
%>
