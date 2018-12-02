/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.jsp.tagext.TagLibraryValidator;

import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.GenerateJavaFile;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.tc.logging.Location;

/**
 * Singleton that contains parsed TLDs and instantiated TLD validators from JSTL and JSF.
 *
 */
public class JEEImplicitTlds {

  private static Location currentLocation = Location.getLocation(JEEImplicitTlds.class);
  private static Location traceLocation = LogContext.getLocationDeploy();

  private ClassLoader classLoader;

  private static final String[] paths = new String[] { "/META-INF/c.tld", "/META-INF/x.tld", "/META-INF/sql.tld", "/META-INF/fmt.tld", "/META-INF/fn.tld", "/META-INF/jsf_core.tld",
      "/META-INF/html_basic.tld",
      //old versions
      "/META-INF/x-1_0.tld", "/META-INF/x-1_0-rt.tld", "/META-INF/sql-1_0.tld", "/META-INF/sql-1_0-rt.tld", "/META-INF/fmt-1_0.tld", "/META-INF/fmt-1_0-rt.tld", "/META-INF/c-1_0.tld",
      "/META-INF/c-1_0-rt.tld" };

  /**
   * Almost duplicated arrays.
   * The first (descriptors) is returned for non-JSF application.
   * The second (descriptors_jsf) is returned for JSF applications. 
   * 'descriptors_jsf' contains all the references from the descriptors in 'descriptors'.
   */
  private TagLibDescriptor[] descriptors = null;

  private TagLibDescriptor[] descriptors_jsf = null;

  private HashMap<String, TagLibraryValidator> validators = new HashMap<String, TagLibraryValidator>(paths.length);

  private HashMap<String, TagLibraryValidator> validators_jsf = new HashMap<String, TagLibraryValidator>(paths.length);

  private volatile static JEEImplicitTlds instance = null;

  //move the par
  private JEEImplicitTlds() {
    this.classLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader("service:servlet_jsp");
  }

  /**
   * The only way to get instance of this class is to call this method.
   * @return
   */
  public static JEEImplicitTlds getInstance() {
    if (instance == null) {
      synchronized (JEEImplicitTlds.class) {
        if (instance == null) {
          instance = new JEEImplicitTlds();
        }
      }
    }
    return instance;
  }

  /**
   * Returns the single instance of the implicit TLD objects 
   * @param isJSFApplication
   * @return
   */
  public TagLibDescriptor[] getJEEImplicitTlds(boolean isJSFApplication) {
    if (descriptors == null) {
      synchronized (this) {
        if (descriptors == null) {
          initialize();
        }
      }
    }
    if (traceLocation.bePath()) {
      traceLocation.pathT("isJSF application : " + isJSFApplication);
    }
    if (isJSFApplication) {
      return descriptors_jsf;
    }
    return descriptors;
  }

  /**
   * Returns instances of the validators for the implicit TLDs.
   * @param isJSFApplication
   * @return
   */
  public HashMap<String, TagLibraryValidator> getTLDValidators(boolean isJSFApplication) {
    if (descriptors == null) {
      synchronized (this) {
        if (descriptors == null) {
          initialize();
        }
      }
    }
    if (traceLocation.bePath()) {
    	traceLocation.pathT("isJSF application : " + isJSFApplication);
    }
    if (isJSFApplication) {
      return validators_jsf;
    }
    return validators;
  }

  /**
   * Loads the TLDs and instantiates the validators.
   */
  private void initialize() {
    List<TagLibDescriptor> v_desc = new ArrayList<TagLibDescriptor>();
    List<TagLibDescriptor> v_desc_jsf = new ArrayList<TagLibDescriptor>();

    boolean parsed = false;
    for (int i = 0; i < paths.length; i++) {

      try {
        TagLibDescriptor tld_desc = new TagLibDescriptor(classLoader);
        parsed = loadTLDListenerFromClassLoaderResource(paths[i], tld_desc);
        // instantiate validator if available
        TagLibraryValidator tlv = GenerateJavaFile.instantiateValidator(classLoader, tld_desc);
        if (parsed) {
          if (!paths[i].equals("/META-INF/jsf_core.tld") && !paths[i].equals("/META-INF/html_basic.tld")) {
            v_desc.add(tld_desc);
            v_desc_jsf.add(tld_desc);
            if (tlv != null) {
              validators.put(tld_desc.getURI(), tlv);
              validators_jsf.put(tld_desc.getURI(), tlv);
            }
						if (traceLocation.bePath()) {
							traceLocation.pathT("added: " + tld_desc.getURI());
						}            
            continue;
          }
          if ((paths[i].equals("/META-INF/jsf_core.tld") || paths[i].equals("/META-INF/html_basic.tld"))) {
            v_desc_jsf.add(tld_desc);
            if (tlv != null) {
              validators_jsf.put(tld_desc.getURI(), tlv);
            }
            if (traceLocation.bePath()) {
            	traceLocation.pathT("added: " + tld_desc.getURI());
            }
          }
        } else {
					if (traceLocation.beError()) { 
						LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000424", "Failed to parse implicit TLD: {0}", new Object[]{tld_desc.getURI()}, null, null);
					}
        }

      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000001", "Error parsing implicit tld: {0}", new Object[]{paths[i]}, e, null, null);
      }
    }

    descriptors = v_desc.toArray(new TagLibDescriptor[v_desc.size()]);
    descriptors_jsf = v_desc_jsf.toArray(new TagLibDescriptor[v_desc_jsf.size()]);
  }

  private boolean loadTLDListenerFromClassLoaderResource(String tldFile, TagLibDescriptor tagLibDesc) throws Exception {
    boolean parsed = false;
    InputStream is = null;
    String tmpTLD = null;
    try {
      if (tldFile.charAt(0) == '/') {
        tmpTLD = tldFile.substring(1, tldFile.length());
      } else {
        tmpTLD = tldFile;
      }

      is = classLoader.getResourceAsStream(tmpTLD);
      if (is == null) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000002", 
          "Resource file [{0}] cannot be found.", new Object[]{tmpTLD}, null, null);
      } else {
        tagLibDesc.loadDescriptorFromStream(is);
        //Exception is thrown if the tld can not be parsed
        parsed = true;
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException io) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000489",
								"Error while closing InputStream for [{0}].", new Object[]{tldFile}, io, null, null);
          }
        }
      }
    }
    return parsed;
  }
}
