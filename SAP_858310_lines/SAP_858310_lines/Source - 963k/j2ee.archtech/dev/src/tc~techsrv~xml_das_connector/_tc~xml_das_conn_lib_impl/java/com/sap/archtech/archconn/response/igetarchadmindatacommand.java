package com.sap.archtech.archconn.response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public interface IGetArchAdminDataCommand 
{
  public ArrayList<? extends Serializable> getArchAdminData() throws IOException;
}
