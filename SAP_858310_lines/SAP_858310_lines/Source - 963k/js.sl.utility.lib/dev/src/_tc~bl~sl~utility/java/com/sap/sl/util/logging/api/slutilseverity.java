package com.sap.sl.util.logging.api;

/**
 * SAP SL Util Logging
 * 
 * @author  hen
 * @version 2
 * modified  02.11.2002
 *           25.06.2003 - md - copied to sl.util
 *           04.07.2003 - cg - separation of api and implementation
 */

public final class SlUtilSeverity {

	private final String name;
	private final int priority;

  private SlUtilSeverity(String s, int i) {
      name = s;
      priority = i;
  }

  public static final SlUtilSeverity FATAL   = new SlUtilSeverity("FATAL",   1);
  public static final SlUtilSeverity ERROR   = new SlUtilSeverity("ERROR",   2);
  public static final SlUtilSeverity WARNING = new SlUtilSeverity("WARNING", 3);
  public static final SlUtilSeverity INFO    = new SlUtilSeverity("INFO",    4);

  public int intValue() {
      return priority;
  }
}