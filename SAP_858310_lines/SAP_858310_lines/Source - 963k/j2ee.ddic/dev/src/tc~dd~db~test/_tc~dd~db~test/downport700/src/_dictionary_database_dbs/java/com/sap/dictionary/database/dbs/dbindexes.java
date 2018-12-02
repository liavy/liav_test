package com.sap.dictionary.database.dbs;

import java.util.*;
import java.io.*;
import com.sap.tc.logging.*;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbIndexes implements DbsConstants {
	private DbFactory factory = null;
	private final HashMap indexesViaName = new HashMap();
	private DbIndex previous = null;
	private DbIndex first = null;
	private DbTable dbTable = null;
	private static final Location loc = Location.getLocation(DbIndexes.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	public DbIndexes() {
	}

	public DbIndexes(DbFactory factory) {
		this.factory = factory;
	}

	public DbIndexes(DbFactory factory, DbIndexes other) {
		this.factory = factory;
	}

	public DbIndexes(DbFactory factory, XmlMap xmlMap) throws Exception {
		this.factory = factory;
		XmlMap nextIndexMap = null;
		DbIndex nextDbIndex = null;
		for (int i = 0; !(nextIndexMap = xmlMap.getXmlMap("index" +
				 (i == 0 ? "" : "" + i))).isEmpty(); i++) {
			//Create new index and add this index to the Position- and Name-HashMap
			nextDbIndex = factory.makeIndex();
			nextDbIndex.setCommonContentViaXml(nextIndexMap);
			add(nextDbIndex);
		}
	}

	public void add(DbIndex dbIndex) {
		dbIndex.setIndexes(this);
		if (previous == null) {
			first = dbIndex;
		} else {
			previous.setNext(dbIndex);
			dbIndex.setPrevious(previous);
		}
		previous = dbIndex;
		indexesViaName.put(dbIndex.getName(), dbIndex);
	}

	public DbIndex getFirst() {
		return first;
	}

	public DbIndex getIndex(String name) {
		return (DbIndex) indexesViaName.get(name);
	}

	public DbIndexIterator iterator() {
		return new DbIndexIterator(first);
	}

	public String toString() {
		String s = "";
		DbIndexIterator iterator = new DbIndexIterator(first);
		while (iterator.hasNext()) {
			s = s + iterator.next();
		}
		return s;
	}

	void writeCommonContentToXmlFile(PrintWriter file, String offset0)
																											 throws Exception {

		//begin indexes-element
		file.println(offset0 + "<indexes>");

		DbIndexIterator iterator = new DbIndexIterator(first);
		while (iterator.hasNext()) {
			iterator.next().writeCommonContentToXmlFile(file, offset0 +
																									 XmlHelper.tabulate());
		}

		//end column-element
		file.println(offset0 + "</indexes>");
	}

	public DbObjectSqlStatements getDdlStatementsForCreate() throws Exception {
		boolean doNotCreate = false;
		DbObjectSqlStatements indexesDef = new DbObjectSqlStatements("");
		DbIndexIterator iterator = iterator();
		DbDeploymentInfo info = null;
		DbIndex index = null;

		while (iterator.hasNext()) {
			index = (DbIndex) iterator.next();
			doNotCreate = index.getDeploymentInfo().doNotCreate();
			if (doNotCreate)
				cat.info(loc, INDEX_CREATE_FORBID, new Object[]
														 { index.getName(), factory.getDatabaseName()});
			if (!doNotCreate)
				indexesDef.merge(index.getDdlStatementsForCreate());
		}
		return indexesDef;
	}

	public DbObjectSqlStatements getDdlStatementsForDrop() throws Exception {
		DbObjectSqlStatements indexesDef = new DbObjectSqlStatements("");
		DbIndexIterator iterator = iterator();

		while (iterator.hasNext()) {
			indexesDef.merge(((DbIndex) iterator.next()).getDdlStatementsForDrop());
		}
		return indexesDef;
	}

	DbIndexesDifference compareTo(DbIndexes target) throws Exception {
		DbIndexIterator iterator = iterator();
		Object obj;
		DbIndexesDifference indexesDiff = new DbIndexesDifference(this, target);
		DbIndexDifference indexDiff = null;
		DbIndex origin;
		boolean doNotCreate = false;
		boolean deleteIfExisting = false;

		if (target == null)
			return indexesDiff;
		HashMap targetIndexesViaName = (HashMap) target.indexesViaName.clone();
		while (iterator.hasNext()) {
			doNotCreate = false;
		  deleteIfExisting = false;
			origin = iterator.next();
			//Get Deployment info of target index
			if (target != null) {
				DbIndex targetIndex = target.getIndex(origin.getName());
				if (targetIndex != null) {
					DbDeploymentInfo info = targetIndex.getDeploymentInfo();
					doNotCreate = info.doNotCreate();
					deleteIfExisting = info.deleteIfExisting();
				}
			}
			//Get target index
			obj = targetIndexesViaName.remove(origin.getName()); //If index exists
			//in target indexes obj will be filled after remove
			if (obj == null) {
				//Index deleted in target
				indexDiff = factory.makeDbIndexDifference(origin, null, Action.DROP);
				indexesDiff.add(indexDiff);
			} else {
				if (doNotCreate) {
					//Index should not exist but has already been created in destination-
					//database. If index should be deleted we do this, changes for index
					//are ignored
					if (deleteIfExisting) {
						indexDiff = factory.makeDbIndexDifference(origin, null,
																															 Action.DROP);
						indexesDiff.add(indexDiff);
					}
				} else {
					indexDiff = origin.compareTo((DbIndex) obj);
					if (indexDiff != null) { //Can be null if nothing is to do
						indexesDiff.add(indexDiff); //DROP_CREATE or ALTER can be possible action
					}
				}
			}
		} //While
		if (!targetIndexesViaName.isEmpty()) {
			//Handle new indexes
			Collection collection = targetIndexesViaName.values();
			Iterator iter = collection.iterator();
			DbIndex nextIndex;
			while (iter.hasNext()) {
				nextIndex = (DbIndex) iter.next();
				DbDeploymentInfo info = nextIndex.getDeploymentInfo();
				doNotCreate = info.doNotCreate();
				if (!doNotCreate) {
					indexDiff = factory.makeDbIndexDifference(null, nextIndex,
																														 Action.CREATE);
					indexesDiff.add(indexDiff);
				}
			}
		}
		if (indexesDiff.isEmpty())
			indexesDiff = null;
		return indexesDiff;
	}

	public void setTable(DbTable dbTable) {
		this.dbTable = dbTable;
	}

	protected void setTableName(String name) {
	  DbIndexIterator iterator = new DbIndexIterator(first);
	  while (iterator.hasNext()) {
		((DbIndex) iterator.next()).setTableName(name);
	  }		
	}
	
	public DbTable getTable() {
		return dbTable;
	}

	public boolean isEmpty() {
		return indexesViaName.isEmpty();
	}

	public int size() {
	  int number = 0;
	  if (indexesViaName != null)
	    number = indexesViaName.size();
	  return number;
	}
	
	public boolean check() {
		boolean ok = true;
		DbIndexIterator iterator = new DbIndexIterator(first);
		while (iterator.hasNext()) {
			ok = ok & ((DbIndex) iterator.next()).check();
		}
		return ok && checkNumber() && checkSpecificContent();
	}

    /**
     *  Checks the index, database independent. The according 
     *  messages in case of failing tests are logged
     *  @return true - if no check fails, false otherwise
     * */
	 boolean checkDbIndependent() {
       boolean ok = true;
       DbIndexIterator iterator = new DbIndexIterator(first);
       while (iterator.hasNext()) {
           ok = ok & ((DbIndex) iterator.next()).checkDbIndependent();
       }
       return ok & checkIndexesAreAllDifferent();
	 }
	
	/**
	*  Checks if number of indexes maintained is allowed
	*  @return true if number of indexes is o.k, false otherwise
	* */
	public boolean checkNumber() {
		return true;
	}
    
    /**
     *  Checks the db-specific parameters between different indexes 
     *  @return true db specific parameters are o.k., false otherwise
     **/
    public boolean checkSpecificContent() {
      return true;
    }

    /**
     *  Checks if there are at least two identical indexes 
     *  @return true - if no two indexes are identical or contain each other,
     *                     false otherwise
     * */  
     public boolean checkIndexesAreAllDifferent() {
        Set cmpReady = indexesViaName.keySet();
        DbIndexIterator iter = iterator();      
        boolean different = true;
        while (iter.hasNext()) {
          DbIndex index1 = iter.next();
          String index1Name = index1.getName();
          DbIndexIterator iter2 = iterator();
          while (iter2.hasNext()) {
            DbIndex index2 = iter2.next();
            String index2Name = index2.getName();
            if (!index1Name.equals(index2Name) && cmpReady.contains(index2Name)) {    
              if (index1.columnsAreEqual((ArrayList)index2.getColumnNames(),index2.getName()))
                different = false;
            }  
          }  
          cmpReady.remove(index1Name);  
        }
        return different;
     }

}
