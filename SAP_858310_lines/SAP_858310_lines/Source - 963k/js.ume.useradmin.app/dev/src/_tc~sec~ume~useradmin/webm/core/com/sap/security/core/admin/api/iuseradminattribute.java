package com.sap.security.core.admin.api;

import com.sap.security.api.IUser;

public interface IUserAdminAttribute extends IUserAdminNode {
    public static final String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/api/IUserAdminAttribute.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    public String getNameSpace();

    public void setNameSpace(String nameSpace);

    public boolean isReadOnly(IUser user);

    public boolean isDisplayed(String permissionname);

    public void setDisplay(String permissionname, boolean allowDisplay);

    public boolean isModifiable(String permissionname);

    public void setModifiable(String permissionname, boolean allowModify);

    public int getMinLength();

    public void setMinLength(int minlength);

    public int getMaxLength();

    public void setMaxLength(int maxLength);

    public int getDisplayLength();

    public void setDisplayLength(int displayLength);

    // public IUserAdminCategory getCategory();

    // public boolean isMemberOfCategory(String categoryUniqueName);

    public boolean isMandatory();

    public void setMandatory(boolean mandatory);

    public boolean isValid(String value);

    public String getValidationRule();

    public void setValidationRule(String validationRule);

    public boolean isValidationCheckOn();

    public void setValidationCheck(boolean trigger);

} // end of IUserAdminField
