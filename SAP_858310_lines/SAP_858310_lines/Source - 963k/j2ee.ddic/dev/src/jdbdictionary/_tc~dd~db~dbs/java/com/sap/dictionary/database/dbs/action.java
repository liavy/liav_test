package com.sap.dictionary.database.dbs;

import java.lang.reflect.*;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class Action implements DbsConstants { 
  private static final Location loc = Location.getLocation(Action.class);
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);	
  private String name = "";
  private String sign = "";
  private String category = "";
  private int order = 0;

  private Action(String name, String sign, String category, int order) {
  	this.name = name; 
  	this.sign = sign; 
  	this.category = category;
  	this.order = order;
  }

  private static String C = "C";
  private static String A = "A";
  private static String M = "M";
  private static String D = "D";
  private static String U = "U";
  private static String N = "N";
  private static String O = "O";
  private static String R = "R";
  

  private static String DDL = "DDL";
  private static String CNV = "CNV";
  private static String DEL = "DEL";
  private static String NOT = "NOT";
  private static String NOA = "NOA";
  private static String REF = "REF";

  public static final Action CONVERT     = new Action("CONVERT",U,CNV,3);
  public static final Action ALTER       = new Action("ALTER",A,DDL,2);
  public static final Action CREATE      = new Action("CREATE",C,DDL,1);
  public static final Action DROP        = new Action("DROP",D,DEL,1);
  public static final Action DROP_CREATE = new Action("DROP_CREATE",M,DDL,1);
  public static final Action NOTHING     = new Action("NOTHING",N,NOT,0);
  public static final Action REFUSE      = new Action("REFUSE",R,REF,4);

  /**
   *  Returns the corresponding Action-object for an action-name  
   *  @param name			Name of the specified action as String 
   *  @return the corresponding Action-object              
   * 
   * */
  public static Action getInstance(String name) {
    Action action = null;

    if (name == null) return null;

    if (name.trim().equalsIgnoreCase("")) return null;
    try {
      Class cl = Class.forName("com.sap.dictionary.database.dbs.Action");
      try {
        Field field = cl.getField(name);
        action = (Action) field.get(cl);
      }
      catch (Exception ex) {//$JL-EXC$
      	cat.warning(loc,EXCEPTION_IN_CLASS, new Object[] {"Action",ex.getMessage()});
      }
    }
    catch (Exception ex) {//$JL-EXC$
      cat.warning(loc,EXCEPTION_IN_CLASS, new Object[] {"Action",ex.getMessage()});
    }
    return action;
  }

  /**
   *  Returns the name of this Action object  
   *  @return the name of the Action object
   * */
  public String getName() {return name;}
  
  public String getSign() {return sign;}
  
  public String getCategory() {return category;}
  
  private int getOrder() { return order; }

  public String toString() {return name;}

  public static Action max(Action a1,Action a2) {
  	if (a1 == null && a2 == null) return null;
  	if (a1 == null) return a2;
  	if (a2 == null) return a1;
  	int o1 = a1.getOrder();
  	int o2 = a2.getOrder();
  	if (o1 > o2)
  		return a1;
  	else
  		return a2;
  }
  
  public int compareTo(Action otherAction) {
  	if (order == otherAction.order)
  		return 0;
  	else if (order < otherAction.order)
  		return -1;
  	else
  		return 1;
  }
  
}
