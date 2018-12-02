package com.sap.security.core.admin.api;

public interface IUserAdminPage extends IUserAdminNode {
    public static final String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/api/IUserAdminPage.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    public String getTitle();
    public void setTitle(String title);

    public void setCategoryOrder(String[] categoryNames);
} // IUserAdminPage