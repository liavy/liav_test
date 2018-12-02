package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;
/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and Xml-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author A. Neufeld & T. Wenner
 * @version 1.0
 */

public class DbArtIndex extends DbIndex {

  public DbArtIndex() {super();}
  
  public DbArtIndex(DbFactory factory) {super(factory);}

  public DbArtIndex(DbFactory factory,DbIndex other) {super(factory,other);}
  
  public DbArtIndex(DbFactory factory, String tableName, String name) {
    super(factory,tableName,name);
  }

  public DbArtIndex(DbFactory factory,DbSchema schema,String tableName,
                    String name)
    {super(factory,schema,tableName,name);}

  public void setCommonContentViaDb () {}

  public void setSpecificContentViaDb () {}

  public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException {}

  public boolean existsOnDb() throws JddException {return false;}
  
  public String toString() {return "";}
}
