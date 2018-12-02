/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.util;

import com.sap.engine.frame.core.monitor.CoreMonitor;

/*
 * Utility class for constructing the version of the engine
 *
 * @author Nikolai Dokovski
 * @version 1.0
 */

public class EngineVersionUtil {
	
	/**
	 * Constructs the Engine Version returned in the Http Server header.
	 * The format of the engine version depends on the core layer implementation.
	 * 
	 * @see com.sap.engine.frame.core.monitor.CoreMonitor#getCoreMajorVersion()
	 * @see com.sap.engine.frame.core.monitor.CoreMonitor#getCoreMinorVersion()
	 */
	public static String getEngineVersion(CoreMonitor core){
		String version = core.getCoreMajorVersion() + "." + core.getCoreMinorVersion();
		return version;
	}
}

