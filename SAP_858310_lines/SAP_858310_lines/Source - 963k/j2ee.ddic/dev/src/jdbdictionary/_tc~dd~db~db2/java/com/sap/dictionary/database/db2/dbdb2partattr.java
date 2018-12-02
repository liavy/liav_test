package com.sap.dictionary.database.db2;

import java.util.LinkedList;
import java.util.ListIterator;

import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title:        Analysis of table and view changes: DB2/390 specific classes
 * Description:  DB2/390 specific analysis of table and view changes. Tool to deliver Db2/390 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2PartAttr {

	private DbDb2TsAttr tsAttr;
	
	// using block
	private Integer priQty;
	private Integer secQty;
	
	// free block
	private Integer pctFree;
	private Integer freePage; 
	
	private Boolean define;
	private String gbpCache;
	private LinkedList keyColValues;
	private ListIterator keyColValueIterator;
	private Boolean trackMod;
	private Boolean compress;
	private Location loc = Logger.getLocation("db2.DbDb2PartAttr");
	private static Category cat = Category.getCategory(Category.SYS_DATABASE,
			Logger.CATEGORY_NAME);
	
	public DbDb2PartAttr () {
	}
	
	public void setTsAttr ( DbDb2TsAttr tsAttr ) {
		
		this.tsAttr = tsAttr;
	}
	
	public void setPriQty ( Integer priQty ) {
		
		this.priQty = priQty;
	}
	
	public void setSecQty ( Integer secQty ) {
		
		this.secQty = secQty;
	}
	
	public void setPctFree ( Integer pctFree ) {
		
		this.pctFree = pctFree;
	}
	
	public void setFreePage ( Integer freePage ) {
		
		this.freePage = freePage;
	}
	
	public void setDefine ( String define ) {
		
		if ( define != null ) {
			
			if ( define.equalsIgnoreCase("YES") ) {
				
				this.define = new Boolean(true);
			} else {
				
				this.define = new Boolean(false);
			}
		}
	}
	
	
	public void setGbpCache ( String gbpCache ) {
		if (gbpCache != null )
		  this.gbpCache = gbpCache.toUpperCase();
	}
	
	public void setKeyColValue ( String keyColValue ) {
	
		if ( keyColValues == null ) {
			
			keyColValues = new LinkedList();
		} 
			
		keyColValues.add(keyColValue);
	}
	
	public void setTrackMod ( String trackModString ) {
		
		Boolean trackModBoolean = null;
		
		if ( trackModString == null ) {
			
			trackModBoolean = new Boolean ( false );
		} else {
		
			trackModBoolean = ( trackModString.equalsIgnoreCase("YES") ) ? new Boolean ( true ) 
										        				   		 : new Boolean ( false );
		}
														        				   		 
		trackMod = trackModBoolean;
	}
	
	public void setCompress ( String compressString ) {
		
		Boolean compressBoolean = null;
		
		if ( compressString == null ) {
			
			compressBoolean = new Boolean ( false );
		} else {
		
			compressBoolean = ( compressString.equalsIgnoreCase("YES") ) ? new Boolean ( true ) 
										        				   		 : new Boolean ( false );
		}
														        				   		 
		compress = compressBoolean;
	}
	
	public int getPriQty () {
		
		if ( priQty == null ) {
				
			if ( tsAttr != null ) {
				
				return tsAttr.getPageSize() * (2 * tsAttr.getSegSize() + 2);
			} else {
				
				return DbDb2Parameters.DEFAULT_PRIQTY;
			}
		}
		
		return priQty.intValue();
	}
	
	public int getSecQty () {
		
		if ( secQty == null ) {
				
			if ( tsAttr != null ) {
				
				return DbDb2Parameters.getSecQty( DbDb2Parameters.getSizeCategory(tsAttr.getTable()) );
			} else {
				
				return DbDb2Parameters.DEFAULT_SECQTY;
			}
		}
		
		return secQty.intValue();
	}
	
	
	public int getPctFree () {
		
		if ( pctFree == null ) {
			
			return DbDb2Parameters.DEFAULT_PCTFREE;
		}
		
		return pctFree.intValue();
	}
	
	public int getFreePage () {
		
		if ( pctFree == null ) {
			
			return DbDb2Parameters.DEFAULT_FREEPAGE;
		}
		
		return freePage.intValue();
	}
	
	public String getDefine () {

		if ( define == null ) {
			
			return DbDb2Parameters.DEFAULT_DEFINE;
		}
				
		if ( define.booleanValue() ) {
			
			return "YES";
		} else {
			
			return "NO";
		}
	}
	
	
	public String getGbpCache () {
		
		if ( gbpCache == null ) {
			
			return DbDb2Parameters.DEFAULT_GBPCACHE;	
		}
		
		return gbpCache;
	}
	
	public String getTrackMod () {
	
		if ( trackMod == null ) {
			
			return DbDb2Parameters.DEFAULT_TRACKMOD;
		}
		
		if ( trackMod.booleanValue() ) {
			
			return "YES";
			
		} else {
			
			return "NO";
			
		}
	}		
	
	public String getCompress () {
	
		if ( compress == null ) {
			
			return DbDb2Parameters.DEFAULT_COMPRESS;
		}
			
		if ( compress.booleanValue() ) {
			
			return "YES";
			
		} else {
			
			return "NO";
			
		}
	}		
	
	public String getKeyColValues () {
		
		String concatKeyColValues = null;
		ListIterator iterator = keyColValues.listIterator( );
		
		while ( iterator.hasNext() ) {

			if ( concatKeyColValues == null ) {
				
				concatKeyColValues = (String)iterator.next();
			} else {
				
				concatKeyColValues += ", " + (String)iterator.next();
			}
		}
		return concatKeyColValues;
	}
	
	public String getFirstKeyColValue () {
		if ( keyColValues == null)
			return null;
		keyColValueIterator = keyColValues.listIterator();
		
		return getNextKeyColValue ();
	}
	
	public String getNextKeyColValue () {
		
		if ( keyColValueIterator.hasNext() ) {
			
			return (String)keyColValueIterator.next();
		} else {
			
			return null;
		}
	}
	
	public void replace(DbDb2PartAttr cloneFrom) {
	    // rest list	
		keyColValues = null;
		
        // set key col names
		String keyColValue = cloneFrom.getFirstKeyColValue();
		while ( keyColValue != null ) {
			this.setKeyColValue ( keyColValue );
			keyColValue =  cloneFrom.getNextKeyColValue();
		}
		
		// set other parameters
		this.setPriQty( new Integer( cloneFrom.getPriQty() ) ); 
		this.setSecQty ( new Integer( cloneFrom.getSecQty () ) ); 
		this.setPctFree( new Integer( cloneFrom.getPctFree() ) ); 
		this.setFreePage ( new Integer( cloneFrom.getFreePage() ) ); 
		this.setDefine( cloneFrom.getDefine() ); 
		this.setGbpCache( cloneFrom.getGbpCache() ); 
		this.setTrackMod( cloneFrom.getTrackMod() ); 
		this.setCompress( cloneFrom.getCompress() ); 		 
	}
	
	public boolean compare(DbDb2PartAttr compareTo) {
		loc.entering("compare");
		boolean equals = true;

		// compare key col values
		String targetKeyColValue = compareTo.getFirstKeyColValue();
		String keyColValue = this.getFirstKeyColValue();
		while ((keyColValue != null) || (targetKeyColValue != null)) {
			if (((keyColValue == null) && (targetKeyColValue != null))
					|| ((keyColValue != null) && (targetKeyColValue == null))
					|| (0 != keyColValue.compareTo(targetKeyColValue))) {
				Object[] arguments = {
						this.tsAttr.getTable().getName(),
						(keyColValue == null ? "UNDEFINED" : keyColValue),
						(targetKeyColValue == null ? "UNDEFINED"
								: targetKeyColValue) };
				cat
						.infoT(
								loc,
								"comparePartAttr ({0}): original key value {1}, target key value {2}",
								arguments);
				equals = false;
				break; // break loop
			}
			targetKeyColValue = compareTo.getNextKeyColValue();
			keyColValue = this.getNextKeyColValue();
		}
		
		// do not check spqce allocations 
		/*if (this.getPriQty() != compareTo.getPriQty()) {
			logEntryFrorCompare("Priqty", (new Integer(this.getPriQty()))
					.toString(), (new Integer(compareTo.getPriQty()))
					.toString());
			equals = false;
		}

		if (this.getSecQty() != compareTo.getSecQty()) {
			logEntryFrorCompare("Secqty", (new Integer(this.getSecQty()))
					.toString(), (new Integer(compareTo.getSecQty()))
					.toString());
			equals = false;
		}*/
		
		/*if (this.getPctFree() != compareTo.getPctFree()) {
			logEntryFrorCompare("PctFree", (new Integer(this.getPctFree()))
					.toString(), (new Integer(compareTo.getPctFree()))
					.toString());
			equals = false;
		}
		
		if (this.getFreePage() != compareTo.getFreePage()) {
			logEntryFrorCompare("Freepage", (new Integer(this.getFreePage()))
					.toString(), (new Integer(compareTo.getFreePage()))
					.toString());
			equals = false;
		}
		
		if ( 0 != this.getDefine().compareTo(compareTo.getDefine())) {
			logEntryFrorCompare("DEFINE", this.getDefine(), compareTo.getDefine());
			equals = false;
		}
		
		if ( 0 !=  this.getGbpCache().compareTo(compareTo.getGbpCache())) {
			logEntryFrorCompare("GBPCache", this.getGbpCache(), compareTo.getGbpCache());
			equals = false;
		}
		
		if ( 0 !=  this.getTrackMod().compareTo(compareTo.getTrackMod())) {
			logEntryFrorCompare("TRACKMOD", this.getTrackMod(), compareTo.getTrackMod());
			equals = false;
		}
		
		if ( 0 != this.getCompress().compareTo(compareTo.getCompress())) {
			logEntryFrorCompare("COMPRESS", this.getCompress(), compareTo.getCompress());
			equals = false;
		}*/
	
		loc.exiting();
		return equals;

	}

	private void logEntryFrorCompare(String attr, String value1, String value2) {
		Object[] arguments = { this.tsAttr.getTable().getName(), value1, value2 };
		cat.infoT(loc, "equalsSpecificContent ({0}): original " + attr
				+ " {1}, target " + attr + " {2}", arguments);
	}
	
	public String getLimitKeyValues() {
		String keyColValues = null;
		String keyColValue = this.getFirstKeyColValue();
		while (keyColValue != null) {
			if ( keyColValues == null )
				keyColValues = "";
			else
				keyColValues += ",";
			keyColValues += keyColValue;
			keyColValue = this.getNextKeyColValue();	
		}
		return keyColValues;		
	}
}
