package com.sap.archtech.daservice.beanfacade;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface CommandBeanLocalHome extends EJBLocalHome {
	public CommandBeanLocal create() throws CreateException;
}
