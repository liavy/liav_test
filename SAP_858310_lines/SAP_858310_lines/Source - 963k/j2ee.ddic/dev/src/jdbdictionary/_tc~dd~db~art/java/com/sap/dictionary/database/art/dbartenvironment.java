package com.sap.dictionary.database.art;

import java.sql.*;
import com.sap.dictionary.database.dbs.*;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbArtEnvironment extends DbEnvironment {
  Connection con = null;

	/**
	 * Constructor for DbArtEnvironment.
	 */
	public DbArtEnvironment() {
		super();
	}

  public DbArtEnvironment(Connection con) {
    this.con = con;
  }
}
