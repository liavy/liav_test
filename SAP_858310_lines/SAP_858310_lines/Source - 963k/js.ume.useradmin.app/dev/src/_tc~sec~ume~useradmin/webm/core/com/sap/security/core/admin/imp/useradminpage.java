package com.sap.security.core.admin.imp;

import java.util.Locale;

import com.sap.security.core.admin.UserAdminLogic;
import com.sap.security.core.admin.SelfRegLogic;
import com.sap.security.core.admin.api.IUserAdminPage;

public class UserAdminPage extends UserAdminAbstractNode implements IUserAdminPage {
    public static final String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/imp/UserAdminPage.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    private String[] pages = {UserAdminLogic.userCreatePage,
                              UserAdminLogic.userModifyPage,
                              UserAdminLogic.userProfileViewPage,
                              UserAdminLogic.userProfileModifyPage,
                              UserAdminLogic.userSearchPage,
                              UserAdminLogic.userProfileModifyPortalPage,
                              SelfRegLogic.applyUserPage,
                              SelfRegLogic.applyCompanyUserPage};
    private String _title;

    public UserAdminPage(String uniqueName) {
        super.setUniqueName(uniqueName);
    }

    public boolean hasParent() {
        return false;
    }

    public String[] getParents() {
        return null;
    }

    public boolean isParent(String parentUniqueName) {
        return false;
    }

    public String getTitle() {
        return this._title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public void setCategoryOrder(String[] categoryNames) {
    }
} // IUserAdminPage