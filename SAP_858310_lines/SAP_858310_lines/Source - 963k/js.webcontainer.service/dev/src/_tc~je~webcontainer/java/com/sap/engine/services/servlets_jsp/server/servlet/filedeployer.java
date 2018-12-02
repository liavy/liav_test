/*
 * Copyright (c) 2000-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.servlet;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import java.util.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.rmi.RemoteException;

import com.sap.engine.frame.core.configuration.*;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.lib.io.hash.HashUtils;

public class FileDeployer {
  private static final String UPDATE_LOCK = "_web_container_update_lock_";
  private static final String HTTP_ALIASES = "HttpAliases";
  private ConfigurationHandler handler = null;
  private ServiceContext serviceContext = null;

  public FileDeployer(ConfigurationHandler handler, ServiceContext serviceContext) throws java.io.IOException {
    if (handler == null) {
      throw new WebIOException(WebIOException.Cannot_create_configuration);
    }
    this.handler = handler;
    this.serviceContext = serviceContext;
  }

  public boolean uploadFileInApplication(String fileName, String entry, String appName) throws IOException {
    long fileId = -1;
    boolean isNewFile = false;
    String uploadedFileLockName = appName + ParseUtils.separatorChar + entry;
    Configuration config = null;
    try {
      Configuration cfg = serviceContext.getDeployContext().getDeployCommunicator().startRuntimeChanges(appName, false);
      config = cfg.getSubConfiguration(Constants.CONTAINER_NAME);
      config = config.getSubConfiguration(Constants.UPDATE);
    } catch (DeploymentException e) {
      throw new WebIOException(WebIOException.CANNOT_START_RUNTIME_CHANGES, e);
    } catch (ConfigurationException e) {
      throw new WebIOException(WebIOException.CANNOT_OPEN_CONFIGURATION_FOR_UPLOAD, e);
    }

    try {
      try {
        serviceContext.getLockContext().lock(UPDATE_LOCK);
      } catch (LockException e) {
        throw new WebIOException(WebIOException.CANNOT_LOCK_CONFIGURATION, new Object[]{UPDATE_LOCK}, e);
      } catch (TechnicalLockException e) {
        throw new WebIOException(WebIOException.CANNOT_LOCK_CONFIGURATION, new Object[]{UPDATE_LOCK}, e);
      }
      try {
        fileId = findFileidInConfig(config, entry);
      } catch (ConfigurationException e) {
        serviceContext.getLockContext().unlock(UPDATE_LOCK);
        throw new WebIOException(WebIOException.ERROR_FINDING_FILE_ENTRY, new Object[]{entry}, e);
      }
      if (fileId == -1) {
        try {
          String fileCounter = (String)config.getConfigEntry(Constants.FILE_COUNTER);
          fileId = Long.parseLong(fileCounter);
        } catch (ConfigurationException e) {
          serviceContext.getLockContext().unlock(UPDATE_LOCK);
          throw new WebIOException(WebIOException.ERROR_FINDING_FILE_ENTRY, new Object[]{Constants.FILE_COUNTER}, e);
        }
        try {
          config.modifyConfigEntry(Constants.FILE_COUNTER, "" + (fileId + 1));
        } catch (ConfigurationException e) {
          serviceContext.getLockContext().unlock(UPDATE_LOCK);
          throw new WebIOException(WebIOException.ERROR_FINDING_FILE_ENTRY, new Object[]{Constants.FILE_COUNTER}, e);
        }
        isNewFile = true;
      }
    } catch (OutOfMemoryError t) {
      throw t;
    } catch (ThreadDeath t) {
      throw t;
    } catch (Throwable t) {
      try {
        serviceContext.getDeployContext().getDeployCommunicator().rollbackRuntimeChanges(appName);
      } catch (RemoteException re) {
        throw new WebIOException(WebIOException.CANNOT_ROLLBACK_RUNTIME_CHANGES, t);
      }
      throw new WebIOException(WebIOException.UNEXCPECTED_ERROR_IN_UPLOAD, t);
    } finally {
      try {
        serviceContext.getLockContext().unlock(UPDATE_LOCK);
      } catch (TechnicalLockException e) {
        try {
          serviceContext.getDeployContext().getDeployCommunicator().rollbackRuntimeChanges(appName);
        } catch (RemoteException re) {
          throw new WebIOException(WebIOException.CANNOT_ROLLBACK_RUNTIME_CHANGES,
              new WebIOException(WebIOException.CANNOT_UNLOCK_CONFIGURATION, new Object[]{UPDATE_LOCK}, e));
        }
        throw new WebIOException(WebIOException.CANNOT_UNLOCK_CONFIGURATION, new Object[]{UPDATE_LOCK}, e);
      }
    }

    try {
      try {
        serviceContext.getLockContext().lock(uploadedFileLockName);
      } catch (LockException e) {
        throw new WebIOException(WebIOException.CANNOT_LOCK_CONFIGURATION, new Object[]{uploadedFileLockName}, e);
      } catch (TechnicalLockException e) {
        throw new WebIOException(WebIOException.CANNOT_LOCK_CONFIGURATION, new Object[]{uploadedFileLockName}, e);
      }
      try {
        if (isNewFile) {
          config.addConfigEntry("" + fileId, entry);
        } else {
          config.modifyConfigEntry("" + fileId, entry);
        }
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.ERROR_MODIFYING_ENTRY, new Object[]{"" + fileId}, e);
      }
      try {
        if (isNewFile) {
          config.addFileEntryByKey("#" + fileId, new File(fileName));
        } else {
          config.updateFileByKey("#" + fileId, new File(fileName));
        }
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.ERROR_MODIFYING_ENTRY, new Object[]{"" + fileId}, e);
      }
    } catch (OutOfMemoryError t) {
      throw t;
    } catch (ThreadDeath t) {
      throw t;
    } catch (Throwable t) {
      try {
        serviceContext.getDeployContext().getDeployCommunicator().rollbackRuntimeChanges(appName);
      } catch (RemoteException re) {
        throw new WebIOException(WebIOException.CANNOT_ROLLBACK_RUNTIME_CHANGES, t);
      }
      throw new WebIOException(WebIOException.UNEXCPECTED_ERROR_IN_UPLOAD, t);
    } finally {
      try {
        serviceContext.getLockContext().unlock(uploadedFileLockName);
      } catch (TechnicalLockException e) {
        try {
          serviceContext.getDeployContext().getDeployCommunicator().rollbackRuntimeChanges(appName);
        } catch (RemoteException re) {
          throw new WebIOException(WebIOException.CANNOT_ROLLBACK_RUNTIME_CHANGES,
              new WebIOException(WebIOException.CANNOT_UNLOCK_CONFIGURATION, new Object[]{uploadedFileLockName}, e));
        }
        throw new WebIOException(WebIOException.CANNOT_UNLOCK_CONFIGURATION, new Object[]{uploadedFileLockName}, e);
      }
    }

    try {
      serviceContext.getDeployContext().getDeployCommunicator().makeRuntimeChanges(appName, false);
    } catch (RemoteException e) {
      throw new WebIOException(WebIOException.CANNOT_MAKE_RUNTIME_CHANGES, e);
    }
    return isNewFile;
  }

  public boolean uploadFileInAlias(String fileName, String entry, String hostName, String aliasName) throws IOException {
    boolean isNew = false;
    long fileId = -1;
    byte[] crc = null;
    Configuration config = null;
    try {
      try {
        config = openAliasConfiguration(hostName, aliasName);
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.CANNOT_OPEN_CONFIGURATION_FOR_UPLOAD, e);
      }
      serviceContext.getLockContext().lock(LockContext.HTTP_UPLOADED_FILES_LOCK);
      try {
        fileId = findFileidInConfig(config, entry);
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.ERROR_FINDING_FILE_ENTRY, new Object[]{entry}, e);
      }
      if (fileId == -1) {
        try {
          String fileCounter = (String)config.getConfigEntry(Constants.FILE_COUNTER);
          fileId = Long.parseLong(fileCounter);
        } catch (ConfigurationException e) {
          throw new WebIOException(WebIOException.ERROR_FINDING_FILE_ENTRY, new Object[]{Constants.FILE_COUNTER}, e);
        }
        try {
          config.modifyConfigEntry(Constants.FILE_COUNTER, "" + (fileId + 1));
        } catch (ConfigurationException e) {
          throw new WebIOException(WebIOException.ERROR_MODIFYING_ENTRY, new Object[]{Constants.FILE_COUNTER}, e);
        }
        isNew = true;
      }
      try {
        if (isNew) {
          config.addConfigEntry("" + fileId, entry);
        } else {
          config.modifyConfigEntry("" + fileId, entry);
        }
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.ERROR_MODIFYING_ENTRY, new Object[]{"" + fileId}, e);
      }
      try {
        FileInputStream fis = null;
        if (isNew) {
          try {
            fis = new FileInputStream(fileName);
            crc = HashUtils.generateFileHash(entry, fis);
          } catch (IOException io) {
            throw new WebIOException(WebIOException.CANNOT_GENERATE_FILE_HASH, new Object[]{entry}, io);
          } finally {
            if (fis != null) {
              fis.close();
            }
          }
          config.addFileEntryByKey("#" + fileId, new File(fileName));
          config.addConfigEntry("$" + fileId, crc);
        } else {
          try {
            fis = new FileInputStream(fileName);
            crc = HashUtils.generateFileHash(entry, fis);
          } catch (IOException io) {
            throw new WebIOException(WebIOException.CANNOT_GENERATE_FILE_HASH, new Object[]{entry}, io);
          } finally {
            if (fis != null) {
              fis.close();
            }
          }
          config.updateFileByKey("#" + fileId, new File(fileName));
          config.modifyConfigEntry("$" + fileId, crc);
        }
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.ERROR_MODIFYING_ENTRY, new Object[]{"#" + fileId}, e);
      }
      try {
        handler.commit();
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.CANNOT_COMMIT_UPLOAD, new Object[]{entry}, e);
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (WebIOException io) {
      try {
        handler.rollback();
      } catch (ConfigurationException ex) {
        throw new WebIOException(WebIOException.CANNOT_CLOSE_CONFIGURATION_FOR_UPLOAD, new Object[]{ex}, io);
      }
      throw io;
    } catch (Throwable t) {
      try {
        handler.rollback();
      } catch (ConfigurationException ex) {
        throw new WebIOException(WebIOException.CANNOT_CLOSE_CONFIGURATION_FOR_UPLOAD, new Object[]{ex}, t);
      }
      throw new WebIOException(WebIOException.UNEXCPECTED_ERROR_IN_UPLOAD, t);
    } finally {
      try {
        handler.closeAllConfigurations();
      } catch (ConfigurationException e) {
        throw new WebIOException(WebIOException.CANNOT_CLOSE_CONFIGURATION, e);
      } finally {
        try {
          serviceContext.getLockContext().unlock(LockContext.HTTP_UPLOADED_FILES_LOCK);
        } catch (TechnicalLockException e) {
          throw new WebIOException(WebIOException.CANNOT_UNLOCK_CONFIGURATION, new Object[]{LockContext.HTTP_UPLOADED_FILES_LOCK}, e);
        }
      }
    }
    return isNew;
  }

  private long findFileidInConfig(Configuration config, String entry) throws ConfigurationException {
    Map fileEntries = config.getAllFileEntries();
    if (fileEntries.isEmpty()) {
      fileEntries = config.getAllConfigEntries();
      if (fileEntries.isEmpty()) {
        config.addConfigEntry(Constants.FILE_COUNTER, "1");
      }
    } else {
      Set keySet = fileEntries.keySet();
      Iterator iter = keySet.iterator();
      while (iter.hasNext()) {
        String nextFilename = (String)iter.next();
        String fsId = nextFilename.substring(1);
        String path = (String)config.getConfigEntry(fsId);
        if (entry.equals(path)) {
          return Long.parseLong(fsId);
        }
      }
    }
    return -1;
  }

  private Configuration openAliasConfiguration(String hostName, String aliasName) throws ConfigurationLockedException, ConfigurationException {
    Configuration config = null;
    try {
      config = handler.openConfiguration(HTTP_ALIASES, ConfigurationHandler.WRITE_ACCESS);
    } catch (NameNotFoundException ex) {
      config = handler.createRootConfiguration(HTTP_ALIASES);
    }
    try {
      config = config.getSubConfiguration(hostName);
    } catch (NameNotFoundException ex) {
      config = config.createSubConfiguration(hostName);
    }
    try {
      return config.getSubConfiguration(aliasName);
    } catch (NameNotFoundException ex) {
      return config.createSubConfiguration(aliasName);
    }
  }
}
