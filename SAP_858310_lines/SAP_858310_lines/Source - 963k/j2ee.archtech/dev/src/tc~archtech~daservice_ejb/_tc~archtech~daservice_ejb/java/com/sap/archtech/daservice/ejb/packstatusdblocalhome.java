package com.sap.archtech.daservice.ejb;

import javax.ejb.EJBLocalHome;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.sql.Timestamp;

public interface PackStatusDBLocalHome extends EJBLocalHome {

	public PackStatusDBLocal create() throws CreateException;

	public PackStatusDBLocal findByPrimaryKey(Long primKey)
			throws FinderException;

	public PackStatusDBLocal createMethod(long colid, Timestamp starttime,
			Timestamp lastupdate, int packres, int packedres, String message)
			throws CreateException;
}
