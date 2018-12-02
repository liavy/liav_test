package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbArtPrimaryKeyDifference extends DbPrimaryKeyDifference {

  public DbArtPrimaryKeyDifference(DbPrimaryKey origin,DbPrimaryKey target, 
                                Action action) {
    super(origin,target,action);
  }

  public DbObjectSqlStatements getDdlStatements(String tableName)
    throws JddException {return null;}

  public DbObjectSqlStatements getDdlStatements(String tableName,
                                                DbTable tableForStorageInfo)
    throws JddException {return null;}

}

