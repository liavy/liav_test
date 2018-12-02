package com.sap.engine.services.sca.plugins.ws.dii;

import java.util.Collection;
import java.util.Iterator;

import com.sap.engine.interfaces.sca.assembly.IComponentArtifact;
import com.sap.engine.interfaces.sca.assembly.IOutgoingBinding;
import com.sap.engine.interfaces.sca.assembly.IReferenceArtifact;
import com.sap.engine.interfaces.sca.assembly.ISCAAssembly;
import com.sap.engine.interfaces.sca.assembly.IServiceArtifact;
import com.sap.engine.interfaces.sca.runtime.ImplementationInstance;
import com.sap.engine.interfaces.sca.spi.ImplementationFactory;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants;
import com.sap.engine.services.sca.plugins.ws.config.ConfigurationDestinationInfo;

import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author I044263
 *
 */
public class DynamicInvocationInterfaceImplementationFactory implements ImplementationFactory {

  private IOutgoingBinding iOutgoingBinding = null;
  
  /**
   * Ctor.
   * @param aOutgoing
   */
  public DynamicInvocationInterfaceImplementationFactory(IOutgoingBinding aOutgoing) {
    super();
    
    iOutgoingBinding = aOutgoing;
  }
  
  public HelperContext getHelperContext() {
    return iOutgoingBinding.getAssembly().getHelperContext();
  }

  public ImplementationInstance newInstance(IServiceArtifact aService) {
    return new DynamicInvocationInterfaceImplementationInstance(null, aService, this);
  }

  public void prepare() {
    
  }

  public void stop() {
    
  }
  
  public ConfigurationDestinationInfo getDestinationInfo() {
    ISCAAssembly assembly = iOutgoingBinding.getAssembly();
    
    ConfigurationDestinationInfo destInfo = new ConfigurationDestinationInfo();    
    destInfo.iAppName = assembly.getAppName();
    destInfo.iCompositeAddress = assembly.getCompositeAddress();
    destInfo.iReferenceAddress = iOutgoingBinding.getAddress();
    destInfo.iBindingType = WebServicePluginConstants.WS_BINDING_TYPE;
    
    Collection<IComponentArtifact> components = assembly.getDomainComponents();
    Iterator<IComponentArtifact> i = components.iterator();
    while (i.hasNext()) {
      IComponentArtifact component = i.next();
      
      IReferenceArtifact ref = component.getReference(destInfo.iReferenceAddress.getLocalPart());
      if (ref == null) 
	continue;
      
      if (ref.getAddress().equals(destInfo.iReferenceAddress)) {
	destInfo.iComponentAddress = component.getAddress();
	break;
      }
    }
    
    return destInfo;
  }  

}
