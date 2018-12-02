package com.sap.dictionary.database.dbs;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbColumnDifferencePlan {
  private boolean typeLenDecIsChanged   = false;
  private boolean typeIsChanged         = false;
  private boolean lengthIsChanged       = false;
  private boolean decimalsAreChanged    = false;
  private boolean nullabilityIsChanged  = false;
  private boolean defaultValueIsChanged = false;
  private boolean positionIsChanged     = false;
  private boolean dataLoss     					= false;
  private boolean longTime = false;

  public DbColumnDifferencePlan() {}

  public void setTypeIsChanged(boolean changed) {
    typeIsChanged = changed;
    if (changed) typeLenDecIsChanged = true;
  }

  public void setLengthIsChanged(boolean changed) {
    lengthIsChanged = changed;
    if (changed) typeLenDecIsChanged = true;
  }

  public void setDecimalsAreChanged(boolean changed) {
    decimalsAreChanged = changed;
    if (changed) typeLenDecIsChanged = true;
  }

  public void setNullabilityIsChanged(boolean changed) {nullabilityIsChanged = changed;}

  public void setDefaultValueIsChanged(boolean changed) {defaultValueIsChanged = changed;}

  public void setPositionIsChanged(boolean changed) {positionIsChanged = changed;}
  
	public void  setDataLoss(boolean dataLoss) {
		this.dataLoss = dataLoss;
	}
	
	
	public void  setLongTime(boolean longTime) {
		this.longTime = longTime;
	}
	

  public boolean typeLenDecIsChanged() {return typeLenDecIsChanged;}

  public boolean typeIsChanged() {return typeIsChanged;}

  public boolean lengthIsChanged() {return lengthIsChanged;}

  public boolean decimalsAreChanged() {return decimalsAreChanged;}

  public boolean nullabilityIsChanged() {return nullabilityIsChanged;}

  public boolean defaultValueIsChanged() {return defaultValueIsChanged;}

  public boolean positionIsChanged() {return positionIsChanged;}

  public boolean somethingIsChanged() {
    return typeLenDecIsChanged || nullabilityIsChanged || defaultValueIsChanged;
  }
  
	public boolean  dataLoss() {
		return dataLoss;
	}
	
	public boolean  longTime() {
		return longTime;
	}

  public String toString() {
  	String res = "\n";
  	if (typeLenDecIsChanged)
  		res += "typeLenDecIsChanged" + "\n";
  	if (typeIsChanged)
  		res += "typeIsChanged" + "\n";
  	if (lengthIsChanged)
  		res += "lengthIsChanged" + "\n";
  	if (decimalsAreChanged)
  		res += "decimalsAreChanged" + "\n";
  	if (decimalsAreChanged)
  		res += "nullabilityIsChanged" + "\n";
  	if (defaultValueIsChanged)
  		res += "defaultValueIsChanged" + "\n";
  	if (dataLoss)
  		res += "dataLoss" + "\n";
  	if (longTime)
  		res += "longTime" + "\n";
    return res;
  }
}
