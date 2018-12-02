package com.sap.sl.util.logging.api;

/**
 * SAP SL Util Logging
 * 
 * @author  hen
 * @version 3
 * modified  02.11.2002 - hen
 *           25.06.2003 - md - copied to sl.util
 *           04.07.2003 - cg - separation of api and implementation
 */

public final class TraceLevel {

	private final String name;
	private final int priority;

  private TraceLevel(String s, int i) {
      name = s;
      priority = i;
  }

  public static final TraceLevel PATH  = new TraceLevel("PATH",  1);
  public static final TraceLevel DEBUG = new TraceLevel("DEBUG", 2);
 
  public int intValue() {
      return priority;
  }
}