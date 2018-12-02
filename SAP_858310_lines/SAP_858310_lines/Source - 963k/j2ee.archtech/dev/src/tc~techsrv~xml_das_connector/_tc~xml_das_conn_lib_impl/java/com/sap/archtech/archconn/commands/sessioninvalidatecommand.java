package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

public class SessionInvalidateCommand extends AbstractArchCommand
{
	protected SessionInvalidateCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "SESSIONINVALIDATE");
  }
}
