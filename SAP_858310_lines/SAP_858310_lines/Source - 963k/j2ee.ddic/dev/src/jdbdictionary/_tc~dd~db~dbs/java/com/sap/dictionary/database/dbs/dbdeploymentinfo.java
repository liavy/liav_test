package com.sap.dictionary.database.dbs;

import java.io.*;

import com.sap.dictionary.database.dbs.DbDeployConfig.Attribute;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbDeploymentInfo implements DbsConstants {
	private static final Location loc = 
		Location.getLocation(DbDeployConfig.class);
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
  private String creationDate         = " ";
  private String author               = " ";
  private String language             = " ";
  private String description          = " ";
  private Action predefinedAction     = null;
  private boolean positionIsRelevant  = false;
  private boolean ignoreConfig  = false;
  private int sizeCategory = 0;
  private boolean isBuffered = false;
  private int genKeyCount = 0;
  private boolean doNotCreate = false;
  private boolean deleteIfExisting = false;
  private XmlMap databaseInfo = null;
  
  private Boolean ignoreCheckTableWidth = null;
  private Boolean specificForce = null;
  private Boolean conversionForce = null;
  private Boolean dropCreateForce = null;
  private Boolean acceptAbortRisk = null;
  private Boolean acceptDataLoss = null;		
  private Boolean acceptLongTime = null;		
  private Boolean fakeMissingDefaultValues = null;
  private Boolean acceptDropColumn = null;
  private Boolean acceptConversion = null;
  private Boolean ignoreRuntimeAtCompare = null;
  private Boolean acceptRuntimeAbsence = null;

  public DbDeploymentInfo() {}

  public DbDeploymentInfo(XmlMap xmlMap,DbFactory factory) { 	 
    creationDate = xmlMap.getString("creation-date");
    XmlMap properties = xmlMap.getXmlMap("properties");
      author = properties.getString("author");
      XmlMap description = properties.getXmlMap("description");
        language         = description.getString("language");
        this.description = description.getString("description");
    predefinedAction     = Action.getInstance(xmlMap.getString("predefined-action"));
    positionIsRelevant   = xmlMap.getBoolean("position-is-relevant");
    ignoreConfig   = xmlMap.getBoolean("ignore-config");
  	XmlMap technicalInfo = xmlMap.getXmlMap("technical-info");
  	if (!technicalInfo.isEmpty()) {
        sizeCategory = technicalInfo.getInt("size-category");	 
        isBuffered   = technicalInfo.getBoolean("is-buffered");	
        genKeyCount  = technicalInfo.getInt("gen-key-count");
  	}    
    databaseInfo = xmlMap.getXmlMap("database-exclusive-info");
    if (!databaseInfo.isEmpty() && databaseInfo != null) {
      XmlMap nextInfo = new XmlMap();
      for (int i=0;
           !(nextInfo = databaseInfo.getXmlMap("database-exclusive" + (i==0? "" : "" + i))).isEmpty();
           i++) {
         if (nextInfo.getString("database").toUpperCase().
                              equalsIgnoreCase(factory.getDatabaseName())) {	
           doNotCreate = true;
           deleteIfExisting = nextInfo.getBoolean("delete-if-existing");
         }  
      }    
    }
    XmlMap config = xmlMap.getXmlMap("config");
  	if (!config.isEmpty()) {
        ignoreCheckTableWidth = config.getBooleanObject("ignoreCheckTableWidth");
      	specificForce = config.getBooleanObject("specificForce");
      	conversionForce = config.getBooleanObject("conversionForce");
      	dropCreateForce = config.getBooleanObject("dropCreateForce");
      	acceptAbortRisk = config.getBooleanObject("acceptAbortRisk");
      	acceptDataLoss = config.getBooleanObject("acceptDataLoss");	
      	acceptLongTime = config.getBooleanObject("acceptLongTime");	
      	fakeMissingDefaultValues = config.getBooleanObject("fakeMissingDefaultValues");
      	acceptDropColumn = config.getBooleanObject("acceptDropColumn");
      	acceptConversion = config.getBooleanObject("acceptConversion");
      	ignoreRuntimeAtCompare = config.getBooleanObject("ignoreRuntimeAtCompare");
      	acceptRuntimeAbsence = config.getBooleanObject("acceptRuntimeAbsence");
      	cat.info(loc,SPECIAL_CONFIGURATION,new Object[] {configToString()});
  	}
  }

  public String getCreationDate() {return creationDate;}

  public String getAuthor() {return author;}
  
  public String getLanguage() {return language;}
  
  public String getDescription() {return description;}
  
  public Action getPredefinedAction() {return predefinedAction;}

  public boolean positionIsRelevant() {return positionIsRelevant;}
  
  public boolean ignoreConfig() {return ignoreConfig;}
  
  public boolean doNotCreate() {return doNotCreate;}
  
  public boolean deleteIfExisting() {return deleteIfExisting;}
  
  public int getSizeCategory() {return sizeCategory;}
  
  public boolean isBuffered() {return isBuffered;}

  public int getGenKeyCount() {return genKeyCount;}

  public void setCreationDate(String creationDate) {this.creationDate = creationDate;}

  public void setAuthor(String author) {this.author = author;}
  
  public void setLanguage(String language) {this.language = language;}

  public void setDescription(String description) {this.description = description;}

  public void setPredefinedAction(Action predefinedAction) {
  	this.predefinedAction = predefinedAction;}

  public void setPositionIsRelevant(boolean positionIsRelevant ) {
  	this.positionIsRelevant = positionIsRelevant;}
  
  public void setIgnoreConfig(boolean ignoreConfig ) {
  	this.ignoreConfig = ignoreConfig;}

  public void setSizeCategory(int sizeCategory) {this.sizeCategory = sizeCategory;}
  
  public void setSizeCategory(boolean isBuffered) {this.isBuffered = isBuffered;}
  
  public void setGenKeyCount(int genKeyCount) {this.genKeyCount = genKeyCount;}
  
  public Boolean ignoreCheckTableWidth() {
  	return ignoreCheckTableWidth;
  }
	public Boolean specificForce() {
		return specificForce;
  }
	public Boolean conversionForce() {
		return conversionForce;
  }
	public Boolean dropCreateForce() {
		return dropCreateForce;
  }
	public Boolean acceptAbortRisk() {
		return acceptAbortRisk;
  }
	public Boolean acceptDataLoss() {
		return acceptDataLoss;
  }	
	public Boolean acceptLongTime() {
		return acceptLongTime;
  }
	public Boolean fakeMissingDefaultValues() {
		return fakeMissingDefaultValues;
  }
	public Boolean acceptDropColumn() {
		return acceptDropColumn;
  }
	public Boolean acceptConversion() {
		return acceptConversion;
  }
	public Boolean ignoreRuntimeAtCompare() {
		return ignoreRuntimeAtCompare;
  }
	public Boolean acceptRuntimeAbsence() {
		return acceptRuntimeAbsence;
  }
	
	private void addToDeployAttribute(DbDeployConfig.Attribute attr,
			Boolean value) {
		
	}
  
  
  public void writeContentToXmlFile(PrintWriter file, String offset0)
                         throws JddException {
    file.println(offset0 + "<properties>");
    String offset1 = offset0 + XmlHelper.tabulate();
    file.println(offset1 + "<creation-date>" + creationDate + "</creation-date>");
    file.println(offset1 + "<author>" + author +  "</author>");
    file.println(offset1 + "<description language=" + "\"" + language
                         + "\"" + ">" + description + "</description>");
    file.println(offset0 + "</properties>");
    
    file.println(offset0 + "<position-is-relevant>" + positionIsRelevant +
                                        "</position-is-relevant>");
    file.println(offset0 + "<ignore-config>" + ignoreConfig +
    					"</ignore-config>");
    file.println(offset0 + "<predefined-action>" +
                 (predefinedAction == null ? "" :  predefinedAction.toString()) +
                 "</predefined-action>"); 
       
    file.println(offset0 + "<technical-info>");
    file.println(offset1 + "<size-category>" + sizeCategory + "</size-category>");
    file.println(offset1 + "<is-buffered>" + isBuffered + "</is-buffered>");
    file.println(offset1 + "<gen-key-count>" + genKeyCount + "</gen-key-count>");
    file.println(offset0 + "</technical-info>");  
     
    if (databaseInfo != null && !databaseInfo.isEmpty()) {
      file.println(offset0 + "<database-exclusive-info>");	
      XmlMap nextInfo = new XmlMap();
      String offset2 = offset1 + XmlHelper.tabulate();
      for (int i=0;
           !(nextInfo = databaseInfo.getXmlMap("database-exclusive" + (i==0? "" : "" + i))).isEmpty();
           i++) {	
        file.println(offset1 + "<database-exclusive>");   	
        file.println(offset2 + "<database>" +  nextInfo.getString("database").toUpperCase() +
                    "</database>");
        file.println(offset2 + "<delete-if-existing>" + nextInfo.getBoolean("delete-if-existing") +            
                    "</delete-if-existing>");  
        file.println(offset1 + "</database-exclusive>");            
      }    
      file.println(offset0 + "</database-exclusive-info>"); 
    }                            	
  }        
  
  public String toString() {
  	String res = "";
  	if (author != null && author.trim().length() != 0 && !author.equals("null") ||
  			creationDate != null && creationDate.trim().length() != 0 &&
  			!creationDate.equals("null"))
  		res += "Author                : " + author + " " + creationDate + "\n";
  	if (description != null && description.trim().length() != 0 && !description.equals("null"))
      res += "Description           : " + description + "\n";
  	if (language != null && language.trim().length() != 0 && !language.equals("null"))
      res += "Language              : " + language + "\n";
  	if (positionIsRelevant)
      res += "Position is relevant" + "\n";
  	if (predefinedAction != null)
      res += "Predefined Action     : " + predefinedAction + "\n";
  	if (sizeCategory != 0)
      res += "Size Category         : " + sizeCategory  + "\n";
  	res += configToString();
  	return res;
  } 
  
  public String configToString() {
  	String res = "";
  	if (ignoreCheckTableWidth != null)
  		res += "ignoreCheckTableWidth  : " + ignoreCheckTableWidth.booleanValue()  + "\n";
  	if (specificForce != null)
  		res += "specificForce  : " + specificForce.booleanValue() + "\n";
  	if (conversionForce != null)
  		res += "conversionForce  : " + conversionForce.booleanValue() + "\n";
  	if (dropCreateForce != null)
  		res += "dropCreateForce  : " + dropCreateForce.booleanValue() + "\n";
  	if (acceptAbortRisk != null)
  		res += "acceptAbortRisk  : " + acceptAbortRisk.booleanValue() + "\n";
  	if (acceptDataLoss != null)
  		res += "acceptDataLoss  : " + acceptDataLoss.booleanValue() + "\n";
  	if (acceptLongTime != null)
  		res += "acceptLongTime  : " + acceptLongTime.booleanValue() + "\n";
  	if (fakeMissingDefaultValues != null)
  		res += "fakeMissingDefaultValues  : " + fakeMissingDefaultValues.booleanValue() + "\n";
  	if (acceptDropColumn != null)
  		res += "acceptDropColumn  : " + acceptDropColumn.booleanValue() + "\n";
  	if (acceptConversion != null)
  		res += "acceptConversion  : " + acceptConversion.booleanValue() + "\n";
  	if (ignoreRuntimeAtCompare != null)
  		res += "ignoreRuntimeAtCompare  : " + ignoreRuntimeAtCompare.booleanValue() + "\n";
  	if (acceptRuntimeAbsence != null)
  		res += "acceptRuntimeAbsence  : " + acceptRuntimeAbsence.booleanValue() + "\n";
  	return res;
  } 
}
