/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.interfaces.textcontainer;

import java.util.HashMap;
import java.util.ResourceBundle;

import com.sap.engine.interfaces.textcontainer.context.TextContainerContextResolution;
import com.sap.engine.interfaces.textcontainer.recipient.TextContainerRecipientListener;

/**
 *
 * Client interface to 'Text Container' service. This interface is recommended 
 * to be used by application, library or service client components.
 * <pre><code>Example:
 * 
 *   InitialContext ctx = ..;
 *   TextContainerManager tci = (TextContainerManager) ctx.lookup("interfaces/textcontainer_api");
 *   ...</code></pre>
 * <br/><br/>Copyright (c) 2006, SAP AG
 * @author  Thomas Goering
 * @version 1.0
 */
public interface TextContainerManager
{

	/**
	 * Gets the system context industry.
	 *
	 * @return System context industry.
	 */
	public String getSystemContextIndustry();

	/**
	 * Gets the system context region.
	 *
	 * @return System context region.
	 */
	public String getSystemContextRegion();

	/**
	 * Gets the system context extension.
	 *
	 * @return System context extension.
	 */
	public String getSystemContextExtension();

	/**
	 * Gets all industry values.
	 *
	 * @return HashMap of industries.
	 */
	public HashMap<String, TextContainerIndustry> getIndustryValues();

	/**
	 * Gets all region values.
	 *
	 * @return HashMap of regions.
	 */
	public HashMap<String, TextContainerRegion> getRegionValues();

	/**
	 * Gets all extension values.
	 *
	 * @return HashMap of extensions.
	 */
	public HashMap<String, TextContainerExtension> getExtensionValues();

	/**
	 * Gets all language values.
	 *
	 * @return HashMap of languages.
	 */
	public HashMap<String, TextContainerLanguage> getLanguageValues();

	/**
	 * Gets all locale values (only explicitly defined locale fallbacks). For locale chains
	 * of a specific locale see {@link TextContainerContextResolution#computeLocaleChain(String, String)}.
	 *
	 * @return HashMap of arrays of locales.
	 */
	public HashMap<String, TextContainerLocale[]> getLocaleValues();

	/**
	 * Registers an recipient object.
	 * 
	 * @param recipient  Recipient name.
	 * @param object  Recipient object (must implement {@link TextContainerRecipient}).
	 * @deprecated use {@link TextContainerManager#registerRecipientListener(String, TextContainerRecipientListener)}
	 */
	public void registerRecipient(String recipient, TextContainerRecipient object) throws TextContainerManagerException;

	/**
	 * Unregisters an recipient object.
	 * 
	 * @param recipient  Recipient name.
	 * @deprecated use {@link TextContainerManager#unregisterRecipientListener(String, TextContainerRecipientListener)}
	 */
	public void unregisterRecipient(String recipient) throws TextContainerManagerException;

	/**
	 * Registers a recipient listener.
	 * 
	 * @param recipient  Recipient name.
	 * @param listener  Recipient listener (must implement {@link TextContainerRecipientListener}).
	 */
	public void registerRecipientListener(String recipient, TextContainerRecipientListener listener) throws TextContainerManagerException;

	/**
	 * Unregisters a recipient listener.
	 * 
	 * @param recipient  Recipient name.
	 * @param listener  Recipient listener (must implement {@link TextContainerRecipientListener}).
	 */
	public void unregisterRecipientListener(String recipient, TextContainerRecipientListener listener) throws TextContainerManagerException;

	/**
	 * Gets the context resolution object.
	 * 
	 * @return Context resolution object
	 */
	public TextContainerContextResolution getContextResolutionObject();

	/**
	 * Get resource bundles in all available locales.
	 *  
	 * @param componentName Component for the bundle.
	 * @param baseName Bundle name.
	 * @return Resource bundle array For each available locale there is a ResourceBundle object in the array. 
	 * @throws TextContainerManagerException If an error occured (e.g. database error or no texts can be found).
	 * @throws TextContainerManagerMissingComponentException If the component cannot be found.
	 * @throws TextContainerManagerMissingBundleException If the bundle cannot be found.
	 */
	public ResourceBundle[] getTexts(String componentName, String baseName) throws TextContainerManagerException;

	/**
	 * Get all existing locales for an original component.
	 *  
	 * @param componentName Original component.
	 * @return Locale array For each available locale there is entry in the array. 
	 * @throws TextContainerManagerException If an error occured (e.g. database error).
	 * @throws TextContainerManagerMissingComponentException If the component cannot be found.
	 */
	public String[] getLocales(String componentName) throws TextContainerManagerException;

	/**
	 * Sets the system context. User must have change permission of ActionPermission
	 * <code>com.sap.engine.services.textcontainer.security.TextContainerPermission</code>.
	 * It is assigned to role <code>Administrator</code> with the deployment of the Text Container service.
	 *
	 * @param industry  The value of the industry attribute for the new system context.
	 * @param region  The value of the region attribute for the new system context.
	 * @param extension  The value of the extension attribute for the new system context.
	 * @throws TextContainerManagerException  n/a.
	 */
	public void setSystemContext(String industry, String region, String extension) throws TextContainerManagerException;

	/**
	 * Sets the industry values. User must have change permission of ActionPermission
	 * <code>com.sap.engine.services.textcontainer.security.TextContainerPermission</code>.
	 * It is assigned to role <code>Administrator</code> with the deployment of the Text Container service.
	 *
	 * @param industries  HashMap of industry values. These values will replace the existing ones.
	 * @throws TextContainerManagerException  n/a.
	 */
	public void setIndustryValues(HashMap<String, TextContainerIndustry> industries) throws TextContainerManagerException;

	/**
	 * Sets the region values. User must have change permission of ActionPermission
	 * <code>com.sap.engine.services.textcontainer.security.TextContainerPermission</code>.
	 * It is assigned to role <code>Administrator</code> with the deployment of the Text Container service.
	 *
	 * @param regions  HashMap of region values. These values will replace the existing ones.
	 * @throws TextContainerManagerException  n/a.
	 */
	public void setRegionValues(HashMap<String, TextContainerRegion> regions) throws TextContainerManagerException;

	/**
	 * Sets the extension values. User must have change permission of ActionPermission
	 * <code>com.sap.engine.services.textcontainer.security.TextContainerPermission</code>.
	 * It is assigned to role <code>Administrator</code> with the deployment of the Text Container service.
	 *
	 * @param extensions  HashMap of extension values. These values will replace the existing ones.
	 * @throws TextContainerManagerException  n/a.
	 */
	public void setExtensionValues(HashMap<String, TextContainerExtension> extensions) throws TextContainerManagerException;

	/**
	 * Sets the language values. User must have change permission of ActionPermission
	 * <code>com.sap.engine.services.textcontainer.security.TextContainerPermission</code>.
	 * It is assigned to role <code>Administrator</code> with the deployment of the Text Container service.
	 *
	 * @param languages  HashMap of language values. These values will replace the existing ones.
	 * @throws TextContainerManagerException  n/a.
	 */
	public void setLanguageValues(HashMap<String, TextContainerLanguage> languages) throws TextContainerManagerException;

	/**
	 * Sets the locale values. User must have change permission of ActionPermission
	 * <code>com.sap.engine.services.textcontainer.security.TextContainerPermission</code>.
	 * It is assigned to role <code>Administrator</code> with the deployment of the Text Container service.
	 *
	 * @param locales  HashMap of arrays of locale values. These values will replace the existing ones.
	 * @throws TextContainerManagerException  n/a.
	 */
	public void setLocaleValues(HashMap<String, TextContainerLocale[]> locales) throws TextContainerManagerException;

	/**
	 * Checks if the current session user has change permission of ActionPermission
	 * <code>com.sap.engine.services.textcontainer.security.TextContainerPermission</code>.
	 * It is assigned to role <code>Administrator</code> with the deployment of the Text Container service.
	 *
	 * @return Boolean value. 
	 * @throws TextContainerManagerException  n/a.
	 */
	public boolean checkAdministrationPermission() throws TextContainerManagerException;

}
