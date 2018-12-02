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
package com.sap.engine.services.webservices.jaxws.handlers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Aug 21, 2006
 */
public class PredefinedCTSHandlerResolverUtil {
  private static final Location LOC = Location.getLocation(PredefinedCTSHandlerResolverUtil.class);
  
  private static Set<String> serviceClassNames;
  
  static {
    serviceClassNames = new HashSet();
    serviceClassNames.add("com.sun.ts.tests.jaxws.sharedclients.dlhelloproviderclient.HelloService");
    serviceClassNames.add("com.sun.ts.tests.webservices12.servlet.HandlerTest.client.HandlerTestService");
    serviceClassNames.add("com.sun.ts.tests.jaxws.sharedclients.rlhandlerclient.RLHandlerService");
  }
  
  public static void setHandlerResolverIfNeeded(Object service) {
    LOC.debugT("setHandlerResolverIfNeeded(): invoked with service '" + service + "'");
    try {
      String clsName = service.getClass().getName();
      if (! serviceClassNames.contains(clsName)) {
        LOC.debugT("setHandlerResolverIfNeeded(): service is unknown. Leaving...");
        return;
      }

      ClassLoader appLoader = service.getClass().getClassLoader();
      InputStream in = appLoader.getResourceAsStream("com/sun/ts/tests/common/vehicle/wsservlet/handler.xml");
      if (in == null) {
        LOC.debugT("setHandlerResolverIfNeeded(): no handler.xml found as a resource for 'wsservlet'.");
        in = appLoader.getResourceAsStream("com/sun/ts/tests/common/vehicle/wsejb/handler.xml");
        if (in == null) {
          LOC.debugT("setHandlerResolverIfNeeded(): no handler.xml found as a resource for 'wsejb'.");
          in = appLoader.getResourceAsStream("com/sun/ts/tests/common/vehicle/wsappclient/handler.xml");
          if (in == null) {
            LOC.debugT("setHandlerResolverIfNeeded(): no handler.xml found as a resource for 'wsappclient'.");
          }
        }
      }
      if (in == null) {
        LOC.debugT("setHandlerResolverIfNeeded(): no handler.xml found as a resource. Leaving...");
        return;
      }
      
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      byte[] arr = new byte[128];
      int b = -1;
      while ((b = in.read(arr)) != -1) {
        buf.write(arr, 0, b);
      }
      in.close();
      //load classes
      String cfg = buf.toString("utf-8");
      if (LOC.beDebug()) {
        LOC.debugT("setHandlerResolverIfNeeded(): loaded configuration: '" + cfg + "'");
      }
      final List<Class> hChainClasses = JAXWSHandlersEngine.getHandlersClassesFromHandlerChains(cfg, appLoader);
      HandlerResolver hr = new HandlerResolver() {
        public List<Handler> getHandlerChain(PortInfo arg0) {
          try {
            return JAXWSHandlersEngine.initializedHandlerChain(hChainClasses);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      };
      Service s = (Service) service;
      s.setHandlerResolver(hr);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
