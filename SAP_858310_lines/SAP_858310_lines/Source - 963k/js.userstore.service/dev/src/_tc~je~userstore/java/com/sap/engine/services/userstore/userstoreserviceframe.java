package com.sap.engine.services.userstore;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.SecurityResourcePermission;
import com.sap.engine.interfaces.keystore.Constants;
import com.sap.engine.interfaces.keystore.KeystoreManager;
import com.sap.engine.interfaces.log.Logger;
import com.sap.engine.interfaces.log.LogInterface;

import com.sap.engine.frame.ApplicationServiceFrame;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.ServiceRuntimeException;
import com.sap.engine.frame.container.event.ContainerEventListener;
import com.sap.engine.frame.core.locking.LockingConstants;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.frame.core.locking.LockingContext;
import com.sap.engine.frame.core.locking.ServerInternalLocking;
import com.sap.engine.boot.SystemProperties;
import com.sap.engine.services.userstore.exceptions.UserstoreResourceAccessor;
import com.sap.engine.services.userstore.exceptions.UserstoreServiceException;
import com.sap.engine.services.userstore.exceptions.UserstoreServiceRuntimeException;
import com.sap.engine.services.userstore.exceptions.BaseSecurityException;
import com.sap.security.api.IRole;
import com.sap.security.api.IRoleFactory;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.role.IAction;
import com.sap.security.core.role.IActionFactory;
import com.sap.security.core.role.NoSuchActionException;
import com.sap.security.core.role.PermissionData;
import com.sap.security.core.role.imp.PermissionRoles;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
/**
 *
 */
public class UserStoreServiceFrame implements ApplicationServiceFrame {
  public static final String ROLE_NAME = "find_alias";

  private static ApplicationServiceContext serviceContext = null;
  private static Logger log = null;
  private static Properties serviceProperties = null;
  private static KeystoreManager keystore = null;
  private static boolean keystoreInitialized = false;
  private static boolean category_location = false;
  private static LockingContext lockingContext = null;
  private static ServerInternalLocking internalLock = null;
  private static Object lockObject = new Object();

  public static final String USERSTORE_CATEGORY = "/System/Security";
  public static final String USERSTORE_LOCATION = "com.sap.engine.services.userstore";

  public void start(ApplicationServiceContext serviceContext) throws ServiceException {
    UserStoreServiceFrame.serviceContext = serviceContext;
    lockingContext = serviceContext.getCoreContext().getLockingContext();
    try {
      internalLock = lockingContext.createServerInternalLocking("$service.userstore", "Synchronization For Userstore Write Operations");
    } catch (TechnicalLockException tle) {
      UserstoreServiceException sse = new UserstoreServiceException(UserstoreServiceException.UNEXPECTED_SERVER_LOCKING_ERROR, tle);
      sse.log();
      throw sse;
    }
    try {    
      new UserstoreResourceAccessor().init(Category.getCategory(USERSTORE_CATEGORY),
                                           Location.getLocation(USERSTORE_LOCATION));
      serviceProperties = serviceContext.getServiceState().getProperties();
      //containerEventListener = new ContainerEventListenerImpl(serviceContext.getContainerContext().getObjectRegistry());
      int mask = ContainerEventListener.MASK_INTERFACE_AVAILABLE |
                 ContainerEventListener.MASK_INTERFACE_NOT_AVAILABLE;
      
      Set names = new HashSet(2);
      names.add("keystore_api");
      names.add("log");
      serviceContext.getServiceState().registerContainerEventListener(mask, names, new ContainerEventListenerImpl());

      HandlerPool.configHandlerFactory = serviceContext.getCoreContext().getConfigurationHandlerFactory();

      serviceContext.getCoreContext().getThreadSystem().registerContextObject(SecurityModificationContext.NAME, new SecurityModificationContext());
    } catch (Exception e) {
      UserstoreServiceException use = new UserstoreServiceException(UserstoreServiceException.UNEXPECTED_SERVICE_ERROR, e);
      use.log();
      throw use;
    }
  }
  

  public boolean changeProperties(Properties properties) throws IllegalArgumentException {
    return false;
  }

  static SecurityContext getSecurityContext() {
    return (SecurityContext) serviceContext.getContainerContext().getObjectRegistry().getServiceInterface("security");
  }

  static synchronized KeystoreManager getKeystoreInterface() {
    if (!keystoreInitialized) {
      try { // keystore service not started yet, waiting...
        UserStoreServiceFrame.class.wait();
      } catch (Exception _) {
        logWarning("getKeystoreInterface()", _);
      }
    }
    
    return keystore;
  }

  static synchronized void setKeystore(KeystoreManager keystore) {
    UserStoreServiceFrame.keystore = keystore;
    if (keystore == null) {
      keystoreInitialized = false;
      return;
    }
    
    initKeystore();
    keystoreInitialized = true;
    
    //weaking-up all threads sleeping on getKeystore() method
    UserStoreServiceFrame.class.notifyAll();
  }

  public static Properties getServiceProperties() {
    return serviceProperties;
  }

  public void stop() throws ServiceRuntimeException {
    try {
      serviceContext.getCoreContext().getThreadSystem().unregisterContextObject(SecurityModificationContext.NAME);
      logNotice("Service userstore stopped OK");
    } catch (Exception e) {
      UserstoreServiceRuntimeException usre = new UserstoreServiceRuntimeException(UserstoreServiceRuntimeException.SERVICE_STOPPING_ERROR, e);
      usre.log();
      throw usre;
    }
  }

  public static synchronized void setLogger(Logger logger) {
    log = logger;
  }
  
  public static void logNotice(String message) {
    if (category_location) {
      UserstoreResourceAccessor.category.logT(Severity.INFO, UserstoreResourceAccessor.location, message);
    } else if (SystemProperties.getBoolean("debug")) {
      System.out.println("[userstore][INFO]: " + message);
    }
  }

  public static void logWarning(String message, Throwable t) {
    if (category_location) {
      //String message = t.getLocalizedMessage();
      if (message != null) {
        UserstoreResourceAccessor.category.log(Severity.WARNING, UserstoreResourceAccessor.location, message);
      }
      if (UserstoreResourceAccessor.location != null) {
        UserstoreResourceAccessor.location.catching(t);
      }
    } else if (SystemProperties.getBoolean("debug")) {
      System.out.println("[userstore][WARNING]: " + message);
      t.printStackTrace();
    }
  }

  public static void logError(String message, Throwable t) {
    if (category_location) {
      //String message = t.getLocalizedMessage();
      if (message != null) {
        UserstoreResourceAccessor.category.log(Severity.ERROR, UserstoreResourceAccessor.location, message);
      }
      if (UserstoreResourceAccessor.location != null) {
        UserstoreResourceAccessor.location.catching(t);
      }
    } else if (SystemProperties.getBoolean("debug")) {
      System.out.println("[userstore][ERROR]: " + message);
      t.printStackTrace();
    }
  }

  public static void logError(String message) {
    if (category_location) {
      UserstoreResourceAccessor.category.logT(Severity.ERROR, UserstoreResourceAccessor.location, message);
      log.log(LogInterface.ERROR, message);
    } else if (SystemProperties.getBoolean("debug")) {
      System.out.println("[userstore][ERROR]: " + message);
    }
  }

  public static void useCategory_LocationLogging(boolean flag) {
    category_location = flag && UserstoreResourceAccessor.category != null;
  }

  static void lock(String name) throws SecurityException {
    long beginTime = System.currentTimeMillis();
    while (System.currentTimeMillis() <= beginTime + 60000) {
      try {
        internalLock.lock("$service.userstore", "userstore_" + name + "_lock", LockingConstants.MODE_EXCLUSIVE_NONCUMULATIVE);
        return;
      } catch (Exception e) {
        synchronized(lockObject) {
          try {
            lockObject.wait(1000);
          } catch (InterruptedException ex) {
            throw new BaseSecurityException(BaseSecurityException.TIMEOUT_WRITE_OPERATION, ex);
          }
        }
      }
    }
    throw new BaseSecurityException(BaseSecurityException.TIMEOUT_WRITE_OPERATION);
  }

  static void releaseLock(String name) {
    try {
      internalLock.unlock("$service.userstore", "userstore_" + name + "_lock", LockingConstants.MODE_EXCLUSIVE_NONCUMULATIVE);
    } catch (Exception e) {
      logError("Unable to release lock <$service.userstore>.", e);
    }
  }

  private static void initKeystore() {
    try {
      if (!keystore.existKeystoreView(Constants.KV_ALIAS_DBMS_USERS)) {
        keystore.createKeystoreView(Constants.KV_ALIAS_DBMS_USERS, new Properties());
      }
      
      
      IRole role = null;
      try {
        IRoleFactory roleFactory = UMFactory.getRoleFactory();
        role = roleFactory.getRoleByUniqueName("Everyone");
        role = roleFactory.getMutableRole(role.getUniqueID());
          
        addViewAction(Constants.KV_ALIAS_DBMS_USERS, role, Constants.FIND_ALIAS);      
      } catch (Exception ex) {
        logWarning("Error in initializing '" + Constants.KV_ALIAS_DBMS_USERS + "' keystore view.", ex);
        if (role != null && role.isMutable()) {
          role.rollback();
        }      
      }
      keystoreInitialized = true;
    } catch (Exception e) {
      logWarning("Error in initializing '" + Constants.KV_ALIAS_DBMS_USERS + "' keystore view.", e);
    }
  }
  
  private static void addViewAction(String keystore, IRole role, String actionName) throws UMException {
    IActionFactory actionFactory = InternalUMFactory.getActionFactory();
    IAction action = null;
    PermissionData permission = null;
    
    try {
      action = actionFactory.getAction("keystore-view." + keystore, IAction.TYPE_UME_ACTION, "view-actions." + actionName + ".all");
    } catch (NoSuchActionException e) {
      action = actionFactory.newAction("keystore-view." + keystore, IAction.TYPE_UME_ACTION, "view-actions." + actionName + ".all");
      permission = new PermissionData("com.sap.engine.services.keystore.impl.security.KeystoreViewPermission", "keystore-view.view." + keystore, actionName + ":$:" + SecurityResourcePermission.INSTANCE_ALL);
      
      action.addPermission(permission);
      action.save();
      action.commit();
    }	
    
    if (!PermissionRoles.isAssignedAction(role, action)) {
      PermissionRoles.addAction(role, action); 
      role.save();
      role.commit();
    }
    
  }  

}

