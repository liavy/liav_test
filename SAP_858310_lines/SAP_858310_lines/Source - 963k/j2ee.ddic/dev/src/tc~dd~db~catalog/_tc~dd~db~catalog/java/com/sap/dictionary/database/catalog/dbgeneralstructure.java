package com.sap.dictionary.database.catalog;

import com.sap.sql.catalog.*;
import java.sql.*;
import java.util.*;
import java.io.*;

import com.sap.dictionary.database.dbs.*;

import java.net.URL;
import java.lang.ClassLoader;
import org.xml.sax.InputSource;
import java.math.BigDecimal;

/**
 * Ueberschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

public class DbGeneralStructure implements Table, DbsConstants, DbsSeverity {
  private String     name;
  private Connection connection = null;
  private DbTable    structure  = null;
  private DbView     view       = null;
  private DbColumns  dbColumns  = null;
  private LinkedList columns    = null;
  private DbPrimaryKey primaryKey = null;

  public DbGeneralStructure(String name,ClassLoader loader,DbFactory factory) 
                            throws JddException {
    String type = null;
  //Constructor to read from XML-file
    try {
      this.name = name;	
      String filename = name + ".gdbtable";
      InputStream stream = loader.getResourceAsStream(filename);
      if (stream == null) {
		filename = name + ".gdbview";
      	stream = loader.getResourceAsStream(filename); 
        if (stream == null)  //Table and View not found
          throw new JddException(ExType.NOT_ON_DB,
                   ErrTexts.concat(ErrTexts.get("014"),name));
        else view = factory.makeView(name);
      }
      else structure = factory.makeTable(name);
      XmlExtractor extractor = new XmlExtractor();
      XmlMap xmlMap     = null;
      InputSource source = new InputSource(stream);
      xmlMap            = extractor.map(source);
      if (xmlMap != null) {
      	if (structure != null) {
          structure.setCommonContentViaXml(xmlMap);
      	  dbColumns  = structure.getColumns();
          primaryKey = structure.getPrimaryKey();
      	}  
        else {
          view.setCommonContentViaXml(xmlMap);  
          dbColumns  = view.getColumns();
        }
        if (dbColumns == null)
          throw new JddException(ExType.XML_ERROR,
               ErrTexts.concat(ErrTexts.get("002"),name));
      }
      else
        throw new JddException(ExType.XML_ERROR,
               ErrTexts.concat(ErrTexts.get("002"),name));
    }
    catch (Exception ex) {//$JL-EXC$
      throw JddException.createInstance(ex);
    }
  }

  public DbGeneralStructure(String name,Connection con,String src,DbFactory factory) 
                    throws JddException {
  //Constructor to read from XML-file in database
    this.name       = name;
    this.connection = con;
    DbRuntimeObjects rtoTable = DbRuntimeObjects.getInstance(factory);
    if (rtoTable.getColumnCount() == 0)
      throw new JddException(ExType.XML_ERROR,ErrTexts.get("015"));
    String xml = rtoTable.getXmlAsString(name);
    if (xml == null || xml.equalsIgnoreCase("")) throw new JddException(ExType.NOT_ON_DB,
      ErrTexts.concat(ErrTexts.get("001"),name));
    StringReader stringReader = new StringReader(xml.trim());
    String type = rtoTable.getType(name);
    rtoTable.closeStatements();
    try {
      if (type.equalsIgnoreCase("T"))	
        structure = factory.makeTable(name);
      else if (type.equalsIgnoreCase("V"))	
        view = factory.makeView(name);  
      else   
        throw new JddException(ExType.NOT_ON_DB,
                          ErrTexts.concat(ErrTexts.get("013"),name));
    }
    catch (Exception ex) {
      throw new JddException(ExType.NOT_ON_DB,
                          ErrTexts.concat(ErrTexts.get("013"),name));
    }
    XmlExtractor extractor = new XmlExtractor();
    XmlMap xmlMap     = null;
    xmlMap            = extractor.map(new InputSource(stringReader));
    if (xmlMap != null) {
      if (type.equalsIgnoreCase("T")) {		
        structure.setCommonContentViaXml(xmlMap);
        dbColumns  = structure.getColumns();
        primaryKey = structure.getPrimaryKey();
      }
      else if (type.equalsIgnoreCase("V")) {  
        view.setCommonContentViaXml(xmlMap);
        dbColumns  = view.getColumns();
      }	
      if (dbColumns == null)
          throw new JddException(ExType.XML_ERROR,
               ErrTexts.concat(ErrTexts.get("002"),name));
    }
    else
      throw new JddException(ExType.XML_ERROR,
                         ErrTexts.concat(ErrTexts.get("002"),name));
  }

  public DbGeneralStructure(String name,Connection connection,DbFactory factory)
      throws JddException {
  //Constructor to use DB as datasource
    try {
      this.name       = name;
      this.connection = connection;
      structure = factory.makeTable(null,name);
      structure.setCommonContentViaDb(factory);
      dbColumns  = structure.getColumns();
      if (dbColumns == null)
        throw new JddException(ExType.NOT_ON_DB,"Table not found");
      primaryKey = structure.getPrimaryKey();
    }
    catch (Exception ex) {//$JL-EXC$
      throw JddException.createInstance(ex);
    }
  }

  protected DbColumns getDbColumns() {return dbColumns;}

  public String getName() {
  	if (structure != null) return structure.getName();
  	else if (view != null) return view.getName();
  	return null;
  }

  public String getSchemaName() {return null;}

  void setColumns() throws Exception {
    //structure.setColumnsViaDb(factory);
    //dbColumns = structure.getColumns();
    DbColumnIterator iter = dbColumns.iterator();
    columns = new LinkedList();
    while (iter.hasNext()) {
      columns.add(iter.next());
    }
  }

  public int getColumnCnt() {
    if (dbColumns != null) return dbColumns.getColumnCnt();
    else return 0;
    //try {setColumns();}
    //catch (Exception ex) {return 0;}
    //return dbColumns.getColumnCnt();
  }

  public ColumnIterator getColumns() {
    if (dbColumns != null) {
      DbColumnIterator iter = dbColumns.iterator();
      return mapToColumnIteratorInterface(iter,primaryKey);
    }
    else return null;
/*    try {
      setColumns();
      DbColumnIterator iter = dbColumns.iterator();
      return mapToColumnIteratorInterface(iter);
    }
    catch (Exception ex) {
      return null;
    } */
  }

  public Column getColumn(int position) {
    //if (dbColumns == null)
      //try {setColumns();}
      //catch (Exception ex) {ex.printStackTrace();return null;}
    if (dbColumns != null) {
       return mapToColumnInterface(dbColumns.getColumn(position),primaryKey);
    }
    else {return null;}
  }

  public Column getColumn(String name) {
    //if (dbColumns == null)
      //try {setColumns();}
      //catch (Exception ex) {return null;}
    if (dbColumns != null) {
      return mapToColumnInterface(dbColumns.getColumn(name),primaryKey);
    }
    else {return null;}
  }

  public int getPrimaryKeyCnt() {
  	if (null == primaryKey) {
       return 0;
  	}
    return primaryKey.getKeyCnt();
  }

  public Column getPrimaryKeyColumn(int position) {
  	if (primaryKey == null) return null;
    return getColumn(primaryKey.getKeyFieldName(position));
  }

  public int getBufferKeyCnt() {
  	if (!isBuffered()) return -1;
  	if (view != null)
  	  return view.getDeploymentInfo().getGenKeyCount();
  	else if (structure != null)
  	  return structure.getDeploymentInfo().getGenKeyCount();
    return -1;
  }  

  public int getTableType() {return 1;}

  private void save() {}

  public void saveToXmlFile(File file) {}

  public boolean isBuffered() {
  	if (structure != null) {
  	  if (structure.getDeploymentInfo() == null) return false;
  	  return structure.getDeploymentInfo().isBuffered();
  	}
  	else if (view != null) {
      if (view.getDeploymentInfo() == null) return false;
      return view.getDeploymentInfo().isBuffered();
   	}
  	return false;
  }

  public boolean hasLogging() {return false;}

  public boolean isClientDependent()  {return false;}

  public Column getClientColumn() {return null;}

  public Set getReferencedTableNames() {
  	if (view != null) 
      return new HashSet(view.getBaseTableNames());
  	else return null;
  }	
  
  public boolean isUpdatable() {
    if (view != null) return view.isUpdatable();
    if (structure != null) return true;
    return false;
  }	
  
  public boolean isGroupedView() {
    if (view != null) return view.isGrouped();
    return false;
  }	
  
  public boolean isView() {
  	if (view != null) return true;
  	return false;
  } 	
  
  public DbGeneralStructure getSavedInstance(String name) {
    return null;
  }

  //Anonymous inner class which implements Interface Column
  public Column mapToColumnInterface(final DbColumn dbColumn,
                                      final DbPrimaryKey dbPrimaryKey) {
    if (dbColumn == null) return null;
    return new Column() {
      public Table getTable() {return DbGeneralStructure.this;}
      public String getName() {return dbColumn.getName();}
      public String getTypeName() {return dbColumn.getJavaSqlTypeName();}
      public int getPosition() {return dbColumn.getPosition();}
      public int getPrimaryKeyPosition() {
      	if (primaryKey != null)
      	  return primaryKey.getKeyFieldPosition(dbColumn.getName());
        else return 0;
      }
      public boolean isPrimaryKey() {
      	if (primaryKey != null)
      	  return primaryKey.isPrimaryKeyField(dbColumn.getName());
      	else return false;
      }
      public int getJdbcType() {return dbColumn.getJavaSqlType();}
      public String getJdbcTypeName() {return dbColumn.getJavaSqlTypeName();}
      public long getSize() {return dbColumn.getLength();}
      public int getDecimals() {return dbColumn.getDecimals();}
      public boolean isNullable() {return !dbColumn.isNotNull();}
      public Object getDefault() {
      	Object defaultValue = null;
      	byte[] binary_array = new byte[256];
      	String defaultValue_as_string = dbColumn.getDefaultValue();
      	
      	if (defaultValue_as_string == null) return null;
      	
      	try {
          switch (getJdbcType())
	      {
	        case (java.sql.Types.VARCHAR):
	      	  defaultValue = defaultValue_as_string;
              break;
            case (java.sql.Types.LONGVARCHAR):
	      	  defaultValue = defaultValue_as_string;
              break;  
            case (java.sql.Types.CLOB):
              defaultValue = null;
		      break;   //no default value -> send null; 
            case (java.sql.Types.BINARY):
              byte[] b = new byte[256];
              int j = 0; int k = 0;
              char ch;
              StringBuffer s = new StringBuffer();
              int len = 0;
              while (j<defaultValue_as_string.length()) {
                ch = defaultValue_as_string.charAt(j);
                s = s.append(ch);
                len = s.length();
                len = len % 2;
                if (len == 0) {
                  b[k] = (byte) Integer.decode("0x" + s).intValue();
                  k = k + 1;
                  s = new StringBuffer();
                }
              }  
              defaultValue = b;
              break;
            case (java.sql.Types.VARBINARY):
          	  defaultValue = null;
		      break;   //no default value -> send null; 
            case (java.sql.Types.LONGVARBINARY):
          	  defaultValue = null;
		      break;   //no default value -> send null; 
            case (java.sql.Types.BLOB):
          	  defaultValue = null;
              break;   //no default value -> send null; 
            case (java.sql.Types.SMALLINT):
          	  defaultValue = new Short(defaultValue_as_string);
              break;
            case (java.sql.Types.INTEGER):
          	  defaultValue = new Integer(defaultValue_as_string);
              break;
            case (java.sql.Types.BIGINT):
          	  defaultValue = new Long(defaultValue_as_string);
              break;  
            case (java.sql.Types.DECIMAL):
          	  defaultValue = new BigDecimal(defaultValue_as_string);
              break;
            case (java.sql.Types.NUMERIC):
          	  defaultValue = new BigDecimal(defaultValue_as_string);
              break;            
            case (java.sql.Types.FLOAT):  
          	  defaultValue = new Double(defaultValue_as_string);
              break;  
            case (java.sql.Types.DOUBLE):  
          	  defaultValue = new Double(defaultValue_as_string);
              break; 
            case (java.sql.Types.REAL):  
          	  defaultValue = new Float(defaultValue_as_string);
              break;   
            case (java.sql.Types.TIME):
              if (defaultValue_as_string.equals("") || defaultValue_as_string == null)
          	    defaultValue = null;	
          	  else
          	    defaultValue = Time.valueOf(defaultValue_as_string);
              break;
            case (java.sql.Types.DATE):
          	  if (defaultValue_as_string.equals("") || defaultValue_as_string == null)
          	    defaultValue = null;	
          	  else
          	    defaultValue = java.sql.Date.valueOf(defaultValue_as_string);
              break;
            case (java.sql.Types.TIMESTAMP):
          	  if (defaultValue_as_string.equals("") || defaultValue_as_string == null)
          	    defaultValue = null;	
          	  else
          	    defaultValue = Timestamp.valueOf(defaultValue_as_string);
              break;
	      }
      	}
      	catch (Exception ex) {
      	  new JddRuntimeException(ExType.OTHER,ex,DBDEFAULT_DEFINITION_ERROR, 
      	  		new Object[] {getJdbcTypeName(),defaultValue_as_string},
				DbsSeverity.ERROR);
      	}
        return defaultValue;
      }
    };
  }

  //Anonymous inner class which implements Interface ColumnIterator
  public ColumnIterator mapToColumnIteratorInterface(final DbColumnIterator iterator,
                                                      final DbPrimaryKey primaryKey) {
    return new ColumnIterator() {
      public Column next() {return mapToColumnInterface(iterator.next(),primaryKey);}
      public boolean hasNext() {return iterator.hasNext();}
    };
  }
  
  //Information about predefinedAction: Inform editor wether table
  //is deleted
  public String getPredefinedAction() {
  	DbDeploymentInfo info = null;
  	if (structure != null)
      info = structure.getDeploymentInfo();
    else if (view != null)
      info = view.getDeploymentInfo(); 
  	if (info != null) { 
      Action action = info.getPredefinedAction();
      if (action != null) return action.toString();
    }   
    return "";   
  }	
}