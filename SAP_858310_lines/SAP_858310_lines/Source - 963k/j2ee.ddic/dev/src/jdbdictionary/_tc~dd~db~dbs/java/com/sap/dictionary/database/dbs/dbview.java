package com.sap.dictionary.database.dbs;

import com.sap.sql.services.OpenSQLServices;
import com.sap.tc.logging.*;

import java.sql.SQLException;
import java.util.*;
import java.io.PrintWriter;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class DbView implements DbsConstants {
   private DbFactory factory = null;
   private DbSchema schema = null;
   private String name = " ";
   private boolean isUpdatable = false;
   private boolean isGrouped = false;
   private String createStatement = null;
   private DbColumns columns = null;
   private ArrayList baseTableNames = new ArrayList();
   private boolean isCaseSensitive = false;
   private boolean columnsAreSet = false;
   private DbDeploymentInfo deploymentInfo = null;
   private DbDeploymentStatus deploymentStatus = null;
   private static final Location loc = Location.getLocation(DbTable.class);
   private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
					
   public DbView() {}

   public DbView(DbFactory factory) {
  	 this.factory = factory;
   }

   public DbView(DbFactory factory, String name) {
 	 this.factory = factory;
 	 this.name = name;
   }

   public DbView(DbFactory factory, DbView other) {
 	 this.factory = factory;
   }

   public DbView(DbFactory factory, DbSchema schema, String name) {
 	 this.factory = factory;
	 this.schema = schema;
	 this.name = name;
   }	

   public void setCommonContentViaXml(XmlMap xmlMap) throws JddException {
	 try {
  	   XmlMap view = xmlMap.getXmlMap("Dbview");
	   name = view.getString("name");
	   name = XmlHelper.checkAndGetName(name,"Dbview");
	   deploymentInfo = new DbDeploymentInfo(view,factory);
	   deploymentStatus = 
		 	DbDeploymentStatus.getInstance(view.getString("deployment-status"));
       String name = null;
	   isUpdatable = view.getBoolean("is-Updatable");
	   isGrouped = view.getBoolean("is-Grouped");
	   createStatement = view.getString("create-statement");
	   XmlMap names = view.getXmlMap("base-table-names");
	   String baseTableName = null;
	   for (int i = 0; !((name = names.getString("base-table-name" + 
						(i == 0 ? "" : "" + i))) == null); i++) {
	     if (!baseTableNames.contains(name))
	       baseTableNames.add(name);      
	   }						
	   setColumns(factory.makeDbColumns(view.getXmlMap("columns")));
	   if (columns.isEmpty()) {
			throw new JddException(ExType.XML_ERROR,
							 DbMsgHandler.get(RTXML_COLS_MISS));
	   }
	 }	
     catch (Exception ex) {
			throw JddException.createInstance(ex);
	 }		
   }
     
   public void setColumns(DbColumns columns) {
	 if (columns != null && columns.isEmpty()) {
	 	columns = null;
		return;
	 }
	 this.columns = columns;
	 //Set table as parent
	 if (columns != null) {
	   columns.setView(this);
	   columnsAreSet = true;
	 }  
   }

   public void setCommonContentViaDb() throws JddException {
	  try {
		  setColumnsViaDb();
		  setBasetableNamesViaDb();
		  setCreateStatementViaDb();
		  
	  } catch (Exception ex) {
		  throw JddException.createInstance(ex);
	  }
   }

   public void setColumnsViaDb() throws JddException {
	  try {
	    DbColumns cols = new DbColumns(factory);
	    cols.setView(this);
	    cols.setContentViaDb(factory);
	    setColumns(cols);
	  }
	  catch (Exception ex) {
	    throw JddException.createInstance(ex);
      } 
   }
 
   public DbFactory getDbFactory() {return factory;} 

   public DbSchema getSchema() {return schema;}
  
   public String getName() {return name;}
  
   public DbColumns getColumns() {return columns;}
  
   public String getCreateStatement() {return createStatement;}

   public void setIsUpdatable(boolean isUpdatable) {
     this.isUpdatable = isUpdatable;
   }

   public boolean isUpdatable() {return isUpdatable;}
  
   public boolean isGrouped() {return isGrouped;}

   public DbDeploymentInfo getDeploymentInfo() {
	  return deploymentInfo;}

   public DbDeploymentStatus getDeploymentStatus() {
	  return deploymentStatus;}

   public ArrayList getBaseTableNames() {return baseTableNames;}

  /**
   *  Analyses if view exists on database or not
   *  @return true - if table exists in database, false otherwise
   *  @exception JddException – error during analysis detected	 
   **/
   public abstract boolean existsOnDb() throws JddException;

  /**
   *  Gets the base table names of this view from database and sets it 
   *  for this view with method setBaseTableNames
   *  @exception JddException – error during analysis detected	 
   **/
   public void setBasetableNamesViaDb() throws JddException {}

  /**
   *  Sets the  basetable names of this view  
   *  @parameter names - the ArrayList of the basetable names 
   **/  
   public void setBaseTableNames(ArrayList names) {
     baseTableNames = names;
   }

  /**
	*  Gets the create statement of this view from the database and 
	*  sets it to this view with method setCreateStatement
	*  @exception JddException – error during detection detected	 
	**/  
   public void setCreateStatementViaDb() throws JddException {} 	 

  /**
   *  Sets the create statement of this view  
   *  @parameter names - the string with the crate statement
   **/
   public void setCreateStatement(String createStatement) {
     this.createStatement = createStatement;
   }

  /**
   *  Checks the view, database independent. The according 
   *  messages in case of failing tests are logged
   *  @return true - if no check fails, false otherwise
   * */
   public boolean checkDbIndependent() {
     return checkName() & checkColumnExists() & checkBasetablesExists();
   }
  
  /**
   *  Checks the view's name
   *  Name contains only characters A..Z 0..9 _
   *  First Character is of set A..Z
   *  Name contains one _ 
   *  Name length is checked from every database by method checkNameLength()
   * */
   boolean checkName() {
     return DbTools.checkName(name,true,true,true,false);
   }
  
  /**
   *  Checks if view has columns 
   *  @return true - if at least one column exists, false otherwise
   * */  
   boolean checkColumnExists() {
     if (columns == null || columns.isEmpty()) {
       cat.error(loc,TABLE_COLUMNS_ARE_MISSING);
       return false;
     }
     return true;
   }
   
   /**
    *  Checks if basetables exist 
    *  @return true - if at least one basetable exists, false otherwise
    * */  
    boolean checkBasetablesExists() {
      if (baseTableNames == null || baseTableNames.isEmpty()) {
        cat.error(loc,BASETABLES_ARE_MISSING);
        return false;
      }
      return true;
    }
  
    public DbObjectSqlStatements getDdlStatementsForCreate()
				   throws JddException {
	  DbObjectSqlStatements viewDef = new DbObjectSqlStatements(name);
	  DbSqlStatement createLine = new DbSqlStatement();
	  boolean doNotCreate = false;

	  if (deploymentInfo != null)
		doNotCreate = deploymentInfo.doNotCreate();
	  if (!doNotCreate) {
		  try {
			  createLine.addLine(createStatement);
			  viewDef.add(createLine);
			  return viewDef;
		  } catch (Exception ex) {
			  throw JddException.createInstance(ex);
		  }
	  }
	  return null;
    }

    public DbObjectSqlStatements getDdlStatementsForDrop() throws JddException {
	  DbObjectSqlStatements viewDef = new DbObjectSqlStatements(name);
	  DbSqlStatement dropLine = new DbSqlStatement(true);

	  try {
		  dropLine.addLine("DROP VIEW" + " " + "\"" + name + "\"");
		  viewDef.add(dropLine);
		  return viewDef;
	  } catch (Exception ex) {//$JL-EXC$
		  throw JddException.createInstance(ex);
	  }
    }

    public void writeCommonContentToXmlFile(PrintWriter file, String offset0)
				   throws JddException {

	  try {
		  file.println(offset0 + XmlHelper.xmlTitle);
		  file.println(offset0 + "<Dbview>"); 

		  String offset1 = offset0 + XmlHelper.tabulate();
		  String offset2 = offset1 + XmlHelper.tabulate();
		  file.println(offset1 + "<name>" + name + "</name>");
		  file.println(offset1 + "<is-Updatable>" + isUpdatable + "</is-Updatable>");
		  file.println(offset1 + "<is-Grouped>" + isGrouped + "</is-Grouped>");
		  file.println(offset1 + "<base-table-names>");
		  for (int i=0;i<baseTableNames.size();i++) {
			file.println(offset1 + "<base-table-name>" +
			      (String) baseTableNames.get(i) + "</base-table-name>");
		  }
		  file.println(offset1 + "</base-table-names>");
		  
		  if (deploymentInfo != null)
			  deploymentInfo.writeContentToXmlFile(file, offset1);

		  if (deploymentStatus != null)
			  file.println(offset1 + "<deployment-status>" +
							   deploymentStatus.getName() + "</deployment-status>");

		  getColumns().writeCommonContentToXmlFile(file, offset1);

		  file.println(offset0 + "</Dbview>");
	  } catch (Exception ex) {
		  throw JddException.createInstance(ex);
	  }
    }

    public String toString() {
	  String s = "View = " + name + "\n" + deploymentInfo + "deploymentStatus: "
						 + deploymentStatus + "\n" + "\n";
      for (int i=0;i<baseTableNames.size();i++) {
        if (i==0)
          s = s + "BaseTables" + "\n";	
        s = s + baseTableNames.get(i) + "\n" ;
      }	
      return s = s + "\n" + columns + "\n" + createStatement;
    }	
}
