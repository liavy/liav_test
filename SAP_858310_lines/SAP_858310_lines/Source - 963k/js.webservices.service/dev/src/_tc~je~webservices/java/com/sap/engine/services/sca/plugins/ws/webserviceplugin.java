package com.sap.engine.services.sca.plugins.ws;


import java.io.File;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.osoa.sca.sdo.Binding;
import org.osoa.sca.sdo.Interface;
import org.osoa.sca.sdo.WsdlPortType;
import org.xmlsoap.schemas.soap.encoding.Array;
import org.xmlsoap.schemas.soap.envelope.Envelope;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.assembly.IOutgoingBinding;
import com.sap.engine.interfaces.sca.assembly.ISCAAssembly;
import com.sap.engine.interfaces.sca.spi.ArchiveManipulatorPlugin;
import com.sap.engine.interfaces.sca.spi.ArchiveManipulatorResult;
import com.sap.engine.interfaces.sca.spi.BindingContext;
import com.sap.engine.interfaces.sca.spi.BindingProvider;
import com.sap.engine.interfaces.sca.spi.ImplementationFactory;
import com.sap.engine.interfaces.sca.spi.InterfaceProvider;
import com.sap.engine.interfaces.sca.spi.PluginException;
import com.sap.engine.interfaces.sca.wire.InterfaceMetadata;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants.WsarGeneratonMode;
import com.sap.engine.services.sca.plugins.ws.sdo.WebServiceBinding;

import com.sap.engine.services.sca.plugins.ws.tools.WSARGenerator;
import com.sap.engine.services.sca.plugins.ws.tools.sdo.das.WebServiceRepository;

import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author I044263
 *
 */
public class WebServicePlugin implements BindingProvider, InterfaceProvider, ArchiveManipulatorPlugin {
  
  private WsdlInterfaceProvider iInterfaceProvider =  null;
  
  /**
   * Ctor.
   */
  public WebServicePlugin() {
    iInterfaceProvider =  new WsdlInterfaceProvider();
  }
	
  public URL[] getXSDs() {
    ClassLoader cl = getClass().getClassLoader();
    
    return new URL[] { 	
	cl.getResource(WebServicePluginConstants.WS_BINDING_XSD_URI),	
	cl.getResource(WebServicePluginConstants.SOAP_XSD_URI),
	cl.getResource(WebServicePluginConstants.ENCODING_XSD_URI)
    };
  }
	
  public final String getName() {
    return WebServicePluginConstants.WS_PLUGIN_NAME_STR;
  }
	
  public Class[] getJavaTypes() {
    return new Class[]{
	Envelope.class,
	Array.class,
	WebServiceBinding.class,
	WsdlPortType.class
    };
  }
	
  public boolean accept(Binding aObj) {
    return aObj instanceof WebServiceBinding;
  }

  public void publish(BindingContext aBinding) throws PluginException {
    WebServiceRepository.getInstance().publish(aBinding);
  }
  
  public static BindingContext getPublishedBinding(QName aKey) {
    return WebServiceRepository.getInstance().getPublishedBinding(aKey);
  }  

  public ImplementationFactory provideImplementation(IOutgoingBinding aOutgoing, ObjectInputStream aInStream) {
    return new WebServiceImplementationFactory(aOutgoing);
  }

  public boolean accept(Interface aObj) {
    return aObj instanceof WsdlPortType ;
  }
	
  public InterfaceMetadata generateMetadata(Interface aIface, SCAResolver aResolver, HelperContext aCtx) {
    return iInterfaceProvider.generateMetadata(aIface, aResolver, aCtx);
  }

  public void enrichContext(InterfaceMetadata aImd, HelperContext aCtx) {
    iInterfaceProvider.enrichContext(aImd, aCtx);
  }

  public List<ArchiveManipulatorResult> executeEvent(int aEventId, ISCAAssembly aAssembly, SCAResolver aResolver, File aFile) throws PluginException {
    List<ArchiveManipulatorResult> result = new LinkedList<ArchiveManipulatorResult>();
    
    if (aEventId == ArchiveManipulatorPlugin.MODULE_GENERATE_START_EVENT) {
      WSARGenerator generator = new WSARGenerator(WsarGeneratonMode.WS);
      generator.generateWSAR(aAssembly, aResolver, aFile, result);
    }
    
    return result;
  }

  public List<Integer> getEvents() {
    List<Integer> l = new LinkedList<Integer>();
    l.add(ArchiveManipulatorPlugin.MODULE_GENERATE_START_EVENT);
    
    return l;
  }
}
