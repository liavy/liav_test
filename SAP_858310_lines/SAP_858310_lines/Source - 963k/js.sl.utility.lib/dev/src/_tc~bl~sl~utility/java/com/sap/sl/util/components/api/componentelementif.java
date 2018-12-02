package com.sap.sl.util.components.api;


/**
 *  The interface <code> ComponentElementIF </code> is the representation of a component version.
 *  A component version is the denotation for a specific version of a component.
 *  <p>
 *  All component versions running in a system are strored in DB table CVERS.
 *  Therefore <code> ComponentElementIF </code> is equivalent to an element of table CVERS.
 *  <p>
 *  Remark: the attributes vendor, name, location and counter form the four keys, decribing
 *  a component version in software logistics.
 *  <p>
 *  An instance of this interface can be created by using the {@link ComponentFactoryIF#getInstance}
 *  method.
 * 
 *@author     jm
 *
 * 29.10.2005 jm added checksum value, currently MD5
 * 18.02.2005 jm added attributes for SP upgrade
 * 
 * 21.12.2004 jm added getComponentProvider
 * 
 * 03.12.2004 jm added perforce data
 * 
 * 21.10.2004 jm added getPatchLevel
 * 
 * 16.05.2003 md created
 * 
 *@version    1.0
 */

public interface ComponentElementIF {

// statics
  /**
   *  Returns the default value for key field subsystem as long as key subsystem is not used.
   */
  final static String DEFAULTSUBSYSTEM = "NO_SUBSYS";
  /**
   *  Returns the value describing a software delta for parameter {@link #getDeltaVersion}.
   */
  final static String DELTAVERSION = "D";
  /**
   *  Returns the value describing a full software version for parameter {@link #getDeltaVersion}.
   */
  final static String FULLVERSION = "F";
  /**
   *  Returns the value describing a software component for parameter {@link #getComponentType}.
   */
  final static String COMPONENTTYPE_SC = "SC";
  /**
   *  Returns the value describing a development component for parameter {@link #getComponentType}.
   */
  final static String COMPONENTTYPE_DC = "DC";

//methods
  /**
   *  Gets the name of the vendor of this component version. 
   *  <p>
   *  In the DB representation vendor is of type string. 
   *@return the vendor
   */
  public String getVendor();

  /**
   *  Gets the name of this component version. 
   *  <p>
   *  In the DB representation name is of type string. 
   *@return the name
   */
  public String getName();

  /**
   *  Gets the componenttype of this component version. 
   *  <p>
   *  Possible types are:
   *  <ul>
   *    <li> SC (software component)
   *    <li> DC (development component)
   *    <li> SC_CR (change request based on a software component)
   *    <li> DC_CR (change request based on a development component)
   *  </ul>
   *  In the DB representation componenttype is of type string and length 10.
   *  Componenttype is a CVERS key field.
   *@return the softwaretype
   */
  public String getComponentType();

  /**
   *  Gets the name of the subsystem this component version is running on. 
   *  <p>
   *  In the future it might be possible to have two versions of the same component running in the
   *  same server. Therefore an additional key field is required to differ the two "places" the two
   *  versions are running on. Right now this attribute is not used. The default value when the subsystem
   *  key is not used is {@link #DEFAULTSUBSYSTEM}.
   *  <p>
   *  In the DB representation subsystem is of type string.
   *  Subsystem is a CVERS key field.
   *@return the subsystem
   */
  public String getSubsystem();

  /**
   *  Gets the name of the location where this component version was created.
   *  Together with the counter attribute, location forms a worldwide unique version.
   *  <p>
   *  In the DB representation location is of type string.
   *@return the location, or <code>null</code> if the property is currently invalid.
   */
  public String getLocation();

  /**
   *  Gets the counter or number of this component version (with respect to its location).
   *  The counter is a timestamp (in format "yyyyMMddHHmmss") and therfore unique within a location.
   *  <p>
   *  In the DB representation counter is of type string.
   *@return the counter, or <code>null</code> if the property is currently invalid.
   */
  public String getCounter();

  /**
   *  Gets the vendor of the software component this component version is part of. This field is only filled
   *  for development components.
   *  <p>
   *  In the DB representation SCVendor is of type string.
   *@return the vendor of the software component, or <code>null</code> if the property is currently invalid.
   */
  public String getSCVendor();

  /**
   *  Gets the name of the software component this component version is part of. This field is only filled
   *  for development components.
   *  <p>
   *  In the DB representation SCName is of type string.
   *@return the name of the software component, or <code>null</code> if the property is currently invalid.
   */
  public String getSCName();
  
  /**
   *  Gets the release of this component version. 
   *  <p>
   *  In the DB representation release is of type string.
   *@return the release, or <code>null</code> if the property is currently invalid.
   */
  public String getRelease();
  
  /**
   *  Gets the servicelevel of this component version. 
   *  <p>
   *  For software components the service level is the support package level,
   *  for development components the service level is the patch level.
   *  <p>
   *  In the DB representation servicelevel is of type string.
   *@return the servicelevel, or <code>null</code> if the property is currently invalid.
   */
  public String getServiceLevel();

  /**
   *  Gets the patchlevel of this component version. 
   *  <p>
   *  In the DB representation patchlevel is of type string.
   *@return the patchlevel, or <code>null</code> if the property is currently invalid.
   */
  public String getPatchLevel();

  /**
   *  Gets the delta information of the current component version. 
   *  <p>
   *  The delta version denotes whether the package is a {@link #DELTAVERSION} 
   *  or a {@link #FULLVERSION}. (Do we need the complete order of predecessors?)
   *  <p>
   *  In the DB representation deltaversion is of type string.
   *@return the deltaversion, or <code>null</code> if the property is currently invalid.
   */
  public String getDeltaVersion();
  
  /**
   *  Gets the updateversion of the current component version. 
   *  <p>
   *  The update version denotes a modification of the above mentioned release/servicelevel. It's kind of
   *  an enhanced modification flag: During initial shipment it is <code> null </code>. After modifikation
   *  it contains Track and MiniTimeStamp of the Change.
   *  <p>
   *  In the DB representation updateversion is of type string.
   *@return the updateversion, or <code>null</code> if the property is currently invalid.
   */
  public String getUpdateVersion();

  /**
   *  Gets the time the current component version was applied to the system. 
   *  <p>
   *  In the DB representation applytime is of type string.
   *@return the time, or <code>null</code> if the property is currently invalid.
   */
  public String getApplyTime();

  /**
   *  Gets the PPMS number of the software component as stored in the SLD. 
   *  <p>
   *  In the DB representation SCElementTypeID is of type string.
   *@return the PPMS number, or <code>null</code> if the property is currently invalid.
   */
  public String getSCElementTypeID();

  /**
   *  Gets the PPMS number of the support package as stored in the SLD. 
   *  <p>
   *  In the DB representation SPElementTypeID is of type string.
   *@return the PPMS number, or <code>null</code> if the property is currently invalid.
   */
  public String getSPElementTypeID();

  /**
   *  Gets the name of the support package as stored in the SLD. 
   *  <p>
   *  In the DB representation SPName is of type string.
   *@return the name of the support package, or <code>null</code> if the property is currently invalid.
   */
  public String getSPName();

  /**
   *  Gets the version of the support package as stored in the SLD. 
   *  <p>
   *  In the DB representation SPVersion is of type string.
   *@return the version of the support package, or <code>null</code> if the property is currently invalid.
   */
  public String getSPVersion();

  /**
   *  Returns the address/URL of the perforce/DTR server.
   *  Has type string in the database.
   */
 
   public String getSourceServer ();

   /**
	*  Returns the changelist number or DTR ISN.
    *  Has type string in the database.
	*/
   public String getChangeNumber ();

   /**
    * Returns the server type, where the sources are located: 'P4' or 'DTR'.
   *  Has type string in the database.
	*/
   public String getServerType ();

   /**
    * Returns the project path on the source server: perforce project path or DTR workspace.
   *  Has type string in the database.
	*/
	public String getProjectName ();


	/**
	 * Returns the software provider of the current component.
     *  Has type string in the database.
	 * 
	 */
	 public String getComponentProvider ();

	/**
	 * Returns the SCVersionIF, stored by a previous createComponentElement.
	 * If this ComponentElement was just read from database then following
	 * method must be called instead:
	 * cversManager.readSCVersion (ComponentElementIF)
	 */
	
	public SCVersionIF getComponentVersion ();
	
	/**
	 * Returns the name of the current source zip checksum algorithm.
	 * Currently only 'MD5' is supported.
	 * If no checksum is defined, then a null value is returned.
	 */
	public String getChecksumAlgorithmName ();
	
	/**
	 * Returns the value of the current source zip checksum.
	 * If no checksum is defined, then a null value is returned.
	 */
	public String getChecksumValue ();
	
   /**
	*  Writes a string consisting of vendor, name, location and counter of the component element. 
	*
	*@return a string with the four keys of a component element
	*/
   
  public String toString();

  /**
   *  Writes a string consisting of all attributes of the component element
   *  Format: "vendor:"+vendor+EOL+
   *          "name:"+name+EOL+
   *          "location:"+location+EOL+
   *          "counter:"+counter+EOL+
   *          "SP-level:"+splevel+EOL+
   *          ...
   *
   *@return a string with all attributes of the component element
   */
  public String toFormattedString();

}