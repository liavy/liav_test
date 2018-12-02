package com.sap.security.core.admin.imp;

import java.util.Locale;
import java.util.Vector;
import com.sap.security.core.admin.api.IUserAdminNode;

public abstract class UserAdminAbstractNode implements IUserAdminNode {
    public static final String basicInfo = "basicInformation";
    public static final String contactInfo = "contactInformation";
    public static final String additionalInfo = "additionalInformation";
    public static final String customizedInfo = "customizedInformation";
    private String _uniqueName;
    private String _description;

    private Vector _categories = new Vector();
    private Vector _children = new Vector();
    private Vector _parents = new Vector();

    public boolean hasParent() {
        return (_parents.isEmpty()==true)?false:true;
    }

    public boolean hasChild() {
        return (_children.isEmpty()==true)?false:true;
    }

    public String getUniqueName() {
        return this._uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this._uniqueName = uniqueName;
    }

    public String getDescription(Locale locale) {
        return this._description;
    }

    public void setDescription(String description, Locale locale) {
        this._description = description;
    }

    public String[] getParents() {
        return (String[]) this._parents.toArray(new String[this._parents.size()]);
    }

    public String[] getChildren() {
        return (String[]) this._children.toArray(new String[this._children.size()]);
    }

    public boolean isParent(String parentUniqueName) {
        int index = this._parents.indexOf(parentUniqueName);
        return (index >= 0)?true:false;
    }

    public boolean isChild(String childUniqueName) {
        int index = this._children.indexOf(childUniqueName);
        return (index >= 0)?true:false;
    }

    public void addChild(String childUniqueName) {
        this._children.add(childUniqueName);
    }

    public void addChildren(String[] childrenUniqueNames) {
        if ( null != childrenUniqueNames ) {
            int length = childrenUniqueNames.length;
            if ( length > 0 ) {
                for (int i=0; i<length; i++) {
                    this._children.add(childrenUniqueNames[i]);
                }
            }
        }
    } // addChildren

    public void removeChild(String childUniqueName) {
    }

    public void removeChildren(String[] childrenUniqueNames) {
    }

    public void insertChild(String childUniqueName, int index) {
    }

    public void setAttributeOrders(String[] attributeUniqueNames) {
    }

    public boolean isExpand() {
        return true;
    }
}