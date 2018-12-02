package com.sap.security.core.admin.api;

import java.util.Locale;
public interface IUserAdminNode {
    public static final String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/api/IUserAdminNode.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    public boolean hasParent();
    public boolean hasChild();

    public String getUniqueName();
    public void setUniqueName(String uniqueName);

    public String getDescription(Locale locale);
    public void setDescription(String description, Locale locale);

    // get Direct Parent
    public String[] getParents();
    // getDirect Child
    public String[] getChildren();

    public boolean isParent(String parentUniqueName);
    public boolean isChild(String childUniqueName);

    public void addChild(String ChildUniqueName);
    public void addChildren(String[] ChildrenUniqueNames);

    public void removeChild(String ChildUniqueName);
    public void removeChildren(String[] ChildrenUniqueNames);

    public void insertChild(String ChildUniqueName, int index);
} // IUserAdminPage