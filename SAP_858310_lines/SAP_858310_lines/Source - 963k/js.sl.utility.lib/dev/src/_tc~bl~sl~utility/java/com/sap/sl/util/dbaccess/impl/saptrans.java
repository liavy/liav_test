package com.sap.sl.util.dbaccess.impl;

/**
 * @author Uli Auer 2003
 * main class for using DBTask as a standalone tool for database export and import
 */

/**
 * main method for using DBTask as a standalone tool for database export and import
 */
final class SapTrans
{
	public static void main(String[] args)
	{
    SapTransTask task = new SapTransTask(args);
    task.execute();
	}
}


