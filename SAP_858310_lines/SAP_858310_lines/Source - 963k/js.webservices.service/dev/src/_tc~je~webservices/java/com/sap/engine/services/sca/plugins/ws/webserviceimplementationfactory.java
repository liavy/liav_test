package com.sap.engine.services.sca.plugins.ws;

import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.osoa.sca.sdo.ComponentType;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.assembly.IComponentArtifact;
import com.sap.engine.interfaces.sca.assembly.IOutgoingBinding;
import com.sap.engine.interfaces.sca.assembly.IReferenceArtifact;
import com.sap.engine.interfaces.sca.assembly.ISCAAssembly;
import com.sap.engine.interfaces.sca.assembly.IServiceArtifact;
import com.sap.engine.interfaces.sca.runtime.ImplementationInstance;
import com.sap.engine.interfaces.sca.spi.ImplementationFactory;
import com.sap.engine.services.sca.plugins.ws.config.ConfigurationDestinationInfo;
import com.sap.engine.services.sca.plugins.ws.sdo.WebServiceBinding;

import commonj.sdo.helper.HelperContext;

public class WebServiceImplementationFactory implements ImplementationFactory {

  private IOutgoingBinding		iWsBinding		= null;
  
  /**
   * Ctor.
   * @param aBinding
   * @param aAssembly
   */
  public WebServiceImplementationFactory(final IOutgoingBinding aBinding) {
    super();

    iWsBinding = aBinding;
  }
  
  public ImplementationInstance getCurrentInstance(IComponentArtifact aArtifat) {
    return null;
  }
	
  public ImplementationInstance newInstance(IServiceArtifact aService) {
    return new WebServiceImplementationInstance(null, aService, this);
  }
	
  public HelperContext getHelperContext() {
     return iWsBinding.getAssembly().getHelperContext();
  }  
  
  public ConfigurationDestinationInfo getDestinationInfo() {
    ISCAAssembly assembly = iWsBinding.getAssembly();
    
    ConfigurationDestinationInfo destInfo = new ConfigurationDestinationInfo();    
    destInfo.iAppName = assembly.getAppName();
    destInfo.iCompositeAddress = assembly.getCompositeAddress();
    destInfo.iReferenceAddress = iWsBinding.getAddress();
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
    
  public void prepare() {
    
  }
	
  public void stop() {
    
  }
	
  public ComponentType introspect(SCAResolver aResolver) {
    return null;
  }
    
  public ComponentType getComponentType() {
    return null;
  }
	
  public void writeMemento(ObjectOutputStream s) {
	  
  }
}
