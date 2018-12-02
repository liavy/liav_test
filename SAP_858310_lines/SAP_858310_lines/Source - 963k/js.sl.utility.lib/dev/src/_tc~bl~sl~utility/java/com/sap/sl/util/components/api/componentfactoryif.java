package com.sap.sl.util.components.api;

import com.sap.sl.util.loader.Loader;

/**
 *  This factory shall be used to create an instance for one of the following classes
 * 
 *  {@link ComponentElementIF} {@link SoftwareComponentIF} {@link SCVersionIF}  {@link SCRequirementIF} {@link SCLevelIF}
 *
 *@author     jm
 *
 * 23.02.2005 jm added new CVL data
 * 21.10.2004 jm added PatchLevel
 * 
 *created    26. Juni 2003
 *@version    1.0
 */

public abstract class ComponentFactoryIF {
  private static ComponentFactoryIF INSTANCE = null;
  private final static String FACTORY_IMPL_CLASS
    = "com.sap.sl.util.components.impl.ComponentFactory";

  /**
   * Gets an instance of <code> ComponentFactoryIF. </code>
   * <p>
   * If you want a special class loader to be
   * used for the loading of the class use
   * {@link com.sap.sl.util.loader.Loader#setClassloader}
   *
   *@return	A <code> ComponentFactoryIF </code> instance
   *@see 	java.lang.ClassLoader
   */
  public static ComponentFactoryIF getInstance() {
    if (null == ComponentFactoryIF.INSTANCE) {
       ComponentFactoryIF.INSTANCE 
        = (ComponentFactoryIF)Loader.getInstanceOfClass(FACTORY_IMPL_CLASS);
    }
    return INSTANCE;
  }

  /**
   *  Creates an instance of <code> ComponentElementIF </code>
   *
   *@return    A <code> ComponentElementIF </code> instance
   */						
  public abstract ComponentElementIF createComponentElement();

  /**
   *  Creates an instance of <code> ComponentElementIF. </code>
   *  <p>
   *  Only for internal use within the <code> com.sp.sl.util </code> package.
   *
   *@param  applyTime  The time the component was applied to the system (in format "yyyyMMddHHmmss").
   *@return    A <code> ComponentElementIF </code> instance
   *
   * @deprecated
   */
	public abstract ComponentElementIF createComponentElement(String vendor, String name,
						  String componentType, String subSystem, String location,
						  String counter, String scVendor, String scName, String release,
						  String serviceLevel, String deltaVersion, String updateVersion,
						  String applyTime, String scElementTypeID, String spElementTypeID,
						  String spName, String spVersion);

  /**
   *  Creates an instance of <code> ComponentElementIF. </code>
   *  <p>
   *  Only for internal use within the <code> com.sp.sl.util </code> package.
   *
   *@param  applyTime  The time the component was applied to the system (in format "yyyyMMddHHmmss").
   *@return    A <code> ComponentElementIF </code> instance
   * @deprecated
   */
	public abstract ComponentElementIF createComponentElement(String vendor, String name,
						  String componentType, String subSystem, String location,
						  String counter, String scVendor, String scName, String release,
						  String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
						  String applyTime, String scElementTypeID, String spElementTypeID,
						  String spName, String spVersion);

	/**  
	 *  Creates an instance of <code> ComponentElementIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 *
	 *@param  applyTime  The time the component was applied to the system (in format "yyyyMMddHHmmss").
	 *@return    A <code> ComponentElementIF </code> instance
	 */
	  public abstract ComponentElementIF createComponentElement(String vendor, String name,
							String componentType, String subSystem, String location,
							String counter, String scVendor, String scName, String release,
							String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
							String applyTime, String scElementTypeID, String spElementTypeID,
							String spName, String spVersion, 
							String servertype, String servername, String changenumber, String projectpath);

	/**  
	 *  Creates an instance of <code> ComponentElementIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 *
	 *@param  applyTime  The time the component was applied to the system (in format "yyyyMMddHHmmss").
	 *@param  compVers   The ComponentElementIF as SCVersionIF for usage by CVL (only for software components).
	 *
	 *@return    A <code> ComponentElementIF </code> instance
	 */
	  public abstract ComponentElementIF createComponentElement(String vendor, String name,
							String componentType, String subSystem, String location,
							String counter, String scVendor, String scName, String release,
							String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
							String applyTime, String scElementTypeID, String spElementTypeID,
							String spName, String spVersion, 
							String servertype, String servername, String changenumber, String projectpath,
							SCVersionIF compVers);

	/**  
	 *  Creates an instance of <code> ComponentElementIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 *
	 *@param  applyTime  The time the component was applied to the system (in format "yyyyMMddHHmmss").
	 *@param  compVers   The ComponentElementIF as SCVersionIF for usage by CVL (only for software components).
	 *
	 *@return    A <code> ComponentElementIF </code> instance
	 */
	  public abstract ComponentElementIF createComponentElement(String vendor, String name,
							String componentType, String subSystem, String location,
							String counter, String scVendor, String scName, String release,
							String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
							String applyTime, String scElementTypeID, String spElementTypeID,
							String spName, String spVersion, 
							String servertype, String servername, String changenumber, String projectpath,
							SCVersionIF compVers, String provider);

	/**  
	 *  Creates an instance of <code> ComponentElementIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 *
	 *@param  vendor     The vendor of the component
	 *@param  name       The nam of the component
	 *@param  componentType Use ComponentElementIF.COMPONENTTYPE_SC or COMPONENTTYPE_DC
	 *@param  subSystem  Currently always ComponentElementIF.DEFAULTSUBSYSEM
	 *@param  location   The location field from the SAP Manifest file
	 *@param  counter    The counter field from the  SAP Manifest file
	 *@param  scVendor   The vendor of the software component of this component (=vendor if this is a software component)
	 *@param  scName     The vendor of the software component of this component (=name if this is a software component)
	 *@param  release    The release of the component. DCs inherit this value from their SC)
	 *@param  serviceLevel The service level (support package level) of the component. DCs inherit this value from their SC)
	 *@param  patchLevel The patch level of the component. DCs inherit this value from their SC)
	 *@param  deltaVersion Use ComponentElementIF.DELTAVERSION or FULLVERSION
	 *@param  updateVersion The updateversion from the SAP Manifest files componentelement
	 *@param  applyTime  The time the component was applied to the system (in format "yyyyMMddHHmmss").
	 *@param  scElementTypeID not used yet, use 'null' value
	 *@param  spElementTypeID not used yet, use 'null' value
	 *@param  spName not used yet, use 'null' value
	 *@param  spVersion not used yet, use 'null' value
	 *@param  servertype 'DTR' or 'P4'
	 *@param  servername full specified source repository server URL or name
	 *@param  changenumber sync changelist for perforce projcts
	 *@param  projectpath root directory on perforce / DTR server
	 *@param  compVers    SCVersion information for JSPM tool (currently still 'null'). Only for software components.
	 *@param  provider   software provider
	 * 
	 *@return    A <code> ComponentElementIF </code> instance
	 */
	  public abstract ComponentElementIF createComponentElement(String vendor, String name,
							String componentType, String subSystem, String location,
							String counter, String scVendor, String scName, String release,
							String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
							String applyTime, String scElementTypeID, String spElementTypeID,
							String spName, String spVersion, 
							String servertype, String servername, String changenumber, String projectpath,
							SCVersionIF compVers, String provider,
							String _algname, String _algvalue);

	/**  
	 *  Creates an instance of <code> SCVersionIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 * 
	 * @param vendor  The vendor of the software component.
	 * @param name    The name of the software component.
	 * 
	 * @return A new software component of type SoftwareComponentIF
	 */
	  public abstract SoftwareComponentIF createSoftwareComponent (String vendor, 
	  						String name);

	/**  
	 *  Creates an instance of <code> SCVersionIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 * 
	 * @param xmlString  The XML string, which describes a software component.
	 * 
	 * @return A software component descriptor of type SoftwareComponentIF.
	 */
	  public abstract SoftwareComponentIF createSoftwareComponentFromXML (String xmlString) throws ComponentElementXMLException;

	/**  
	 *  Creates an instance of <code> SCLevelIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 * 
	 * @param release        The release of the software component.
	 * @param serviceLevel   The service level (support package level) of the software component.
	 * @param patchLevel     The patch level of the software component.
	 * 
	 * @return  A component level descriptor of type SCLevelIF
	 */
	  public abstract SCLevelIF createSCLevel (
							String release,
							String serviceLevel,
							String patchLevel);

	/**  
	 *  Creates an instance of <code> SCVersionIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 * 
	 * @param vendor  The vendor of the software component.
	 * @param name    The name of the software component.
	 * @param provider The provider of the software component.
	 */
	  public abstract SCVersionIF createSCVersion (String vendor, 
							String name,
							String provider, 
							String location,
							String counter,
							SCLevelIF level,
							SCVersionIF history,
							SCRequirementIF[] required,
							SCLevelIF[] coverages,
							SCLevelIF[] compatibles);

	/**  
	 *  Creates an instance of <code> SCVersionIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 */
	/**
	 * Analyses given XML string and returns assembled SCVersionIF.
	 */
	public abstract SCVersionIF createSCVersionFromXML (String xmlString) throws ComponentElementXMLException;
	

	/**  
	 *  Creates an instance of <code> SCRequirementIF. </code>
	 *  <p>
	 *  Only for internal use within the <code> com.sp.sl.util </code> package.
	 */
	  public abstract SCRequirementIF createSCRequirement (
	  						SoftwareComponentIF comp,
							String provider,
							SCLevelIF level);

}
