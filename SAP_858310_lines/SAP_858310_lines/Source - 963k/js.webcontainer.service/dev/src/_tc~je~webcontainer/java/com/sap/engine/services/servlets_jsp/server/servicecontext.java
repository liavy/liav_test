/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.transaction.TransactionManager;

import com.sap.bc.proj.jstartup.JStartupFramework;
import com.sap.engine.boot.SystemProperties;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.frame.core.configuration.ConfigurationHandlerFactory;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.webservices.server.management.WSManager;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.httpserver.interfaces.HttpHandler;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.servlets_jsp.HttpSessionDebugListener;
import com.sap.engine.services.servlets_jsp.chain.WebContainerScope;
import com.sap.engine.services.servlets_jsp.lib.util.EngineVersionUtil;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainer;
import com.sap.engine.services.servlets_jsp.server.qos.RDResourceProvider;
import com.sap.engine.services.servlets_jsp.server.qos.WCERDResourceProvider;
import com.sap.engine.services.servlets_jsp.server.security.policy.WebContainerPolicy;
import com.sap.engine.services.timeout.TimeoutManager;
import com.sap.tc.logging.Location;

public class ServiceContext implements WebContainerScope {
  public static final String INSTANCE_NAME_KEY = "SAPMYNAME";
  public static final String SAPSTART = "SAPSTART";

  private static ServiceContext activeServiceContext = null;
  private static Location traceLocation = LogContext.getLocationService();

  private HttpHandler httpHandler = null;
  private WebContainer webContainer = null;
  private ApplicationServiceContext applicationServiceContext = null;
  private WebContainerProperties webContainerProperties = new WebContainerProperties();
  private WebContainerPolicy webContainerPolicy = null;
  private WebMonitoring webMonitoring = null;
  private DeployContext deployContext = null;
  private PoolContext poolContext = null;
  private ClusterContext clusterContext = null;
  // TODO - i024079 fix: check with web container developers
  //private ConnectionsContext connectionsContext = new ConnectionsContext();
  private LockContext lockContext = null;
  private LoadContext loadContext = null;
  private MessageContext messageContext = null;
  private ConfigurationAccessor configurationAccessor = null;
  private ThreadSystem threadSystem = null;
  private WSManager wsManager = null;
  private TransactionManager transactionManager = null;
  private HttpProvider httpProvider = null;
  private SecurityContext securityContext = null;
  private ClassLoader serviceLoader = Thread.currentThread().getContextClassLoader();
  private String fullVersion = null;
  private JspContext jspContext = null;
  private TimeoutManager timeoutManager = null;
  /**
   * Server Name as returned by the Startup Framework
   */
  private String serverName = null;
  private byte[] version = null;
  private int serverId = -1;
  private String INSTANCE_NAME = null;
  private long shutdownTime = -1;
  private String workDirectory = null;
  private String tempDirectory = null;
  private Properties classLoaderManagerProperties = null;
  private HttpSessionDebugListener httpSessionDebugListener = null;
  private String debugRequestParameterName = null;
  public static byte[] applicationStoppedContent = null;
  public static long applicationStoppedContentModified = 0;
  private ConfigurationHandlerFactory configurationHandlerFactory = null;
  
  /**
   * Marks service state - used to define method behavior that depends on service status.
   */
  private boolean isServiceStarting = true;
  private RDResourceProvider rdResourceProvider;
  private WCERDResourceProvider wceRDResourceProvider;

  public ServiceContext(ApplicationServiceContext applicationServiceContext) throws IOException, ServiceException {
    long time = System.currentTimeMillis();
    long newtime = time;
    long newtime1 = time;
    isServiceStarting = true;
    activeServiceContext = this;
    this.applicationServiceContext = applicationServiceContext;
    this.serverId = applicationServiceContext.getClusterContext().getClusterMonitor().getCurrentParticipant().getClusterId();
    boolean beDebug = traceLocation.beDebug();
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("applicationServiceContext.getClusterContext().getClusterMonitor().getCurrentParticipant().getClusterId() >>> " + (newtime - time));
		}

		this.threadSystem = applicationServiceContext.getCoreContext().getThreadSystem();
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("applicationServiceContext.getCoreContext().getThreadSystem() >>> " + (newtime1 - newtime));
		}

    this.loadContext = applicationServiceContext.getCoreContext().getLoadContext();
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("applicationServiceContext.getCoreContext().getLoadContext() >>> " + (newtime - newtime1));
		}

    this.workDirectory = applicationServiceContext.getServiceState().getWorkingDirectoryName();
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("applicationServiceContext.getServiceState().getWorkingDirectoryName() >>> " + (newtime1 - newtime));
		}

    File tempdir = new File(workDirectory, "temp");
    this.tempDirectory = tempdir.getPath();
    // Temporary directory will be deleted on every service start
    if (FileUtils.deleteDirectory(tempdir)) {
      if (traceLocation.beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000569",
						"Fail to delete {0} directory", new Object[]{tempdir.getAbsolutePath()}, null, null);
			}
    }
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("Delete service temp directory >>> " + (newtime - newtime1));
		}

		initVersion(applicationServiceContext.getCoreContext().getCoreMonitor().getCoreVersion(), EngineVersionUtil.getEngineVersion(applicationServiceContext.getCoreContext().getCoreMonitor()));
    if (beDebug) {
			newtime1 = System.currentTimeMillis();
			traceLocation.debugT("initVersion() >>> " + (newtime1 - newtime));
		}

    webContainerProperties.updateProperties(applicationServiceContext.getServiceState().getProperties());
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("updateProperties() >>> " + (newtime - newtime1));
		}

    deployContext = new DeployContext(applicationServiceContext.getServiceState().getPersistentContainer());
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("new DeployContext() >>> " + (newtime1 - newtime));
		}

    webMonitoring = new WebMonitoring(deployContext);
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("new WebMonitoring() >>> " + (newtime - newtime1));
		}

    lockContext = new LockContext(applicationServiceContext);
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("new LockContext() >>> " + (newtime1 - newtime));
		}

    configurationAccessor = new ConfigurationAccessor(applicationServiceContext.getCoreContext().getConfigurationHandlerFactory(), this);
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("new ConfigurationAccessor() >>> " + (newtime - newtime1));
		}

    webContainerPolicy = new WebContainerPolicy();
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("new WebContainerPolicy() >>> " + (newtime1 - newtime));
		}

		poolContext = new PoolContext(webContainerProperties.getMinPoolSize(),
      webContainerProperties.getMaxPoolSize(),
      webContainerProperties.getDecreaseCapacityPoolSize());
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("new PoolContext() >>> " + (newtime - newtime1));
		}

		messageContext = applicationServiceContext.getClusterContext().getMessageContext();
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("getMessageContext() >>> " + (newtime1 - newtime));
		}

		clusterContext = new ClusterContext(applicationServiceContext.getClusterContext());
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("new ClusterContext() >>> " + (newtime - newtime1));
		}

		httpHandler = new HttpHandlerImpl();
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("new HttpHandlerImpl() >>> " + (newtime1 - newtime));
		}

		webContainer = new WebContainer(applicationServiceContext.getServiceState().getServiceName());
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("new WebContainer() >>> " + (newtime - newtime1));
		}

		classLoaderManagerProperties = applicationServiceContext.getCoreContext().getCoreMonitor().getManagerProperties("ClassLoaderManager");
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT(
					"applicationServiceContext.getCoreContext().getCoreMonitor().getManagerProperties(\"ClassLoaderManager\") >>> " + (newtime1 - newtime));
		}

		configurationHandlerFactory = applicationServiceContext.getCoreContext().getConfigurationHandlerFactory();
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("applicationServiceContext.getCoreContext().getConfigurationHandlerFactory() >>> " + (newtime - newtime1));
		}

		deployContext.loadGlobalDD();
    if (beDebug) {
      newtime1 = System.currentTimeMillis();
			traceLocation.debugT("deployContext.loadGlobalDD() >>> " + (newtime1 - newtime));
		}

		jspContext = new JspContext(webContainerProperties);
    if (beDebug) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("new JspContext() >>> " + (newtime - newtime1));
			traceLocation.debugT("Whole time >>> " + (System.currentTimeMillis() - time));
		}
  }



  public static ServiceContext getServiceContext() {
    return activeServiceContext;
  }

  public ApplicationServiceContext getApplicationServiceContext() {
    return applicationServiceContext;
  }

  public HttpHandler getHttpHandler() {
    return httpHandler;
  }

  public WebContainer getWebContainer() {
    return webContainer;
  }

  public WebContainerProperties getWebContainerProperties() {
    return webContainerProperties;
  }

  public WebContainerPolicy getWebContainerPolicy() {
    return webContainerPolicy;
  }

  public WebMonitoring getWebMonitoring() {
    return webMonitoring;
  }

  public JspContext getJspContext() {
    return jspContext;
  }

  public DeployContext getDeployContext() {
    return deployContext;
  }

  public PoolContext getPoolContext() {
    return poolContext;
  }

  public MessageContext getMessageContext() {
    return messageContext;
  }

//TODO - i024079 fix: check with web container developers
  
  //public ConnectionsContext getConnectionsContext() {
  //  return connectionsContext;
  //}

  public LockContext getLockContext() {
    return lockContext;
  }

  public ConfigurationAccessor getConfigurationAccessor() {
    return configurationAccessor;
  }

  public ConfigurationHandlerFactory getConfigurationHandlerFactory() {
    return configurationHandlerFactory;
  }

  public WSManager getWSManager() {
    return wsManager;
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }
  
  public TimeoutManager getTimeoutManager(){
	  return timeoutManager;
  }

  public HttpProvider getHttpProvider() {
    return httpProvider;
  }

  public SecurityContext getSecurityContext() {
    return securityContext;
  }

  public int getServerId() {
    return serverId;
  }

  public byte[] getServerVersion() {
    return version;
  }

  public String getFullServerVersion() {
    return fullVersion;
  }

  public String getServerName() {
    return serverName;
  }

  public String getInstanceName() {
    return INSTANCE_NAME;
  }

  public ClassLoader getServiceLoader() {
    return serviceLoader;
  }

  public LoadContext getLoadContext() {
    return loadContext;
  }

  public ThreadSystem getThreadSystem() {
    return threadSystem;
  }

  public long getShutdownTime() {
    return shutdownTime;
  }

  public String getWorkDirectory() {
    return workDirectory;
  }

  // Returns path to directory that would be deleted on every service start
  public String getTempDirectory() {
    return tempDirectory;
  }

  public Properties getClassLoaderManagerProperties() {
    return classLoaderManagerProperties;
  }

  public HttpSessionDebugListener getHttpSessionDebugListener() {
    return httpSessionDebugListener;
  }

  public String getDebugRequestParameterName() {
    return debugRequestParameterName;
  }

  public ClusterContext getClusterContext() {
    return clusterContext;
  }

  private void initVersion(String ver, String majorVer) {
    serverName = JStartupFramework.getParam("is/server_name");
    String serverVersion = serverName + " " +
      JStartupFramework.getParam("is/server_version");
    fullVersion = serverVersion + " / AS Java " + ver;
    version = (serverVersion + " / AS Java " + majorVer).getBytes();
    String sapstart = SystemProperties.getProperty(SAPSTART);
    if ((sapstart != null) && sapstart.equals("1")) {
      INSTANCE_NAME = SystemProperties.getProperty(INSTANCE_NAME_KEY);
      if ((INSTANCE_NAME != null) && INSTANCE_NAME.equalsIgnoreCase("null")) {
        INSTANCE_NAME = null;
      }
    } else {
      INSTANCE_NAME = null;
    }
  }

  protected void setShutdownTime(long shutdownTime) {
    this.shutdownTime = shutdownTime;
  }

  protected void setWSManager(WSManager wsManager) {
    this.wsManager = wsManager;
  }

  protected void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  protected void setTimeoutManager(TimeoutManager timeoutManager) {
	    this.timeoutManager = timeoutManager;
	  }

  protected void setHttpProvider(HttpProvider httpProvider) {
    this.httpProvider = httpProvider;
  }

  protected void setSecurityContext(SecurityContext securityContext) {
    this.securityContext = securityContext;
  }

  protected void setHttpSessionDebugListener(HttpSessionDebugListener httpSessionDebugListener) {
    this.httpSessionDebugListener = httpSessionDebugListener;
  }

  protected void setDebugRequestParameterName(String debugRequestParameterName) {
    this.debugRequestParameterName = debugRequestParameterName;
  }

  public boolean isServiceStarting() {
    return isServiceStarting;
  }

  public void setServiceStarting(boolean isServiceStarting) {
    this.isServiceStarting = isServiceStarting;
  }

  public RDResourceProvider getRDResourceProvider() {
    if (rdResourceProvider == null) {
      rdResourceProvider = new RDResourceProvider();
    }
    return rdResourceProvider;
  }

  public WCERDResourceProvider getWCERDResourceProvider() {
    if (wceRDResourceProvider == null) {
      wceRDResourceProvider = new WCERDResourceProvider();
    }
    return wceRDResourceProvider;
  }
  
  public static boolean isAccountingEnabled() {
    ServiceContext ctx = ServiceContext.getServiceContext();
    if (ctx != null && 
        ctx.getHttpProvider() != null &&
        ctx.getHttpProvider().getHttpProperties() != null &&
        ctx.getHttpProvider().getHttpProperties().isRequestAccountingEnabled() &&
        Accounting.isEnabled() ) {
      return true;
    } else {
      return false;
    }
  }
}
