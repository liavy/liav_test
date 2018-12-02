/*
 * Created on 2005-11-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.gui;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.xml.rpc.ServiceException;

import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MetadataVisualFrame extends JFrame {

  private MetadataVisualPanel metadataVisualPanel;
  private ServiceFactoryConfig serviceFactoryConfig;
  
  public MetadataVisualFrame() {
    super("Metadata Visual");
  }
  
  private ServiceFactoryConfig initServiceFactoryConfig() {
    if(serviceFactoryConfig == null) {
      serviceFactoryConfig = new ServiceFactoryConfig();
    }
    serviceFactoryConfig.setAppendDefaultBindings(true);
    return(serviceFactoryConfig);
  }
  
  public void setAdditionalClassPath(String additionalClassPath) {
    initServiceFactoryConfig().setAdditionalClassPath(additionalClassPath);
  }
  
  public void setJavacPath(String javacPath) {
    initServiceFactoryConfig().setJavacPath(javacPath);
  }
  
  public void setPassword(String password) {
    initServiceFactoryConfig().setPassword(password);
  }
  
  public void setProxy(String host, String port) {
    initServiceFactoryConfig().setProxy(host, port);
  }
  
  public void setProxyBypass(String proxyBypass) {
    initServiceFactoryConfig().setProxyBypass(proxyBypass);
  }
  
  public void setTemporaryDir(String tempDir) {
    initServiceFactoryConfig().setTemporaryDir(tempDir);
  }
  
  public void setUser(String user) {
    initServiceFactoryConfig().setUser(user);
  }
  
  public void setPropertyString(String key, String value) {
    initServiceFactoryConfig().setPropertyString(key, value);
  }
  
  public void setAppendDefaultBinding(boolean appendDefaltBinding) {
    initServiceFactoryConfig().setAppendDefaultBindings(appendDefaltBinding);
  }
  
  protected void removeProperty(String key) {
    initServiceFactoryConfig().remove(key);
  }
  
  public void visualize(String wsdlURL) throws Exception {
    visualize(createDGenericService(wsdlURL));
  }
  
  private DGenericService createDGenericService(String wsdlURL) throws ServiceException {
    ServiceFactoryConfig serviceFactoryConfig = new ServiceFactoryConfig();
    serviceFactoryConfig.setAppendDefaultBindings(true);
    DGenericService dGenericService = GenericServiceFactory.newInstance().createService(wsdlURL, initServiceFactoryConfig());
    return(dGenericService);
  }
  
  public void visualize(DGenericService dGenericService) throws Exception { 
    init();
    metadataVisualPanel = new MetadataVisualPanel();
    visualizeOnlyService(dGenericService);
    getContentPane().add(metadataVisualPanel);
    setVisible(true);
  }
  
  public void visualize() {
    init();
    getContentPane().add(createVisualizationSplitPane());
    setVisible(true);
  }
  
  private void init() {
    setSize(1000, 700);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  private JSplitPane createVisualizationSplitPane() {
    metadataVisualPanel = new MetadataVisualPanel();
    ServiceFactoryManagementPanel serviceFactoryManagementPanel = new ServiceFactoryManagementPanel(this); 
    JSplitPane visualizationSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, metadataVisualPanel, serviceFactoryManagementPanel);
    visualizationSplitPane.setDividerLocation(500);
    visualizationSplitPane.setOneTouchExpandable(true);
    return(visualizationSplitPane);
  }
  
  protected void visualizeOnlyService(String wsdlURL) throws ServiceException {
    visualizeOnlyService(createDGenericService(wsdlURL));
  }
  
  protected void visualizeOnlyService(DGenericService service) {
    metadataVisualPanel.visualize(service);
  }
  
  public static void main(String[] args) throws Exception {
    MetadataVisualFrame metadataVisualFrame = new MetadataVisualFrame(); 
    metadataVisualFrame.visualize();
  }
    
}
