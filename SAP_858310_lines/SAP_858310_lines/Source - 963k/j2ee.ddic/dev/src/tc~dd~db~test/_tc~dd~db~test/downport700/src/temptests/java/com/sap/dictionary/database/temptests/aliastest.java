/*
 * Created on 15.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.dictionary.database.temptests;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.sap.dictionary.database.Examples.BasicTableVeri;
import com.sap.dictionary.database.dbs.Database;
import com.sap.dictionary.database.dbs.DbBasicTable;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.veris.VeriTools;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d019347
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AliasTest {
	private static final Location loc = Location.getLocation(AliasTest.class);
	private static final Category cat = 
					Category.getCategory(Logger.CATEGORY_NAME);

	/**
	 * 
	 */
	public AliasTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Logger.setLoggingConfiguration("default");
		Location.getLocation("com.sap.dictionary.database").setEffectiveSeverity(
			Severity.ERROR);
		try {
			ArrayList cnns = VeriTools.getTestConnections();
			if (exec(cnns, false))
				System.out.println("!!!!!!!!!!!!VERI  SUCCESS !!!!!!!!!!!!");
			else
				System.out.println("????????????VERI  ERRORS  ????????????");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean exec(ArrayList cnns) {
		return exec(cnns, true);
	}

	public static boolean exec(ArrayList cnns, boolean breakIfError) {
		boolean resultIsOk = true;
		for (int i = 0; i < cnns.size(); i++) {
			Connection con = (Connection) cnns.get(i);
			try {	
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ "
						 + Database.getDatabase(con).getAbbreviation() + 
													" $$$$$$$$$$$$$$$$$$$$$$$$$$$$");	
				DbFactory factory = new DbFactory(con);
				System.out.println("is alias " + factory.getTools().isAlias("BLABLA"));
			} catch (Exception e) {
				resultIsOk = false;
				e.printStackTrace();
			}

			if (!resultIsOk) {
				if (breakIfError)
					return false;
				System.out.println("????????????VERI  ERRORS  ????????????");
				System.out.println("============================================");
				System.out.println(" ");
				resultIsOk = false;
			} else {
				System.out.println("!!!!!!!!!!!!VERI  SUCCESS !!!!!!!!!!!!");
				System.out.println("============================================");
				System.out.println(" ");
			}

		}
		return resultIsOk;
	}
}
