
package com.sap.sl.util.components.impl;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentElementXMLException;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.components.api.SCLevelIF;
import com.sap.sl.util.components.api.SCRequirementIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.components.api.SoftwareComponentIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  Use this factory in order to create an conponent element instance, like:
 *  <ul>
 *    <li> a ComponentElement ({@link com.sap.components.api.ComponentElementIF}</li>
 *  </ul>
 *
 *@author     md
 *@created    19. Juni 2003
 *@version    1.0
 */

public final class ComponentFactory extends ComponentFactoryIF {

  private static final SlUtilLogger log = SlUtilLogger.getLogger(ComponentFactory.class.getName());

  public ComponentFactory() {
  }

  /**
   *  get instance of a ComponentElement
   *
   *@return    The ComponentElement instance
   */
  public ComponentElementIF createComponentElement(String vendor, String name,
						String componentType, String subSystem, String location,
						String counter, String scVendor, String scName, String release,
						String serviceLevel, String deltaVersion, String updateVersion,
						String applyTime, String scElementTypeID, String spElementTypeID,
						String spName, String spVersion) {
	log.warning ("createComponentElement() deprecated "+vendor+" "+name);
	 return new ComponentElement(vendor, name, componentType, subSystem, location,
						counter, scVendor, scName, release, serviceLevel, "0", deltaVersion,
						updateVersion, applyTime, scElementTypeID, spElementTypeID,
						spName, spVersion, null, null, null, null);
  }
  /**
   *  get instance of a ComponentElement
   *
   *@return    The ComponentElement instance
   */
  public ComponentElementIF createComponentElement(String vendor, String name,
						String componentType, String subSystem, String location,
						String counter, String scVendor, String scName, String release,
						String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
						String applyTime, String scElementTypeID, String spElementTypeID,
						String spName, String spVersion) {
	log.warning ("createComponentElement() deprecated PatchLevel "+vendor+" "+name);
	 return new ComponentElement(vendor, name, componentType, subSystem, location,
						counter, scVendor, scName, release, serviceLevel, patchLevel, deltaVersion,
						updateVersion, applyTime, scElementTypeID, spElementTypeID,
						spName, spVersion, null, null, null, null);
  }
  public ComponentElementIF createComponentElement() {
    return new ComponentElement();
  }

  public ComponentElementIF createComponentElement(String vendor, String name,
						String componentType, String subSystem, String location,
						String counter, String scVendor, String scName, String release,
						String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
						String applyTime, String scElementTypeID, String spElementTypeID,
						String spName, String spVersion,
	String servertype, String servername, String changenumber, String projectpath)
 {

	 return new ComponentElement(vendor, name, componentType, subSystem, location,
						counter, scVendor, scName, release, serviceLevel, patchLevel, deltaVersion,
						updateVersion, applyTime, scElementTypeID, spElementTypeID,
						spName, spVersion, servertype, servername, changenumber, projectpath);
  }

  public ComponentElementIF createComponentElement(String vendor, String name,
						String componentType, String subSystem, String location,
						String counter, String scVendor, String scName, String release,
						String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
						String applyTime, String scElementTypeID, String spElementTypeID,
						String spName, String spVersion,
						String servertype, String servername, String changenumber, 
						String projectpath,
						SCVersionIF compVers)
	
 {
	 return new ComponentElement (vendor, name, componentType, subSystem, location,
						counter, scVendor, scName, release, serviceLevel, patchLevel, deltaVersion,
						updateVersion, applyTime, scElementTypeID, spElementTypeID,
						spName, spVersion, servertype, servername, 
						changenumber, projectpath, compVers);
  }

  public ComponentElementIF createComponentElement(String vendor, String name,
						String componentType, String subSystem, String location,
						String counter, String scVendor, String scName, String release,
						String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
						String applyTime, String scElementTypeID, String spElementTypeID,
						String spName, String spVersion,
						String servertype, String servername, String changenumber, 
						String projectpath,
						SCVersionIF compVers, String provider)
	
 {
	 return new ComponentElement (vendor, name, componentType, subSystem, location,
						counter, scVendor, scName, release, serviceLevel, patchLevel, deltaVersion,
						updateVersion, applyTime, scElementTypeID, spElementTypeID,
						spName, spVersion, servertype, servername, 
						changenumber, projectpath, compVers, provider);
  }

  public ComponentElementIF createComponentElement(String vendor, String name,
						String componentType, String subSystem, String location,
						String counter, String scVendor, String scName, String release,
						String serviceLevel, String patchLevel, String deltaVersion, String updateVersion,
						String applyTime, String scElementTypeID, String spElementTypeID,
						String spName, String spVersion,
						String servertype, String servername, String changenumber, 
						String projectpath,
						SCVersionIF compVers, String provider,
						String _algname, String _algvalue)
	
 {
	 return new ComponentElement (vendor, name, componentType, subSystem, location,
						counter, scVendor, scName, release, serviceLevel, patchLevel, deltaVersion,
						updateVersion, applyTime, scElementTypeID, spElementTypeID,
						spName, spVersion, servertype, servername, 
						changenumber, projectpath, compVers, provider,
						_algname, _algvalue);
  }

  public SCVersionIF createSCVersion (String vendor, 
						String name,
						String provider, 
						String location,
						String counter,
						SCLevelIF level,
						SCVersionIF history,
						SCRequirementIF[] required,
						SCLevelIF[] coverages,
						SCLevelIF[] compatibles) {
		return new SCVersion (vendor, name, 
		provider, 
		location,
		counter,
		level,
		history,
		required,
		coverages,
		compatibles);
  	}

	public SCVersionIF createSCVersionFromXML (String xmlString) throws ComponentElementXMLException {
		  XMLParser parser = new XMLParser ();
		  return parser.parse(xmlString);
	  }

	public SCLevelIF createSCLevel (String _release, String _serviceLevel, String _patchLevel) {
		return new SCLevel (_release, _serviceLevel, _patchLevel);
	}
	
	public SoftwareComponentIF createSoftwareComponent (String vendor, String name) {
		return new SoftwareComponent (vendor, name);
	}
	
	public SoftwareComponentIF createSoftwareComponentFromXML (String xmlString) {
		return new SoftwareComponent (null, null);
	}
	
	public SCRequirementIF createSCRequirement (SoftwareComponentIF comp, 
						String provider, SCLevelIF level) {
		
		return new SCRequirement (comp.getVendor(), comp.getName(), provider, level);
	}
	
}
