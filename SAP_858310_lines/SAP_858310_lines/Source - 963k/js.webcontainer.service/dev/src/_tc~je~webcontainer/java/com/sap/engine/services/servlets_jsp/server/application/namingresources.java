/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.application;

import static com.sap.engine.lib.constants.PersistenceResRefConstants.*;
import static com.sap.engine.lib.injection.ReferenceObjectFactory.*;

import com.sap.engine.interfaces.webservices.server.management.WSClientReferencedStructure;
import com.sap.engine.interfaces.webservices.server.management.exception.WSBaseException;
import com.sap.engine.interfaces.webservices.server.wsclient.ServiceRefAddr;
import com.sap.engine.lib.constants.MapRefAddr;
import com.sap.engine.lib.descriptors.webj2eeengine.ServerComponentRefType;
import com.sap.engine.lib.descriptors5.javaee.*;
import com.sap.engine.lib.injection.CachingJNDIObjectFactory;
import com.sap.engine.lib.injection.FieldInjector;
import com.sap.engine.lib.injection.InjectionException;
import com.sap.engine.lib.injection.InjectionMatrix;
import com.sap.engine.lib.injection.Injector;
import com.sap.engine.lib.injection.JNDIObjectFactory;
import com.sap.engine.lib.injection.MethodInjector;
import com.sap.engine.lib.injection.ObjectFactory;
import com.sap.engine.lib.injection.ReferenceObjectFactory;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.utils.NamingUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.lang.String;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class NamingResources {
  //Authorization constants
  private static final String CONTAINER_AUTH = "Container";
  private static final String APPLICATION_AUTH = "Application";
  private static final String RESOURCE_UNSHAREABLE = "Unshareable";
  private static final String RESOURCE_SHAREABLE = "Shareable";

  private static final String RES_TYPE = "res-type";
  private static final String RES_NAME = "res-name";
  private static final String RES_AUTH = "res-auth";
  private static final String SHARING_SCOPE = "sharing-scope";
  private static final String TX_SUPPORT = "tx-support";
  private static final String INTERFACE_TYPE = "interfaceType";
  private static final String EJB_LINK = "ejb-link";
  private static final String EJB_JAR = "ejb-jar";
  private static final String EJB_REF_TYPE = "ejb-ref-type";
  private static final String JNDI_NAME = "jndi-name";
  private static final String REMOTE = "remote";
  private static final String HOME = "home";
  private static final String LOCAL = "local";
  private static final String LOCAL_HOME = "local-home";
  private static final String UNDETERMINED = "undetermined";

  private static final String COMPONENT_OBJECT_FACTORY = "com.sap.engine.services.jndi.ComponentObjectFactory";
  private static final String JAVAMAIL_SESSION_OBJECT_FACTORY = "com.sap.engine.services.javamail.server.MailSesionObjectFactory";
  
  private static final Location currentLocation = Location.getLocation(NamingResources.class);

  private String aliasForDirectory = null;
  private String aliasName = null;
  private String applicationName = null;
  private ClassLoader applicationClassLoader = null;
  private Hashtable<String, InjectionMatrix> injectionMatrixes = new Hashtable<String, InjectionMatrix>();

  private ServerComponentRefType[] componentRefs = null;
  private EjbLocalRefType[] ejbLocalRefs = null;
  private com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType[] addEjbLocalRefs = null;
  private EjbRefType[] ejbRefs = null;
  private com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType[] addEjbRefs = null;
  private EnvEntryType[] envEntries = null;
  private ResourceRefType[] resourceRefs = null;
  private com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType[] addResourceRefs = null;
  private ResourceEnvRefType[] resourceEnvRefs = null;
  private com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType[] addResourceEnvRefs = null;
  private ServiceRefType[] serviceRefTypes = null;
  private PersistenceContextRefType[] persistenceContextRefs = null;
  private PersistenceUnitRefType[] persistenceUnitRefs = null;
  private MessageDestinationRefType[] msgDestinationRefs = null;
  private com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType[] addMsgDestinationRefs = null;
  private MessageDestinationType[] msgDestinations = null;
  private com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType[] addMsgDestinations = null;

  public NamingResources(ApplicationContext applicationContext) {
    this.applicationName = applicationContext.getApplicationName();
    this.aliasName = applicationContext.getAliasName();
    this.aliasForDirectory = applicationContext.getAliasForDirectory();
    this.applicationClassLoader = applicationContext.getClassLoader();
  }//end of constructor

  public void initNamingReferences(WebDeploymentDescriptor webDesc) {
    initEJBReferences(webDesc);
    initLocalEJBReferences(webDesc);
    initServiceReferences(webDesc);
    initEnvEntries(webDesc);
    initResourceReferences(webDesc);
    initResourceEnvReferences(webDesc);
    initWebServicesReferences(webDesc);
    initPersistenceContextReferences(webDesc);
    initPersistenceUnitReferences(webDesc);
    initMsgDestinationReferences(webDesc);
    initMsgDestinations(webDesc);
  }//end of initNamingReferences(WebDeploymentDescriptor webDesc)

  public void bindNamingResources(Vector<String> warnings) throws DeploymentException {
    Context subContext = NamingUtils.createNamingContexts(aliasForDirectory, aliasName, applicationName);
    bindEJBReferences(subContext, warnings);
    bindLocalEJBReferences(subContext, warnings);
    bindServiceReferences(subContext, warnings);
    bindEnvEntries(subContext, warnings);
    bindResourceReferences(subContext, warnings);
    bindResourceEnvReferences(subContext, warnings);
    bindWebServicesReferences(subContext, warnings);
    bindWebServicesClients(subContext, warnings);
    bindPersistenceContextReferences(subContext, warnings);
    bindPersistenceUnitReferences(subContext, warnings);
    bindMsgDestinationReferences(subContext, warnings);
    try {
      NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/ORB", org.omg.CORBA.ORB.init(new String[0], null));
    } catch (NamingException e) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.CANNOT_BIND_ORB, new Object[]{aliasName, e.toString()}).toString());
    }
  }//end of bindNamingResources(Vector<String> warnings)

  private void initServiceReferences(WebDeploymentDescriptor webDesc) {
    ServerComponentRefType[] newComponentsRefs = webDesc.getWebJ2EEEngine().getServerComponentRef();

    if (newComponentsRefs == null) {
      return;
    }

    if (componentRefs == null) {
      componentRefs = newComponentsRefs;
    } else {
      Vector<ServerComponentRefType> componentRefsVector = new Vector<ServerComponentRefType>(Arrays.asList(componentRefs));
      componentRefsVector.addAll(Arrays.asList(newComponentsRefs));
      componentRefs = (ServerComponentRefType[]) componentRefsVector.toArray(new ServerComponentRefType[componentRefsVector.size()]);
    }
  }//end of initServiceReferences(WebDeploymentDescriptor webDesc)

  private void bindServiceReferences(Context subContext, Vector<String> warnings) {
    for (int k = 0; componentRefs != null && k < componentRefs.length; k++) {
      try {
        NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + componentRefs[k].getName(),
          new Reference(componentRefs[k].getJndiName(), COMPONENT_OBJECT_FACTORY, ""));
      } catch (NamingException e) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_COMPONENT_REFERENCE, new Object[]{componentRefs[k].getName(), aliasName, e.toString()}).toString());
      }
    }
  }//end of bindServiceReferences(Context subContext)

  private void initEnvEntries(WebDeploymentDescriptor webDesc) {
    EnvEntryType[] envEntry = webDesc.getEnvEntries();

    if (envEntry == null) {
      return;
    }

    if (envEntries == null) {
      envEntries = envEntry;
    } else {
      Vector<EnvEntryType> all = new Vector<EnvEntryType>(Arrays.asList(envEntries));
      all.addAll(Arrays.asList(envEntry));
      envEntries = (EnvEntryType[]) all.toArray(new EnvEntryType[all.size()]);
    }
  }//end of initEnvEntries(WebDeploymentDescriptor webDesc)

  private void bindEnvEntries(Context subContext, Vector<String> warnings) {
    for (int i = 0; envEntries != null && i < envEntries.length; i++) {
      //used for injection
      ObjectFactory factory = null;
      String envEntryName = "";
      try {
        if (envEntries[i].getEnvEntryValue() != null && envEntries[i].getEnvEntryName() != null) {
          envEntryName = envEntries[i].getEnvEntryName().get_value();

          //used for injection
          factory = new CachingJNDIObjectFactory("/java:comp/env/" + envEntryName, getEntryValue(envEntries[i]));
          createInjections(applicationClassLoader, envEntries[i].getInjectionTarget(), factory);

          //bind resource
          NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + envEntryName,
            getEntryValue(envEntries[i]));
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_ENV_ENTRY, new Object[]{envEntryName, aliasName, e.toString()}).toString());
      }
    }

    try {
      //bind resource
      NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/UserTransaction",
        new Reference("UserTransaction", COMPONENT_OBJECT_FACTORY, ""));
    } catch (NamingException e) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.CANNOT_BIND_USERTRANSACTION, new Object[]{aliasName, e.toString()}).toString());
    }
  }//end of bindEnvEntries(Context subContext)

  private void initResourceReferences(WebDeploymentDescriptor webDesc) {
    ResourceRefType[] newResourceRefs = webDesc.getResReferences();

    if (newResourceRefs == null) {
      return;
    }

    if (resourceRefs == null) {
      resourceRefs = newResourceRefs;
      addResourceRefs = new com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType[resourceRefs.length];
      for (int i = 0; i < resourceRefs.length; i++) {
        addResourceRefs[i] = webDesc.getResourceReferenceFromAdditional(resourceRefs[i]);
      }
    } else {
      Vector<ResourceRefType> all = new Vector<ResourceRefType>(Arrays.asList(resourceRefs));
      all.addAll(Arrays.asList(newResourceRefs));
      resourceRefs = (ResourceRefType[]) all.toArray(new ResourceRefType[all.size()]);

      Vector<com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType> temp = new Vector<com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType>(Arrays.asList(addResourceRefs));
      for (int i = 0; i < newResourceRefs.length; i++) {
        temp.add(webDesc.getResourceReferenceFromAdditional(newResourceRefs[i]));
      }
      addResourceRefs = (com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType[]) temp.toArray(new com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType[temp.size()]);
    }
  }//end of initResourceReferences(WebDeploymentDescriptor webDesc)

  private void bindResourceReferences(Context subContext, Vector<String> warnings) {
    for (int k = 0; resourceRefs != null && k < resourceRefs.length; k++) {
      String resRefName = resourceRefs[k].getResRefName() != null ? resourceRefs[k].getResRefName().get_value() : null;

      String resourceType = null;
      if (resourceRefs[k].getResType() != null) {
        resourceType = resourceRefs[k].getResType().get_value();
      } else if (resourceRefs[k].getInjectionTarget() != null) {
        InjectionTargetType[] injectionTargets = resourceRefs[k].getInjectionTarget();
        for (int j = 0; j < injectionTargets.length; j++) {
          Injector injector = createInjector(applicationClassLoader, injectionTargets[j]);
          resourceType = injector.getTargetType();
          break;
        }
      }

      com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType addResRef = addResourceRefs[k];
      String resourceName = (addResRef != null && addResRef.getResLink() != null) ? addResRef.getResLink() : null;
      if (resourceName == null) {
        //use mapped name from the standard descriptor
        resourceName = (resourceRefs[k].getMappedName() != null) ? resourceRefs[k].getMappedName().get_value() : null;
      }

      if (resourceType != null) {
        //used for injection
        ObjectFactory factory = null;
        if (resourceType.equals("org.omg.CORBA_2_3.ORB")) {
          //used for injection
          factory = new CachingJNDIObjectFactory("/java:comp/ORB");
        } else if (resourceType.equals("javax.mail.Session")) { //mail connection
          try {
            //used for injection
            factory = new CachingJNDIObjectFactory("/java:comp/env/" + resRefName);

            //bind resource
            NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + resRefName,
              new Reference("java:comp/env/mail/MailSession", JAVAMAIL_SESSION_OBJECT_FACTORY, ""));
          } catch (NamingException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_BIND_RESOURCE_REFERENCE, new Object[]{resRefName, aliasName, e.toString()}).toString());
          }
        } else if (resourceType.equals("java.net.URL")) { //URL
          try {
            if (resourceName != null) {
              //used for injection
              factory = new CachingJNDIObjectFactory("/java:comp/env/" + resRefName);

              //bind resource
              NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + resRefName,
                new URL(resourceName));
            }
          } catch (MalformedURLException muex) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.RESOURCE_IN_WEB_APPLICATION_WILL_NOT_BE_USED_REASON_IS,
              new Object[]{resRefName, aliasName, muex.toString()}).toString());
          } catch (NamingException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_BIND_RESOURCE_REFERENCE, new Object[]{resRefName, aliasName, e.toString()}).toString());
          }
        } else if (resourceType.equals("javax.xml.ws.WebServiceContext")) { //@Resource WebServiceContext
          //used for injection
          ReferenceObjectFactory factory1 = null;
          ClassLoader factoryLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(LIBRARY_WEBSERVICES_LOADER);
          Reference ref = null;
          try {
            factory1 = new ReferenceObjectFactory(resourceType, WEBSERVICE_CONTEXT_OBJECT_FACTORY, LIBRARY_WEBSERVICES_LOADER, factoryLoader);
          } catch (InjectionException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION, new Object[]{aliasName, e.toString()}).toString());
          }
          if (factory1 != null) {
            ref = factory1.getReference();
          } else {
            ref = new Reference(resourceType, WEBSERVICE_CONTEXT_OBJECT_FACTORY, LIBRARY_WEBSERVICES_LOADER);
          }
          try {
            //used for injection
            factory = factory1;

            //bind resource
            NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + resRefName, ref);
          } catch (NamingException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_BIND_RESOURCE_REFERENCE, new Object[]{resRefName, aliasName, e.toString()}).toString());
          }
        } else if (resourceType.equals("javax.transaction.TransactionSynchronizationRegistry")) {
        	/*
        	 * The TransactionSynchronizationRegistry is not a resource adapter, that's why it cannot
        	 * be processed by the ReferenceObjectFactory.RESOURCE_OBJECT_FACTORY, which can process
        	 * only references for resource adapters. (see JavaEE 5, EE.5.10, CSN 1414830 2007).
        	 * The object is bound to /java:comp/TransactionSynchronizationRegistry by the connector service.
        	 */
					try {
						if (resourceName == null || resourceName.length() == 0) {
							resourceName = resRefName;
						}
						// used for injection - look up the object from root - it is bound there by the connector service
						factory = new CachingJNDIObjectFactory(
								"/java:comp/TransactionSynchronizationRegistry");

						Reference ref = new Reference("java:comp/TransactionSynchronizationRegistry", COMPONENT_OBJECT_FACTORY, "");
						ref.add(new StringRefAddr(RES_TYPE, resourceType));
						ref.add(new StringRefAddr(RES_NAME, resourceName));

						// bind resource also in the web component's subcontext
						NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/TransactionSynchronizationRegistry", ref);
					} catch (NamingException e) {
						warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
								WebWarningException.CANNOT_BIND_RESOURCE_REFERENCE, new Object[] { resRefName, aliasName, e.toString() }).toString());
					}
        } else {
        	//all other resources
          if (resourceName == null || resourceName.length() == 0) {
            resourceName = resRefName;
          }

          //used for injection
          ReferenceObjectFactory factory1 = null;
          ClassLoader factoryLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(SERVICE_CONNECTOR_LOADER);
          Reference ref = null;

          try {
            factory1 = new ReferenceObjectFactory(resourceType, RESOURCE_OBJECT_FACTORY, SERVICE_CONNECTOR_LOADER, factoryLoader);
          } catch (InjectionException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION, new Object[]{aliasName, e.toString()}).toString());
          }
          if (factory1 != null) {
            ref = factory1.getReference();
          } else {
            ref = new Reference(resourceType, RESOURCE_OBJECT_FACTORY, SERVICE_CONNECTOR_LOADER);
          }

          ref.add(new StringRefAddr(RES_TYPE, resourceType));
          ref.add(new StringRefAddr(RES_NAME, resourceName));

          if (resourceRefs[k].getResAuth() != null && resourceRefs[k].getResAuth().get_value() != null && resourceRefs[k].getResAuth().get_value().equalsIgnoreCase(CONTAINER_AUTH)) {
            ref.add(new StringRefAddr(RES_AUTH, CONTAINER_AUTH));
          } else {
            ref.add(new StringRefAddr(RES_AUTH, APPLICATION_AUTH));
          }

          if (resourceRefs[k].getResSharingScope() != null && resourceRefs[k].getResSharingScope().get_value() != null && resourceRefs[k].getResSharingScope().get_value().equalsIgnoreCase(RESOURCE_UNSHAREABLE)) {
            ref.add(new StringRefAddr(SHARING_SCOPE, RESOURCE_UNSHAREABLE));
          } else {
            ref.add(new StringRefAddr(SHARING_SCOPE, RESOURCE_SHAREABLE));
          }

          if (addResRef != null && addResRef.getNonTransactional() != null) {
            ref.add(new StringRefAddr(TX_SUPPORT, "false")); //by default transactional
          }

          try {
            //used for injection
            factory = factory1;

            //bind resource
            NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + resRefName, ref);
          } catch (NamingException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_BIND_RESOURCE_REFERENCE, new Object[]{resRefName, aliasName, e.toString()}).toString());
          }
        }

        if (factory != null) {
          createInjections(applicationClassLoader, resourceRefs[k].getInjectionTarget(), factory);
        }
      } else {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_RESOURCE_REFERENCE, new Object[]{resRefName, aliasName, "Resource type is missing."}).toString());
      }
    }
  }//end of bindResourceReferences(Context subContext, Vector<String> warnings)

  private void initResourceEnvReferences(WebDeploymentDescriptor webDesc) {
    ResourceEnvRefType[] newResourceRef = webDesc.getResEnvReferences();

    if (newResourceRef == null) {
      return;
    }

    if (resourceEnvRefs == null) {
      resourceEnvRefs = newResourceRef;
      addResourceEnvRefs = new com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType[resourceEnvRefs.length];
      for (int i = 0; i < resourceEnvRefs.length; i++) {
        addResourceEnvRefs[i] = webDesc.getResEnvRefFromAdditional(resourceEnvRefs[i]);
      }
    } else {
      Vector<ResourceEnvRefType> all = new Vector<ResourceEnvRefType>(Arrays.asList(resourceEnvRefs));
      all.addAll(Arrays.asList(newResourceRef));
      resourceEnvRefs = (ResourceEnvRefType[]) all.toArray(new ResourceEnvRefType[all.size()]);

      Vector<com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType> temp = new Vector<com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType>(Arrays.asList(addResourceEnvRefs));
      for (int i = 0; i < newResourceRef.length; i++) {
        temp.add(webDesc.getResEnvRefFromAdditional(newResourceRef[i]));
      }
      addResourceEnvRefs = (com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType[]) temp.toArray(new com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType[temp.size()]);
    }
  }//end of initResourceEnvReferences(WebDeploymentDescriptor webDesc)

  private void bindResourceEnvReferences(Context subContext, Vector<String> warnings) {
    for (int k = 0; resourceEnvRefs != null && k < resourceEnvRefs.length; k++) {
      if (resourceEnvRefs[k] == null) {
        continue;
      }

      String resourceEnvRefName = resourceEnvRefs[k].getResourceEnvRefName() != null ? resourceEnvRefs[k].getResourceEnvRefName().get_value() : null;

      String resourceEnvType = null;
      if (resourceEnvRefs[k].getResourceEnvRefType() != null) {
        resourceEnvType = resourceEnvRefs[k].getResourceEnvRefType().get_value();
      } else if (resourceEnvRefs[k].getInjectionTarget() != null) {
        InjectionTargetType[] injectionTargets = resourceEnvRefs[k].getInjectionTarget();
        for (int j = 0; j < injectionTargets.length; j++) {
          Injector injector = createInjector(applicationClassLoader, injectionTargets[j]);
          resourceEnvType = injector.getTargetType();
          break;
        }
      }

      com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType addResRef = addResourceEnvRefs[k];
      String jndiName = (addResRef != null && addResRef.getJndiName() != null) ? addResRef.getJndiName() : null;
      if (jndiName == null && resourceEnvRefName != null) {
        jndiName = (resourceEnvRefs[k].getMappedName() != null) ? resourceEnvRefs[k].getMappedName().get_value() : resourceEnvRefName;
      }

      if (resourceEnvType != null && resourceEnvType.equals("javax.jms.Queue")) { //jms Queue backwards compatibility
        jndiName = "jmsQueues/" + jndiName;
      } else if (resourceEnvType != null && resourceEnvType.equals("javax.jms.Topic")) { //jms Topic backwards compatibility
        jndiName = "jmsTopics/" + jndiName;
      } else if (resourceEnvType == null || (!resourceEnvType.equals("javax.transaction.UserTransaction") && !resourceEnvType.equals("javax.resource.cci.InteractionSpec"))) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.UNRECOGNIZED_RESOURCEENVTYPE_IN_WEB_APPLICATION_IT_WILL_BE_IGNORED,
          new Object[]{resourceEnvType, aliasName}).toString());
      }

      //used for injection
      ObjectFactory factory = null;
      try {
        boolean skipBinding = false;
        //used for injection
        if (resourceEnvType != null && resourceEnvType.equals("javax.transaction.UserTransaction")) {
          factory = new CachingJNDIObjectFactory("/java:comp/UserTransaction");
          skipBinding = true;
        } else {
          factory = new CachingJNDIObjectFactory("/java:comp/env/" + resourceEnvRefName);
        }

        if (!skipBinding) {
          //bind resource
          NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + resourceEnvRefName,
            new Reference(jndiName, COMPONENT_OBJECT_FACTORY, ""));
        }

        createInjections(applicationClassLoader, resourceEnvRefs[k].getInjectionTarget(), factory);
      } catch (NamingException e) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_RESOURCE_ENV_REFERENCE, new Object[]{resourceEnvRefName, aliasName, e.toString()}).toString());
      }
    }
  }//end of bindResourceEnvReferences(Context subContext, Vector<String> warnings)

  private void initEJBReferences(WebDeploymentDescriptor webDesc) {
    EjbRefType[] ejbRefsTemp = webDesc.getEjbRefs();

    if (ejbRefsTemp == null) {
      return;
    }

    if (ejbRefs == null) {
      ejbRefs = ejbRefsTemp;
      addEjbRefs = new com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType[ejbRefs.length];
      for (int i = 0; i < ejbRefs.length; i++) {
        addEjbRefs[i] = webDesc.getEjbRefFromAdditional(ejbRefs[i]);
      }
    } else {
      Vector<EjbRefType> all = new Vector<EjbRefType>(Arrays.asList(ejbRefs));
      Vector<com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType> allAdd = new Vector<com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType>(Arrays.asList(addEjbRefs));

      int j = 0;
      for (int i = 0; i < ejbRefsTemp.length; i++) {
        for (j = 0; j < ejbRefs.length; j++) {
          if (ejbRefsTemp[i].equals(ejbRefs[j])) {
            break;
          }
        }
        if (j == ejbRefs.length) {
          all.add(ejbRefsTemp[i]);
          allAdd.add(webDesc.getEjbRefFromAdditional(ejbRefsTemp[i]));
        }
      }

      ejbRefs = (EjbRefType[]) all.toArray(new EjbRefType[all.size()]);
      addEjbRefs = (com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType[]) allAdd.toArray(new com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType[allAdd.size()]);
    }
  }//end of initEJBReferences(WebDeploymentDescriptor webDesc)

  private void bindEJBReferences(Context subContext, Vector<String> warnings) {
    for (int k = 0; ejbRefs != null && k < ejbRefs.length; k++) {
      if (ejbRefs[k] != null) {
        String ejbRefName = ejbRefs[k].getEjbRefName() != null ? ejbRefs[k].getEjbRefName().get_value() : null;

        com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType addEjbRef = addEjbRefs[k];
        String realBeanName = (addEjbRef != null && addEjbRef.getJndiName() != null) ? addEjbRef.getJndiName() : null;
        if (realBeanName == null) {
          //use mapped name from the standard descriptor
          realBeanName = (ejbRefs[k].getMappedName() != null) ? ejbRefs[k].getMappedName().get_value() : null;
        }

        // for interoperability refs
        if ((realBeanName != null) && realBeanName.startsWith("corbaname:")) {
          try {
            NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + ejbRefName,
              new Reference(realBeanName, COMPONENT_OBJECT_FACTORY, null));
          } catch (NamingException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_BIND_EJB_REFERENCE, new Object[]{ejbRefName, realBeanName, aliasName, e.toString()}).toString());
          }
          continue;
        }

        String ejbRefType = ejbRefs[k].getEjbRefType() != null ? ejbRefs[k].getEjbRefType().get_value() : null;

        String ejbLink = ejbRefs[k].getEjbLink() != null ? ejbRefs[k].getEjbLink().get_value() : null;

        String remoteBeanType = ejbRefs[k].getRemote() != null ? ejbRefs[k].getRemote().get_value() : null;

        String homeBeanType = ejbRefs[k].getHome() != null ? ejbRefs[k].getHome().get_value() : null;

        // this is the name of the naming reference
        String beanName = null;

        // could be 'remote' or 'undetermined'
        String interfaceType = null;

        // could be 'home', 'remote' or 'undetermined'
        String interfaceNameType = null;

        // this should be the corresponding name of the home interface, remote interface or annotation's beanInterface
        String interfaceName = null;

        List<StringRefAddr> additionalParams = new ArrayList<StringRefAddr>();

        if (homeBeanType != null && homeBeanType.equals(remoteBeanType)) {
          // this is an annotation with beanInterface attribute
          interfaceType = UNDETERMINED;
          interfaceNameType = UNDETERMINED;
          interfaceName = homeBeanType;
          beanName = interfaceName.substring(interfaceName.lastIndexOf('.') + 1);
        } else if (homeBeanType != null) {
          // this is a xml descriptor with specified home interface
          interfaceType = REMOTE;
          interfaceNameType = HOME;
          interfaceName = homeBeanType;
          beanName = interfaceName.substring(interfaceName.lastIndexOf('.') + 1);

          if (remoteBeanType != null) {
            StringRefAddr remoteParam = new StringRefAddr(REMOTE, remoteBeanType);
            additionalParams.add(remoteParam);
          }
        } else if (remoteBeanType != null) {
          // this is a xml descriptor with specified remote interface, but without specified home interface
          interfaceType = REMOTE;
          interfaceNameType = REMOTE;
          interfaceName = remoteBeanType;
          beanName = interfaceName.substring(interfaceName.lastIndexOf('.') + 1);
        }

        //used for injection
        ReferenceObjectFactory factory = null;
        ClassLoader factoryLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(SERVICE_EJB_LOADER);
        try {
          factory = new ReferenceObjectFactory(beanName, EJB_OBJECT_FACTORY, SERVICE_EJB_LOADER, factoryLoader);
        } catch (InjectionException ie) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION, new Object[]{aliasName, ie.toString()}).toString());
        }

        Reference ref = null;
        if (factory != null) {
          ref = factory.getReference();
        } else {
          ref = new Reference(beanName, EJB_OBJECT_FACTORY, "");
        }
        ref.add(new StringRefAddr("clientAppName", applicationName));//optimization
        ref.add(new StringRefAddr(INTERFACE_TYPE, interfaceType));
        ref.add(new StringRefAddr(interfaceNameType, interfaceName));

        if (ejbLink != null) {
          int indexOfDies = ejbLink.indexOf('#');
          if (indexOfDies != -1) {
            String ejbJarName = ejbLink.substring(0, indexOfDies);
            String realEjbLinkValue = ejbLink.substring(indexOfDies + 1);

            StringRefAddr ejbLinkParam = new StringRefAddr(EJB_LINK, realEjbLinkValue);
            additionalParams.add(ejbLinkParam);

            StringRefAddr ejbJarParam = new StringRefAddr(EJB_JAR, ejbJarName);
            additionalParams.add(ejbJarParam);

          } else {
            StringRefAddr ejbLinkParam = new StringRefAddr(EJB_LINK, ejbLink);
            additionalParams.add(ejbLinkParam);
          }
        }

        if (ejbRefType != null) {
          StringRefAddr ejbRefTypeParam = new StringRefAddr(EJB_REF_TYPE, ejbRefType);
          additionalParams.add(ejbRefTypeParam);
        }

        if (realBeanName != null) {
          StringRefAddr jndiNameParam = new StringRefAddr(JNDI_NAME, realBeanName);
          additionalParams.add(jndiNameParam);
        }

        for (StringRefAddr addParam : additionalParams) {
          ref.add(addParam);
        }

        try {
          //bind resource
          NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + ejbRefName, ref);
        } catch (NamingException e) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.CANNOT_BIND_EJB_REFERENCE, new Object[]{ejbRefName, realBeanName, aliasName, e.toString()}).toString());
        }

        //used for injection
        if (factory != null) {
          createInjections(applicationClassLoader, ejbRefs[k].getInjectionTarget(), factory);
        }
      }
    }
  }//end of bindEJBReferences(Context subContext, Vector<String> warnings)

  private void initLocalEJBReferences(WebDeploymentDescriptor webDesc) {
    EjbLocalRefType[] ejbLocalRefsTemp = webDesc.getEjbLocalRefs();

    if (ejbLocalRefsTemp == null) {
      return;
    }

    if (ejbLocalRefs == null) {
      ejbLocalRefs = ejbLocalRefsTemp;
      addEjbLocalRefs = new com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType[ejbLocalRefs.length];
      for (int i = 0; i < addEjbLocalRefs.length; i++) {
        addEjbLocalRefs[i] = webDesc.getEjbLocalRefFromAdditional(ejbLocalRefs[i]);
      }
    } else {
      Vector<EjbLocalRefType> all = new Vector<EjbLocalRefType>(Arrays.asList(ejbLocalRefs));
      Vector<com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType> allAdd = new Vector<com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType>(Arrays.asList(addEjbLocalRefs));

      int j = 0;
      for (int i = 0; i < ejbLocalRefsTemp.length; i++) {
        for (j = 0; j < ejbLocalRefs.length; j++) {
          if (ejbLocalRefsTemp[i].equals(ejbLocalRefs[j])) {
            break;
          }
        }
        if (j == ejbLocalRefs.length) {
          all.add(ejbLocalRefsTemp[i]);
          allAdd.add(webDesc.getEjbLocalRefFromAdditional(ejbLocalRefsTemp[i]));
        }
      }

      ejbLocalRefs = (EjbLocalRefType[]) all.toArray(new EjbLocalRefType[all.size()]);
      addEjbLocalRefs = (com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType[]) allAdd.toArray(new com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType[allAdd.size()]);
    }
  }//end of initLocalEJBReferences(WebDeploymentDescriptor webDesc)

  private void bindLocalEJBReferences(Context subContext, Vector<String> warnings) {
    for (int k = 0; ejbLocalRefs != null && k < ejbLocalRefs.length; k++) {
      if (ejbLocalRefs[k] != null) {
        String ejbRefName = ejbLocalRefs[k].getEjbRefName() != null ? ejbLocalRefs[k].getEjbRefName().get_value() : null;

        com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType addEjbLocalRef = addEjbLocalRefs[k];
        String realBeanName = (addEjbLocalRef != null && addEjbLocalRef.getJndiName() != null) ? addEjbLocalRef.getJndiName() : null;
        if (realBeanName == null) {
          //use mapped name from the standard descriptor
          realBeanName = (ejbLocalRefs[k].getMappedName() != null) ? ejbLocalRefs[k].getMappedName().get_value() : null;
        }

        // for interoperability refs
        if ((realBeanName != null) && realBeanName.startsWith("corbaname:")) {
          try {
            NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + ejbRefName,
              new Reference(realBeanName, COMPONENT_OBJECT_FACTORY, null));
          } catch (NamingException e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_BIND_EJB_LOCAL_REFERENCE, new Object[]{ejbRefName, realBeanName, aliasName, e.toString()}).toString());
          }
          continue;
        }

        String ejbRefType = ejbLocalRefs[k].getEjbRefType() != null ? ejbLocalRefs[k].getEjbRefType().get_value() : null;

        String ejbLink = ejbLocalRefs[k].getEjbLink() != null ? ejbLocalRefs[k].getEjbLink().get_value() : null;

        String localBeanType = ejbLocalRefs[k].getLocal() != null ? ejbLocalRefs[k].getLocal().get_value() : null;

        String localHomeBeanType = ejbLocalRefs[k].getLocalHome() != null ? ejbLocalRefs[k].getLocalHome().get_value() : null;

        // this is the name of the naming reference
        String beanName = null;

        // could be 'local' or 'undetermined'
        String interfaceType = null;

        // could be 'home', 'local' or 'undetermined'
        String interfaceNameType = null;

        // this should be the corresponding name of the local home interface, local interface or annotation's beanInterface
        String interfaceName = null;

        List<StringRefAddr> additionalParams = new ArrayList<StringRefAddr>();

        if (localHomeBeanType != null && localHomeBeanType.equals(localBeanType)) {
          // this is an annotation with beanInterface attribute
          interfaceType = UNDETERMINED;
          interfaceNameType = UNDETERMINED;
          interfaceName = localHomeBeanType;
          beanName = interfaceName.substring(interfaceName.lastIndexOf('.') + 1);
        } else if (localHomeBeanType != null) {
          // this is a xml descriptor with specified home interface
          interfaceType = LOCAL;
          interfaceNameType = LOCAL_HOME;
          interfaceName = localHomeBeanType;
          beanName = interfaceName.substring(interfaceName.lastIndexOf('.') + 1);

          if (localBeanType != null) {
            StringRefAddr localParam = new StringRefAddr(LOCAL, localBeanType);
            additionalParams.add(localParam);
          }
        } else if (localBeanType != null) {
          // this is a xml descriptor with specified remote interface, but without specified home interface
          interfaceType = LOCAL;
          interfaceNameType = LOCAL;
          interfaceName = localBeanType;
          beanName = interfaceName.substring(interfaceName.lastIndexOf('.') + 1);
        }

        //used for injection
        ReferenceObjectFactory factory = null;
        ClassLoader factoryLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(SERVICE_EJB_LOADER);
        try {
          factory = new ReferenceObjectFactory(beanName, EJB_OBJECT_FACTORY, SERVICE_EJB_LOADER, factoryLoader);
        } catch (InjectionException ie) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION, new Object[]{aliasName, ie.toString()}).toString());
        }

        Reference ref = null;
        if (factory != null) {
          ref = factory.getReference();
        } else {
          ref = new Reference(beanName, EJB_OBJECT_FACTORY, "");
        }
        ref.add(new StringRefAddr("clientAppName", applicationName));//optimization
        ref.add(new StringRefAddr(INTERFACE_TYPE, interfaceType));
        ref.add(new StringRefAddr(interfaceNameType, interfaceName));

        if (ejbLink != null) {
          int indexOfDies = ejbLink.indexOf('#');
          if (indexOfDies != -1) {
            String ejbJarName = ejbLink.substring(0, indexOfDies);
            String realEjbLinkValue = ejbLink.substring(indexOfDies + 1);

            StringRefAddr ejbLinkParam = new StringRefAddr(EJB_LINK, realEjbLinkValue);
            additionalParams.add(ejbLinkParam);

            StringRefAddr ejbJarParam = new StringRefAddr(EJB_JAR, ejbJarName);
            additionalParams.add(ejbJarParam);

          } else {
            StringRefAddr ejbLinkParam = new StringRefAddr(EJB_LINK, ejbLink);
            additionalParams.add(ejbLinkParam);
          }
        }

        if (ejbRefType != null) {
          StringRefAddr ejbRefTypeParam = new StringRefAddr(EJB_REF_TYPE, ejbRefType);
          additionalParams.add(ejbRefTypeParam);
        }

        if (realBeanName != null) {
          StringRefAddr jndiNameParam = new StringRefAddr(JNDI_NAME, realBeanName);
          additionalParams.add(jndiNameParam);
        }

        for (StringRefAddr addParam : additionalParams) {
          ref.add(addParam);
        }

        try {
          //bind resource
          NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + ejbRefName, ref);
        } catch (NamingException e) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.CANNOT_BIND_EJB_LOCAL_REFERENCE, new Object[]{ejbRefName, realBeanName, aliasName, e.toString()}).toString());
        }

        //used for injection
        if (factory != null) {
          createInjections(applicationClassLoader, ejbLocalRefs[k].getInjectionTarget(), factory);
        }
      }
    }
  }//end of bindLocalEJBReferences(Context subContext, Vector<String> warnings)

  private void initWebServicesReferences(WebDeploymentDescriptor webDesc) {
    ServiceRefType[] newServiceRefTypes = webDesc.getServiceRefs();

    if (newServiceRefTypes == null) {
      return;
    }

    if (serviceRefTypes == null) {
      serviceRefTypes = newServiceRefTypes;
    } else {
      Vector<ServiceRefType> all = new Vector<ServiceRefType>(Arrays.asList(serviceRefTypes));
      all.addAll(Arrays.asList(newServiceRefTypes));
      serviceRefTypes = (ServiceRefType[]) all.toArray(new ServiceRefType[all.size()]);
    }
  }//end of initWebServicesReferences(WebDeploymentDescriptor webDesc)

  private void bindWebServicesReferences(Context subContext, Vector<String> warnings) {
    //Support of old Web Services Clients. Backwards compatibility reasons.
    if (ServiceContext.getServiceContext().getWSManager() == null) {
      return;
    }

    WSClientReferencedStructure referencedStructure[] = null;
    try {
      referencedStructure = ServiceContext.getServiceContext().getWSManager().getWSClientReferencedObjects(applicationName);
    } catch (WSBaseException ws) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.CANNOT_BIND_WEB_SERVICES_CLIENTS_FOR_APPLICATION_ERROR_IS,
        new Object[]{"", applicationName, ws.toString()}).toString());
      return;
    }

    if (referencedStructure == null || referencedStructure.length == 0) {
      return;
    }

    String jndiLinkName = null;
    for (int i = 0; i < referencedStructure.length; i++) {
      if (referencedStructure[i].getContextRoot().equals(aliasName)) {
        jndiLinkName = referencedStructure[i].getJndiLinkName();
        try {
          NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + jndiLinkName, referencedStructure[i].getReferencedObject());
        } catch (NamingException ne) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.CANNOT_BIND_WEB_SERVICES_CLIENTS_FOR_APPLICATION_ERROR_IS,
            new Object[]{jndiLinkName, applicationName, ne.toString()}).toString());
        }
      }
    }
  }//end of bindWebServicesReferences(Context subContext, Vector<String> warnings)

  private void bindWebServicesClients(Context subContext, Vector<String> warnings) {
    //Support of Web Services Clients compatible with j2ee 1.4
    for (int k = 0; serviceRefTypes != null && k < serviceRefTypes.length; k++) {
      String serviceRefName = (serviceRefTypes[k].getServiceRefName() != null) ? serviceRefTypes[k].getServiceRefName().get_value() : null;

      String serviceInterface = (serviceRefTypes[k].getServiceInterface() != null) ? serviceRefTypes[k].getServiceInterface().get_value() : null;

      if (serviceRefName != null && serviceInterface != null && !serviceRefName.equals("") && !serviceInterface.equals("")) {
        //used for injection
        ReferenceObjectFactory factory = null;
        ClassLoader factoryLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(INTERFACE_WEBSERVICE_LOADER);
        try {
          factory = new ReferenceObjectFactory(serviceInterface, WEBSERVICE_OBJECT_FACTORY, INTERFACE_WEBSERVICE_LOADER, factoryLoader);
        } catch (InjectionException ie) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION, new Object[]{aliasName, ie.toString()}).toString());
        }

        String warName = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getWarName(aliasName, applicationName);
        Class serviceIntClass = null;
        try {
          serviceIntClass = applicationClassLoader.loadClass(serviceInterface);
        } catch (ClassNotFoundException e) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.CANNOT_BIND_WEB_SERVICES_CLIENTS_FOR_APPLICATION_ERROR_IS,
            new Object[]{"", applicationName, e.toString()}).toString());
          continue;
        }
        ServiceRefAddr serviceRefAddr = new ServiceRefAddr(applicationName, ServiceRefAddr.WEB_MODULE, warName,
          aliasName, serviceRefName, serviceIntClass);

        Reference ref;
        if (factory != null) {
          ref = factory.getReference();
        } else {
          ref = new Reference(serviceInterface, WEBSERVICE_OBJECT_FACTORY, INTERFACE_WEBSERVICE_LOADER);
        }

        ref.add(serviceRefAddr);
        try {
          //bind resource
          NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + serviceRefName, ref);
        } catch (NamingException e) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.CANNOT_BIND_WEB_SERVICES_CLIENTS_FOR_APPLICATION_ERROR_IS,
            new Object[]{serviceRefName, applicationName, e.toString()}).toString());
        }

        if (factory != null) {
          createInjections(applicationClassLoader, serviceRefTypes[k].getInjectionTarget(), factory);
        }
      } else {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_WEBSERVICE_CLIENT, new Object[]{aliasName}).toString());
      }
    }
  }//end of bindWebServicesClients(Context subContext, Vector<String> warnings)

  private void initPersistenceContextReferences(WebDeploymentDescriptor webDesc) {
    PersistenceContextRefType[] newPersistenceContextRefTypes = webDesc.getPersistenceContextRefs();

    if (newPersistenceContextRefTypes == null) {
      return;
    }

    if (persistenceContextRefs == null) {
      persistenceContextRefs = newPersistenceContextRefTypes;
    } else {
      Vector<PersistenceContextRefType> all = new Vector<PersistenceContextRefType>(Arrays.asList(persistenceContextRefs));
      all.addAll(Arrays.asList(newPersistenceContextRefTypes));
      persistenceContextRefs = (PersistenceContextRefType[]) all.toArray(new PersistenceContextRefType[all.size()]);
    }
  }//end of initPersistenceContextReferences(WebDeploymentDescriptor webDesc)

  private void bindPersistenceContextReferences(Context subContext, Vector<String> warnings) {
    for (int i = 0; persistenceContextRefs != null && i < persistenceContextRefs.length; i++) {
      if (persistenceContextRefs[i] == null) {
        continue;
      }

      String persistenceContextRefName = persistenceContextRefs[i].getPersistenceContextRefName() != null ?
      	persistenceContextRefs[i].getPersistenceContextRefName().get_value() : null;
      String persistenceUnitName = persistenceContextRefs[i].getPersistenceUnitName() != null ?
      	persistenceContextRefs[i].getPersistenceUnitName().get_value() : "";
      String persistenceContextType = persistenceContextRefs[i].getPersistenceContextType() != null ?
        persistenceContextRefs[i].getPersistenceContextType().get_value().toUpperCase() : TRANSACTION;
      PropertyType[] persistenceProperties = persistenceContextRefs[i].getPersistenceProperty();

      //used for injection
      ReferenceObjectFactory factory = null;
      ClassLoader factoryLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(SERVICE_ORPERSISTENCE_LOADER);
      try {
        factory = new ReferenceObjectFactory(ENTITY_MANAGER, ORPERSISTENCE_OBJECT_FACTORY, SERVICE_ORPERSISTENCE_LOADER, factoryLoader);
      } catch (InjectionException ie) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION, new Object[]{aliasName, ie.toString()}).toString());
      }

      String warName = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getWarName(aliasName, applicationName);

      Reference ref;
      if (factory != null) {
        ref = factory.getReference();
      } else {
        ref = new Reference(ENTITY_MANAGER, ORPERSISTENCE_OBJECT_FACTORY, SERVICE_ORPERSISTENCE_LOADER);
      }
      ref.add(new StringRefAddr(RESOURCE_APPLICATION, applicationName));
      ref.add(new StringRefAddr(RESOURCE_ARCHIVE, warName));
      ref.add(new StringRefAddr(RESOURCE_NAME, persistenceUnitName));
      ref.add(new StringRefAddr(RESOURCE_TYPE, ENTITY_MANAGER));
      ref.add(new StringRefAddr(RESOURCE_CONTEXT_TYPE, persistenceContextType));
			if (persistenceProperties != null && persistenceProperties.length > 0) {
				HashMap<String, String> map = new HashMap<String, String>();
				for (PropertyType pType : persistenceProperties) {
					map.put(pType.getName().get_value(), pType.getValue().get_value());
				}
	      ref.add(new MapRefAddr(map));
			}

      try {
        //bind resource
        NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + persistenceContextRefName, ref);
      } catch (NamingException e) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_ENTITY_MANAGER, new Object[]{persistenceContextRefName, aliasName, e.toString()}).toString());
      }

      if (factory != null) {
        createInjections(applicationClassLoader, persistenceContextRefs[i].getInjectionTarget(), factory);
      }
    }
  }//end of bindPersistenceContextReferences(Context subContext, Vector<String> warnings)

  private void initPersistenceUnitReferences(WebDeploymentDescriptor webDesc) {
    PersistenceUnitRefType[] newPersistenceUnitRefTypes = webDesc.getPersistenceUnitRefs();

    if (newPersistenceUnitRefTypes == null) {
      return;
    }

    if (persistenceUnitRefs == null) {
      persistenceUnitRefs = newPersistenceUnitRefTypes;
    } else {
      Vector<PersistenceUnitRefType> all = new Vector<PersistenceUnitRefType>(Arrays.asList(persistenceUnitRefs));
      all.addAll(Arrays.asList(newPersistenceUnitRefTypes));
      persistenceUnitRefs = (PersistenceUnitRefType[]) all.toArray(new PersistenceUnitRefType[all.size()]);
    }
  }//end of initPersistenceUnitReferences(WebDeploymentDescriptor webDesc)

  private void bindPersistenceUnitReferences(Context subContext, Vector<String> warnings) {
    for (int i = 0; persistenceUnitRefs != null && i < persistenceUnitRefs.length; i++) {
      if (persistenceUnitRefs[i] == null) {
        continue;
      }

      String persistenceUnitRefName = persistenceUnitRefs[i].getPersistenceUnitRefName() != null ? persistenceUnitRefs[i].getPersistenceUnitRefName().get_value() : null;
      String persistenceUnitName = persistenceUnitRefs[i].getPersistenceUnitName() != null ? persistenceUnitRefs[i].getPersistenceUnitName().get_value() : "";
      String warName = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getWarName(aliasName, applicationName);

      //used for injection
      ReferenceObjectFactory factory = null;
      ClassLoader factoryLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(SERVICE_ORPERSISTENCE_LOADER);
      try {
        factory = new ReferenceObjectFactory(ENTITY_MANAGER_FACTORY, ORPERSISTENCE_OBJECT_FACTORY, SERVICE_ORPERSISTENCE_LOADER, factoryLoader);
      } catch (InjectionException ie) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION, new Object[]{aliasName, ie.toString()}).toString());
      }

      Reference ref;
      if (factory != null) {
        ref = factory.getReference();
      } else {
        ref = new Reference(ENTITY_MANAGER_FACTORY, ORPERSISTENCE_OBJECT_FACTORY, SERVICE_ORPERSISTENCE_LOADER);
      }
      ref.add(new StringRefAddr(RESOURCE_APPLICATION, applicationName));
      ref.add(new StringRefAddr(RESOURCE_ARCHIVE, warName));
      ref.add(new StringRefAddr(RESOURCE_NAME, persistenceUnitName));
      ref.add(new StringRefAddr(RESOURCE_TYPE, ENTITY_MANAGER_FACTORY));

      try {
        //bind resource
        NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + persistenceUnitRefName, ref);
      } catch (NamingException e) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_ENTITY_MANAGER_FACTORY, new Object[]{persistenceUnitRefName, aliasName, e.toString()}).toString());
      }

      if (factory != null) {
        createInjections(applicationClassLoader, persistenceUnitRefs[i].getInjectionTarget(), factory);
      }
    }
  }//end of bindPersistenceUnitReferences(Context subContext, Vector<String> warnings)

  private void initMsgDestinations(WebDeploymentDescriptor webDesc) {
    MessageDestinationType[] newMessageDestinationTypes = webDesc.getMsgDestinations();

    if (newMessageDestinationTypes == null) {
      return;
    }

    if (msgDestinations == null) {
      msgDestinations = newMessageDestinationTypes;
      addMsgDestinations = new com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType[msgDestinations.length];
      for (int i = 0; i < msgDestinations.length; i++) {
        addMsgDestinations[i] = webDesc.getMsgDestinationFromAdditional(msgDestinations[i]);
      }
    } else {
      Vector<MessageDestinationType> all = new Vector<MessageDestinationType>(Arrays.asList(msgDestinations));
      all.addAll(Arrays.asList(newMessageDestinationTypes));
      msgDestinations = (MessageDestinationType[]) all.toArray(new MessageDestinationType[all.size()]);

      Vector<com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType> temp = new Vector<com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType>(Arrays.asList(addMsgDestinations));
      for (int i = 0; i < newMessageDestinationTypes.length; i++) {
        temp.add(webDesc.getMsgDestinationFromAdditional(newMessageDestinationTypes[i]));
      }
      addMsgDestinations = (com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType[]) temp.toArray(new com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType[temp.size()]);
    }
  }//end of initMsgDestinations(WebDeploymentDescriptor webDesc)

  private void initMsgDestinationReferences(WebDeploymentDescriptor webDesc) {
    MessageDestinationRefType[] newMessageDestinationRefTypes = webDesc.getMsgDestinationRefs();

    if (newMessageDestinationRefTypes == null) {
      return;
    }

    if (msgDestinationRefs == null) {
      msgDestinationRefs = newMessageDestinationRefTypes;
      addMsgDestinationRefs = new com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType[msgDestinationRefs.length];
      for (int i = 0; i < msgDestinationRefs.length; i++) {
        addMsgDestinationRefs[i] = webDesc.getMsgDestinationRefFromAdditional(msgDestinationRefs[i]);
      }
    } else {
      Vector<MessageDestinationRefType> all = new Vector<MessageDestinationRefType>(Arrays.asList(msgDestinationRefs));
      all.addAll(Arrays.asList(newMessageDestinationRefTypes));
      msgDestinationRefs = (MessageDestinationRefType[]) all.toArray(new MessageDestinationRefType[all.size()]);

      Vector<com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType> temp = new Vector<com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType>(Arrays.asList(addMsgDestinationRefs));
      for (int i = 0; i < newMessageDestinationRefTypes.length; i++) {
        temp.add(webDesc.getMsgDestinationRefFromAdditional(newMessageDestinationRefTypes[i]));
      }
      addMsgDestinationRefs = (com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType[]) temp.toArray(new com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType[temp.size()]);
    }
  }//end of initPersistenceUnitReferences(WebDeploymentDescriptor webDesc)

  private void bindMsgDestinationReferences(Context subContext, Vector<String> warnings) {
    for (int i = 0; msgDestinationRefs != null && i < msgDestinationRefs.length; i++) {
      if (msgDestinationRefs[i] == null) {
        continue;
      }

      String msgDestinationRefName = msgDestinationRefs[i].getMessageDestinationRefName() != null ? msgDestinationRefs[i].getMessageDestinationRefName().get_value() : null;

      String msgDestinationType = null;
      if (msgDestinationRefs[i].getMessageDestinationType() != null) {
        msgDestinationType = msgDestinationRefs[i].getMessageDestinationType().get_value();
      } else if (msgDestinationRefs[i].getInjectionTarget() != null) {
        InjectionTargetType[] injectionTargets = msgDestinationRefs[i].getInjectionTarget();
        for (int j = 0; j < injectionTargets.length; j++) {
          Injector injector = createInjector(applicationClassLoader, injectionTargets[j]);
          msgDestinationType = injector.getTargetType();
          break;
        }
      } else {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_BIND_MESSAGE_DESTINATION, new Object[]{msgDestinationRefName, "Message destination type is missing",  aliasName}).toString());
        continue;
      }

      String messageDestinationLink = null;
      String jndiName = null;
      if (msgDestinationRefs[i].getMessageDestinationLink() != null) {
        messageDestinationLink = msgDestinationRefs[i].getMessageDestinationLink().get_value();
        for (int j = 0; j < msgDestinations.length; j++) {
          if (msgDestinations[j].getMessageDestinationName().get_value().equals(messageDestinationLink)) {
            com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType addMsgDestinationType = addMsgDestinations[j];
            jndiName = (addMsgDestinationType != null && addMsgDestinationType.getJndiName() != null) ? addMsgDestinationType.getJndiName() : null;
            if (jndiName == null) {
              //use mapped name from standard descriptor
              jndiName = (msgDestinations[j].getMappedName() != null) ? msgDestinations[j].getMappedName().get_value() : messageDestinationLink;
            }
            break;
          }
        }

        if (jndiName == null) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.CANNOT_BIND_MESSAGE_DESTINATION, new Object[]{msgDestinationRefName, "Message destination jndi name is missing.", aliasName}).toString());
          continue;
        }
      } else {
        com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType addMsgDestinationRefType = addMsgDestinationRefs[i];
        jndiName = (addMsgDestinationRefType != null && addMsgDestinationRefType.getJndiName() != null) ? addMsgDestinationRefType.getJndiName() : null;
        if (jndiName == null && msgDestinationRefName != null) {
          //use mapped name from standard descriptor
          jndiName = (msgDestinationRefs[i].getMappedName() != null) ? msgDestinationRefs[i].getMappedName().get_value() : msgDestinationRefName;
        }
      }

      //used for injection
      ObjectFactory factory = null;
      if (msgDestinationType != null) {
        if (msgDestinationType.equals("javax.jms.Queue")) {
          jndiName = "jmsQueues/" + jndiName;
          factory = new CachingJNDIObjectFactory("/java:comp/env/" + msgDestinationRefName);
        } else if (msgDestinationType.equals("javax.jms.Topic")) {
          jndiName = "jmsTopics/" + jndiName;
          factory = new CachingJNDIObjectFactory("/java:comp/env/" + msgDestinationRefName);
        } else {
          factory = new JNDIObjectFactory("/java:comp/env/" + msgDestinationRefName);
        }

        try {
          //bind resource
          NamingUtils.bind(subContext, aliasForDirectory + "/java:comp/env/" + msgDestinationRefName,
            new Reference(jndiName, COMPONENT_OBJECT_FACTORY, ""));
        } catch (NamingException e) {
           warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.CANNOT_BIND_MSG_DEST_REF, new Object[]{msgDestinationRefName, aliasName, e.toString()}).toString());
        }

        createInjections(applicationClassLoader, msgDestinationRefs[i].getInjectionTarget(), factory);
      }
    }
  }//end of bindMsgDestinationReferences(Context subContext)

  private Object getEntryValue(EnvEntryType envEntry) {
    String type = null;
    if (envEntry.getEnvEntryType() != null) {
      type = envEntry.getEnvEntryType().get_value();
    } else if (envEntry.getInjectionTarget() != null) {
      InjectionTargetType[] injectionTargets = envEntry.getInjectionTarget();
      for (int j = 0; j < injectionTargets.length; j++) {
        Injector injector = createInjector(applicationClassLoader, injectionTargets[j]);
        type = injector.getTargetType();
        break;
      }
    }

    String value = envEntry.getEnvEntryValue().get_value();

    if (type != null) {
      if (type.equals("java.lang.Double")) {
        return new Double(value);
      }
      if (type.equals("java.lang.Integer")) {
        return new Integer(value);
      }
      if (type.equals("java.lang.Boolean")) {
        return new Boolean(value);
      }
      if (type.equals("java.lang.Float")) {
        return new Float(value);
      }
      if (type.equals("java.lang.Short")) {
        return new Short(value);
      }
      if (type.equals("java.lang.Byte")) {
        return new Byte(value);
      }
      if (type.equals("java.lang.Long")) {
        return new Long(value);
      }
      if (type.equals("java.lang.String")) {
        return value;
      }
      if (type.equals("java.lang.Character")) {
        return new Character(value.charAt(0));
      }
    }

    return null;
  }//end of getEntryValue(EnvEntryType envEntry)

  public Hashtable<String, InjectionMatrix> getInjectionMatrixes() {
    return injectionMatrixes;
  }//end of getInjectionMatrixes()

  private void createInjections(ClassLoader applicationLoader, InjectionTargetType[] injectionTargets, ObjectFactory factory) {
    for (int i = 0; injectionTargets != null && i < injectionTargets.length; i++) {
      Injector injector = createInjector(applicationLoader, injectionTargets[i]);
      if (injector != null) {
        injector.setFactory(factory);
        InjectionMatrix injectionMatrix = injectionMatrixes.get(injectionTargets[i].getInjectionTargetClass().get_value());
        if (injectionMatrix == null) {
          injectionMatrix = new InjectionMatrix();
          injectionMatrixes.put(injectionTargets[i].getInjectionTargetClass().get_value(), injectionMatrix);
        }
        injectionMatrix.addInjector(injector);
      }
    }
  }//end of createInjections(ClassLoader applicationLoader, InjectionTargetType[] injectionTargets, ObjectFactory factory)

  private Injector createInjector(ClassLoader applicationLoader, InjectionTargetType injectionTarget) {
    String targetClassName = injectionTarget.getInjectionTargetClass().get_value();

    Class targetClass = null;
    try {
      targetClass = applicationLoader.loadClass(targetClassName);
    } catch (ClassNotFoundException cnfe) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000333",
        "Cannot load class [{0}] while trying to prepare injection information for [{1}] web application.", 
        new Object[]{targetClassName, aliasName}, cnfe, null, null);
      return null;
    }

    String targetName = injectionTarget.getInjectionTargetName().get_value();
    String upperCasedName = Character.toUpperCase(targetName.charAt(0)) + targetName.substring(1);
    try {
      return new MethodInjector(getInjectionTargetMethod(targetClass, upperCasedName));
    } catch (NoSuchMethodException nsme) {
      try {
        return new FieldInjector(getInjectionTargetField(targetClass, targetName));
      } catch (NoSuchFieldException nsfe) {
        if (LogContext.getLocationDeploy().beError()) {
          LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000422", 
            "Cannot find neither property nor field with name [{0}] in the class [{1}].", 
            new Object[]{targetName, targetClassName}, nsfe, null, null);

        }
        return null;
      }
    }
  }//end of createInjector(ClassLoader applicationLoader, InjectionTargetType injectionTarget)

  private Method getInjectionTargetMethod(Class targetClass, String upperCasedName) throws NoSuchMethodException {
    for (Class currentClass = targetClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
      for (Method m : currentClass.getDeclaredMethods()) {
        if (m.getName().equals("get" + upperCasedName) && m.getParameterTypes().length == 0) {
          return currentClass.getDeclaredMethod("set" + upperCasedName, new Class[]{m.getReturnType()});
        } else if (m.getName().equals("set" + upperCasedName) && m.getParameterTypes().length == 1) {
          return m;
        }
      }
    }

    throw new NoSuchMethodException("Method [" + "get" + upperCasedName + "] was not found in [" + targetClass.getName() +
      "] and its superclasses or was declared private in a super class of [" + targetClass.getName() + "].");
  }//end of getInjectionTargetGetter(Class targetClass, String upperCasedName)

  private Field getInjectionTargetField(Class targetClass, String fieldName) throws NoSuchFieldException {
    for (Class currentClass = targetClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
      for (Field f : currentClass.getDeclaredFields()) {
        if (f.getName().equals(fieldName)) {
          return f;
        }
      }
    }

    throw new NoSuchFieldException("Field [" + targetClass + "] was not found in [" + targetClass.getName() +
      "] and its superclasses or was declared private in a super class of [" + targetClass.getName() + "].");
  }//end of getInjectionTargetGetter(Class targetClass, String upperCasedName)

}//end of class
