package com.sap.sl.util.cvers.api;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ModifiedComponentIF;
import com.sap.sl.util.components.api.SCVersionIF;

/**
 *  The central interface to access all components deployed in a system. The package is based on the table
 *  CVERS (component version registry).
 *  <p>
 *  The whole library is part of the standard installation of a SAP J2EE engine.
 *  Therefore no additional deployments are required to use the CVERS functionality.
 *  <p>
 *  This interface contains methods to
 *  <ul>
 *    <li> read CVERS entries </li>
 *    <li> write CVERS entries </li>
 *    <li> remove CVERS entries </li>
 *  </ul>
 *  where a CVERS entry is repesented by the {@link ComponentElementIF} interface.
 *  <p>
 *  To create an instance of this interface the {@link CVersFactoryIF#getInstance} method can be used.
 * 
 *@author     md
 *
 *@version    1.0
 */

public interface CVersManagerIF {

  /**
   *  Reads all component versions active in the system
   *
   *@return    The component versions active in the system, or <code>null</code> if there are no component versions.
   */
  public ComponentElementIF[] readCVers() throws CVersAccessException;

  /**
   *  Reads the component versions active in the system for a specific component type
   *
   *@return    The corresponding component versions active in the system, or <code>null</code> if there are no component versions.
   */
  public ComponentElementIF[] readCVers(String componentType) throws CVersAccessException;

  /**
   *  Reads the component versions active in the system for a specific component type and subsystem
   *
   *@param  componentType  The component type of the searched component
   *@param  subsystem     The subsystem were the component is searched (default see {@link ComponentElementIF})
   *@return    The corresponding component versions active in the system, or <code>null</code> if there are no component versions.
   */
  public ComponentElementIF[] readCVers(String componentType, String subsystem) throws CVersAccessException;

  /**
   *  Reads the version of a specific component
   *
   *@param  vendor        The vendor of the searched
   *@param  name          The name of the searched component
   *@param  componentType  The component type of the searched component
   *@param  subsystem     The subsystem were the component is searched (default see {@link ComponentElementIF})
   *@return    The corresponding component version active in the system right now, or <code>null</code> if there is no component version matching the parameters
   */
  public ComponentElementIF readCVers(String vendor, String name, String componentType, String subsystem) throws CVersAccessException;

  /**
   *  Writes new entries to the component version Table CVERS
   *  and deletes implicitly outdated versions 
   *
   *@param  cversElements   An array of the newly applied component versions
   */
  public void writeCVers(ComponentElementIF[] cversElements) throws CVersAccessException;

  /**
   *  Removes entries of the component version Table CVERS
   *
   *@param  cversElements   An array of the component versions to be removed
   */
  public void removeCVers(ComponentElementIF[] cversElements) throws CVersAccessException;

  /**
   * Returns the given ComponentElement as SCVersionIF.
   */
  public SCVersionIF readSCVersion (ComponentElementIF compElem) throws CVersAccessException;
  
  /**
   *  NOTE: This method must only be used by engine deploy service and SDM.
   * 
   *  Updates CVERS entries based on DeployService repository.
   *
   *@param  cversElements   An array of the newly applied component versions
   *@deprecated please use syncCVersUpdate below instead.
   */
  public void syncCVers(ComponentElementIF[] cversElements) throws CVersAccessException;

  /**
   *  NOTE: This method must only be used by engine deploy service and SDM.
   * 
   *  Must be called by DeployService after all syncCVers calls have been done.
   *  This method removes all outdated entries, which have not been refreshed
   *  by syncCVers.
   *@deprecated please use syncCVersUpdate below instead.
   */
  public void syncFinished () throws CVersAccessException;
  
  /**
   *  NOTE: This method must only be used by engine deploy service and SDM.
   * 
   *  Updates CVERS entries based on DeployService repository.
   *  This function expects all component versions, which are defined in the DeployRepoistory,
   *  in one call. Internally all other versions, which exist in CVERS, are deleted !
   * 
   *@param  cversElements   An array of all component versions
   */
  public SyncResultIF syncCVersUpdate (ComponentElementIF[] cVersElements);

  public static final int NWDI_SYSTEMROLE_DEV = 1;
  public static final int NWDI_SYSTEMROLE_PROD = 2;
  public static final int NWDI_SYSTEMROLE_NONE = 0;

  /**
   * Checks, if current runtime system is part of a NWDI track. If yes, this method
   * distinguishes between development and productive systems.
   * @return NWDI_SYSTEMROLE_DEV if at least one component is deployed via NWDI and the system is configured as a DEV or CONS system 
   * @return NWDI_SYSTEMROLE_PROD if at least one component is deployed via NWDI and the system is configured as a TEST or PROD system
   * @return NWDI_SYSTEMROLE_NONE if no component is deployed via NWDI
   * @throws CVersAccessException
   */
  public int getNwdiRole () throws CVersAccessException;

  /**
   * Checks, if current runtime system is (partly) deplyed via a NWDI DEV system.
   * @return true, if at least one component is deployed via NWDI, otherwise false
   * @throws CVersAccessException
   * @deprecated
   */
  public boolean isDEVsystem () throws CVersAccessException;
  
  /**
   * Checks, if current runtime system is (partly) deplyed via a NWDI CONS system.
   * @return true, if at least one component is deployed via NWDI, otherwise false
   * @throws CVersAccessException
   * @deprecated
   */
  public boolean isCONSsystem () throws CVersAccessException;
  
  /**
   * Checks, if current runtime system is (partly) deplyed via a NWDI TEST system.
   * @return true, if at least one component is deployed via NWDI, otherwise false
   * @throws CVersAccessException
   * @deprecated
   */
  public boolean isTESTsystem () throws CVersAccessException;
  
  /**
   * Checks, if current runtime system is (partly) deplyed via a NWDI PROD system.
   * @return true, if at least one component is deployed via NWDI, otherwise false
   * @throws CVersAccessException
   * @deprecated
   */
  public boolean isPRODsystem () throws CVersAccessException;
  
  /**
   * Internal method: return current DB schema version
   * @author d036263
   */
  
  public ModifiedComponentIF[] getModifiedComponents () throws CVersAccessException;
  
  public int getDBSchema ();
  
}
