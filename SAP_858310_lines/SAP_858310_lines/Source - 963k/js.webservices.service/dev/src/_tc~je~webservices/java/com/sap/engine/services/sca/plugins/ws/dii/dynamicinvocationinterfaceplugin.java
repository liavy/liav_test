package com.sap.engine.services.sca.plugins.ws.dii;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import com.sap.sca.sdo.DiiInterface;
import org.osoa.sca.sdo.Interface;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.assembly.AssemblyException;
import com.sap.engine.interfaces.sca.assembly.ISCAAssembly;
import com.sap.engine.interfaces.sca.spi.ArchiveManipulatorPlugin;
import com.sap.engine.interfaces.sca.spi.ArchiveManipulatorResult;
import com.sap.engine.interfaces.sca.spi.BindingContext;
import com.sap.engine.interfaces.sca.spi.InterfaceProvider;
import com.sap.engine.interfaces.sca.spi.PluginException;
import com.sap.engine.interfaces.sca.wire.InterfaceMetadata;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants.WsarGeneratonMode;
import com.sap.engine.services.sca.plugins.ws.tools.WSARGenerator;
import com.sap.engine.services.sca.plugins.ws.tools.sdo.das.WebServiceRepository;

import commonj.sdo.helper.HelperContext;

/**
 * @author I044263
 *
 */
public class DynamicInvocationInterfacePlugin implements InterfaceProvider, ArchiveManipulatorPlugin {
  
  private DynamicInvocationInterfaceInterfaceProvider 	iInterfaceProvider 	= null;
  
  /**
   * Ctor.
   */
  public DynamicInvocationInterfacePlugin() {
    iInterfaceProvider = new DynamicInvocationInterfaceInterfaceProvider();
  }

  public Class[] getJavaTypes() {
    return new Class[]{
	DiiInterface.class
    };
  }

  public String getName() {
    return WebServicePluginConstants.INTERFACE_DII_NAME_STR;
  }

  public URL[] getXSDs() {
    return new URL[] { 
  	getClass().getClassLoader().getResource(WebServicePluginConstants.DII_INTERFACE_XSD_URI)
    };
  }

  public boolean accept(Interface aInterface) {
    return aInterface instanceof DiiInterface;
  }

  public void enrichContext(InterfaceMetadata aMetadata, HelperContext aContext) throws AssemblyException {
    iInterfaceProvider.enrichContext(aMetadata, aContext);    
  }

  public InterfaceMetadata generateMetadata(Interface aInterface, SCAResolver aResolver, HelperContext aContext) throws AssemblyException {
    return iInterfaceProvider.generateMetadata(aInterface, aResolver, aContext);
  }

  public List<ArchiveManipulatorResult> executeEvent(int aEventId, ISCAAssembly aAssembly, SCAResolver aResolver, File aFile) throws PluginException {
    List<ArchiveManipulatorResult> result = new LinkedList<ArchiveManipulatorResult>();
    
    if (aEventId == ArchiveManipulatorPlugin.MODULE_GENERATE_START_EVENT) {
      WSARGenerator generator = new WSARGenerator(WsarGeneratonMode.DII);
      generator.generateWSAR(aAssembly, aResolver, aFile, result);
    }
    
    return result;
  }

  public List<Integer> getEvents() {
    List<Integer> l = new LinkedList<Integer>();
    l.add(ArchiveManipulatorPlugin.MODULE_GENERATE_START_EVENT);
    
    return l;
  }
  
  public static BindingContext getPublishedBinding(QName aKey) {
    return WebServiceRepository.getInstance().getPublishedBinding(aKey);
  } 
}

