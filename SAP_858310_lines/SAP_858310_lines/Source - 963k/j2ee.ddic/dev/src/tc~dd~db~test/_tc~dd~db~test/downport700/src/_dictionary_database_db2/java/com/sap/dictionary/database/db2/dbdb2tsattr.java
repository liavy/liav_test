package com.sap.dictionary.database.db2;

import java.util.LinkedList;
import java.util.ListIterator;

import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title: Analysis of table and view changes: DB2/390 specific classes
 * Description: DB2/390 specific analysis of table and view changes. Tool to
 * deliver Db2/390 specific database information. Copyright: Copyright (c) 2001
 * Company: SAP AG
 * 
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2TsAttr {

	private DbDb2Table table;

	private Double pageSizeFactor;

	private Boolean partitioned;

	private Boolean lob;

	private ListIterator partsIterator;

	private LinkedList keyColNames;

	private ListIterator keyColNameIterator;

	private Boolean memberCluster;

	private Integer dsSize;

	private Boolean log;

	private Boolean close;

	private String bufferPool;

	private String lockRule;

	private Integer lockMax;

	private Integer pageSize;

	private Integer segSize;

	private Integer maxRows;

	private LinkedList partsAttr;

	private DbDb2PartAttr partAttr;

	private Boolean erase;

	private Location loc = Logger.getLocation("db2.DbDb2TsAttr");

	private static Category cat = Category.getCategory(Category.SYS_DATABASE,
			Logger.CATEGORY_NAME);

	public DbDb2TsAttr() {
	}

	public void setTable(DbDb2Table table) {

		this.table = table;
	}

	public DbDb2PartAttr setNextPart() throws JddException {

		loc.entering("setNextPart");

		if (partitioned == null) {

			cat.errorT(loc,
					"know nothing about partitioning - wrong call sequence");
			loc.exiting();
			throw new JddException(
					"know nothing about partitioning - wrong call sequence");
		}

		partAttr = new DbDb2PartAttr();

		if (partitioned.booleanValue()) {

			if (partsAttr == null) {

				partsAttr = new LinkedList();
			}

			partsAttr.add(partAttr);
		}

		loc.exiting();
		return partAttr;
	}

	public void setPartitioned(Boolean partitioned) {

		this.partitioned = partitioned;
	}

	public void changePartitioned(Boolean partitioned) {

		if (this.partitioned == null) {

			setPartitioned(partitioned);
		} else {
			if (this.partitioned.booleanValue() == partitioned.booleanValue()) {
				return;
			} else {
				this.partitioned = partitioned;
				if (partitioned.booleanValue() == false) {
					partsAttr = null;
					keyColNames = null;
				} else {
					partsAttr = new LinkedList();
					partsAttr.add(partAttr);
				}

			}
		}
	}

	public void setKeyColName(String keyColName) {

		if (keyColNames == null) {

			keyColNames = new LinkedList();
		}

		keyColNames.add(keyColName);
	}

	public void setMemberCluster(String memberCluster) {

		if (memberCluster != null) {

			if (memberCluster.equalsIgnoreCase("NO")) {

				this.memberCluster = new Boolean(false);
			} else {

				this.memberCluster = new Boolean(true);
			}
		}
	}

	public void setDsSize(Integer dsSize) {

		if (segSize != null) {

			// not allowed -> throw exception
		} else {

			this.dsSize = dsSize;
		}
	}

	public void setLob(Boolean lob) {

		if (memberCluster.booleanValue()) {

			// not allowed -> throw exception
		} else {

			this.lob = lob;
		}
	}

	public void setLog(String log) {

		if (log != null) {

			if (log.equalsIgnoreCase("NO")) {

				this.log = new Boolean(false);
			} else {

				this.log = new Boolean(true);
			}
		}
	}

	public void setClose(String close) {

		if (close != null) {

			if (close.equalsIgnoreCase("NO")) {

				this.close = new Boolean(false);
			} else {

				this.close = new Boolean(true);
			}
		}
	}

	public void setBufferPool(String bufferPool) {

		if (bufferPool != null) {

			this.bufferPool = bufferPool.toUpperCase();
		}
	}

	public void setLockRule(String lockRule) {

		if (lockRule != null) {

			this.lockRule = lockRule.toUpperCase();
		}
	}

	public void setLockMax(Integer lockMax) {

		this.lockMax = lockMax;
	}

	public void setPageSize(Integer pageSize) {

		this.pageSize = pageSize;

		if (pageSize != null) {

			if ((pageSize.intValue() != DbDb2Parameters.PAGESIZE_4K)
					|| (pageSize.intValue() != DbDb2Parameters.PAGESIZE_8K)
					|| (pageSize.intValue() != DbDb2Parameters.PAGESIZE_16K)
					|| (pageSize.intValue() != DbDb2Parameters.PAGESIZE_32K)) {
				// no valid pagesize -> throw exception
			}
		}
	}

	public void setSegSize(Integer segSize) {

		if (((lob != null) && (lob.booleanValue() == true))
				|| ((partitioned != null) && (partitioned.booleanValue() == true))
				|| (dsSize != null)) {

			// not allowed -> throw exception
		} else {

			this.segSize = segSize;
		}
	}

	public void setMaxRows(Integer maxRows) {

		this.maxRows = maxRows;
	}

	public String getMemberCluster() {

		if (memberCluster == null) {
			return null;
		}

		if (memberCluster.booleanValue()) {

			return "MEMBER CLUSTER";
		}

		return null;
	}

	public void setPageSizeFactor(double factor) {

		pageSizeFactor = new Double(factor);
	}

	public void setErase(String eraseString) {

		Boolean eraseBoolean = null;

		if (eraseString == null) {

			eraseBoolean = new Boolean(false);
		} else {

			eraseBoolean = (eraseString.equalsIgnoreCase("YES")) ? new Boolean(
					true) : new Boolean(false);
		}

		erase = eraseBoolean;
	}

	public DbDb2Table getTable() {

		return table;
	}

	public int getNumParts() {

		if (partsAttr == null) {

			return DbDb2Parameters.DEFAULT_NUMPARTS;
		}

		return partsAttr.size();
	}

	public int getDsSize() {

		if (dsSize == null) {

			return DbDb2Parameters.DEFAULT_DSSIZE;
		}

		return dsSize.intValue();
	}

	public String getLog() {

		if (log == null) {

			return DbDb2Parameters.DEFAULT_LOG;
		}

		if (log.booleanValue()) {

			return "YES";
		} else {

			return "NO";
		}
	}

	public String getClose() {

		if (close == null) {

			return DbDb2Parameters.DEFAULT_CLOSE;
		}

		if (close.booleanValue()) {

			return "YES";
		} else {

			return "NO";
		}
	}
	
	public String getBufferPool() {

		if (bufferPool == null) {			
			bufferPool = DbDb2Environment.getBufferPoolFromPageSize(getPageSize());			
		}

		return bufferPool;
	}
	
	// for certain operations like drop/recreate 
	// it is mandatory to recalculate the pagesize and the bufferpool
	// as the table might need to be created in a larger bufferpool
	public void reCalculateBufferPool() {
		this.pageSize = null;
		this.bufferPool = null;
		getPageSize();
		getBufferPool();
	}
	
	public String getLockRule() {

		if (lockRule == null) {

			return DbDb2Parameters.DEFAULT_LOCKRULE;
		}

		return lockRule;
	}

	public int getLockMax() {

		if (lockMax == null) {

			return DbDb2Parameters.DEFAULT_LOCKMAX;
		}

		return lockMax.intValue();
	}

	public int getPageSize() {

		if (pageSize == null) {

			double factor = 0.0;
			int rowLength = DbDb2Environment.getRowLength(table.getColumns());
			int size = 0;

			if (pageSizeFactor == null) {

				factor = DbDb2Parameters.DEFAULT_PAGESIZE_FACTOR;
			} else {

				factor = pageSizeFactor.doubleValue();
			}

			if ((rowLength + 3) < (int) (4038 * factor)) {

				size = 4;
			} else if ((rowLength + 1) < (int) (8000 * factor)) {

				size = 8;
			} else if ((rowLength + 1) < (int) (16000 * factor)) {

				size = 16;
			} else {

				size = 32;
			}
			pageSize = new Integer(size);
		}

		return pageSize.intValue();
	}

	public Double getPageSizeFactor() {
		return pageSizeFactor;
	}

	public int getSegSize() {

		if (segSize == null) {

			int mySegSize = 4 + 8 * DbDb2Parameters.getSizeCategory(table);
			if (mySegSize > DbDb2Parameters.MAX_SEGSIZE) {

				mySegSize = DbDb2Parameters.MAX_SEGSIZE;
			}

			return mySegSize;
		}

		return segSize.intValue();
	}

	public int getMaxRows() {

		if (maxRows == null) {

			return DbDb2Parameters.DEFAULT_MAXROWS;
		}

		return maxRows.intValue();
	}

	public DbDb2PartAttr getFirstPart() {

		if (partitioned.booleanValue() == false) {

			return partAttr;
		} else {

			partsIterator = partsAttr.listIterator();

			return getNextPart();
		}
	}

	public DbDb2PartAttr getNextPart() {

		if (partsIterator.hasNext()) {

			return (DbDb2PartAttr) partsIterator.next();
		} else {

			return null;
		}
	}

	public boolean getPartitioned() {

		if (partitioned == null) {

			return false;
		}

		return partitioned.booleanValue();
	}

	public String getKeyColNames() {

		String concatKeyColNames = null;
		ListIterator iterator = keyColNames.listIterator();

		while (iterator.hasNext()) {

			if (concatKeyColNames == null) {

				concatKeyColNames = (String) iterator.next();
				;
			} else {

				concatKeyColNames += ", " + (String) iterator.next();
				;
			}
		}

		return concatKeyColNames;
	}

	public String getFirstKeyColName() {

		keyColNameIterator = keyColNames.listIterator();

		return getNextKeyColName();
	}

	public String getNextKeyColName() {

		if (keyColNameIterator.hasNext()) {

			return (String) keyColNameIterator.next();
		} else {

			return null;
		}
	}

	public String getErase() {

		if (erase == null) {

			return DbDb2Parameters.DEFAULT_ERASE;
		}

		if (erase.booleanValue()) {

			return "YES";

		} else {

			return "NO";

		}
	}

	public void replace(DbDb2TsAttr cloneFrom) {
		boolean fexp = false;
		// remove old partioning if any
		changePartitioned(new Boolean(false));

		if (cloneFrom.getPartitioned()) {
			// partitioned table space
			this.setPartitioned(new Boolean(true));

			// set Key Col names
			String keyColName = cloneFrom.getFirstKeyColName();
			while (keyColName != null) {
				this.setKeyColName(keyColName);
				keyColName = cloneFrom.getNextKeyColName();
			}

			// now set parts
			DbDb2PartAttr clonePartAttr = cloneFrom.getFirstPart();
			DbDb2PartAttr partAttr = null;

			while (clonePartAttr != null) {
				try {
					this.setNextPart();
				} catch (Exception e) {
					fexp = true; // avoid lint errors
				}
				this.partAttr.replace(clonePartAttr);
				this.partAttr.setTsAttr(this);
				clonePartAttr = cloneFrom.getNextPart();
			}
		} else {
			// non partitioned table
			this.setPartitioned(new Boolean(false));
			try {
				this.setNextPart();
			} catch (Exception e) {
				fexp = true; // avoid lint errors
			}

		}

		this.setDsSize( new Integer( cloneFrom.getDsSize() ) );
		this.setLog(cloneFrom.getLog());
		this.setClose(cloneFrom.getClose());
		// this.setBufferPool(cloneFrom.getBufferPool());
		this.setLockMax( new Integer( cloneFrom.getLockMax() ) );
		this.setSegSize( new Integer( cloneFrom.getSegSize() ) );
		// this.setPageSize( new Integer( cloneFrom.getPageSize() ) );
		this.setMaxRows( new Integer( cloneFrom.getMaxRows() ) );
		this.setMemberCluster(cloneFrom.getMemberCluster());
		// this.setPageSizeFactor(cloneFrom.getPageSize());
		this.setErase(cloneFrom.getErase());
	}

	public boolean equalsSpecificContent(DbDb2TsAttr compareTo) {
		loc.entering("equalsSpecificContent");
		boolean equals = true;

		if (compareTo.getPartitioned() != this.getPartitioned()) {
			Object[] arguments = { table.getName(),
					(this.getPartitioned() ? "" : "NOT "),
					(compareTo.getPartitioned() ? "" : "NOT ") };
			cat
					.infoT(
							loc,
							"equalsSpecificContent ({0}): original is {1}partitioned, target is {2}partitioned",
							arguments);
			equals = false;
		}

		if (this.getPartitioned() && compareTo.getPartitioned()) {
			// compare key col names
			String targetKeyColName = compareTo.getFirstKeyColName();
			String keyColName = this.getFirstKeyColName();
			while ((keyColName != null) || (targetKeyColName != null)) {
				if (((keyColName == null) && (targetKeyColName != null))
						|| ((keyColName != null) && (targetKeyColName == null))
						|| (0 != keyColName.compareTo(targetKeyColName))) {
					Object[] arguments = {
							table.getName(),
							(keyColName == null ? "UNDEFINED" : keyColName),
							(targetKeyColName == null ? "UNDEFINED"
									: targetKeyColName) };
					cat
							.infoT(
									loc,
									"equalsSpecificContent ({0}): original key column name {1}, target key column name {2}",
									arguments);
					equals = false;
					break; // break loop
				}
				targetKeyColName = compareTo.getNextKeyColName();
				keyColName = this.getNextKeyColName();
			}

			// now compare parts
			DbDb2PartAttr targetPartAttr = compareTo.getFirstPart();
			DbDb2PartAttr partAttr = this.getFirstPart();
			int pi = 0;
			while ((targetPartAttr != null) || (partAttr != null)) {
				pi++;
				if (((partAttr == null) && (targetPartAttr != null))
						|| ((partAttr != null) && (targetPartAttr == null))
						|| (!partAttr.compare(targetPartAttr))) {
					Object[] arguments = { table.getName(),
							( partAttr == null ? "UNDEFINED" : (new Integer(pi)).toString() ),
							(targetPartAttr == null ? "UNDEFINED" : (new Integer(pi)).toString() ) };
					cat
							.infoT(
									loc,
									"equalsSpecificContent ({0}): original partition {1} differs from target partition {2}",
									arguments);
					equals = false;
					break; // break loop
				}
				targetPartAttr = compareTo.getNextPart();
				partAttr = this.getNextPart();
			}
		}

		/*
		 * if (this.getDsSize() != compareTo.getDsSize()) {
		 * logEntryFrorCompare("DSSize", (new Integer(this.getDsSize()))
		 * .toString(), (new Integer(compareTo.getDsSize())) .toString());
		 * equals = false; }
		 */

		/*
		 * if ( 0 != this.getLog().compareTo(compareTo.getLog())) {
		 * logEntryFrorCompare("LOG", this.getLog(), compareTo.getLog()); equals =
		 * false; }
		 * 
		 * if ( 0 != this.getClose().compareTo(compareTo.getClose())) {
		 * logEntryFrorCompare("CLOSE", this.getClose(), compareTo.getClose());
		 * equals = false; }
		 * 
		 * if ( 0 != this.getBufferPool().compareTo(compareTo.getBufferPool())) {
		 * logEntryFrorCompare("Bufferpool", this.getBufferPool(), compareTo
		 * .getBufferPool()); equals = false; }
		 * 
		 * if (this.getLockMax() != compareTo.getLockMax()) {
		 * logEntryFrorCompare("Lock Max", (new Integer(this.getLockMax()))
		 * .toString(), (new Integer(compareTo.getLockMax())) .toString());
		 * equals = false; }
		 */

		/*
		 * if (this.getSegSize() != compareTo.getSegSize()) {
		 * logEntryFrorCompare("Segsize", (new Integer(this.getSegSize()))
		 * .toString(), (new Integer(compareTo.getSegSize())) .toString());
		 * equals = false; }
		 */

		/*
		 * if (this.getPageSize() != compareTo.getPageSize()) {
		 * logEntryFrorCompare("Pagesize", (new Integer(this.getPageSize()))
		 * .toString(), (new Integer(compareTo.getPageSize())) .toString());
		 * equals = false; }
		 * 
		 * if (this.getMaxRows() != compareTo.getMaxRows()) {
		 * logEntryFrorCompare("Max Rows", (new Integer(this.getMaxRows()))
		 * .toString(), (new Integer(compareTo.getMaxRows())) .toString());
		 * equals = false; }
		 * 
		 * if ( 0 != this.getErase().compareTo(compareTo.getErase())) {
		 * logEntryFrorCompare("Erase", this.getErase(), compareTo.getErase());
		 * equals = false; }
		 */
		loc.exiting();
		return equals;

	}

	private void logEntryFrorCompare(String attr, String value1, String value2) {
		Object[] arguments = { table.getName(), value1, value2 };
		cat.infoT(loc, "equalsSpecificContent ({0}): original " + attr
				+ " {1}, target " + attr + " {2}", arguments);
	}

	public String getKeyNames() {
		String keyColNames = null;
		String keyColName = this.getFirstKeyColName();
		while (keyColName != null) {
			if (keyColNames == null)
				keyColNames = "";
			else
				keyColNames += ", ";
			keyColNames += keyColName;
			keyColName = this.getNextKeyColName();
		}
		return keyColNames;
	}
}
