package com.sap.security.core.admin.imp;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.security.Permission;

import com.sap.security.api.IPrincipal;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sap.security.api.UMException;
import com.sap.security.core.admin.api.IUserAdminAttribute;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.role.IAction;
import com.sap.security.core.util.IUMTrace;

public class UserAdminAttribute extends UserAdminAbstractNode implements IUserAdminAttribute {
    public static final String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/imp/UserAdminAttribute.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
    private String _uniqueName;
    private String _description;
    private String[] sections = {};

    private int _minLength = 0;
    private int _maxLength = 100;
    private int _displayLength = 20;

    private boolean _mandatory = false;
    private static Map visibles;
    private static Map mutables;

    // load available permissions
    static {
        Iterator actions = InternalUMFactory.getServiceRepository().getAllServiceActions();
        visibles = new HashMap();
        mutables = new HashMap();
        Iterator permissions;
        IAction action = null;
        Permission permission;
        while ( actions.hasNext() ) {
            action = (IAction) actions.next();
            permissions = action.getPermissions();
            while ( permissions.hasNext() ) {
                visibles.put(((Permission)permissions.next()).getName(), Boolean.TRUE);
                mutables.put(((Permission)permissions.next()).getName(), Boolean.TRUE);
            }
        }
    }

    public UserAdminAttribute(String uniqueName) {
        this._uniqueName = uniqueName;
    }

    public String getNameSpace() {
        return "";
    }

    public void setNameSpace(String nameSpace) {
    }

    public boolean hasChild() {
        return false;
    }

    public boolean isReadOnly(IUser user) {
        try {
        return UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(user.getUniqueID(), IPrincipal.DEFAULT_NAMESPACE, this.getUniqueName());
        } catch (UMException ex) {
			trace.debugT("isReadOnly", ex);        	
            return false;
        }
    }

    public boolean isDisplayed(String permissionname) {
        if ( !visibles.containsKey(permissionname) )
            return false;
        else
            return ((Boolean)visibles.get(permissionname)).booleanValue();
    }

    public void setDisplay(String permissionname, boolean allowDisplay) {
        visibles.put(permissionname, new Boolean(allowDisplay));
    }

    public boolean isModifiable(String permissionname) {
        if ( !mutables.containsKey(permissionname) )
            return false;
        else
            return ((Boolean)mutables.get(permissionname)).booleanValue();
    }

    public void setModifiable(String permissionname, boolean allowModify) {
        mutables.put(permissionname, new Boolean(allowModify));
    }

    public int getMinLength() {
        return this._minLength;
    }

    public void setMinLength(int minLength) {
        this._minLength = minLength;
    }

    public int getMaxLength() {
        return this._maxLength;
    }

    public void setMaxLength(int maxLength) {
        this._maxLength = maxLength;
    }

    public int getDisplayLength() {
        return this._displayLength;
    }

    public void setDisplayLength(int displayLength) {
        this._displayLength = displayLength;
    }

    // public IUserAdminCategory getCategory();

    // public boolean isMemberOfCategory(String categoryUniqueName);

    public boolean isMandatory() {
        return this._mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this._mandatory = mandatory;
    }

    public boolean isValid(String value) {
        return true;
    }

    public String getValidationRule() {
        return new String();
    }

    public void setValidationRule(String validationRule) {
    }

    public boolean isValidationCheckOn() {
        return true;
    }

    public void setValidationCheck(boolean trigger) {
    }
} // end of IUserAdminField
