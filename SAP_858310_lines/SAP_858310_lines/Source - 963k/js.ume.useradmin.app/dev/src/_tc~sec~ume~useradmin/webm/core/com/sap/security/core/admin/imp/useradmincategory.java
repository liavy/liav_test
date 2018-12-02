package com.sap.security.core.admin.imp;

import java.util.Vector;

import com.sap.security.core.admin.api.IUserAdminCategory;

public class UserAdminCategory extends UserAdminAbstractNode  implements IUserAdminCategory {
    public static final String basicInfo = "basicInformation";
    public static final String contactInfo = "contactInformation";
    public static final String additionalInfo = "additionalInformation";
    public static final String customizedInfo = "customizedInformation";
    private String _uniqueName;
    private String _description;

    private Vector _categories = new Vector();

    public UserAdminCategory(String uniqueName) {
        this._uniqueName = uniqueName;
        this._categories.add(basicInfo);
        this._categories.add(contactInfo);
        this._categories.add(additionalInfo);
        this._categories.add(customizedInfo);
    }

    public void setAttributeOrders(String[] attributeUniqueNames) {
    }

    public boolean isExpand() {
        return true;
    }
}