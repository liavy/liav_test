package com.sap.archtech.initservice;

class ActionsBuilderException extends Exception
{
  private static final long serialVersionUID = 42L;
  
  ActionsBuilderException(String excMsg)
  {
    super(excMsg);
  }
  
  ActionsBuilderException(String excMsg, Throwable t)
  {
    super(excMsg, t);
  }
  ActionsBuilderException(Throwable t)
  {
    super(t);
  }
}
