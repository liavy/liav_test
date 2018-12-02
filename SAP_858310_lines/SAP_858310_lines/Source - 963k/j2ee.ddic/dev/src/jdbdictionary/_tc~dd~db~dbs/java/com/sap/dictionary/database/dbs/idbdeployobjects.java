/*
 * Created on Apr 28, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.dbs;

import java.util.GregorianCalendar;

/**
 * @author d003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IDbDeployObjects {
	public static final String WAIT     = " ";
	public static final String ANALYSED = "A";
	public static final String RUNNING  = "R";
	public static final String ERROR    = "E";
	
	//deploy info order
	public static final int NAME = 0;
	public static final int TIMESTAMP = 1;
	public static final int MODKIND = 2;
	public static final int ACTION_ = 3;
	public static final int HINTS = 4;
	public static final int STATUS = 5;
	public static final int XMLMAP = 6;

	
	public void put(String name, long timestamp, XmlMap xmlMap);

	public void put(String name, Action action, String hints,Object xmlData);
	
	public void put(String name, Action action, Object xmlData);

	public void put(String name, Object xmlData);

	public void put(String name, long timestamp, Object xmlData);

	public void put(String name, long timestamp, Action action, Object xmlData);

	/**
	 * Variable info contains String name, Long timestamp, String(1) modKind,
	 * Action action, String(1) state, XmlMap xmlMap in this sequence
	 */
	public void put(String name, Object[] info);
	
	public XmlMap get(String name);
	
	public Object[] getRow(String name);
		
	public boolean isEmpty();
	
	public boolean contains(String name);
	
	public Object[] nextToFullScan();
	
	public Object[] nextToAnalyse();
			
	public Object[] nextToModify();
	
	public Object[] nextToConvert();
	
	public boolean hasNextToModify();
	
	public boolean hasNextToConvert();
	
	public boolean dbModificationNecessary();
	
	public boolean dbConversionNecessary();
	
	public void reset();
	
	public void remove(String name);

	public Action getAction(String name);
	
  public void setAction(String name, Action action);
    
  public void setStatus(String name, String status);
  
	public void setAnalyseResult(String name, Action action,String hints,String status);

	public Object convertXmlData(Object xmlData) throws Exception;
}
