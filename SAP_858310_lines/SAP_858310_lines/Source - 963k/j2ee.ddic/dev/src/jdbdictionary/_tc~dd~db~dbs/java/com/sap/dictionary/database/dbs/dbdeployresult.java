/*
 * Created on 18.05.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.dictionary.database.dbs;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author d019347
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DbDeployResult implements DbsConstants,DbsSeverity{

	/**
	 * 
	 */
	private static final Location loc = 
		Location.getLocation(DbDeployResult.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private int result = SUCCESS;
	
	public DbDeployResult() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public int get() {
		return result;
	}
	
	public void set(int newResult) {
		if (result < newResult) {
			synchronized (this) {
				if (result < newResult) {
					result = newResult;
				}
			}
		}
	}
	
	public void log() {
		if (result == ERROR)
			cat.info(loc, MOD_FINISHWITH_ERR);
		else if (result == WARNING)
			cat.info(loc, MOD_FINISHWITH_WARN);
		else
			cat.info(loc, MOD_FINISHWITH_SUCC);
	}

}
