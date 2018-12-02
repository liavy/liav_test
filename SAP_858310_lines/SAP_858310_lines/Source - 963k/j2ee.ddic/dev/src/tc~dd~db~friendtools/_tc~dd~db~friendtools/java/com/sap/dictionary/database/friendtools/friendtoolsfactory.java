package com.sap.dictionary.database.friendtools;

import java.sql.*;

import com.sap.dictionary.database.dbs.ICtNameTranslator;
import com.sap.dictionary.database.dbs.IDbDeployObjects;
import com.sap.dictionary.database.dbs.JddException;
/**
 * @author d003550
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class FriendToolsFactory {

	public static FriendTools getInstance(Connection con) throws JddException {
		FriendTools ft = new RuntimeTableFriendTools(con);
		return ft;
	}

	public static FriendTools getInstance(Connection con,
			ICtNameTranslator translator) throws JddException {
		FriendTools ft = new RuntimeTableFriendTools(con, translator);
		return ft;
	}

	public static FriendTools getInstance() throws JddException {
		FriendTools ft = new RuntimeTableFriendTools();
		return ft;
	}
}