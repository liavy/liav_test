package com.sap.archtech.archconn.mbeans;

import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sap.security.api.permissions.ActionPermission;

public class ConnectorAccessPermission extends ActionPermission
{
  private static final long serialVersionUID = 42L;
  
  public ConnectorAccessPermission(String arg0, String arg1)
  {
    super(arg0, arg1);
  }
  
  static void checkPermission(String permissionName, String archSetName)
  {
    boolean hasPermission = true;
    if(archSetName == null || archSetName.equals(""))
    {
      hasPermission = false;
    }
    else
    {
      IUser user = UMFactory.getAuthenticator().getLoggedInUser();
      hasPermission = user.hasPermission(new ConnectorAccessPermission(permissionName, archSetName));
    }
    if(!hasPermission)
    {
      String errText = new StringBuilder("Missing permission \"").append(permissionName).append("\" for archiving set \"").append(archSetName).append("\"").toString();
      throw new RuntimeException(new ArchConnException(errText));
    }
  }
}
