package com.sap.security.core.admin.api;

public interface IUserAdminCategory extends IUserAdminNode {
    public static final String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/api/IUserAdminCategory.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    public final String basicInfo = "BASIC_INFO";
    public final String contactInfo = "CONTACT_INFO";
    public final String additionalInfo = "ADDITIONAL_INFO";
    public final String customizedInfo = "CUSTOMIZED_INFO";

    public void setAttributeOrders(String[] attributeUniqueNames);
    public boolean isExpand();
}